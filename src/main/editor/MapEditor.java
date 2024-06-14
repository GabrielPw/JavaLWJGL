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
import org.lwjgl.opengl.*;

import javax.print.attribute.standard.PrinterMessageFromOperator;
import java.io.*;
import java.lang.Math;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;

public class MapEditor {

    private DoubleBuffer mouseXBuffer, mouseYBuffer;

    private int VAO;
    private VBO VBOMap;
    private EBO EBOMap;
    private Matrix4f projection, view;
    private Window window;
    private ImGuiLayer imGuiLayer;
    private Grid grid;
    private Shader mapShader;

    private float winMaxZoom = 20.f;
    private Vector3f viewPos = new Vector3f(0.f,0.f, 0.f);
    private float viewScale = 1.f;

    HashMap<Vector2i, Integer> tileMap = new HashMap<>();

    public MapEditor(String atlasPath, int atlasSize, int tileSize){
        DoubleBuffer mouseXBuffer = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer mouseYBuffer = BufferUtils.createDoubleBuffer(1);

        projection  = new Matrix4f().identity();
        view        = new Matrix4f().identity();
        window      = new Window("Editor.", 640, 480, projection);
        imGuiLayer  = new ImGuiLayer(atlasPath, atlasSize, tileSize);
        grid        = new Grid(40, 40);
        mapShader = new Shader("editor/tile_vert.glsl", "editor/tile_frag.glsl");
        TextureAtlas textureAtlas = new TextureAtlas(atlasSize, atlasSize, tileSize, tileSize);

        glfwSetFramebufferSizeCallback(window.getID(), (windowID, w, h) -> {
            glViewport(0, 0, w, h);
            window.setWidth(w);
            window.setHeight(h);
            window.updateProjectionMatrix();
        });
        glfwSetMouseButtonCallback(window.getID(), (windowID, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS && imGuiLayer.isTileBeingSelected()) {

                glfwGetCursorPos(window.getID(), mouseXBuffer, mouseYBuffer);

                double mouseX = mouseXBuffer.get(0);
                double mouseY = mouseYBuffer.get(0);

                // Normalizar as coordenadas do mouse para o intervalo [-1, 1]
                float x = (float) (2.0f * mouseX / window.getWidth() - 1.0f);
                float y = (float) (1.0f - 2.0f * mouseY / window.getHeight());

                // Criar um vetor com as coordenadas normalizadas
                Vector4f mouseClipCoords = new Vector4f(x, y, -1.0f, 1.0f);

                // Transformar as coordenadas do clip space para o eye space
                Matrix4f projection = window.getProjection();
                Vector4f mouseEyeCoords = projection.invert(new Matrix4f()).transform(mouseClipCoords);

                // Definir o z e w para o espaço de eye space
                mouseEyeCoords.z = -1.0f;
                mouseEyeCoords.w = 0.0f;

                // Inicializar a matriz de visualização
                Matrix4f view = new Matrix4f().identity().translate(viewPos).scale(viewScale);

                // Transformar as coordenadas do eye space para o world space
                Vector4f mouseWorldCoords = view.invert(new Matrix4f()).transform(mouseEyeCoords);

                // Considerar a posição da câmera ao pegar as coordenadas do mundo
                float worldX = mouseWorldCoords.x + Math.abs(viewPos.x);
                float worldY = mouseWorldCoords.y - viewPos.y;

                int snappedX = (int) Math.floor(worldX);
                int snappedY = (int) Math.floor(worldY + 1.f);

                System.out.println("X: " + worldX + " | Y: " + worldY);
                Vector2f tilePos = new Vector2f(snappedX, -snappedY);

                boolean isClickInsideGridBounds = snappedX >= 0 && snappedX < grid.getNUMCOLS();

                Vector2i tilePosInAtlas = imGuiLayer.getSelectedTileCoordInAtlas();
                int tileCol = tilePosInAtlas.x;
                int tileRow = tilePosInAtlas.y;
                int tilesPerRow = imGuiLayer.getAtlasSize() / imGuiLayer.getTileSize();

                int tileIndexInAtlas = tileRow * tilesPerRow + tileCol;
                if (isClickInsideGridBounds) {
                    System.out.println("Index: " + tileIndexInAtlas);
                    addTileToBuffer(tilePos, tileIndexInAtlas, textureAtlas);
                    tileMap.put(new Vector2i((int) tilePos.x, (int) tilePos.y), tileIndexInAtlas);
                }
            }

        });

