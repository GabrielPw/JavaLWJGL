package main.editor;

import imgui.ImGui;
import imgui.ImGuiIO;
import main.game.Shader;
import main.game.Window;
import main.game.graphics.Primitives;
import main.game.graphics.TextureLoader;
import main.game.graphics.TexturePaths;
import main.game.graphics.Vertex;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;

public class MapEditor {

    private int VBO, VAO, EBO;
    private int VBOSelectedTile;

    private Shader shader;
    private final int MAP_MAXROWS = 40; // quantidade máxima de tiles que o mapa pode ter em linhas.
    private final int MAP_MAXCOLS = 40;
    private Window window;
    private ImGuiLayer imGuiLayer;
    private int textureAtlas;

    private double previousTime;
    private double frameTimeAccumulator;
    private int frameCount;
    private Grid grid;
    private Matrix4f projection;
    private Matrix4f view;
    private Matrix4f selectedTileModel;
    Vector3f viewPos = new Vector3f(0.f, 0.f, 0.f);
    float viewSpeed = .2f;
    private Vector3f viewScale = new Vector3f(0.1f, 0.1f, 0.f);

    private Vector3f selectedTilePos = new Vector3f(0.5f, 0.5f, 0);
    private Vector2f selectedTileIndex = new Vector2f(0, 0);
    private Matrix4f selectedTileView = new Matrix4f();

    private DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
    private DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
    Vector3f TILE_SIZE = new Vector3f(1.f, 1.f, 0.f);
    public MapEditor(String atlasPath){

        view              = new Matrix4f().identity();
        projection        = new Matrix4f().identity();
        selectedTileModel = new Matrix4f().identity();

        window = new Window("Editor", 640, 480, projection);

        textureAtlas = TextureLoader.loadTexture(TexturePaths.textureAtlas16v1);

        imGuiLayer = new ImGuiLayer(atlasPath);
        imGuiLayer.init(window.getID()); // Inicializar ImGui

        this.VAO             = GL30.glGenVertexArrays();
        this.VBO             = GL30.glGenBuffers();
        this.EBO             = GL30.glGenBuffers();
        this.VBOSelectedTile = GL30.glGenBuffers();

        this.shader = new Shader("editor/grid_vertex_shader.glsl", "editor/grid_fragment_shader.glsl");
        this.grid = new Grid(MAP_MAXROWS, MAP_MAXCOLS, TILE_SIZE.x);

        //window.setZoom(1.f);
        //window.updateProjectionMatrix();

        glfwSetFramebufferSizeCallback(window.getID(), (windowID, w, h) -> {
            glViewport(0, 0, w, h);
            window.setWidth(w);
            window.setHeight(h);
            window.updateProjectionMatrix();
        });



        /*
        glfwSetCursorPosCallback(window.getID(), (windowID, xpos, ypos) -> {
            cursorPosCallback(windowID, xpos, ypos);
        });
        */

        createBuffers();
    }

    public void run(){

        StringBuilder newTitle = new StringBuilder();
        while (!glfwWindowShouldClose(window.getID())){

            GL11.glClearColor((26.f / 255), (26.f / 255), (26.f / 255), 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);

            double currentTime = glfwGetTime();
            float deltaTime = (float) (currentTime - previousTime); // Calcular deltaTime
            previousTime = currentTime; // Atualizar previousTime

            frameTimeAccumulator += deltaTime; // Acumular o tempo decorrido
            frameCount++;

            if (frameTimeAccumulator >= 1.0) { // Se passou um segundo

                frameCount = 0; // Resetar contagem de frames
                frameTimeAccumulator = 0.0; // Resetar o acumulador
            }

            window.updateProjectionMatrix();
            moveCamera();

            view.identity();
            view.translate(viewPos);
            view.scale(viewScale);

            grid.update(view);
            grid.render();

            //System.out.println(ImGui.isItemHovered()? "Sim" : "Não");
            if (imGuiLayer.isTileBeingSelected() && !ImGui.isItemHovered()){

                int tileCol = imGuiLayer.getSelectedTileCoordInAtlas().x;
                int tileRow = imGuiLayer.getSelectedTileCoordInAtlas().y;

                renderSelected(tileCol, tileRow);
            }

            imGuiLayer.render(deltaTime);

            glfwPollEvents();
            glfwSwapBuffers(window.getID());
        }

        destroy();

        GL.createCapabilities();
        glfwSwapInterval(1);
        glfwDestroyWindow(window.getID());
        glfwTerminate();
    }

