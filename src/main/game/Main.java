package main.game;

import main.game.graphics.Primitives;
import main.game.graphics.map.GameMap;
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
import static org.lwjgl.opengl.GL11.glViewport;

public class Main {

    public static void main(String[] args) {

        Window window = new Window("Testando texturas", 640, 480);
        double previousTime = glfwGetTime();
        int frameCount = 0;

        Matrix4f projection = new Matrix4f().ortho2D(0, window.getWidth(), 0, window.getHeight()).identity();

        glfwSetFramebufferSizeCallback(window.getID(), (windowID, w, h) -> {
            glViewport(0, 0, w, h);
            window.updateProjectionMatrix(projection, w, h);
        });

        GL30.glFrontFace( GL30.GL_CCW );
        GL30.glCullFace(GL30.GL_BACK);
        GL30.glEnable(GL30.GL_CULL_FACE);

        Matrix4f view = new Matrix4f().identity();

        Player player = new Player(new Vector2f(0.f, 0.f), TexturePaths.textureMokkoCharacter, new Shader("player_vertex_shader.glsl", "player_fragment_shader.glsl"));
        GameMap gameMap = new GameMap(TexturePaths.textureAtlas16v1, new Shader("vertex_shader.glsl", "fragment_shader.glsl"));

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

            float cameraTranslationX = (float) Math.cos(timeValue * 0.3f);

            view.identity();
            view.translate(new Vector3f(-.8f, 0.9f, 0.f));
            view.translate(new Vector3f(cameraTranslationX, 0.f, 0.f));

            gameMap.setScale(.13f);
            gameMap.update(projection, view, timeValue);
            gameMap.render();

            player.setScale(.13f);
            player.update(projection, view, timeValue);
            player.render();

            glfwPollEvents();
            glfwSwapBuffers(window.getID());
        }

        gameMap.destroy();

        GL.createCapabilities();
        glfwSwapInterval(1);
        glfwDestroyWindow(window.getID());
        glfwTerminate();

    }
}
