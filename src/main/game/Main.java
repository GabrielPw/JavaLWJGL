package main.game;

import main.editor.MapEditor;
import main.game.graphics.TexturePaths;

public class Main {
    public static void main(String[] args) {

        MapEditor editor = new MapEditor(TexturePaths.textureAtlas16v1, 320, 16);
        editor.run();

        //Game game = new Game("2D TopDown Game - Gabriel Xavier ", 640, 480);
        //game.run();

    }

}
