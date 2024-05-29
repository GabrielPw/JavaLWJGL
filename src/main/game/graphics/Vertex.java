package main.game.graphics;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Vertex {

    public Vector3f position;
    public Vector3f color;
    public Vector2f textureCoord;

    public Vertex(Vector3f verticePosition, Vector3f verticeColor, Vector2f verticeTexturePos){

        this.position       = verticePosition;
        this.color          = verticeColor;
        this.textureCoord   = verticeTexturePos;
    }
}
