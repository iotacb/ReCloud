#version 330 core

in vec2 TexCoord;

uniform sampler2D inputTexture;
uniform vec2 resolution;
uniform float effectTime;
uniform float previousZone;
uniform float currentZone;
uniform float zoneBlend;
uniform float danger;
uniform float storm;
uniform float lightning;

out vec4 FragColor;

float hash21(vec2 value) {
    value = fract(value * vec2(123.34, 456.21));
    value += dot(value, value + 45.32);
    return fract(value.x * value.y);
}

vec3 zoneTint(float zone) {
    if (zone < 0.5) return vec3(0.18, 0.62, 0.58);
    if (zone < 1.5) return vec3(0.18, 0.72, 0.42);
    if (zone < 2.5) return vec3(0.58, 0.28, 0.78);
    return vec3(0.28, 0.54, 0.92);
}

void main() {
    vec2 safeResolution = max(resolution, vec2(1.0));
    float aspect = safeResolution.x / safeResolution.y;
    vec2 centered = TexCoord - 0.5;
    vec2 aspectDelta = centered * vec2(aspect, 1.0);
    float edge = smoothstep(0.28, 0.78, length(aspectDelta));

    float curvature = 0.012 + storm * 0.004;
    vec2 uv = TexCoord + centered * dot(centered, centered) * curvature;
    float verticalHaze = sin(uv.y * 31.0 - effectTime * 1.7)
            * sin(uv.y * 8.0 + effectTime * 0.43);
    uv.x += verticalHaze * (0.00042 + storm * 0.00058) * (0.35 + edge);
    uv = clamp(uv, 0.002, 0.998);

    vec2 radial = normalize(aspectDelta + vec2(0.0001));
    radial.x /= aspect;
    float dangerPulse = danger * (0.68 + 0.32 * sin(effectTime * 5.4));
    float split = 0.00045 + storm * 0.00038 + dangerPulse * edge * 0.0022;
    vec3 color;
    color.r = texture(inputTexture, clamp(uv + radial * split, 0.0, 1.0)).r;
    color.g = texture(inputTexture, uv).g;
    color.b = texture(inputTexture, clamp(uv - radial * split, 0.0, 1.0)).b;

    vec3 tint = mix(zoneTint(previousZone), zoneTint(currentZone),
            smoothstep(0.0, 1.0, zoneBlend));
    float scanline = 0.5 + 0.5 * sin(TexCoord.y * safeResolution.y * 1.08);
    float grain = hash21(floor(TexCoord * safeResolution * 0.42)
            + floor(effectTime * 24.0));
    color *= 0.986 + scanline * 0.014;
    color += (grain - 0.5) * 0.018;
    color += tint * (0.018 + storm * edge * 0.012);

    vec3 dangerColor = vec3(0.55, 0.035, 0.06);
    color = mix(color, color * 0.72 + dangerColor, dangerPulse * edge * 0.34);
    color += vec3(0.62, 0.84, 1.0) * lightning * (0.14 + edge * 0.08);
    color *= 1.0 - edge * (0.055 + danger * 0.08);

    FragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
