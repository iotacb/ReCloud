#version 330 core

in vec2 pixelPosition;

uniform vec4 shapeBounds;
uniform vec4 topColor;
uniform vec4 bottomColor;
uniform vec4 borderColor;
uniform vec4 glowColor;
uniform vec4 sheenColor;
uniform float cornerRadius;
uniform float edgeSoftness;
uniform float borderWidth;
uniform float glowSize;
uniform float glowIntensity;
uniform float sheenWidth;
uniform float sheenSpeed;
uniform float sheenIntensity;
uniform float pulseSpeed;
uniform float pulseAmount;
uniform float time;

out vec4 FragColor;

float roundedBoxDistance(vec2 point, vec2 halfSize, float radius) {
    vec2 q = abs(point) - halfSize + radius;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius;
}

void main() {
    vec2 halfSize = shapeBounds.zw * 0.5;
    vec2 center = shapeBounds.xy + halfSize;
    vec2 local = pixelPosition - center;
    float radius = min(cornerRadius, min(halfSize.x, halfSize.y));
    float distance = roundedBoxDistance(local, halfSize, radius);
    float shapeAlpha = 1.0 - smoothstep(-edgeSoftness, edgeSoftness, distance);

    vec2 innerSize = max(halfSize - vec2(borderWidth), vec2(0.0));
    float innerRadius = max(0.0, radius - borderWidth);
    float innerDistance = roundedBoxDistance(local, innerSize, innerRadius);
    float innerAlpha = 1.0 - smoothstep(-edgeSoftness, edgeSoftness, innerDistance);
    float borderFactor = clamp(shapeAlpha - innerAlpha, 0.0, 1.0);

    vec2 uv = (pixelPosition - shapeBounds.xy) / max(shapeBounds.zw, vec2(1.0));
    vec4 fill = mix(topColor, bottomColor, clamp(uv.y, 0.0, 1.0));
    float pulse = pulseSpeed > 0.0
        ? 1.0 + sin(time * pulseSpeed * 6.2831853) * pulseAmount
        : 1.0;
    fill.rgb *= pulse;

    if (sheenIntensity > 0.0 && sheenSpeed != 0.0) {
        float travel = fract(time * sheenSpeed) * (1.0 + sheenWidth * 2.0) - sheenWidth;
        float stripe = 1.0 - smoothstep(0.0, sheenWidth, abs(uv.x + uv.y * 0.18 - travel));
        fill.rgb = mix(fill.rgb, sheenColor.rgb, stripe * sheenColor.a * sheenIntensity);
    }

    vec4 surface = mix(fill, borderColor, borderFactor);
    surface.a *= shapeAlpha;

    float glowFalloff = glowSize > 0.0 ? exp(-max(distance, 0.0) * 3.0 / glowSize) : 0.0;
    float glowAlpha = glowColor.a * glowIntensity * glowFalloff * (1.0 - shapeAlpha);
    float outputAlpha = surface.a + glowAlpha * (1.0 - surface.a);
    vec3 outputColor = outputAlpha > 0.0
        ? (surface.rgb * surface.a + glowColor.rgb * glowAlpha * (1.0 - surface.a)) / outputAlpha
        : vec3(0.0);
    FragColor = vec4(outputColor, outputAlpha);
}
