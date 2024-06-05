#version 330 core

out vec4 FragColor;

in vec2 fragPos;
in vec2 vertexTextureCoord;

uniform float time;
uniform sampler2D playerTexture;
uniform vec2 actualSpriteOffset;

void main()
{
    vec2 st = gl_FragCoord.xy;

    vec3 color = vec3(fragPos, 1.f);
    color = vec3(1);

    // Dimensões do atlas e do sprite
    float atlasWidth = 192.0f;
    float atlasHeight = 128.0f;
    float spriteWidth = 16.0f;
    float spriteHeight = 16.0f;

    // Calcula as coordenadas de textura normalizadas para o sprite atual
    vec2 spriteSize = vec2(spriteWidth / atlasWidth, spriteHeight / atlasHeight);
    vec2 vertexTextureCoordNorm = vertexTextureCoord * spriteSize + actualSpriteOffset * spriteSize;

    vertexTextureCoordNorm.x -= 0.001f; // evitar atlas bleeding.
    vertexTextureCoordNorm.y += 0.001f; // evitar atlas bleeding.

    FragColor = texture(playerTexture, vertexTextureCoordNorm) * vec4(color, 1);
};