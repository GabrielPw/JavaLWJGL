#version 330 core
layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTextureCoord;

out vec2 fragPos;
out vec2 textureCoord;

uniform mat4 model;
uniform mat4 view;

void main()
{
    fragPos            = aPos;
    textureCoord       = aTextureCoord;

    vec3 position = vec3(aPos, 0.f);

    gl_Position =  model * view * vec4(position, 1.0);
};