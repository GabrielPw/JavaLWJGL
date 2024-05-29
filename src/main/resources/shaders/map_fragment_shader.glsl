#version 330 core

out vec4 FragColor;

in vec3 fragPos;
in vec3 vertexColor;
in vec2 vertexTextureCoord;

uniform sampler2DArray textureArray;

void main() {

    vec4 color = texture(textureArray, vec3(vertexTextureCoord, 0));
    FragColor = vec4(color);
}