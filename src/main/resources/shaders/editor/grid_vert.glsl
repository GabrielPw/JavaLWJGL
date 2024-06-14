#version 330 core

layout (location = 0) in vec2 aPos;

out vec2 pos;

uniform mat4 view;
uniform mat4 projection;
void main() {

    pos = aPos;

    vec3 position = vec3(aPos, 0.0f);
    gl_Position =  projection * view * vec4(position, 1.0);
}