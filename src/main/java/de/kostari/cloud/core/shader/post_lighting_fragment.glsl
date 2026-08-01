#version 330 core

#define MAX_LIGHTS 16
#define MAX_OCCLUDERS 32

in vec2 TexCoord;

uniform sampler2D inputTexture;
uniform vec2 viewportPosition;
uniform vec2 viewportSize;
uniform vec3 ambientColor;
uniform float ambientIntensity;
uniform int lightCount;
uniform int occluderCount;

// position.xy, radius, intensity
uniform vec4 lightData[MAX_LIGHTS];
// color.rgb, falloff exponent
uniform vec4 lightColors[MAX_LIGHTS];
// casts shadows, source radius, shadow strength, unused
uniform vec4 lightShadows[MAX_LIGHTS];

// center.xy, half-size.xy
uniform vec4 occluderBounds[MAX_OCCLUDERS];
// cos(angle), sin(angle), opacity, unused
uniform vec4 occluderTransforms[MAX_OCCLUDERS];

out vec4 FragColor;

vec2 toOccluderSpace(vec2 point, vec2 center, float cosine, float sine) {
    vec2 relative = point - center;
    return vec2(
        relative.x * cosine + relative.y * sine,
        -relative.x * sine + relative.y * cosine
    );
}

bool pointInsideBox(vec2 point, vec2 halfSize) {
    return abs(point.x) < halfSize.x && abs(point.y) < halfSize.y;
}

bool segmentHitsBox(vec2 start, vec2 end, vec2 halfSize) {
    vec2 direction = end - start;
    vec2 safeDirection = vec2(
        abs(direction.x) < 0.00001 ? (direction.x < 0.0 ? -0.00001 : 0.00001) : direction.x,
        abs(direction.y) < 0.00001 ? (direction.y < 0.0 ? -0.00001 : 0.00001) : direction.y
    );

    vec2 first = (-halfSize - start) / safeDirection;
    vec2 second = (halfSize - start) / safeDirection;
    vec2 nearer = min(first, second);
    vec2 farther = max(first, second);
    float entry = max(nearer.x, nearer.y);
    float exit = min(farther.x, farther.y);

    // Ignore contact exactly at the light or shaded pixel to avoid self-shadow
    // speckling on the edge of an occluder.
    return exit >= max(entry, 0.0005) && entry < 0.9995 && exit > 0.0005;
}

float traceShadowRay(vec2 lightPosition, vec2 worldPosition) {
    float visibility = 1.0;

    for (int index = 0; index < MAX_OCCLUDERS; index++) {
        if (index >= occluderCount) {
            break;
        }

        vec4 bounds = occluderBounds[index];
        vec4 transform = occluderTransforms[index];
        vec2 localLight = toOccluderSpace(
            lightPosition, bounds.xy, transform.x, transform.y);

        // A fixture surrounding the light itself should not swallow the light.
        if (pointInsideBox(localLight, bounds.zw)) {
            continue;
        }

        vec2 localPixel = toOccluderSpace(
            worldPosition, bounds.xy, transform.x, transform.y);
        // The visible blocker receives light; it only shadows geometry behind
        // its silhouette.
        if (pointInsideBox(localPixel, bounds.zw)) {
            continue;
        }
        if (segmentHitsBox(localLight, localPixel, bounds.zw)) {
            visibility *= 1.0 - transform.z;
            if (visibility <= 0.001) {
                return 0.0;
            }
        }
    }

    return visibility;
}

float shadowVisibility(vec2 lightPosition, vec2 worldPosition, float sourceRadius) {
    if (sourceRadius <= 0.001) {
        return traceShadowRay(lightPosition, worldPosition);
    }

    vec2 ray = worldPosition - lightPosition;
    float rayLength = length(ray);
    if (rayLength <= 0.001) {
        return 1.0;
    }

    // One spatially dithered area-light sample keeps the cost equal to a hard
    // shadow ray. Neighboring pixels sample different source positions, which
    // produces a stable soft penumbra without tripling the occluder loop.
    float noise = fract(52.9829189 * fract(dot(gl_FragCoord.xy, vec2(0.06711056, 0.00583715))));
    vec2 perpendicular = vec2(-ray.y, ray.x) / rayLength;
    vec2 sampledLight = lightPosition + perpendicular * ((noise * 2.0 - 1.0) * sourceRadius);
    return traceShadowRay(sampledLight, worldPosition);
}

void main() {
    vec4 scene = texture(inputTexture, TexCoord);
    vec2 worldPosition = viewportPosition + vec2(
        TexCoord.x * viewportSize.x,
        (1.0 - TexCoord.y) * viewportSize.y
    );

    vec3 illumination = ambientColor * ambientIntensity;

    for (int index = 0; index < MAX_LIGHTS; index++) {
        if (index >= lightCount) {
            break;
        }

        vec4 data = lightData[index];
        vec4 color = lightColors[index];
        vec4 shadow = lightShadows[index];
        float distanceToLight = distance(worldPosition, data.xy);
        if (distanceToLight >= data.z) {
            continue;
        }

        float normalizedDistance = clamp(distanceToLight / data.z, 0.0, 1.0);
        float attenuation = pow(1.0 - normalizedDistance, max(color.a, 0.01)) * data.w;

        if (shadow.x > 0.5 && occluderCount > 0) {
            float visibility = shadowVisibility(data.xy, worldPosition, shadow.y);
            attenuation *= mix(1.0, visibility, clamp(shadow.z, 0.0, 1.0));
        }

        illumination += color.rgb * attenuation;
    }

    FragColor = vec4(scene.rgb * max(illumination, vec3(0.0)), scene.a);
}
