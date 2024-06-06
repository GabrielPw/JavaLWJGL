package main.game;

import imgui.ImGui;
import imgui.app.Application;
import imgui.app.Configuration;
import main.editor.MapEditor;
import main.game.graphics.TexturePaths;

public class Main {
    public static void main(String[] args) {

        MapEditor editor = new MapEditor(TexturePaths.textureAtlas16v1);
        editor.run();

        //Game game = new Game("2D TopDown Game - Gabriel Xavier ", 640, 480);
        //game.run();

    }

}
