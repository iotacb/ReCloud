#version 330 core

in vec2 TexCoord;

uniform sampler2D inputTexture;
uniform float exposure;
uniform float contrast;
uniform float saturation;
uniform float temperature;
uniform float tint;
uniform float gamma;

out vec4 FragColor;

void main() {
    vec4 scene = texture(inputTexture, TexCoord);
    vec3 color = scene.rgb;

    color *= exp2(exposure);
    color = (color - 0.5) * contrast + 0.5;

    float luminance = dot(color, vec3(0.2126, 0.7152, 0.0722));
    color = mix(vec3(luminance), color, saturation);

    color.r += temperature * 0.10;
    color.b -= temperature * 0.10;
    color.g += tint * 0.06;
    color.r -= tint * 0.03;
    color.b -= tint * 0.03;

    color = pow(max(color, vec3(0.0)), vec3(1.0 / max(gamma, 0.001)));
    FragColor = vec4(clamp(color, 0.0, 1.0), scene.a);
}
