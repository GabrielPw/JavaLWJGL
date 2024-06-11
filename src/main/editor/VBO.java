package main.editor;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;

public class VBO {

    private final int ID;
    public VBO(FloatBuffer verticesBuffer, int usage) {

        this.ID = GL30.glGenBuffers();
        GL30.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.ID);
        GL30.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, usage);
    }

    // construtor para caso inicie um VBO vazio.
    public VBO(int bufferSize, int usage) {

        this.ID = GL30.glGenBuffers();
        GL30.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.ID);
        GL30.glBufferData(GL15.GL_ARRAY_BUFFER, bufferSize, usage);
    }

    void bind(){

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, this.ID);
    }

    void subData(int offset, FloatBuffer verticesBuffer){

        GL30.glBufferSubData(GL15.GL_ARRAY_BUFFER, offset, verticesBuffer);
    }

    public int getID() {
        return ID;
    }
}
