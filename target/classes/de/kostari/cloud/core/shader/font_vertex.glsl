#version 330 core

layout(location = 0) in vec2 position;
layout(location = 1) in vec2 texCoord;
layout(location = 2) in vec4 color;

out vec2 TexCoord;
out vec4 Color;

uniform mat4 combinedMatrix;

void main() {
    gl_Position = combinedMatrix * vec4(position, 0.0, 1.0);
    TexCoord = texCoord;
    Color = color;
}
