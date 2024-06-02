#version 330 core

out vec4 FragColor;

in vec2 fragPos;
in vec2 vertexTextureCoord;

uniform float time;
uniform sampler2D playerTexture;

void main()
{
    vec2 st = gl_FragCoord.xy;

    vec3 color = vec3(fragPos, 1.f);
    color = vec3(1);

    FragColor = texture(playerTexture, vertexTextureCoord) * vec4(color, 1);
};