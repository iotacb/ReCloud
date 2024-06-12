#version 330 core

in vec2 TexCoord;   // The texture coordinate from the vertex shader
in vec4 Color;      // The color from the vertex shader

uniform sampler2D textureSampler; // The texture sampler

out vec4 FragColor; // The final output color

void main() {
    vec4 texColor = texture(textureSampler, TexCoord); // Sample the texture at the texture coordinate
    FragColor = texColor * Color; // Multiply the sampled texture color by the input color
}
