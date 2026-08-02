#version 330 core

layout(location = 0) in vec2 unitPosition;

uniform vec2 viewportSize;
uniform vec4 quadBounds;

out vec2 pixelPosition;

void main() {
    pixelPosition = quadBounds.xy + unitPosition * quadBounds.zw;
    vec2 ndc = vec2(
        pixelPosition.x / viewportSize.x * 2.0 - 1.0,
        1.0 - pixelPosition.y / viewportSize.y * 2.0
    );
    gl_Position = vec4(ndc, 0.0, 1.0);
}
