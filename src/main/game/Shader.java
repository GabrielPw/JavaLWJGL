package main.game;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

public class Shader {

    public int ID;

    public Shader(String vertexShaderPath, String fragmentShaderPath){

        this.ID = createShaderProgram(vertexShaderPath, fragmentShaderPath);
    }

    public void use(){
        GL20.glUseProgram(ID);
    }

    public void addUniformMatrix4fv(String uniformName, Matrix4f matrix){

        int uniformLocation = GL20.glGetUniformLocation(ID, uniformName);

        if (uniformLocation != -1) {
            FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
            matrix.get(matrixBuffer);

            GL20.glUniformMatrix4fv(uniformLocation, false, matrixBuffer);
        } else {
            uniformErrorMessage(uniformName);
        }
    }

    public void addUniform1f(String uniformName, float value){

        int uniformLocation = GL20.glGetUniformLocation(ID, uniformName);


        if (uniformLocation != -1) {
            GL20.glUniform1f(uniformLocation, value);
        }
        else { uniformErrorMessage(uniformName);}
    }
    public void addUniform1i(String uniformName, int value){

        int uniformLocation = GL20.glGetUniformLocation(ID, uniformName);


        if (uniformLocation != -1) {
            GL20.glUniform1i(uniformLocation, value);
        }
        else { uniformErrorMessage(uniformName);}
    }


    private void uniformErrorMessage(String uniformName){
        System.err.println("Uniform " + uniformName + " not found in shader program.");
    }


    public void addUniform2fvArray(String uniformName, Vector2f[] vectors) {
        int uniformLocation = GL20.glGetUniformLocation(ID, uniformName);
        if (uniformLocation != -1) {

            FloatBuffer buffer = BufferUtils.createFloatBuffer(vectors.length * 2);

            for (Vector2f vector : vectors) {
                buffer.put(vector.x).put(vector.y);
            }
            buffer.flip();

            GL20.glUniform2fv(uniformLocation, buffer);
        } else {
            System.err.println("Uniform " + uniformName + " not found in shader program.");
        }
    }

    private static int loadShader(String file, int type) {
        StringBuilder shaderSource = new StringBuilder();
        try {
            InputStream inputStream = Main.class.getResourceAsStream("/shaders/" + file);
            if (inputStream == null) {
                System.err.println("Shader file not found: " + file);
                System.exit(-1);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Can't read file: " + file);
            e.printStackTrace();
            System.exit(-1);
        }
        int shaderID = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);
        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.err.println("Couldn't compile the shader: " + file);
            System.err.println(GL20.glGetShaderInfoLog(shaderID, 512));
            System.exit(-1);
        }
        return shaderID;
    }

    private int createShaderProgram(String vertexShaderPath, String fragmentShaderPath) {

        int vertexShader   = loadShader(vertexShaderPath, GL30.GL_VERTEX_SHADER);
        int fragmentShader = loadShader(fragmentShaderPath, GL30.GL_FRAGMENT_SHADER);

        int shaderProgram = GL20.glCreateProgram();
        GL20.glAttachShader(shaderProgram, vertexShader);
        GL20.glAttachShader(shaderProgram, fragmentShader);
        GL20.glLinkProgram(shaderProgram);

        if (GL20.glGetProgrami(shaderProgram, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            System.err.println("Failed to link shader program: " + GL20.glGetProgramInfoLog(shaderProgram));
            System.exit(1);
        }

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);

        return shaderProgram;
    }
}
