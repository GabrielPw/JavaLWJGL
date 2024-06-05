#version 330 core
layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aVertexTextureCoord;

out vec2 fragPos;
out vec2 vertexTextureCoord;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

void main()
{
    fragPos            = aPos;
    vertexTextureCoord = aVertexTextureCoord;

    vec3 position = vec3(aPos, 1.f);

    gl_Position = projection * view * model *  vec4(position, 1.0);
};