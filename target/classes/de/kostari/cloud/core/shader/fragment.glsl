#version 330 core

in vec2 TexCoord;   // The texture coordinate from the vertex shader
in vec4 Color;      // The color from the vertex shader

out vec4 FragColor; // The final output color

void main() {
    FragColor = Color; // Set the output color to the input color (no texture applied)
}
