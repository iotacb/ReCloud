#version 330 core

in vec2 TexCoord;

uniform sampler2D inputTexture;
uniform vec2 resolution;
uniform vec2 mouse;
uniform float time;
uniform float strength;
uniform int mode;
uniform int mouseDown;

out vec4 FragColor;

float circleMask(float distanceFromMouse, float radius, float softness) {
    return 1.0 - smoothstep(radius - softness, radius, distanceFromMouse);
}

void main() {
    vec2 uv = TexCoord;
    vec2 safeResolution = max(resolution, vec2(1.0));
    vec2 mouseUv = vec2(mouse.x / safeResolution.x, 1.0 - mouse.y / safeResolution.y);

    // Correct distances so the interaction stays circular on wide windows.
    vec2 aspect = vec2(safeResolution.x / safeResolution.y, 1.0);
    vec2 fromMouse = (uv - mouseUv) * aspect;
    float distanceFromMouse = length(fromMouse);
    float interaction = circleMask(distanceFromMouse, 0.34, 0.15);
    float clickBoost = mouseDown == 1 ? 1.8 : 1.0;

    vec3 color;

    if (mode == 0) {
        // A time-driven ripple that bends the scene around the pointer.
        vec2 direction = fromMouse / max(distanceFromMouse, 0.001);
        float wave = sin(distanceFromMouse * 55.0 - time * 7.0);
        vec2 offset = direction * wave * interaction * strength * clickBoost * 0.012;
        offset.x /= aspect.x;
        color = texture(inputTexture, clamp(uv + offset, 0.0, 1.0)).rgb;
    } else if (mode == 1) {
        // Pixelate only inside the pointer field and animate its grid size.
        float pixelSize = mix(3.0, 24.0, strength)
                + sin(time * 3.0) * 2.0 * strength;
        vec2 cells = safeResolution / max(pixelSize, 1.0);
        vec2 pixelUv = (floor(uv * cells) + 0.5) / cells;
        color = texture(inputTexture, mix(uv, pixelUv, interaction)).rgb;

        vec2 cellEdge = abs(fract(uv * cells) - 0.5);
        float gridLine = smoothstep(0.46, 0.50, max(cellEdge.x, cellEdge.y));
        color += vec3(0.10, 0.65, 0.85) * gridLine * interaction * strength;
    } else {
        // Split RGB sampling directions to create chromatic aberration.
        vec2 direction = fromMouse / max(distanceFromMouse, 0.001);
        vec2 offset = direction * interaction * strength * clickBoost * 0.018;
        offset.x /= aspect.x;

        color.r = texture(inputTexture, clamp(uv + offset, 0.0, 1.0)).r;
        color.g = texture(inputTexture, uv).g;
        color.b = texture(inputTexture, clamp(uv - offset, 0.0, 1.0)).b;
    }

    // The ring makes the active interaction radius easy to see in every mode.
    float ring = 1.0 - smoothstep(0.003, 0.012, abs(distanceFromMouse - 0.34));
    vec3 ringColor = mix(vec3(0.15, 0.85, 1.0), vec3(1.0, 0.25, 0.65),
            0.5 + 0.5 * sin(time * 2.0));
    color += ringColor * ring * (0.25 + strength * 0.45);

    FragColor = vec4(color, 1.0);
}
