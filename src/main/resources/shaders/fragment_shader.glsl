#version 330 core

out vec4 FragColor;

in vec3 fragPos;
in vec3 vertexColor;
in vec2 vertexTextureCoord;

uniform sampler2D ourTexture;

void main()
{
    //FragColor = vec4(vertexColor, 1.0);
    FragColor = texture(ourTexture, vertexTextureCoord);
};