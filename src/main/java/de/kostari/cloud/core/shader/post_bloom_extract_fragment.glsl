#version 330 core

in vec2 TexCoord;

uniform sampler2D inputTexture;
uniform float threshold;

out vec4 FragColor;

void main() {
    vec4 scene = texture(inputTexture, TexCoord);
    float thresholdValue = clamp(threshold, 0.0, 1.0);
    float brightness = max(max(scene.r, scene.g), scene.b);
    float kneeEnd = max(thresholdValue + 0.001, min(1.0, thresholdValue + 0.35));
    float contribution = smoothstep(thresholdValue, kneeEnd, brightness);
    vec3 bloom = max(scene.rgb - vec3(thresholdValue), vec3(0.0)) * contribution;

    FragColor = vec4(bloom, scene.a);
}
