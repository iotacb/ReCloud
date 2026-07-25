#version 330 core

in vec2 TexCoord;   // The texture coordinate from the vertex shader
in vec4 Color;      // The color from the vertex shader
flat in int TextureIndex;

uniform sampler2D textureSamplers[16];

out vec4 FragColor; // The final output color

vec4 sampleTexture(int textureIndex, vec2 uv) {
    switch (textureIndex) {
        case 0: return texture(textureSamplers[0], uv);
        case 1: return texture(textureSamplers[1], uv);
        case 2: return texture(textureSamplers[2], uv);
        case 3: return texture(textureSamplers[3], uv);
        case 4: return texture(textureSamplers[4], uv);
        case 5: return texture(textureSamplers[5], uv);
        case 6: return texture(textureSamplers[6], uv);
        case 7: return texture(textureSamplers[7], uv);
        case 8: return texture(textureSamplers[8], uv);
        case 9: return texture(textureSamplers[9], uv);
        case 10: return texture(textureSamplers[10], uv);
        case 11: return texture(textureSamplers[11], uv);
        case 12: return texture(textureSamplers[12], uv);
        case 13: return texture(textureSamplers[13], uv);
        case 14: return texture(textureSamplers[14], uv);
        case 15: return texture(textureSamplers[15], uv);
    }
    return texture(textureSamplers[0], uv);
}

void main() {
    FragColor = sampleTexture(TextureIndex, TexCoord) * Color;
}
