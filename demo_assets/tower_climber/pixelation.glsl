#version 330 core

in vec2 TexCoord;

uniform sampler2D inputTexture;
uniform vec2 resolution;
uniform float virtualHeight;
uniform float strength;

out vec4 FragColor;

void main() {
    vec2 safeResolution = max(resolution, vec2(1.0));
    float pixelSize = max(1.0, safeResolution.y / max(1.0, virtualHeight));
    vec2 gridSize = safeResolution / pixelSize;
    vec2 pixelUv = (floor(TexCoord * gridSize) + 0.5) / gridSize;

    vec4 original = texture(inputTexture, TexCoord);
    vec4 pixelated = texture(inputTexture, clamp(pixelUv, 0.0, 1.0));
    FragColor = mix(original, pixelated, clamp(strength, 0.0, 1.0));
}
