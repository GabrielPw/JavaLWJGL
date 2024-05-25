package main.game;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import java.nio.FloatBuffer;

public class Mesh {

    private int VBO, VAO, EBO;
    private Vector3f[] vertices;
    private int[] indices;
    private Matrix4f transform = new Matrix4f();
    private Vector2f[] translations;
    private Vector3f scale, position;

    Mesh(Vector3f[] vertices, int[] indices){

        this.vertices = vertices;
        this.indices = indices;

        this.scale    = new Vector3f(1.f);
        this.position = new Vector3f(0.f);
        setupMesh();
    }


    private void setupMesh(){

        // Creating Buffers.
        this.VAO = GL30.glGenVertexArrays();
        this.VBO = GL30.glGenBuffers();
        this.EBO = GL30.glGenBuffers();

        GL30.glBindVertexArray(VAO);

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length * 3); // Cada vetor tem 3 componentes (x, y, z)
        for (Vector3f vertex : vertices) {
            vertexBuffer.put(vertex.x);
            vertexBuffer.put(vertex.y);
            vertexBuffer.put(vertex.z);
        }

        vertexBuffer.flip();

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, VBO);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertexBuffer, GL30.GL_STATIC_DRAW);

        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, EBO);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indices, GL30.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);
    }

    void render(int shaderProgram){

        GL30.glBindVertexArray(VAO);

        int transformUniform = GL20.glGetUniformLocation(shaderProgram, "transform");
        FloatBuffer transformBuffer = BufferUtils.createFloatBuffer(16);
        transform.get(transformBuffer);

        // Passando variaveis para os uniforms do shader.
        GL20.glUniformMatrix4fv(transformUniform, false, transformBuffer);

        GL11.glDrawElements(GL11.GL_TRIANGLES, indices.length, GL11.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
    }

    public void renderInstanced(int shaderProgram, int instanceCount) {

        GL30.glBindVertexArray(VAO);

        int offsetsUniform = GL20.glGetUniformLocation(shaderProgram, "offsets");
        int transformUniform = GL20.glGetUniformLocation(shaderProgram, "transform");

        setupTranslationForInstanced(instanceCount);

        FloatBuffer offsetsBuffer = BufferUtils.createFloatBuffer(instanceCount * 2); // Cada Vector2f tem dois componentes (x e y)
        for (Vector2f translation : translations) {
            offsetsBuffer.put(translation.x);
            offsetsBuffer.put(translation.y);
        }
        offsetsBuffer.flip();

        FloatBuffer transformBuffer = BufferUtils.createFloatBuffer(16);
        transform.get(transformBuffer);

        // Passando variaveis para os uniforms do shader.
        GL20.glUniform2fv(offsetsUniform, offsetsBuffer);
        GL20.glUniformMatrix4fv(transformUniform, false, transformBuffer);

        GL31.glDrawElementsInstanced(GL11.GL_TRIANGLES, indices.length, GL11.GL_UNSIGNED_INT, 0, instanceCount);
        GL30.glBindVertexArray(0);
    }

    void update(){

        transform.identity();

        this.position.x += 0.02f;

        scale(this.scale);
        move(this.position);

    }

    private void setupTranslationForInstanced(int instancesCount){

        this.translations = new Vector2f[instancesCount];
        float xGap = 1.5f; // considerar largura da figura ao configurar gap, para evitar sobreposições.
        for (int i = 0; i < translations.length; i++) {
            float xOffset = i * xGap;
            translations[i] = new Vector2f(xOffset, 0.0f);
        }
    }

    public int getVBO() {
        return VBO;
    }

    public int getVAO() {
        return VAO;
    }

    public int getEBO() {
        return EBO;
    }

    public Matrix4f getTransform() {
        return transform;
    }

    private void scale(Vector3f scaleFactor){

        transform.scale(scaleFactor);
    }

    private void move(Vector3f position){

        transform.translate(position);
    }

    public void setScale(Vector3f scale) {

        this.scale = scale;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }
}