package main.game;

import main.game.graphics.*;
import main.game.graphics.map.Tile;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;

public class Player {

    private int VAO, VBO, EBO;
    private int texture;
    private Vector2f position;
    private float speed;
    private Matrix4f model;
    private Vector2f scale;
    private Shader shader;
    private AnimationManager animationManager;
    private boolean isMoving;

    public Player(Vector2f position, String texturePath, Shader shader){

        this.VAO = GL30.glGenVertexArrays();
        this.VBO = GL30.glGenBuffers();
        this.EBO = GL30.glGenBuffers();

        this.position = position;
        this.shader = shader;
        this.model = new Matrix4f().identity();
        this.animationManager = new AnimationManager(8, ActualAnimation.WALKING_DOWN_REDUCED, 16, 12);
        this.speed = 2.f;
        this.scale = new Vector2f(1.f, 1.f);
        this.isMoving = false;

        this.texture = TextureLoader.loadTexture(texturePath);
        createBuffers();
    }
    public void render(){
        GL30.glBindVertexArray(VAO);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER,VBO);
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, EBO);

        GL11.glDrawElements(GL11.GL_TRIANGLES, Primitives.squareIndices.length, GL11.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
    }

    public void update(Matrix4f projection, Matrix4f view, float deltaTime, Window window){

        GL30.glBindVertexArray(VAO);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER,VBO);
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, EBO);

        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, texture);

        isMoving = false;
        move(window, deltaTime);
        if (isMoving) {
            animationManager.setQuantityFrames(8);
            System.out.println("IsMoving");
        }else {
            animationManager.setQuantityFrames(1);
        }
        animationManager.play(deltaTime);
        shader.use();

        model.identity();
        shader.addUniform1f("time", deltaTime);
        shader.addUniformMatrix4fv("projection", projection);
        shader.addUniformMatrix4fv("view", view);
        shader.addUniform1f("texture", texture);
        shader.addUniform2fv("actualSpriteOffset", new Vector2f(animationManager.getActualFrame(), animationManager.getActualAnimation()));

        model.identity();
        model.translate(new Vector3f(this.position, 0.f));
        model.scale(this.scale.x, this.scale.y, 1.f);

        shader.addUniformMatrix4fv("model", model);

    }

    private void move(Window window, float deltaTime){
        Vector2f direction = new Vector2f(0, 0);

        //System.out.println("DeltaTime: " + deltaTime);
        if (window.isKeyPressed(GLFW.GLFW_KEY_W)) {
            isMoving = true;
            animationManager.setActualAnimation(ActualAnimation.WALKING_UP, 8);
            direction.y += 1;
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_S)) {
            isMoving = true;
            animationManager.setFramesPerSecond(12);
            animationManager.setActualAnimation(ActualAnimation.WALKING_DOWN_REDUCED, 8);
            direction.y -= 1;
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_A)) {
            isMoving = true;
            animationManager.setFramesPerSecond(12);
            animationManager.setActualAnimation(ActualAnimation.WALKING_RIGHT, 9);
            direction.x -= 1;
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_D)) {
            isMoving = true;
            animationManager.setFramesPerSecond(8);
            animationManager.setActualAnimation(ActualAnimation.WALKING_LEFT, 9);
            direction.x += 1;
        }

        if (direction.length() > 0) {
            direction.normalize();
            position.add(direction.mul(speed * deltaTime));
        }
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
        this.scale = new Vector2f(scale);
    }

    public Vector2f getPosition() {
        return position;
    }

    public float getSpeed() {
        return speed;
    }
}
