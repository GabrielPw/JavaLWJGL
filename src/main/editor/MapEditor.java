package main.editor;

import imgui.ImGui;
import imgui.ImGuiIO;
import main.game.Shader;
import main.game.Window;
import main.game.graphics.Primitives;
import main.game.graphics.TextureLoader;
import main.game.graphics.TexturePaths;
import main.game.graphics.Vertex;
import main.game.graphics.map.Tile;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import javax.print.attribute.standard.PrinterMessageFromOperator;
import java.lang.Math;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;

public class MapEditor {

    private int VAO;
    VBO VBOMap, VBOSelectedTile;
    EBO EBOMap, EBOTileSelected;
    private Shader shaderTileSelectedAndGrid;
    private Shader shaderMap;
    private final int QNT_ATLAS_COLS = 20;
    private final int QNT_ATLAS_ROWS = 20;

    private final int MAP_MAXROWS = 40; // quantidade máxima de tiles que o mapa pode ter em linhas.
    private final int MAP_MAXCOLS = 40;
    private Window window;
    private ImGuiLayer imGuiLayer;
    private int textureAtlas;
    private Grid grid;
    private Matrix4f projection;
    private Matrix4f view;
    private Matrix4f selectedTileModel;
    Vector3f viewPos = new Vector3f(0.f, 0.f, 0.f);
    float viewSpeed = .2f;
    private Vector3f viewScale = new Vector3f(0.1f, 0.1f, 0.f);
    private Vector2f selectedTileIndex = new Vector2f(0, 0);
    private Matrix4f selectedTileView = new Matrix4f();
    private DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
    private DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
    Vector3f TILE_SIZE = new Vector3f(1.f, 1.f, 0.f);
    public MapEditor(String atlasPath){

        Vector3f cameraPos = new Vector3f(0.0f, 0.0f, 2.0f); // Posição da câmera
        Vector3f cameraTarget = new Vector3f(0.0f, 0.0f, 0.0f); // Ponto de olhar
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f); // Vetor para cima

        view = new Matrix4f().lookAt(cameraPos, cameraTarget, up);
        projection        = new Matrix4f().identity();
        selectedTileModel = new Matrix4f().identity();

        window = new Window("Editor", 640, 480, projection);

        textureAtlas = TextureLoader.loadTexture(TexturePaths.textureAtlas16v1);

        imGuiLayer = new ImGuiLayer(atlasPath);
        imGuiLayer.init(window.getID()); // Inicializar ImGui

        this.VAO             = GL30.glGenVertexArrays();

        this.shaderTileSelectedAndGrid = new Shader("editor/grid_vert.glsl", "editor/grid_frag.glsl");
        this.shaderMap                 = new Shader("editor/map_vert.glsl",  "editor/map_frag.glsl");
        this.grid = new Grid(MAP_MAXROWS, MAP_MAXCOLS, TILE_SIZE.x);

        window.updateProjectionMatrix();

        glfwSetFramebufferSizeCallback(window.getID(), (windowID, w, h) -> {
            glViewport(0, 0, w, h);
            window.setWidth(w);
            window.setHeight(h);
            window.updateProjectionMatrix();
        });

