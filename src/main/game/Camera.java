package main.game;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera {
    private Vector2f position;
    private Matrix4f viewMatrix;

    public Camera(Vector2f position) {
        this.position = new Vector2f(position);
        this.viewMatrix = new Matrix4f();
        updateViewMatrix();
    }

    public void setPosition(Vector2f position) {
        this.position.set(position);
        updateViewMatrix();
    }

    private void updateViewMatrix() {
        viewMatrix.identity();
        viewMatrix.translate(new Vector3f(-position.x, -position.y, 0.0f));
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }
}