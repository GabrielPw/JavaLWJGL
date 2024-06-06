package main.game.graphics;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class Primitives {
    public static final Vertex[] triangleVertices = {

            // Bottom left
            new Vertex(
                    new Vector3f(-0.5f, -0.5f, 0f),                         // Position
                    new Vector3f(255.f / 255.f, 0.f / 255.f, 0.f / 255.f),  // Color
                    new Vector2f(0.0f, 0.0f)                                   // TextureCoord
            ),

            // Bottom right
            new Vertex(
                    new Vector3f(0.5f, -0.5f, 0f),
                    new Vector3f(0.f / 255.f, 255.f / 255.f, 0.f / 255.f),
                    new Vector2f(1.0f, 0.0f)
            ),

            // top
            new Vertex(
                    new Vector3f(0.0f, 0.5f, 0f),
                    new Vector3f(0.f / 255.f, 0.f / 255.f, 255.f / 255.f),
                    new Vector2f(0.5f, 1.0f)
            ),
    };

    public static final int[] triangleIndices = {
            0,1,2,
    };

    public static final Vertex[] squareVertices = {

            new Vertex(
                    new Vector3f(-0.5f,  0.5f, 0.0f),
                    new Vector3f(255.f / 255.f, 0.f   / 255.f, 0.f   / 255.f),
                    new Vector2f(0.0f, 0.0f)
            ),  // Top Left
            new Vertex(
                    new Vector3f(-0.5f, -0.5f, 0.0f),
                    new Vector3f(0.f   / 255.f, 255.f / 255.f, 0.f   / 255.f),
                    new Vector2f(0.0f, 1.0f)
            ),  // Bottom Left
            new Vertex(
                    new Vector3f( 0.5f, -0.5f, 0.0f),
                    new Vector3f(0.f   / 255.f, 0.f   / 255.f, 255.f / 255.f),
                    new Vector2f(1.0f, 1.0f)
            ),  // Bottom Right
            new Vertex(
                    new Vector3f( 0.5f,  0.5f, 0.0f),
                    new Vector3f(0.f   / 255.f, 255.f / 255.f, 255.f / 255.f),
                    new Vector2f(1.0f, 0.0f)
            ),  // Top Right
    };

    public static final int[] squareIndices = {
            0, 1, 2, // First Triangle: Top Left, Bottom Left, Bottom Right
            2, 3, 0  // Second Triangle: Bottom Right, Top Right, Top Left
    };

}
