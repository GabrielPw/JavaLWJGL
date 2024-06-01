package main.game;

import main.game.graphics.Primitives;
import main.game.graphics.TextureLoader;
import main.game.graphics.Vertex;
import main.game.graphics.map.Map;
import main.game.graphics.map.Tile;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.*;

import java.io.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

class Main{

    public static void main(String[] args) {

        glfwInit();
        glfwWindowHint(GLFW_SAMPLES, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);

        long windowID = glfwCreateWindow(640, 480, "My GLFW Window", NULL , NULL);

        if (windowID == NULL)
        {
            System.err.println("Error creating a window");
            System.exit(1);
        }

        glfwMakeContextCurrent(windowID);
        GL.createCapabilities();
        glDisable(GL_DEPTH_TEST); // ? for 2d or 3D?
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Set up a resize callback
        glfwSetFramebufferSizeCallback(windowID, (window, width, height) -> {
            glViewport(0, 0, width, height);
        });

        // Shader
        Shader shaderProgram = new Shader("vertex_shader.glsl", "fragment_shader.glsl");

        // Texture.
        String textureGrass  = "src/main/resources/assets/grassblock.jpg";
        String textureWooden = "src/main/resources/assets/wooden2.jpg";
        String textureTileBlue  = "src/main/resources/assets/tile32/tileblue.png";
        String textureTileGreen = "src/main/resources/assets/tile32/tilegreen.png";


        Tile[][] worldTiles = new Tile[][]{
            {new Tile(new Vector3f(0.f,0.f, 0.f), textureTileBlue), new Tile(new Vector3f(1.f, 0.f, 0.f), textureTileGreen)},
        };

        Map gameMap =  new Map(worldTiles, "map_vertex_shader.glsl", "map_fragment_shader.glsl");

        double previousTime = glfwGetTime();
        int frameCount = 0;

        while (!glfwWindowShouldClose(windowID)) {

            GL11.glClearColor((80.f / 255), (100.f / 255), (121.f / 255), 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);

            // FPS
            double currentTime = glfwGetTime();
            frameCount++;
            // If a second has passed.
            if ( currentTime - previousTime >= 1.0 )
            {
                // Display the frame count here any way you want.
                glfwSetWindowTitle(windowID, "OpenGL Game. FPS[" + frameCount + "]");

                frameCount = 0;
                previousTime = currentTime;
            }

            float timeValue = (float)glfwGetTime();
            GL20.glUseProgram(shaderProgram.ID);

            gameMap.render(timeValue);

            // ending loop.
            GL30.glBindVertexArray(0);
            GL20.glUseProgram(0);

            glfwPollEvents();
            glfwSwapBuffers(windowID);
        }

        gameMap.destroy();

        GL.createCapabilities();
        glfwSwapInterval(1);

        glfwDestroyWindow(windowID);
        glfwTerminate();
    }
}