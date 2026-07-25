#version 330 core

in vec2 TexCoord;

uniform sampler2D inputTexture;
uniform sampler2D bloomTexture;
uniform float intensity;

out vec4 FragColor;

void main() {
    vec4 scene = texture(inputTexture, TexCoord);
    vec3 bloom = texture(bloomTexture, TexCoord).rgb;

    FragColor = vec4(scene.rgb + bloom * intensity, scene.a);
}
