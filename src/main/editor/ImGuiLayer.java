package main.editor;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import main.game.graphics.TextureLoader;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

public class ImGuiLayer {

    private int atlasTexture;
    private boolean isTileBeingSelected = false;
    private Vector2i selectedTileCoordInAtlas = new Vector2i(0, 0); // tile index in atlas.
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private float[] btnColorBlue = {0.2f, 0.2f, .5f, 1.f};
    private float[] btnColorRed = {0.5f, 0.2f, .2f, 1.f};
    private float[] zoomTiles = {2.f};

    public ImGuiLayer(String atlasPath){

        this.atlasTexture = TextureLoader.loadTexture(atlasPath);
    }

    public void init(long windowID) {
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard); // Habilitar controles de teclado
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable); // Habilitar docking
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable); // Habilitar viewports múltiplas

        imGuiGlfw.init(windowID, true);
        imGuiGl3.init("#version 150");

        // Configurar tema
        ImGui.styleColorsDark();
        // Personalize as cores se desejar
        ImGui.getStyle().setColor(ImGuiCol.WindowBg, 0.1f, 0.1f, 0.1f, 1.0f);
    }

    public void render(float deltaTime) {
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        int tileSize = 16;
        int atlasWidth = 320; // Largura da sua imagem atlas
        int atlasHeight = 320; // Altura da sua imagem atlas
        int tilesPerRow = atlasWidth / tileSize;
        int tilesPerColumn = atlasHeight / tileSize;

        float winPosX   = ImGui.getWindowPosX();
        float winPosY   = ImGui.getWindowPosY();
        float winWidth  = ImGui.getWindowWidth();
        float winHeight = ImGui.getWindowHeight();
        float mainContainerWidth = winWidth;
        float mainContainerHeight = winHeight;

        //ImGui.setNextWindowPos(winPosX, winPosY);
        //ImGui.setNextWindowSize(mainContainerWidth,mainContainerHeight); // Ajuste o tamanho conforme necessário

        ImGui.begin("Tilemap Editor");

        ImGui.sliderFloat("Zoom Factor", zoomTiles, 0.5f, 5.0f);
        // Seção de Tiles
        if (ImGui.beginChild("TilesSection", mainContainerWidth, mainContainerHeight, true)) {
            for (int row = 0; row < tilesPerColumn; row++) {
                for (int col = 0; col < tilesPerRow; col++) {
                    // Calcular coordenadas de textura
                    float u1 = (float) col * tileSize / atlasWidth;
                    float v1 = (float) row * tileSize / atlasHeight;
                    float u2 = u1 + (float) tileSize / atlasWidth;
                    float v2 = v1 + (float) tileSize / atlasHeight;

                    // Exibir tile com zoom
                    ImGui.image(atlasTexture, tileSize * zoomTiles[0], tileSize * zoomTiles[0], u1, v1, u2, v2);

                    if (ImGui.isItemHovered() && ImGui.isMouseClicked(0)) {
                        System.out.println("Tile clicado na posição: (" + col + ", " + row + ")");

                        isTileBeingSelected = true;
                        selectedTileCoordInAtlas.x = col;
                        selectedTileCoordInAtlas.y = row;
                    }

                    // Espaçamento entre tiles
                    if (col < tilesPerRow - 1) {
                        ImGui.sameLine();
                    }
                }
            }
            ImGui.endChild();
        }

        // Seção de Config
        if (ImGui.beginChild("ConfigSection", 0, 70, true)) {
            int colorBlue = rgbaToInt(btnColorBlue);
            int colorRed = rgbaToInt(btnColorRed);

            ImGui.pushStyleColor(ImGuiCol.Button, colorBlue);
            ImGui.button("Save map");
            ImGui.popStyleColor();

            ImGui.pushStyleColor(ImGuiCol.Button, colorRed);
            ImGui.button("Load map from txt");
            ImGui.popStyleColor();

            ImGui.endChild();
        }

        ImGui.end();

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        // Se estiver utilizando viewports múltiplas
        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long backupWindowPtr = GLFW.glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            GLFW.glfwMakeContextCurrent(backupWindowPtr);
        }
    }

    private static int rgbaToInt(float[] color) {
        int r = (int) (color[0] * 255.0f);
        int g = (int) (color[1] * 255.0f);
        int b = (int) (color[2] * 255.0f);
        int a = (int) (color[3] * 255.0f);
        return ((a & 0xFF) << 24) | ((b & 0xFF) << 16) | ((g & 0xFF) << 8) | ((r & 0xFF));
    }

    public void dispose() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }

    public boolean isTileBeingSelected() {
        return isTileBeingSelected;
    }

    public Vector2i getSelectedTileCoordInAtlas() {
        return selectedTileCoordInAtlas;
    }
}