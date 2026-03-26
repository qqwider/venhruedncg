#version 150

in vec3 Position;
in vec2 UV0; // Теперь UV идет вторым
in vec4 Color; // Цвет третьим

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec2 texCoord;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    texCoord = UV0;
}