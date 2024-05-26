package main.game;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Vertex {

    Vector3f position;
    Vector3f color;
    Vector2f textureCoord;

    public Vertex(Vector3f verticePosition, Vector3f verticeColor, Vector2f verticeTexturePos){

        this.position       = verticePosition;
        this.color          = verticeColor;
        this.textureCoord   = verticeTexturePos;
    }
}
