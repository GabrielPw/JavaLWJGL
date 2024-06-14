package main.editor;

import org.joml.Vector2f;

public class TextureAtlas {

    private int atlasWidth;
    private int atlasHeight;
    private int tileWidth;
    private int tileHeight;
    private int tilesPerRow;

    public TextureAtlas(int atlasWidth, int atlasHeight, int tileWidth, int tileHeight) {
        this.atlasWidth = atlasWidth;
        this.atlasHeight = atlasHeight;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.tilesPerRow = atlasWidth / tileWidth;
    }

    public Vector2f[] getTextureCoordinates(int tileIndex) {

        float spriteWidthNorm = (float) tileWidth  / atlasWidth;
        float spriteHeightNorm =(float) tileHeight / atlasHeight;

        // Offset to avoid atlas bleeding
        float offset  = 0.5f / atlasWidth; // Half a pixel offset for X coordinates
        float offsetY = 0.5f / atlasHeight; // Half a pixel offset for Y coordinates

        int column = tileIndex % tilesPerRow;
        int row = tileIndex / tilesPerRow;

        // Troquei o yMin com o YMax pois a textura estava invertida.
        float xMin = column * spriteWidthNorm + offset;
        float xMax = (column + 1) * spriteWidthNorm - offset;
        float yMin = (row + 1) * spriteHeightNorm - offsetY; //
        float yMax = row * spriteHeightNorm + offsetY;

        return new Vector2f[] {
            new Vector2f(xMin, yMax),  // Top Left
            new Vector2f(xMin, yMin),  // Bottom Left
            new Vector2f(xMax, yMin),  // Bottom Right
            new Vector2f(xMax, yMax)   // Top Right
        };
    }

}
