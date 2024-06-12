#version 330 core

layout(location = 0) in vec2 aPos;        // The position variable has attribute position 0
layout(location = 1) in vec2 aTexCoord;   // The texture coordinate variable has attribute position 1
layout(location = 2) in vec4 aColor;      // The color variable has attribute position 2

uniform mat4 combinedMatrix;              // The combined projection and view matrix

out vec2 TexCoord;    // Output the texture coordinate to the fragment shader
out vec4 Color;       // Output the color to the fragment shader

void main() {
    TexCoord = aTexCoord;    // Pass the texture coordinate to the fragment shader
    Color = aColor;          // Pass the color to the fragment shader
    gl_Position = combinedMatrix * vec4(aPos, 0.0, 1.0); // Apply the combined matrix to the position and set the final position
}
