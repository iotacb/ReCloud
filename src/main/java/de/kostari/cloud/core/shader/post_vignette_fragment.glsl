#version 330 core

in vec2 TexCoord;

uniform sampler2D inputTexture;
uniform float intensity;
uniform float radius;
uniform float smoothness;
uniform vec3 vignetteColor;

out vec4 FragColor;

void main() {
    vec4 scene = texture(inputTexture, TexCoord);
    float distanceFromCenter = distance(TexCoord, vec2(0.5));
    float amount = smoothstep(radius, radius + smoothness, distanceFromCenter) * intensity;
    FragColor = vec4(mix(scene.rgb, vignetteColor, clamp(amount, 0.0, 1.0)), scene.a);
}
