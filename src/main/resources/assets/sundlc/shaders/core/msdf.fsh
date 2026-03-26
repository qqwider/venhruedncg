#version 150

in vec4 vertexColor;
in vec2 texCoord;

uniform sampler2D Sampler0;

out vec4 fragColor;

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

void main() {
    // Если общая альфа вершины 0, то сразу выходим, чтобы не было вспышек
    if (vertexColor.a < 0.001) discard;

    vec3 msd = texture(Sampler0, texCoord).rgb;
    float sd = median(msd.r, msd.g, msd.b);

    float screenPxDistance = 10.0 * (sd - 0.5);
    // Защита от деления на 0 при анимации масштаба
    float fw = max(fwidth(screenPxDistance), 0.0001);
    float opacity = clamp(screenPxDistance / fw + 0.5, 0.0, 1.0);

    float finalAlpha = vertexColor.a * opacity;
    if (finalAlpha < 0.01) discard;

    fragColor = vec4(vertexColor.rgb, finalAlpha);
}