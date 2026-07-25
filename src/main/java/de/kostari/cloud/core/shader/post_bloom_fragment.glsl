#version 330 core

in vec2 TexCoord;

uniform sampler2D inputTexture;
uniform vec2 resolution;
uniform float threshold;
uniform float intensity;
uniform float radius;

out vec4 FragColor;

void main() {
    vec4 scene = texture(inputTexture, TexCoord);
    vec2 texel = 1.0 / max(resolution, vec2(1.0));
    vec3 bloom = vec3(0.0);
    float totalWeight = 0.0;

    for (int y = -2; y <= 2; y++) {
        for (int x = -2; x <= 2; x++) {
            vec2 offset = vec2(float(x), float(y)) * texel * radius;
            vec3 sampleColor = texture(inputTexture, TexCoord + offset).rgb;
            float brightness = max(max(sampleColor.r, sampleColor.g), sampleColor.b);
            float weight = max(0.0, 1.0 - length(vec2(float(x), float(y))) / 3.0);
            bloom += max(sampleColor - vec3(threshold), vec3(0.0)) * smoothstep(threshold, 1.0, brightness) * weight;
            totalWeight += weight;
        }
    }

    bloom /= max(totalWeight, 0.0001);
    FragColor = vec4(scene.rgb + bloom * intensity, scene.a);
}
