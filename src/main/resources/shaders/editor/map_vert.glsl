#version 330 core
layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTextureCoord;

out vec2 pos;
out vec2 textureCoord;

uniform mat4 view;
void main() {

    pos          = aPos;
    textureCoord = aTextureCoord;

    vec3 position = vec3(aPos.x, aPos.y, 0.0f);
    gl_Position =  view * vec4(position, 1.0);
}