package main.game;

import main.game.graphics.TexturePaths;
import main.game.graphics.map.GameMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;

public class Game {

    Window window;
    Player player;
    GameMap gameMap;
    Camera camera;
    double previousTime;
    double frameTimeAccumulator;
    int frameCount;

    public Game(String windowTile, int winWidth, int winHeight){
        Matrix4f projection = new Matrix4f();

        window = new Window(windowTile, winWidth,winHeight, projection);
        previousTime = glfwGetTime();
        frameTimeAccumulator = 0.0; // Acumulador para o tempo decorrido
        frameCount = 0;

        window.setZoom(2.f);
        window.updateProjectionMatrix();

        glfwSetFramebufferSizeCallback(window.getID(), (windowID, w, h) -> {
            glViewport(0, 0, w, h);
            window.setWidth(w);
            window.setHeight(h);
            window.updateProjectionMatrix();
        });

        GL30.glFrontFace( GL30.GL_CCW );
        GL30.glCullFace(GL30.GL_BACK);
        GL30.glEnable(GL30.GL_CULL_FACE);

        player = new Player(new Vector2f(0.f, 0.f), TexturePaths.textureAtlasMokkoCharacter, new Shader("player_vertex_shader.glsl", "player_fragment_shader.glsl"));
        gameMap = new GameMap(TexturePaths.textureAtlas16v1, new Shader("vertex_shader.glsl", "fragment_shader.glsl"));
        camera = new Camera(new Vector2f(player.getPosition()));
    }

    void run(){
        while (!glfwWindowShouldClose(window.getID())) {
            GL11.glClearColor((20.f / 255), (40.f / 255), (51.f / 255), 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);

            double currentTime = glfwGetTime();
            float deltaTime = (float) (currentTime - previousTime); // Calcular deltaTime
            previousTime = currentTime; // Atualizar previousTime

            frameTimeAccumulator += deltaTime; // Acumular o tempo decorrido
            frameCount++;

            if (frameTimeAccumulator >= 1.0) { // Se passou um segundo
                glfwSetWindowTitle(window.getID(), "OpenGL Game. FPS[" + frameCount + "]");
                frameCount = 0; // Resetar contagem de frames
                frameTimeAccumulator = 0.0; // Resetar o acumulador
            }

            window.updateProjectionMatrix();
            camera.setPosition(player.getPosition());
            Matrix4f viewMatrix = camera.getViewMatrix();

            gameMap.update(window.getProjection(), viewMatrix, frameCount);
            gameMap.render();

            player.setScale(0.80f); // Definindo a escala do jogador
            player.update(window.getProjection(), viewMatrix, deltaTime, window);
            player.render();

            if(window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)){
                System.out.println("Pressionou!!!");
                window.setZoom(window.getZoom() + 0.1f);
                window.updateProjectionMatrix();
            } else if (window.isKeyPressed(GLFW_KEY_LEFT_CONTROL)){
                window.setZoom(window.getZoom() - 0.1f);
                window.updateProjectionMatrix();
            }

            //System.out.println("Player pos: " + player.getPosition().x + " | " + player.getPosition().y);

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
