package main.editor;

import main.game.Shader;
import main.game.graphics.Vertex;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;

public class Grid {


    private int VAO, VBO;
    private Matrix4f model;
    private final int ROWS, COLS;
    private final float CELLSIZE;
    private float scale;
    private final Shader shader;
    private final Vertex[] lineVertices = {
            new Vertex(new Vector2f(-0.5f, 0.5f), new Vector3f(69.f / 255.f, 69.f / 255.f, 71.f / 255.f)),
            new Vertex(new Vector2f( 0.5f, 0.5f), new Vector3f(69.f / 255.f, 69.f / 255.f, 71.f / 255.f)),
    };

    public Grid(int qntRow, int qntCol, float cellSize){

        this.ROWS = qntRow;
        this.COLS = qntCol;
        this.CELLSIZE = cellSize;
        this.scale = .012f;
        this.model = new Matrix4f().identity();

        this.shader = new Shader("editor/grid_vertex_shader.glsl", "editor/grid_fragment_shader.glsl");
        this.VAO = GL30.glGenVertexArrays();
        this.VBO = GL30.glGenBuffers();

        createBuffers();
    }

    void update(Matrix4f view){
        shader.use();
        model.identity();

        model.scale(this.scale);
        shader.addUniformMatrix4fv("model", model);
        shader.addUniformMatrix4fv("view", view);
        shader.addUniform1i("hasTexture", 0);

    }

    public void render(){

        System.out.println("Rendering GRID.");

        GL30.glBindVertexArray(VAO);
        GL30.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);

        int totalVertices = 3 * (ROWS + COLS);
        GL30.glDrawArrays(GL30.GL_LINES, 0, ROWS * 5 * 2);

        GL30.glBindVertexArray(0);
    }

    void createBuffers(){

        GL30.glBindVertexArray(VAO);

        // 3 linhas horizontais para teste
        FloatBuffer gridBuffer = BufferUtils.createFloatBuffer((COLS + 1 + ROWS + 1) * lineVertices.length * 5); // adicionei 1 para ele fechar a ultima linha da grid.

        float CELLSIZETESTE = 0.2f;
        for (int row = 0; row < ROWS + 1; row++) {
            gridBuffer.put(new float[]{
                    lineVertices[0].position.x,
                    lineVertices[0].position.y - row * CELLSIZE,
                    lineVertices[0].color.x,
                    lineVertices[0].color.y,
                    lineVertices[0].color.z,

                    lineVertices[1].position.x + COLS * CELLSIZE - 1.f,
                    lineVertices[1].position.y - row * CELLSIZE,
                    lineVertices[1].color.x,
                    lineVertices[1].color.y,
                    lineVertices[1].color.z,
            });
        }

        for (int col = 0; col < COLS + 1; col++) {
            gridBuffer.put(new float[]{
                    lineVertices[0].position.x + col * CELLSIZE,
                    lineVertices[0].position.y,
                    lineVertices[0].color.x,
                    lineVertices[0].color.y,
                    lineVertices[0].color.z,

                    (lineVertices[1].position.x + col * CELLSIZE) - 1.f,
                    lineVertices[1].position.y - ROWS * CELLSIZE,
                    lineVertices[1].color.x,
                    lineVertices[1].color.y,
                    lineVertices[1].color.z,
            });
        }

        System.out.println("CellSize: " + CELLSIZETESTE);
        gridBuffer.flip();

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, VBO);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, gridBuffer, GL30.GL_STATIC_DRAW);

        int positionSize = 2;
        int colorSize = 3;
        int vertexSizeBytes = (positionSize + colorSize) * Float.BYTES;

        GL30.glVertexAttribPointer(0, positionSize, GL30.GL_FLOAT, false, vertexSizeBytes, 0);
        GL30.glVertexAttribPointer(1, colorSize, GL30.GL_FLOAT, false, vertexSizeBytes, positionSize * Float.BYTES);

        GL30.glEnableVertexAttribArray(0);
        GL30.glEnableVertexAttribArray(1);

        GL30.glBindVertexArray(0);
    }
}
