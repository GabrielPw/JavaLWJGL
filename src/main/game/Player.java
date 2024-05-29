package main.game;

import main.game.graphics.Vertex;
import org.joml.Vector3f;

public class Player extends Mesh{

    private int texture;

    Player(Vertex[] vertices, int[] indices, int vao, int vbo, int ebo) {

        super(vertices, indices, vao, vbo, ebo);
    }

    @Override
    public void update(){

        transform.identity();

        scale(this.scale);
        keyboardInput();

    }

    private void scale(Vector3f scaleFactor){

        transform.scale(scaleFactor);
    }

    private void keyboardInput(){


    }
}
