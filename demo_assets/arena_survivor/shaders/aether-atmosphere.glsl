#version 330 core

in vec2 TexCoord;

uniform sampler2D inputTexture;
uniform vec2 resolution;
uniform float effectTime;
uniform vec2 playerCenter;
uniform float danger;
uniform float bossEnergy;
uniform float combat;

out vec4 FragColor;

float hash21(vec2 point) {
    point = fract(point * vec2(123.34, 456.21));
    point += dot(point, point + 45.32);
    return fract(point.x * point.y);
}

float luminance(vec3 color) {
    return dot(color, vec3(0.2126, 0.7152, 0.0722));
}

void main() {
    vec2 safeResolution = max(resolution, vec2(1.0));
    float aspect = safeResolution.x / safeResolution.y;
    vec2 centered = (TexCoord - vec2(0.5)) * vec2(aspect, 1.0);
    float edge = smoothstep(0.18, 0.82, length(centered));

    float flowX = sin(TexCoord.y * 19.0 + effectTime * 0.72)
            + sin(TexCoord.y * 43.0 - effectTime * 0.31) * 0.35;
    float flowY = cos(TexCoord.x * 17.0 - effectTime * 0.44) * 0.45;
    vec2 flow = vec2(flowX, flowY) * (0.00020 + combat * 0.00032);

    float chroma = edge * (0.00035 + combat * 0.00035 + bossEnergy * 0.0008);
    vec2 split = vec2(chroma / aspect, chroma * 0.25);
    vec2 uv = clamp(TexCoord + flow, 0.0, 1.0);
    vec3 color;
    color.r = texture(inputTexture, clamp(uv + split, 0.0, 1.0)).r;
    color.g = texture(inputTexture, uv).g;
    color.b = texture(inputTexture, clamp(uv - split, 0.0, 1.0)).b;

    vec2 playerDelta = (TexCoord - playerCenter) * vec2(aspect, 1.0);
    float playerDistance = length(playerDelta);
    float auraRadius = 0.11 + sin(effectTime * 2.2) * 0.012;
    float auraRing = exp(-pow((playerDistance - auraRadius) / 0.035, 2.0));
    float auraCore = 1.0 - smoothstep(0.0, 0.28, playerDistance);
    color += vec3(0.10, 0.82, 0.74) * (auraRing * 0.025 + auraCore * 0.012);

    float scanline = sin(gl_FragCoord.y * 1.58 + effectTime * 1.5) * 0.5 + 0.5;
    color *= 0.988 + scanline * 0.012;

    vec2 gridUv = TexCoord * safeResolution / 74.0;
    vec2 gridCell = abs(fract(gridUv) - 0.5);
    float gridLine = smoothstep(0.465, 0.5, max(gridCell.x, gridCell.y));
    float darkMask = 1.0 - smoothstep(0.08, 0.42, luminance(color));
    float gridPulse = 0.5 + 0.5 * sin(effectTime * 1.25 + gridUv.x * 0.31 + gridUv.y * 0.23);
    color += vec3(0.06, 0.40, 0.44) * gridLine * darkMask * gridPulse * 0.022;

    float bossSweep = 0.5 + 0.5 * sin(effectTime * 2.4 + TexCoord.y * 9.0);
    color += vec3(0.32, 0.06, 0.46) * bossEnergy * edge * bossSweep * 0.055;

    float dangerPulse = 0.55 + 0.45 * sin(effectTime * 6.4);
    float dangerEdge = smoothstep(0.20, 0.88, length(centered));
    color = mix(color, color * vec3(1.18, 0.54, 0.58),
            danger * danger * dangerEdge * dangerPulse * 0.20);
    color += vec3(0.20, 0.005, 0.018) * danger * dangerEdge * dangerPulse * 0.11;

    float grain = hash21(gl_FragCoord.xy + floor(effectTime * 50.0)) - 0.5;
    color += grain * (0.010 + combat * 0.005);

    FragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
