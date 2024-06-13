#version 330 core

in vec2 TexCoord;
in vec4 Color;

out vec4 FragColor;

uniform sampler2D fontTexture;

void main() {
    FragColor = texture(fontTexture, TexCoord) * Color;
}
