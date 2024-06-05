package main.game;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private long ID;

    private int width;
    private int height;
    private float zoom;
    private static final float MIN_ZOOM = 0.1f;
    private static final float MAX_ZOOM = 5.0f;
    private float scrollSpeed = 0.1f;
    private Map<Integer, Boolean> keyState;
    Matrix4f projection;
    public Window(String title, int width, int height, Matrix4f projection){

        this.width  = width;
        this.height = height;

        glfwInit();
        glfwWindowHint(GLFW_SAMPLES, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);

        this.ID = glfwCreateWindow(width,height,title, NULL , NULL);
        this.projection = projection;

        if (this.ID == NULL)
        {
            System.err.println("Error creating a window");
            System.exit(1);
        }

        glfwMakeContextCurrent(this.ID);
        GL.createCapabilities();
        glDisable(GL_DEPTH_TEST); // ? for 2d or 3D?
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Configurar o callback de entrada
        keyState = new HashMap<>();
        org.lwjgl.glfw.GLFW.glfwSetKeyCallback(ID, (window, key, scancode, action, mods) -> {
            if (action == GLFW.GLFW_PRESS) {
                keyState.put(key, true);
            } else if (action == GLFW.GLFW_RELEASE) {
                keyState.put(key, false);
            }
        });

        setupScrollCallback(ID);

    }

    public void setupScrollCallback(long window) {
        glfwSetScrollCallback(window, (windowHandle, xOffset, yOffset) -> {
            float sensitivity = 0.2f;
            float newZoom = this.zoom + (float) yOffset * sensitivity;
            setZoom(newZoom);
        });
    }

    public boolean isKeyPressed(int key) {
        return keyState.getOrDefault(key, false);
    }


    public void updateProjectionMatrix() {
        float aspectRatio = (float) this.width / this.height;
        float orthoHeight = 1.0f * zoom;
        float orthoWidth = orthoHeight * aspectRatio;
        float centerX = orthoWidth / 2.0f;
        float centerY = orthoHeight / 2.0f;
        projection.identity();
        projection.ortho(-centerX, centerX, -centerY, centerY, -1.0f, 1.0f);
    }

    public void setZoom(float zoom) {
        this.zoom = Math.max(MIN_ZOOM, Math.min(zoom, MAX_ZOOM));
        updateProjectionMatrix();
    }

    public long getID() {
        return ID;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getZoom() {
        return zoom;
    }



    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
