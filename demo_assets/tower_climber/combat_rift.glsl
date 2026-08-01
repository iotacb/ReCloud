#version 330 core

in vec2 TexCoord;

uniform sampler2D inputTexture;
uniform vec2 resolution;
uniform vec2 center;
uniform float progress;
uniform float mode;
uniform float facing;
uniform float strength;

out vec4 FragColor;

float hash21(vec2 value) {
    value = fract(value * vec2(234.34, 435.35));
    value += dot(value, value + 34.23);
    return fract(value.x * value.y);
}

vec3 chromaticSample(vec2 uv, vec2 direction, float split) {
    vec3 color;
    color.r = texture(inputTexture, clamp(uv + direction * split, 0.0, 1.0)).r;
    color.g = texture(inputTexture, clamp(uv, 0.0, 1.0)).g;
    color.b = texture(inputTexture, clamp(uv - direction * split, 0.0, 1.0)).b;
    return color;
}

void main() {
    vec2 safeResolution = max(resolution, vec2(1.0));
    float aspect = safeResolution.x / safeResolution.y;
    vec2 delta = (TexCoord - center) * vec2(aspect, 1.0);
    float distanceFromCenter = length(delta);
    float fade = 1.0 - smoothstep(0.48, 1.0, progress);
    vec2 uv = TexCoord;
    vec3 color;

    if (mode < 0.5) {
        vec2 bladeDirection = normalize(vec2(facing, -0.58));
        vec2 bladeNormal = vec2(-bladeDirection.y, bladeDirection.x);
        float sweep = mix(-0.28, 0.42, smoothstep(0.0, 1.0, progress));
        float along = dot(delta, bladeDirection);
        float across = dot(delta, bladeNormal);
        float blade = exp(-pow((along - sweep) / 0.036, 2.0))
                * (1.0 - smoothstep(0.04, 0.52, abs(across))) * fade;
        float ringRadius = progress * 0.58;
        float ring = exp(-pow((distanceFromCenter - ringRadius) / 0.038, 2.0)) * fade;
        vec2 offset = bladeNormal * blade * 0.022 + normalize(delta + vec2(0.001)) * ring * 0.013;
        offset.x /= aspect;
        uv = clamp(uv - offset * strength, 0.0, 1.0);
        vec2 splitDirection = bladeNormal;
        splitDirection.x /= aspect;
        color = chromaticSample(uv, splitDirection, (blade * 0.009 + ring * 0.004) * strength);
        color += vec3(0.72, 0.32, 1.0) * blade * 0.48 * strength;
        color += vec3(0.28, 1.0, 0.88) * ring * 0.2 * strength;
    } else if (mode < 1.5) {
        float bandIndex = floor(TexCoord.y * 42.0);
        float noise = hash21(vec2(bandIndex, floor(progress * 25.0)));
        float band = step(0.62, noise) * fade;
        float local = 1.0 - smoothstep(0.08, 0.72, distanceFromCenter);
        float xShift = (noise - 0.5) * 0.052 * band * (0.35 + local) * strength;
        uv.x = clamp(uv.x + xShift, 0.0, 1.0);
        vec2 splitDirection = vec2(1.0, 0.0);
        color = chromaticSample(uv, splitDirection, (0.003 + band * 0.008) * fade * strength);
        float edge = smoothstep(0.24, 0.82, distanceFromCenter);
        float flash = (0.55 + 0.45 * sin(progress * 94.0)) * fade;
        color = mix(color, color * vec3(1.18, 0.48, 0.42), edge * flash * 0.44 * strength);
        color += vec3(0.5, 0.02, 0.03) * edge * fade * 0.16 * strength;
    } else {
        vec2 direction = normalize(delta + vec2(0.001));
        vec2 screenDirection = vec2(direction.x / aspect, direction.y);
        float tunnel = exp(-pow((distanceFromCenter - progress * 0.82) / 0.065, 2.0)) * fade;
        float pull = (1.0 - smoothstep(0.0, 0.72, distanceFromCenter)) * fade;
        vec2 zoomOffset = screenDirection * (0.009 * pull + 0.024 * tunnel) * strength;
        vec3 sampleA = texture(inputTexture, clamp(uv - zoomOffset, 0.0, 1.0)).rgb;
        vec3 sampleB = texture(inputTexture, clamp(uv - zoomOffset * 2.2, 0.0, 1.0)).rgb;
        color = mix(texture(inputTexture, uv).rgb, (sampleA + sampleB) * 0.5, 0.62 * fade);
        color += vec3(0.38, 0.95, 1.0) * tunnel * 0.42 * strength;
        color += vec3(1.0, 0.72, 0.24) * pull * 0.09 * strength;
    }

    FragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
