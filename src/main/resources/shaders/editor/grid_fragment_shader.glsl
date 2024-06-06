#version 330 core

out vec4 FragColor;

in vec2 fragPos;
in vec2 textureCoord;

uniform bool hasTexture;
uniform sampler2D textureAtlas;
uniform vec2 actualSpriteOffset;

void main()
{
    // Dimensões do atlas e do sprite
    float atlasWidth = 320.0f;
    float atlasHeight = 320.0f;
    float spriteWidth = 16.0f;
    float spriteHeight = 16.0f;

    // Calcula as coordenadas de textura normalizadas para o sprite atual
    vec2 spriteSize = vec2(spriteWidth / atlasWidth, spriteHeight / atlasHeight);
    vec2 vertexTextureCoordNorm = textureCoord * spriteSize + actualSpriteOffset * spriteSize;

    vertexTextureCoordNorm.x -= 0.001f; // evitar atlas bleeding.
    vertexTextureCoordNorm.y += 0.001f; // evitar atlas bleeding.

    // renderizando selected tile.
    if(hasTexture){
        FragColor = texture(textureAtlas, textureCoord);
    } else {
        // renderizando cor da grid.
        FragColor = vec4(vec3(1.f), 1.f);
    }
};