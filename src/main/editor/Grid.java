package main.editor;

import main.game.Shader;
import main.game.graphics.Vertex;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;

class GridLine{
    protected Vector2f posV1;
    protected Vector2f posV2;
    protected GridLine(float x1, float y1, float x2, float y2){

        this.posV1 = new Vector2f(x1, y1);
        this.posV2 = new Vector2f(x2, y2);
    }

    protected GridLine(Vector2f vertice1, Vector2f vertice2){

        this.posV1 = vertice1;
        this.posV2 = vertice2;
    }

    public float[] getV1PosAsFloatArray() {
        return new float[]{posV1.x, posV1.y};
    }

    public float[] getV2PosAsFloatArray() {
        return new float[]{posV2.x, posV2.y};
    }
}

public class Grid {

    private int VAO;
    private VBO VBO;
    private Shader shader;
    private final int NUMROWS, NUMCOLS;
    private final float lineSize = 1.f;
    private final GridLine horizontalLine = new GridLine(
            0.f, 0.0f,
            1.f, 0.0f
    );

    private final GridLine verticalLine = new GridLine(
            0.f, 0.0f,
            0.f, -1f
    );

    public Grid(int numRows, int numCols) {
        this.NUMCOLS = numCols;
        this.NUMROWS = numRows;
        this.shader = new Shader("editor/grid_vert.glsl", "editor/grid_frag.glsl");
        createBuffers();
    }

    private void createBuffers() {
        int qntLinhas = NUMCOLS + NUMROWS + 2; // adicionei mais 2 para fechar a ultima linha horiz. e vert. da grid.

        int qntVertices = 2;
        int lineVerticeSize = 2;
        int bufferSize = (qntVertices * lineVerticeSize * qntLinhas);
        FloatBuffer lineBuffer = BufferUtils.createFloatBuffer(bufferSize);

        for (int row = 0; row <= NUMROWS; row++) {
            lineBuffer.put(horizontalLine.posV1.x * NUMCOLS); // ajusta a linha horizontal ao longo do eixo X
            lineBuffer.put(horizontalLine.posV1.y - lineSize * row); // ajusta a linha horizontal ao longo do eixo Y
            lineBuffer.put(horizontalLine.posV2.x * NUMCOLS); // ajusta a linha horizontal ao longo do eixo X
            lineBuffer.put(horizontalLine.posV2.y - lineSize * row); // ajusta a linha horizontal ao longo do eixo Y
        }

        // Adicionando linhas verticais
        for (int col = 0; col <= NUMCOLS; col++) {
            lineBuffer.put(verticalLine.posV1.x + lineSize * col); // ajusta a linha vertical ao longo do eixo X
            lineBuffer.put(verticalLine.posV1.y * NUMROWS); // ajusta a linha vertical ao longo do eixo Y
            lineBuffer.put(verticalLine.posV2.x + lineSize * col); // ajusta a linha vertical ao longo do eixo X
            lineBuffer.put(verticalLine.posV2.y * NUMROWS); // ajusta a linha vertical ao longo do eixo Y
        }
        lineBuffer.flip();

        this.VAO = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(VAO);
        this.VBO = new VBO(lineBuffer, GL30.GL_STATIC_DRAW);

        GL30.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 2 * Float.BYTES, 0);
        GL30.glEnableVertexAttribArray(0);
    }

    public void render(Matrix4f view, Matrix4f projection) {

        shader.use();
        shader.addUniformMatrix4fv("view", view);
        shader.addUniformMatrix4fv("projection", projection);

        GL30.glBindVertexArray(VAO);
        VBO.bind();

        // Desenha 1 linha (2 vértices)
        GL30.glDrawArrays(GL30.GL_LINES, 0, (NUMROWS + NUMCOLS + 2) * 2);
        GL30.glBindVertexArray(0);
    }

    public void destroy(){
        GL30.glDeleteBuffers(VBO.getID());
        GL30.glDeleteVertexArrays(this.VAO);

    }

    public int getNUMROWS() {
        return NUMROWS;
    }

    public int getNUMCOLS() {
        return NUMCOLS;
    }
}
