#version 330 core

in vec2 TexCoord;

uniform sampler2D inputTexture;
uniform vec2 resolution;
uniform float effectTime;
uniform vec2 center;
uniform vec2 direction;
uniform float progress;
uniform float intensity;
uniform float mode;

out vec4 FragColor;

float hash21(vec2 point) {
    point = fract(point * vec2(234.34, 435.35));
    point += dot(point, point + 34.23);
    return fract(point.x * point.y);
}

vec3 chromaticSample(vec2 uv, vec2 axis, float split) {
    vec3 color;
    color.r = texture(inputTexture, clamp(uv + axis * split, 0.0, 1.0)).r;
    color.g = texture(inputTexture, clamp(uv, 0.0, 1.0)).g;
    color.b = texture(inputTexture, clamp(uv - axis * split, 0.0, 1.0)).b;
    return color;
}

void main() {
    vec2 safeResolution = max(resolution, vec2(1.0));
    float aspect = safeResolution.x / safeResolution.y;
    float fade = 1.0 - smoothstep(0.48, 1.0, progress);
    vec2 uv = TexCoord;
    vec3 color;

    if (mode < 0.5) {
        vec2 motion = vec2(direction.x / aspect, direction.y);
        float local = 1.0 - smoothstep(0.05, 0.75,
                length((TexCoord - center) * vec2(aspect, 1.0)));
        float smear = (0.004 + local * 0.015) * fade * intensity;
        vec3 sum = texture(inputTexture, uv).rgb * 0.38;
        sum += texture(inputTexture, clamp(uv - motion * smear, 0.0, 1.0)).rgb * 0.27;
        sum += texture(inputTexture, clamp(uv - motion * smear * 2.1, 0.0, 1.0)).rgb * 0.19;
        sum += texture(inputTexture, clamp(uv - motion * smear * 3.4, 0.0, 1.0)).rgb * 0.16;
        color = sum;
        color += vec3(0.08, 0.72, 0.64) * local * fade * 0.08 * intensity;
        color = mix(color, chromaticSample(uv, motion, 0.0045 * fade * intensity), 0.42);
    } else if (mode < 1.5) {
        float row = floor(TexCoord.y * 64.0);
        float tick = floor(effectTime * 36.0);
        float noise = hash21(vec2(row, tick));
        float band = step(0.69, noise) * fade;
        float shift = (noise - 0.5) * 0.060 * band * intensity;
        uv.x = clamp(uv.x + shift, 0.0, 1.0);
        color = chromaticSample(uv, vec2(1.0, 0.0),
                (0.003 + band * 0.010) * fade * intensity);
        float edge = smoothstep(0.12, 0.78,
                length((TexCoord - center) * vec2(aspect, 1.0)));
        color = mix(color, color * vec3(1.25, 0.38, 0.48), edge * fade * 0.46 * intensity);
    } else if (mode < 2.5) {
        vec2 delta = (TexCoord - center) * vec2(aspect, 1.0);
        float distanceFromCenter = length(delta);
        float angle = atan(delta.y, delta.x);
        float vortex = (1.0 - smoothstep(0.0, 0.78, distanceFromCenter)) * fade * intensity;
        angle += vortex * (0.28 + 0.18 * sin(progress * 8.0));
        vec2 warped = vec2(cos(angle), sin(angle)) * distanceFromCenter;
        warped.x /= aspect;
        uv = clamp(center + warped * (1.0 - vortex * 0.035), 0.0, 1.0);
        vec2 radial = normalize(delta + vec2(0.001));
        radial.x /= aspect;
        color = chromaticSample(uv, radial, 0.010 * vortex);
        float ring = exp(-pow((distanceFromCenter - progress * 0.82) / 0.065, 2.0)) * fade;
        color += vec3(0.48, 0.12, 0.78) * (vortex * 0.12 + ring * 0.38) * intensity;
    } else {
        vec2 delta = (TexCoord - center) * vec2(aspect, 1.0);
        float distanceFromCenter = length(delta);
        vec2 radial = normalize(delta + vec2(0.001));
        radial.x /= aspect;
        float wave = sin(distanceFromCenter * 38.0 - progress * 22.0) * fade;
        float lift = (1.0 - smoothstep(0.0, 0.95, distanceFromCenter)) * fade;
        uv = clamp(uv - radial * wave * 0.006 * intensity, 0.0, 1.0);
        color = chromaticSample(uv, radial, abs(wave) * 0.0035 * intensity);
        color += vec3(0.38, 0.88, 1.0) * lift * 0.10 * intensity;
    }

    FragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
