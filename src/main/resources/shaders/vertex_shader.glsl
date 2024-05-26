#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aVertexColor;
layout (location = 2) in vec2 aVertexTextureCoord;

out vec3 fragPos;
out vec3 vertexColor;
out vec2 vertexTextureCoord;

uniform vec2 offsets[4];
uniform mat4 transform;

void main()
{

    vertexColor        = aVertexColor;
    vertexTextureCoord = aVertexTextureCoord;

    vec2 offset = offsets[gl_InstanceID];

    fragPos = (transform * vec4(aPos + vec3(offset, 0.0), 1.0)).xyz;

    gl_Position = transform * vec4(aPos + vec3(offset, 0.0), 1.0);
};