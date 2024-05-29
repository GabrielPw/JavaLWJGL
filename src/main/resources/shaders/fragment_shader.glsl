#version 330 core

out vec4 FragColor;

in vec3 fragPos;
in vec3 vertexColor;
in vec2 vertexTextureCoord;

uniform float time;

uniform sampler2D ourTexture;

void main()
{

    vec2 st = gl_FragCoord.xy;

    vec4 textureColor = texture(ourTexture, vertexTextureCoord);

    FragColor = vec4(textureColor);
};