package main.game.graphics.map;

import main.game.Shader;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Map {

    private int VBO, VAO, EBO, VBOTextureIndex;
    private Shader shaderProgram;
    private Tile[][] tiles;
    private HashSet<String> allTexturePath;
    private Matrix4f model;
    private int textureArray;
    private int[] allTextureIds;

    public Map(Tile[][] tiles, String vertShader, String fragShader) {

        this.tiles = tiles;
        this.shaderProgram = new Shader(vertShader, fragShader);
        this.model = new Matrix4f().identity();

        initializeBuffers();
        setupTexturePaths();
    }

    private void initializeBuffers(){
        this.VAO             = GL30.glGenVertexArrays();
        this.VBO             = GL30.glGenBuffers();
        this.VBOTextureIndex = GL30.glGenBuffers();
        this.EBO             = GL30.glGenBuffers();

        GL30.glBindVertexArray(VAO);

        int totalVertices = tiles.length * tiles[0].length;

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(totalVertices * 4 * 8); // tile formado por 4 vertices, cada um com 8 informações.

        for (Tile[] tileRow : tiles) {
            for (Tile tile : tileRow) {
                float[] data = tile.getVerticesData();
                vertexBuffer.put(data);
            }
        }

        vertexBuffer.flip();

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, VBO);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertexBuffer, GL30.GL_STATIC_DRAW);

        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, EBO);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, tiles[0][0].indices, GL30.GL_STATIC_DRAW);

        GL30.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 8 * Float.BYTES, 0);
        GL30.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
        GL30.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, 8 * Float.BYTES, 6 * Float.BYTES); // Texture coordinates

        GL30.glEnableVertexAttribArray(0);
        GL30.glEnableVertexAttribArray(1);
        GL30.glEnableVertexAttribArray(2);

    }

    public void render(float deltaTime){

        GL30.glBindVertexArray(VAO);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, VBO);
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, EBO);

        shaderProgram.use();
        shaderProgram.addUniform1f("time", deltaTime);
        shaderProgram.addUniformMatrix4fv("model", this.model);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, textureArray);
        GL30.glActiveTexture(GL30.GL_TEXTURE0);

        setupTilePosition();

        model.identity();
        model.scale(0.5f);

        GL30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, textureArray);

        GL31.glDrawElementsInstanced(GL11.GL_TRIANGLES, tiles[0][0].indices.length, GL11.GL_UNSIGNED_INT, 0, 4);
        GL30.glBindVertexArray(0);
    }

    private void setupTilePosition(){

        List<Vector2f> tilePositionsList = new ArrayList<>();

        for (int row = 0; row < tiles.length; row++) {
            for (int col = 0; col < tiles[row].length; col++) {
                Tile tile = tiles[row][col];
                tilePositionsList.add(new Vector2f(tile.getPosition().x, tile.getPosition().y));
            }
        }

        Vector2f[] tilePositions = tilePositionsList.toArray(new Vector2f[0]);

        shaderProgram.addUniform2fvArray("tilePositionOffset", tilePositions);
    }

    public void setupTexturePaths() {
        allTexturePath = new HashSet<>();

        int w = 0, h = 0;
        HashMap<String, ByteBuffer> textureData = new HashMap<>();

        for (Tile[] tileRow : tiles) {
            for (Tile tile : tileRow) {
                String texturePath = tile.getTexturePath();
                if (allTexturePath.add(texturePath)) {
                    allTextureIds = new int[allTexturePath.size()];
                    allTextureIds[allTexturePath.size() - 1] = tile.getTexture();
                }
            }
        }

        for (String texturePath : allTexturePath) {
            IntBuffer width = BufferUtils.createIntBuffer(1);
            IntBuffer height = BufferUtils.createIntBuffer(1);
            IntBuffer channels = BufferUtils.createIntBuffer(1);
            ByteBuffer data = STBImage.stbi_load(texturePath, width, height, channels, 4);

            if (data == null) {
                throw new RuntimeException("Failed to load a texture file!"
                        + System.lineSeparator() + STBImage.stbi_failure_reason());
            }
            if (w == 0 && h == 0) {
                w = width.get(0);
                h = height.get(0);
            } else if (w != width.get(0) || h != height.get(0)) {
                throw new RuntimeException("All textures must have the same dimensions.");
            }

            textureData.put(texturePath, data);
        }

        textureArray = GL30.glGenTextures();

        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY ,textureArray);

        GL42.glTexStorage3D(GL30.GL_TEXTURE_2D_ARRAY, 1, GL30.GL_RGBA8, w, h, allTexturePath.size());

        int layerCount = allTexturePath.size();
        for (java.util.Map.Entry<String, ByteBuffer> entry : textureData.entrySet()) {

            ByteBuffer data = entry.getValue();
            GL30.glTexSubImage3D( GL30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, 0, w, h, layerCount, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, data);
        }

        GL30.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY,GL30.GL_TEXTURE_MIN_FILTER,GL30.GL_LINEAR);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY,GL30.GL_TEXTURE_MAG_FILTER,GL30.GL_LINEAR);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY,GL30.GL_TEXTURE_WRAP_S,GL30.GL_CLAMP_TO_EDGE);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY,GL30.GL_TEXTURE_WRAP_T,GL30.GL_CLAMP_TO_EDGE);
    }

    public void destroy(){
        GL30.glDeleteBuffers(VBO);
        GL30.glDeleteBuffers(EBO);
        GL30.glDeleteVertexArrays(VAO);
    }


}

