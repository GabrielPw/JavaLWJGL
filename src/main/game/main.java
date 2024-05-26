package main.game;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.*;

import java.io.*;
import java.nio.file.Paths;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

class Main{

    public static void main(String[] args) {

        Vertex[] triangleVertices = {

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

        int[] triangleIndices = {
                0,1,2,
        };

        Vertex[] squareVertices = {

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

        int[] squareIndices = {
                0, 1, 2, // First Triangle: Top Left, Bottom Left, Bottom Right
                2, 3, 0  // Second Triangle: Bottom Right, Top Right, Top Left
        };

        glfwInit();
        glfwWindowHint(GLFW_SAMPLES, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);

        long windowID = glfwCreateWindow(640, 480, "My GLFW Window", NULL , NULL);

        if (windowID == NULL)
        {
            System.err.println("Error creating a window");
            System.exit(1);
        }

        glfwMakeContextCurrent(windowID);
        GL.createCapabilities();
        glDisable(GL_DEPTH_TEST); // ? for 2d or 3D?
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Shader
        int shaderProgram = createShaderProgram();

        // Texture.
        int textureGrass = TextureLoader.loadTexture("src/main/resources/assets/grassblock.jpg");
        int textureWooden = TextureLoader.loadTexture("src/main/resources/assets/wooden2.jpg");

        Mesh triangleMesh = new Mesh(triangleVertices, triangleIndices);
        Mesh quadMesh = new Mesh(squareVertices, squareIndices);

        quadMesh.setScale(new Vector3f(0.5f));
        quadMesh.setPosition(new Vector3f(-1.5f));

        for (Vertex vertex : quadMesh.getVertices()) {

            System.out.println("Position: (" + vertex.position.x + " | " + vertex.position.y + " | "  + vertex.position.z + ")");
            System.out.println("Color: ("    + vertex.color.x + " | " + vertex.color.y + " | "  + vertex.color.z + ")");
            System.out.println("Texture: ("  + vertex.textureCoord.x + " | " + vertex.textureCoord.y + ")");

            System.out.println("------------");
        }

        while (!glfwWindowShouldClose(windowID)) {

            glViewport(0, 0, 640, 480);
            GL11.glClearColor((80.f / 255), (100.f / 255), (121.f / 255), 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);

            GL20.glUseProgram(shaderProgram);

            GL30.glBindTexture(GL11.GL_TEXTURE_2D, textureWooden);
            triangleMesh.render(shaderProgram);
            triangleMesh.update();

            GL30.glBindTexture(GL11.GL_TEXTURE_2D, textureGrass);
            quadMesh.renderInstanced(shaderProgram, 4);
            quadMesh.update();

            // ending loop.
            GL30.glBindVertexArray(0);
            GL20.glUseProgram(0);

            glfwPollEvents();
            glfwSwapBuffers(windowID);
        }

        GL.createCapabilities();
        glfwSwapInterval(1);

        glfwDestroyWindow(windowID);
        glfwTerminate();
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

    private static int createShaderProgram() {
        int vertexShader   = loadShader("vertex_shader.glsl", GL20.GL_VERTEX_SHADER);
        int fragmentShader = loadShader("fragment_shader.glsl", GL20.GL_FRAGMENT_SHADER);

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