#version 330 core

in vec2 TexCoord;

uniform sampler2D inputTexture;
uniform vec2 resolution;
uniform vec2 direction;
uniform float radius;

out vec4 FragColor;

void main() {
    vec2 texel = 1.0 / max(resolution, vec2(1.0));
    vec2 sampleStep = direction * texel * max(radius, 0.5);
    vec3 color = texture(inputTexture, TexCoord).rgb * 0.2270270270;

    color += texture(inputTexture, TexCoord + sampleStep * 1.3846153846).rgb * 0.3162162162;
    color += texture(inputTexture, TexCoord - sampleStep * 1.3846153846).rgb * 0.3162162162;
    color += texture(inputTexture, TexCoord + sampleStep * 3.2307692308).rgb * 0.0702702703;
    color += texture(inputTexture, TexCoord - sampleStep * 3.2307692308).rgb * 0.0702702703;

    FragColor = vec4(color, 1.0);
}
