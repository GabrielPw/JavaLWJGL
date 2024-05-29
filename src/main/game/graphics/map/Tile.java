package main.game.graphics.map;

import main.game.graphics.Primitives;
import main.game.graphics.TextureLoader;
import main.game.graphics.Vertex;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Tile {


    protected Vertex[] vertices = Primitives.squareVertices;
    protected int[] indices = Primitives.squareIndices;

    private String texturePath;
    private Vector3f position;
    private Vector2f scale;
    private int texture;

    public Tile(Vector3f pos, String texturePath){

        this.position = pos;
        this.texturePath = texturePath;
        this.scale = new Vector2f(1.f);

        this.texture = loadTexture();
    }

    protected int loadTexture(){
;
        return TextureLoader.loadTexture(this.texturePath);
    }

    protected float[] getVerticesData(){

        float[] verticesData = new float[vertices.length * 8];
        int index = 0;
        for (int i = 0; i < vertices.length; i++) {
            verticesData[index++] = vertices[i].position.x;
            verticesData[index++] = vertices[i].position.y;
            verticesData[index++] = vertices[i].position.z;
            verticesData[index++] = vertices[i].color.x;
            verticesData[index++] = vertices[i].color.y;
            verticesData[index++] = vertices[i].color.z;
            verticesData[index++] = vertices[i].textureCoord.x;
            verticesData[index++] = vertices[i].textureCoord.y;
        }

        return verticesData;
    }

    public int getTexture() {
        return texture;
    }

    public Vector3f getPosition() {
        return position;
    }

    public String getTexturePath() {
        return texturePath;
    }
}
