package main.game.graphics.map;

import main.game.Shader;
import main.game.graphics.Primitives;
import main.game.graphics.TextureLoader;
import main.game.graphics.Vertex;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class Tile {

    private Matrix4f model;
    private Vector3f position;
    private Vertex[] vertices;
    private Vector3f scale;
    private int spriteIndex;

    public Tile(Vector3f position, int index){

        this.position    = position;
        this.vertices = Primitives.squareVertices;
        this.scale       = new Vector3f(1.f);
        this.model       = new Matrix4f().identity();
        this.spriteIndex = index;
    }

    public void update(Shader shader){

        this.model.identity();
        this.model.scale(this.scale);
        this.model.translate(this.position);

        shader.addUniformMatrix4fv("model", this.model);

        //GL30.glActiveTexture(GL30.GL_TEXTURE0);
        //GL30.glBindTexture(GL30.GL_TEXTURE_2D, this.textureID); // Ativa a textura correta
    }

    public Vector2f[] calculateTextureCoordinates(int columns, int rows, int spriteWidth, int spriteHeight, int textureWidth, int textureHeight) {
        float spriteWidthNorm = (float) spriteWidth / textureWidth;
        float spriteHeightNorm = (float) spriteHeight / textureHeight;

        // Offset to avoid atlas bleeding
        float offset = 0.5f / textureWidth; // Half a pixel offset for X coordinates
        float offsetY = 0.5f / textureHeight; // Half a pixel offset for Y coordinates

        int column = this.spriteIndex % columns;
        int row = this.spriteIndex / columns;

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

    public Matrix4f getModel() {
        return model;
    }

    public Vector3f getPosition() {
        return position;
    }


    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public void setScale(Vector3f scale) {
        this.scale = scale;
    }

    public int getSpriteIndex() {
        return spriteIndex;
    }

    public float[] getPositionAsFloatArray() {

        float[] positions = new float[3];

        positions[0] = (this.position.x);
        positions[1] = (this.position.y);
        positions[2] = (this.position.z);

        return positions;
    }

    public Vertex[] getVertices() {
        return vertices;
    }
}
