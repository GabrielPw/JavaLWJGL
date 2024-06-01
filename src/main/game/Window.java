package main.game;

import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private long ID;
    public Window(String title, int width, int height){

        glfwInit();
        glfwWindowHint(GLFW_SAMPLES, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);

        this.ID = glfwCreateWindow(width,height,title, NULL , NULL);

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

        // Set up a resize callback
        glfwSetFramebufferSizeCallback(this.ID, (window, width_, height_) -> {
            glViewport(0, 0, width_, height_);
        });
    }

    public long getID() {
        return ID;
    }
}
