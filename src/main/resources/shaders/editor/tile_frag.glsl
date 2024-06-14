#version 330 core

out vec4 FragColor;

in vec2 fragPos;
in vec2 textureCoord;

uniform sampler2D textureAtlas;

void main()
{
    vec3 color = vec3(1.f);

    FragColor = texture(textureAtlas, textureCoord);
    //FragColor = vec4(color, 1.f);
};