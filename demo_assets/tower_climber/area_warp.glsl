#version 330 core

in vec2 TexCoord;

uniform sampler2D inputTexture;
uniform vec2 resolution;
uniform vec2 center;
uniform float progress;

out vec4 FragColor;

void main() {
    vec2 safeResolution = max(resolution, vec2(1.0));
    vec2 aspect = vec2(safeResolution.x / safeResolution.y, 1.0);
    vec2 delta = (TexCoord - center) * aspect;
    float distanceFromCenter = length(delta);
    float radius = progress * 1.42;
    float ring = exp(-pow((distanceFromCenter - radius) / 0.045, 2.0));
    float fade = 1.0 - smoothstep(0.55, 1.0, progress);
    vec2 direction = delta / max(distanceFromCenter, 0.001);
    vec2 offset = direction * ring * fade * 0.032;
    offset.x /= aspect.x;

    vec2 uv = clamp(TexCoord - offset, 0.0, 1.0);
    float split = ring * fade * 0.008;
    vec3 color;
    color.r = texture(inputTexture, clamp(uv + direction * split, 0.0, 1.0)).r;
    color.g = texture(inputTexture, uv).g;
    color.b = texture(inputTexture, clamp(uv - direction * split, 0.0, 1.0)).b;
    color += vec3(0.16, 0.95, 0.83) * ring * fade * 0.42;

    FragColor = vec4(color, 1.0);
}
