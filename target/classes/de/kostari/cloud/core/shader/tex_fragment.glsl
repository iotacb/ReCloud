#version 330 core

in vec2 vTexCoords;
in vec4 vColor;

out vec4 fragColor;

uniform sampler2D textureSampler;

void main() {
    vec4 texColor = texture(textureSampler, vTexCoords);
    fragColor = texColor * vColor;
}
