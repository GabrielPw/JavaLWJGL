package main.game;

import main.game.graphics.Primitives;
import main.game.graphics.TextureLoader;
import main.game.graphics.Vertex;
import main.game.graphics.map.Tile;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Player {

    private int VAO, VBO, EBO;
    private int texture;
    private Vector2f position;
    private Matrix4f model;
    private float scale;
    private Shader shader;

    public Player(Vector2f position, String texturePath, Shader shader){

        this.VAO = GL30.glGenVertexArrays();
        this.VBO = GL30.glGenBuffers();
        this.EBO = GL30.glGenBuffers();

        this.position = position;
        this.shader = shader;
        this.model = new Matrix4f().identity();
        this.scale = 1.f;

        this.texture = TextureLoader.loadTexture(texturePath, GL30.GL_TEXTURE0);
        createBuffers();
    }
    public void render(){
        GL30.glBindVertexArray(VAO);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER,VBO);
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, EBO);

        GL11.glDrawElements(GL11.GL_TRIANGLES, Primitives.squareIndices.length, GL11.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
    }

    public void update(Matrix4f projection, Matrix4f view, float deltaTime){

        GL30.glBindVertexArray(VAO);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER,VBO);
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, EBO);

        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, texture);

        shader.use();

        model.identity();
        shader.addUniform1f("time", deltaTime);
        shader.addUniformMatrix4fv("projection", projection);
        shader.addUniformMatrix4fv("view", view);
        shader.addUniform1f("texture", texture);

        model.identity();
        model.scale(this.scale);

        shader.addUniformMatrix4fv("model", model);
    }

    void createBuffers(){

        /*
        * Se adicionar mais objetos ao VBO,
        * Lembrar de ajustar/adicionar os indices correspondentes
        * */

        GL30.glBindVertexArray(VAO);

        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer( Primitives.squareVertices.length * 4);

        for (Vertex vertex : Primitives.squareVertices) {
            verticesBuffer.put(vertex.position.x);
            verticesBuffer.put(vertex.position.y);
            verticesBuffer.put(vertex.textureCoord.x);
            verticesBuffer.put(vertex.textureCoord.y);
        }

        verticesBuffer.flip();

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER,VBO);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, verticesBuffer, GL30.GL_STATIC_DRAW);

        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, EBO);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, Primitives.squareIndices, GL30.GL_STATIC_DRAW);

        GL30.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 4 * Float.BYTES, 0);
        GL30.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES); // Texture coordinates

        GL30.glEnableVertexAttribArray(0);
        GL30.glEnableVertexAttribArray(1);

        verticesBuffer.clear();
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
}
