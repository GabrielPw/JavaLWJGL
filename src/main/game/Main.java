package main.game;

import main.game.graphics.Primitives;
import main.game.graphics.map.Tile;
import main.game.graphics.TextureLoader;
import main.game.graphics.Vertex;
import main.game.graphics.TexturePaths;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public class Main {

    public static void main(String[] args) {

        Window window = new Window("Testando texturas", 640, 480);
        double previousTime = glfwGetTime();
        int frameCount = 0;

        Matrix4f view = new Matrix4f().identity();
        Matrix4f tileModel = new Matrix4f().identity(); // Model that represents all tiles.

        Shader shader = new Shader("vertex_shader.glsl", "fragment_shader.glsl");
        int atlasTexture = TextureLoader.loadTexture(TexturePaths.textureAtlas1, GL30.GL_TEXTURE0);
        Tile t1 = new Tile(new Vector3f(-2.0f, 0.f, 0.f), 0);

        List<Tile> tiles = new ArrayList<>(Arrays.asList(t1));

        for (int i = -1; i < 4; i++){

            Tile tile = new Tile(new Vector3f(i*1f, 0, 0), i);
            tiles.add(tile);
        }

        int VAO, VBO, EBO;
        VAO = GL30.glGenVertexArrays();
        VBO = GL30.glGenBuffers();
        EBO = GL30.glGenBuffers();

        createBuffers(VAO, VBO, EBO, tiles);

        while (!glfwWindowShouldClose(window.getID())) {
            GL11.glClearColor((20.f / 255), (40.f / 255), (51.f / 255), 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);

            double currentTime = glfwGetTime();
            frameCount++;
            if ( currentTime - previousTime >= 1.0 )
            {
                glfwSetWindowTitle(window.getID(), "OpenGL Game. Original - FPS[" + frameCount + "]");
                frameCount = 0;
                previousTime = currentTime;
            }

            float timeValue = (float)glfwGetTime();

            GL30.glBindVertexArray(VAO);
            GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER,VBO);
            GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, EBO);

            shader.use();

            // View Transformations.
            view.identity();
            view.scale(0.4f);

            shader.addUniform1f("time", timeValue);
            shader.addUniformMatrix4fv("view", view);
            shader.addUniform1f("atlasTexture", atlasTexture);

            GL30.glActiveTexture(GL30.GL_TEXTURE0);
            GL30.glBindTexture(GL30.GL_TEXTURE_2D, atlasTexture);

            tileModel.identity();
            tileModel.scale(0.6f);

            shader.addUniformMatrix4fv("model", tileModel);
            render(tiles.size());

            glfwPollEvents();
            glfwSwapBuffers(window.getID());
        }

        destroy(VAO, VBO, EBO);
        GL30.glDeleteTextures(atlasTexture);
        GL.createCapabilities();
        glfwSwapInterval(1);

        glfwDestroyWindow(window.getID());
        glfwTerminate();

    }

    public static void render(int qntOfTiles){

        //GL11.glDrawElements(GL11.GL_TRIANGLES, Primitives.squareIndices.length * 2, GL11.GL_UNSIGNED_INT, 0);
        GL11.glDrawElements(GL11.GL_TRIANGLES, Primitives.squareIndices.length * qntOfTiles, GL11.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
    }


    public static void createBuffers(int VAO, int VBO, int EBO, List<Tile> tiles){

        GL30.glBindVertexArray(VAO);

        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(tiles.size() * Primitives.squareVertices.length * 8);
        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(tiles.size() * Primitives.squareIndices.length);

        int vertexOffset = 0;
        for (Tile tile : tiles) {

            System.out.println("TilePosX: " + tile.getPosition().x);
            int index = 0;
            Vector2f[] textCoord = tile.calculateTextureCoordinates(4, 4, 32, 32, 128, 128);
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
    }

    static void destroy(int VAO, int VBO, int EBO){
        GL30.glDeleteBuffers(VBO);
        GL30.glDeleteBuffers(EBO);
        GL30.glDeleteVertexArrays(VAO);

        //GL30.glDeleteTextures(allTextures);
    }
}