        glfwSetMouseButtonCallback(window.getID(), (windowID, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS && imGuiLayer.isTileBeingSelected()) {
                glfwGetCursorPos(window.getID(), xBuffer, yBuffer);

                double mouseX = xBuffer.get(0);
                double mouseY = yBuffer.get(0);

                float mouseXNorm = (float) (mouseX / window.getWidth() - 0.5f) * 2;
                float mouseYNorm = (float) -(mouseY / window.getHeight() - 0.5f) * 2;

                addTiletoMap(new Vector2f(mouseXNorm, mouseYNorm));
            }
        });

        createBuffers();
    }

    public void run(){
        while (!glfwWindowShouldClose(window.getID())){

            GL11.glClearColor((26.f / 255), (26.f / 255), (26.f / 255), 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);

            window.updateProjectionMatrix();
            moveCamera();

            view.identity();
            view.translate(viewPos);
            view.scale(viewScale);

            grid.update(view);
            grid.render();

            renderMap();

            //System.out.println(ImGui.isItemHovered()? "Sim" : "Não");
            if (imGuiLayer.isTileBeingSelected() && !ImGui.isItemHovered()){

                int tileCol = imGuiLayer.getSelectedTileCoordInAtlas().x;
                int tileRow = imGuiLayer.getSelectedTileCoordInAtlas().y;

                renderSelected(tileCol, tileRow);
            }

            imGuiLayer.render();

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

        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(Primitives.squareVertices.length * 4 * MAP_MAXROWS * MAP_MAXCOLS);
        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(Primitives.squareIndices.length * MAP_MAXROWS * MAP_MAXCOLS);

        this.VBOMap = new VBO(verticesBuffer.capacity() * Float.BYTES, GL30.GL_DYNAMIC_DRAW);
        this.EBOMap = new EBO(indicesBuffer.capacity() * Integer.BYTES, GL30.GL_DYNAMIC_DRAW);

        // VBO TileSelected.
        FloatBuffer selectedTileVertices = BufferUtils.createFloatBuffer(Primitives.squareVertices.length * 4);
        for (Vertex vertex : Primitives.squareVertices) {

            selectedTileVertices.put(vertex.position.x);
            selectedTileVertices.put(vertex.position.y);
            selectedTileVertices.put(vertex.getTextureCoordAsFloatArray());
        }

        selectedTileVertices.flip();

        this.VBOSelectedTile = new VBO(selectedTileVertices, GL15.GL_STATIC_DRAW);
        this.EBOTileSelected = new EBO(Primitives.squareIndices, GL30.GL_STATIC_DRAW);

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

        //System.out.println(mouseXNorm + " - " + mouseYNorm);
        selectedTileModel.translate(snappedX , snappedY, 0.f);
        selectedTileView.scale(viewScale);

        selectedTileIndex.x = colInAtlas;
        selectedTileIndex.y = rowInAtlas;
        shaderTileSelectedAndGrid.use();
        shaderTileSelectedAndGrid.addUniformMatrix4fv("model", selectedTileModel);
        shaderTileSelectedAndGrid.addUniformMatrix4fv("view", selectedTileView);
        shaderTileSelectedAndGrid.addUniform1i("hasTexture", 1);
        shaderTileSelectedAndGrid.addUniform1f("textureAtlas", textureAtlas);
        shaderTileSelectedAndGrid.addUniform2fv("indexInAtlas", selectedTileIndex);

        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, textureAtlas);

        GL30.glBindVertexArray(VAO);
        VBOSelectedTile.bind();
        EBOTileSelected.bind();

        GL11.glDrawElements(GL11.GL_TRIANGLES, Primitives.squareIndices.length, GL11.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
    }

    private void renderMap(){

        GL30.glBindVertexArray(VAO);

        view.identity();
        shaderMap.use();
        //shaderMap.addUniformMatrix4fv("model",new Matrix4f().identity());
        shaderMap.addUniformMatrix4fv("view", view);
        shaderMap.addUniform1f("textureAtlas", textureAtlas);

        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, textureAtlas);

        GL30.glBindVertexArray(VAO);
        this.VBOMap.bind();
        this.EBOMap.bind();

        GL11.glDrawElements(GL11.GL_TRIANGLES, MAP_MAXROWS * MAP_MAXCOLS * Primitives.squareIndices.length, GL11.GL_UNSIGNED_INT, 0);

    }
    private void addTiletoMap(Vector2f cellPosition ){

        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(Primitives.squareVertices.length * 4);
        for (Vertex vertice : Primitives.squareVertices) {
            verticesBuffer.put(cellPosition.x);
            verticesBuffer.put(cellPosition.y);
            verticesBuffer.put(vertice.textureCoord.x);
            verticesBuffer.put(vertice.textureCoord.y);
        }
        verticesBuffer.flip();

        int tileIndex = 0;
        VBOMap.bind();
        VBOMap.subData(tileIndex * Primitives.squareVertices.length * 4 * Float.BYTES, verticesBuffer);

        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(Primitives.squareIndices.length);
        int offset = tileIndex * Primitives.squareVertices.length;
        for (int index : Primitives.squareIndices) {
            indicesBuffer.put(index + offset);
        }
        indicesBuffer.flip();

        EBOMap.bind();
        EBOMap.subData(tileIndex * Primitives.squareIndices.length * Integer.BYTES ,indicesBuffer);
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


    private void destroy(){
        GL30.glDeleteBuffers(this.VBOMap.getID());
        GL30.glDeleteBuffers(this.VBOSelectedTile.getID());
        GL30.glDeleteBuffers(this.EBOMap.getID());
        GL30.glDeleteBuffers(this.EBOTileSelected.getID());
        GL30.glDeleteVertexArrays(this.VAO);

        GL30.glDeleteTextures(this.textureAtlas);
    }
}
