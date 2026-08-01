#version 330 core

in vec2 TexCoord;

uniform sampler2D inputTexture;
uniform vec2 resolution;
uniform vec2 center;
uniform float progress;
uniform float strength;
uniform float damage;
uniform vec3 tint;

out vec4 FragColor;

vec3 splitSample(vec2 uv, vec2 direction, float amount) {
    vec3 color;
    color.r = texture(inputTexture, clamp(uv + direction * amount, 0.0, 1.0)).r;
    color.g = texture(inputTexture, clamp(uv, 0.0, 1.0)).g;
    color.b = texture(inputTexture, clamp(uv - direction * amount, 0.0, 1.0)).b;
    return color;
}

void main() {
    vec2 safeResolution = max(resolution, vec2(1.0));
    float aspect = safeResolution.x / safeResolution.y;
    vec2 delta = (TexCoord - center) * vec2(aspect, 1.0);
    float distanceFromCenter = length(delta);
    vec2 direction = delta / max(distanceFromCenter, 0.001);

    float eased = 1.0 - pow(1.0 - clamp(progress, 0.0, 1.0), 2.0);
    float radius = eased * 1.18;
    float ring = exp(-pow((distanceFromCenter - radius) / 0.034, 2.0));
    float innerRing = exp(-pow((distanceFromCenter - radius * 0.72) / 0.055, 2.0));
    float fade = 1.0 - smoothstep(0.55, 1.0, progress);
    float impulse = (ring * 0.032 + innerRing * 0.008) * fade * strength;

    vec2 sampleDirection = vec2(direction.x / aspect, direction.y);
    vec2 uv = clamp(TexCoord - sampleDirection * impulse, 0.0, 1.0);
    vec3 color = splitSample(uv, sampleDirection, ring * fade * 0.009 * strength);
    color += tint * ring * fade * 0.42 * strength;
    color += tint * innerRing * fade * 0.10;

    float edge = smoothstep(0.18, 0.88, distanceFromCenter);
    float damagePulse = damage * fade * (0.55 + 0.45 * sin(progress * 82.0));
    color = mix(color, color * vec3(1.18, 0.46, 0.52), edge * damagePulse * 0.42);
    color += vec3(0.34, 0.01, 0.04) * edge * damagePulse * 0.2;

    FragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