        imGuiLayer.init(window.getID());

        VAO = GL30.glGenVertexArrays();
        window.setMaxZoom(winMaxZoom);
        window.setZoom(20.f);
        window.updateProjectionMatrix();
        createMapBuffers();
        loadMapFromTxt(MapPaths.map1, textureAtlas);
    }

    public void run(){
        while (!glfwWindowShouldClose(window.getID())){

            GL11.glClearColor((26.f / 255), (26.f / 255), (26.f / 255), 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);

            window.updateProjectionMatrix();

            view.identity();
            view.translate(viewPos);
            //view.scale(viewScale);

            moveCamera();

            grid.render(view, window.getProjection());
            renderMapTiles(window.getProjection());
            imGuiLayer.render();

            if (imGuiLayer.isSaveMapRequested()) {
                saveMapToTxt(MapPaths.map1);
                imGuiLayer.resetSaveMapRequested();
            }

            glfwPollEvents();
            glfwSwapBuffers(window.getID());
        }

        grid.destroy();
        GL30.glDeleteTextures(imGuiLayer.getAtlasTexture());
        destroy();

        GL.createCapabilities();
        glfwSwapInterval(1);
        glfwDestroyWindow(window.getID());
        glfwTerminate();
    }

    private void createMapBuffers(){

        GL30.glBindVertexArray(VAO);

        int grid_qnt_cols = grid.getNUMCOLS();
        int grid_qnt_rows = grid.getNUMROWS();
        int numTiles = grid_qnt_cols * grid_qnt_rows;

        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(Primitives.squareVertices.length * 4 * numTiles);
        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(Primitives.squareIndices.length * numTiles);

        VBOMap = new VBO(verticesBuffer.capacity() * Float.BYTES, GL30.GL_DYNAMIC_DRAW);
        EBOMap = new EBO(indicesBuffer.capacity() * Integer.BYTES, GL30.GL_DYNAMIC_DRAW);

        GL30.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 4 * Float.BYTES, 0);
        GL30.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES); // corrigido o offset para 2 * Float.BYTES
        GL30.glEnableVertexAttribArray(0);
        GL30.glEnableVertexAttribArray(1);

        for (int i = 0; i < numTiles; i++) {
            for (int index : Primitives.squareIndices) {
                indicesBuffer.put(index + i * Primitives.squareVertices.length);
            }
        }
        indicesBuffer.flip();
        EBOMap.subData(0, indicesBuffer);

        GL30.glBindVertexArray(0);
    }

    private void addTileToBuffer(Vector2f tilePos, int tileAtlasIndex, TextureAtlas textureAtlas){

        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(Primitives.squareVertices.length * 4); // 4 --> cada vértice tem (x, y, uv.x, uv.y)

        float halfTileSize = 0.5f; // adjust tile in grid cell
        Vector2f[] texCoords = textureAtlas.getTextureCoordinates(tileAtlasIndex);

        for (int i = 0; i < Primitives.squareVertices.length; i++) {
            Vertex squareVertex = Primitives.squareVertices[i];
            verticesBuffer.put(squareVertex.position.x + tilePos.x + halfTileSize);
            verticesBuffer.put(squareVertex.position.y - tilePos.y - halfTileSize);
            verticesBuffer.put(texCoords[i].x); // Corrigido para usar as coordenadas de textura corretas
            verticesBuffer.put(texCoords[i].y); // Corrigido para usar as coordenadas de textura corretas
        }

        verticesBuffer.flip();

        int tileIndex = (int) (tilePos.y * grid.getNUMCOLS() + tilePos.x);
        int vertexStartIndex = tileIndex * Primitives.squareVertices.length * 4 * Float.BYTES;

        VBOMap.subData(vertexStartIndex, verticesBuffer);

        GL30.glBindVertexArray(0);
    }
    private void renderMapTiles(Matrix4f projection){

        int atlasTexture = imGuiLayer.getAtlasTexture();
        GL30.glActiveTexture(GL13.GL_TEXTURE0);
        GL30.glBindTexture(GL11.GL_TEXTURE_2D, atlasTexture);

        mapShader.use();
        mapShader.addUniformMatrix4fv("view", view);
        mapShader.addUniformMatrix4fv("projection", projection);
        mapShader.addUniform1i("textureAtlas", 0); // Use texture unit 0

        GL30.glBindVertexArray(VAO);
        VBOMap.bind();
        EBOMap.bind();

        int numTiles = grid.getNUMCOLS() * grid.getNUMROWS();
        GL11.glDrawElements(GL11.GL_TRIANGLES, numTiles * Primitives.squareIndices.length, GL11.GL_UNSIGNED_INT, 0);
    }

    // reading text file.
    private void loadMapFromTxt(String txtPath, TextureAtlas textureAtlas) {
        try (BufferedReader br = new BufferedReader(new FileReader(txtPath))) {
            String line;
            int y = 0;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(" ");
                for (int x = 0; x < values.length; x++) {
                    // Verifique se o valor é vazio ou não
                    if (values[x].isEmpty() || values[x].equals("...")) {
                        continue; // Pular espaços vazios
                    }
                    int tileIndex = Integer.parseInt(values[x]);
                    Vector2f tilePos = new Vector2f(x, y);
                    addTileToBuffer(tilePos, tileIndex, textureAtlas);
                }
                y++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void saveMapToTxt(String filePath) {

        // get Bigger x, y.
        Vector2i maxComponents = Collections.max(tileMap.entrySet(), Map.Entry.comparingByValue()).getKey();
        maxComponents.x+=1;
        maxComponents.y+=1;
        System.out.println("Max-value: " + maxComponents.x + " , " + maxComponents.y);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (int y = 0; y < maxComponents.y; y++) {
                for (int x = 0; x < Math.abs(maxComponents.x); x++) {
                    Vector2i pos = new Vector2i(x, y);
                    if (tileMap.containsKey(pos)) {
                        int tileIndex = tileMap.get(pos);
                        writer.write(String.format("%03d ", tileIndex));
                    } else {
                        writer.write("... ");
                    }
                }
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void moveCamera(){

        float viewSpeed = 0.2f;
        if (window.isKeyPressed(GLFW_KEY_D)){
            System.out.println("X: " + viewPos.x);
            float margin = 0.3f;
            viewPos.x -= viewSpeed;
            if ( viewPos.x <= -(grid.getNUMCOLS())){
                viewPos.x = -(grid.getNUMCOLS());
            }
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            viewPos.x += viewSpeed;
            if ( viewPos.x >= 0.0f){
                viewPos.x = 0.0f;
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
            if ( viewPos.y >= grid.getNUMROWS()){
                viewPos.y = grid.getNUMROWS();
            }
        }

    }
    private void destroy(){
        GL30.glDeleteBuffers(this.VBOMap.getID());
        //GL30.glDeleteBuffers(this.VBOSelectedTile.getID());
        GL30.glDeleteBuffers(this.EBOMap.getID());
        //GL30.glDeleteBuffers(this.EBOTileSelected.getID());
        GL30.glDeleteVertexArrays(this.VAO);

        //GL30.glDeleteTextures(this.textureAtlas);
    }
}
