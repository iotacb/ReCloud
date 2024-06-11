#version 330 core

layout(location = 0) in vec2 position;
layout(location = 1) in vec2 texCoords;
layout(location = 2) in vec4 color;

out vec4 vColor;

uniform mat4 projection;

void main() {
    vColor = color;
    gl_Position = projection * vec4(position, 0.0, 1.0);
}
