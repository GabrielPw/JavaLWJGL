package main.game.graphics.map;

import main.game.Shader;
import main.game.graphics.Primitives;
import main.game.graphics.TextureLoader;
import main.game.graphics.TexturePaths;
import main.game.graphics.Vertex;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameMap {

    private int VAO, VBO, EBO;
    private int atlasTexture;
    private Matrix4f model;
    private float scale;
    private Shader shader;
    List<Tile> tiles = new ArrayList<>();
    private int qntRowsOfTiles;
    private int qntColsOfTiles;
    private int qntTiles;

    private final int spriteSize = 16;
    private final int atlasQntCols = 20;
    private final int atlasQntRows = 20;
    private final int textureSize = 320;


    public GameMap(String atlasTexturePath, Shader shader){

        this.VAO = GL30.glGenVertexArrays();
        this.VBO = GL30.glGenBuffers();
        this.EBO = GL30.glGenBuffers();

        this.shader = shader;
        this.model = new Matrix4f().identity();
        this.scale = 1.f;

        this.atlasTexture = TextureLoader.loadTexture(atlasTexturePath);

        //this.tiles = generateTiles();
        loadMapFromFile("src/main/resources/maps/map1.txt");
        this.qntTiles = this.tiles.size();
        createBuffers();

        tiles.clear(); // free memory
        tiles = null;
    }

    public void render(){

        GL30.glBindVertexArray(VAO);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER,VBO);
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, EBO);

        GL11.glDrawElements(GL11.GL_TRIANGLES, Primitives.squareIndices.length * qntTiles, GL11.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
    }

    public void update(Matrix4f projection, Matrix4f view, float deltaTime){

        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, atlasTexture);

        shader.use();

        model.identity();
        shader.addUniform1f("time", deltaTime);
        shader.addUniformMatrix4fv("projection", projection);
        shader.addUniformMatrix4fv("view", view);
        shader.addUniform1f("atlasTexture", atlasTexture);

        model.identity();
        model.scale(this.scale);

        shader.addUniformMatrix4fv("model", model);
    }

    private void createBuffers(){

        GL30.glBindVertexArray(VAO);

        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(tiles.size() * Primitives.squareVertices.length * 8);
        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(tiles.size() * Primitives.squareIndices.length);

        int vertexOffset = 0;
        for (Tile tile : tiles) {

            System.out.println("TilePosX: " + tile.getPosition().x);
            int index = 0;
            Vector2f[] textCoord = tile.calculateTextureCoordinates(atlasQntCols, atlasQntRows, spriteSize,spriteSize, textureSize,textureSize);
            for (Vertex squareVertice : tile.getVertices()) {
                Vector2f textureCoord = textCoord[index];

                verticesBuffer.put(squareVertice.position.x + tile.getPosition().x);
                verticesBuffer.put(squareVertice.position.y + tile.getPosition().y);
                verticesBuffer.put(squareVertice.position.z + tile.getPosition().z);
                verticesBuffer.put(squareVertice.color.x);
                verticesBuffer.put(squareVertice.color.y);
                verticesBuffer.put(squareVertice.color.z);
                verticesBuffer.put(textureCoord.x);
                verticesBuffer.put(textureCoord.y);

                index++;
            }

            for (int valueIndex : Primitives.squareIndices) {
                indicesBuffer.put(valueIndex + vertexOffset);
            }
            vertexOffset += Primitives.squareVertices.length;
        }

        verticesBuffer.flip();
        indicesBuffer.flip();

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER,VBO);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, verticesBuffer, GL30.GL_STATIC_DRAW);

        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, EBO);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL30.GL_STATIC_DRAW);

        GL30.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 8 * Float.BYTES, 0);
        GL30.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
        GL30.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, 8 * Float.BYTES, 6 * Float.BYTES); // Texture coordinates

        GL30.glEnableVertexAttribArray(0);
        GL30.glEnableVertexAttribArray(1);
        GL30.glEnableVertexAttribArray(2);

        verticesBuffer.clear();
        indicesBuffer.clear();
    }

    private List<Tile> generateTiles(){
        Tile t1 = new Tile(new Vector3f(-2.0f, 0.f, 0.f), 0);
        List<Tile> tiles = new ArrayList<>(Arrays.asList(t1));

        for (int i = -1; i < 4; i++){

            Tile tile = new Tile(new Vector3f(i*1f, 0, 0), i);
            tiles.add(tile);
        }

        return tiles;
    }

    private void loadMapFromFile(String mapFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(mapFilePath))) {
            String line;
            int y = 0;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(" ");
                qntColsOfTiles = values.length;
                for (int x = 0; x < values.length; x++) {
                    // Verifique se o valor é vazio ou não
                    if (values[x].isEmpty() || values[x].equals("...")) {
                        continue; // Pular espaços vazios
                    }
                    int tileIndex = Integer.parseInt(values[x]);
                    Tile tile = new Tile(new Vector3f(x, -y, 0), tileIndex);
                    tiles.add(tile);
                }
                y++;
            }
            qntRowsOfTiles = y;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void destroy(){
        GL30.glDeleteBuffers(this.VBO);
        GL30.glDeleteBuffers(this.EBO);
        GL30.glDeleteVertexArrays(this.VAO);

        GL30.glDeleteTextures(this.atlasTexture);
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
}
