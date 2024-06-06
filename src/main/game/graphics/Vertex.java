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

    public Vertex(Vector2f verticePosition, Vector3f verticeColor){

        this.position = new Vector3f(verticePosition.x, verticePosition.y, 0.f);
        this.color = verticeColor;
    }

    public float[] getPositionAsFloatArray(){

        float[] pos = new float[3];

        pos[0] = position.x;
        pos[1] = position.y;
        pos[2] = position.z;

        return pos;
    }

    public float[] getColorAsFloatArray(){

        float[] colorArray = new float[3];

        colorArray[0] = color.x;
        colorArray[1] = color.y;
        colorArray[2] = color.z;

        return colorArray;
    }

    public float[] getTextureCoordAsFloatArray(){

        float[] textCoords = new float[2];

        textCoords[0] = textureCoord.x;
        textCoords[1] = textureCoord.y;

        return textCoords;
    }
}
