#version 330 core

out vec4 FragColor;
in vec2 pos;

void main() {

    vec3 color = vec3(1.f, 1.f, 1.f);

    FragColor = vec4(color, 1.f);
}