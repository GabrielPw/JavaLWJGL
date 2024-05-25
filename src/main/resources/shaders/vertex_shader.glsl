#version 330 core
layout (location = 0) in vec3 aPos;

out vec3 fragPos;

uniform vec2 offsets[3];
uniform mat4 transform;

void main()
{

    vec2 offset = offsets[gl_InstanceID];

    fragPos = (transform * vec4(aPos + vec3(offset, 0.0), 1.0)).xyz;

    gl_Position = transform * vec4(aPos + vec3(offset, 0.0), 1.0);
};