    private void createBuffers(){

        GL30.glBindVertexArray(VAO);

        FloatBuffer selectedTileVertices = BufferUtils.createFloatBuffer(Primitives.squareVertices.length * 4);

        for (Vertex vertex : Primitives.squareVertices) {

            selectedTileVertices.put(vertex.position.x);
            selectedTileVertices.put(vertex.position.y);
            selectedTileVertices.put(vertex.getTextureCoordAsFloatArray());
        }

        selectedTileVertices.flip();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, VBOSelectedTile);
        GL30.glBufferData(GL15.GL_ARRAY_BUFFER, selectedTileVertices, GL15.GL_STATIC_DRAW);

        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, EBO);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, Primitives.squareIndices, GL30.GL_STATIC_DRAW);

        GL30.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 4 * Float.BYTES, 0);
        GL30.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);

        GL30.glEnableVertexAttribArray(0);
        GL30.glEnableVertexAttribArray(1);

        selectedTileVertices.clear();
    }

    private void renderSelected(int colInAtlas, int rowInAtlas){

        glfwGetCursorPos(window.getID(), xBuffer, yBuffer);
        double mouseX = xBuffer.get(0);
        double mouseY = yBuffer.get(0);
        int windowWidth = window.getWidth();
        int windowHeight = window.getHeight();

        xBuffer.clear();
        yBuffer.clear();

        // Converter a posição do mouse para coordenadas normalizadas OpenGL
        float mouseXNorm = ((float) mouseX - (float) windowWidth  / 2) / (windowWidth  / 2); // 0,0 da janela deslocar p/ 0,0 do openGL
        float mouseYNorm = ((float) mouseY - (float) windowHeight / 2) / (windowHeight / 2);

        float snappedX = (float) (((float) Math.floor(mouseXNorm / viewScale.x) * 0.1f) +  0.1 / 2);
        float snappedY = (float) (((float) Math.floor(mouseYNorm / viewScale.y) * -0.1f) - 0.1 / 2);

        selectedTileModel.identity();
        selectedTileView.identity();

        System.out.println(mouseXNorm + " - " + mouseYNorm);
        selectedTileModel.translate(snappedX , snappedY, 0.f);
        selectedTileView.scale(viewScale);

        selectedTileIndex.x = colInAtlas;
        selectedTileIndex.y = rowInAtlas;
        shader.use();
        shader.addUniformMatrix4fv("model", selectedTileModel);
        shader.addUniformMatrix4fv("view", selectedTileView);
        shader.addUniform1i("hasTexture", 1);
        shader.addUniform1f("textureAtlas", textureAtlas);
        shader.addUniform2fv("indexInAtlas", selectedTileIndex);

        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, textureAtlas);

        GL30.glBindVertexArray(VAO);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER,VBOSelectedTile);
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, EBO);

        GL11.glDrawElements(GL11.GL_TRIANGLES, Primitives.squareIndices.length, GL11.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
    }

    public void moveCamera(){

        if (window.isKeyPressed(GLFW_KEY_D)){
            System.out.println("X: " + viewPos.x);
            float margin = 0.3f;
            viewPos.x -= viewSpeed;
            if ( viewPos.x <= -(MAP_MAXCOLS * 0.1f)){
                viewPos.x = -(MAP_MAXCOLS * 0.1f);
            }
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            viewPos.x += viewSpeed;
            if ( viewPos.x >= 0.4f){
                viewPos.x = 0.4f;
            }
        }
        if (window.isKeyPressed(GLFW_KEY_W)){

            viewPos.y -= viewSpeed;
            if ( viewPos.y <= -0.4f){
                viewPos.y = -0.4f;
            }
        }
        if(window.isKeyPressed(GLFW_KEY_S)) {
            float margin = .4f;
            viewPos.y += viewSpeed;
            if ( viewPos.y >= MAP_MAXROWS * 0.1f + margin){
                viewPos.y = MAP_MAXROWS * 0.1f + margin;
            }
        }
    }
    private void cursorPosCallback(long windowID, double xpos, double ypos) {
        // Verifique se o ImGui está capturando o cursor
        if (!ImGui.getIO().getWantCaptureMouse()) {
            // Se o ImGui não estiver capturando o cursor, focar a janela principal
            glfwFocusWindow(windowID);
        }
    }


    private void destroy(){
        GL30.glDeleteBuffers(this.VBO);
        GL30.glDeleteBuffers(this.EBO);
        GL30.glDeleteVertexArrays(this.VAO);

        GL30.glDeleteTextures(this.textureAtlas);
    }
}
