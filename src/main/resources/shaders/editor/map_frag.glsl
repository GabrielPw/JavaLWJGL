#version 330 core

out vec4 FragColor;
in vec2 pos;
in vec2 textureCoord;

uniform sampler2D textureAtlas;
void main() {

    vec3 color = vec3(1.f, 0.f, 0.f);
    FragColor = vec4(vec3(pos, 1.0f), 1.f);
}