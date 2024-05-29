#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aVertexColor;
layout (location = 2) in vec2 aVertexTextureCoord;

out vec3 fragPos;
out vec3 vertexColor;
out vec2 vertexTextureCoord;

uniform float time;
uniform vec2 tilePositionOffset[4];
uniform mat4 model;

void main()
{
    fragPos = aPos;
    vertexColor = aVertexColor;
    vertexTextureCoord = aVertexTextureCoord;

    vec3 tileOffset = vec3(tilePositionOffset[gl_InstanceID], 0);

    tileOffset.x *= cos(time);
    gl_Position = model * vec4(aPos + tileOffset, 1.0);
};