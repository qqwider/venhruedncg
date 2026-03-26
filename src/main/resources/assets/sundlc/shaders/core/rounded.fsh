#version 150
precision mediump float;

in vec4 vertexColor;
in vec2 localPos; // Всегда 0.0 - 1.0

uniform sampler2D Sampler0;
uniform vec2 Size;
uniform vec4 Radius;
uniform int UseTexture;
uniform vec4 AtlasRegion; // [u1, v1, u2, v2]

out vec4 fragColor;

float sdRoundedBox(vec2 p, vec2 b, vec4 r) {
    r.xy = (p.x > 0.0) ? r.zw : r.xy;
    r.x  = (p.y > 0.0) ? r.y  : r.x;
    vec2 q = abs(p) - b + r.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;
}

void main() {
    vec2 p = localPos * Size;
    vec2 halfSize = Size * 0.5;

    float dist = sdRoundedBox(p - halfSize, halfSize, Radius);
    float alpha = smoothstep(0.5, -0.5, dist);

    if (alpha <= 0.0) discard;

    vec4 color = vertexColor;
    if (UseTexture == 1) {
        // Вырезаем лицо из атласа на основе локальной позиции 0-1
        vec2 uv = mix(AtlasRegion.xy, AtlasRegion.zw, localPos);
        color *= texture(Sampler0, uv);
    }

    fragColor = vec4(color.rgb, color.a * alpha);
}