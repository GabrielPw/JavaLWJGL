package main.game;


import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;

public class TextureLoader {

    public static int loadTexture(String path) {

        if (!Paths.get(path).toFile().exists()) {
            System.err.println("Texture file not found: " + path);
            System.exit(1);
        }

        // Prepare buffers for width, height and channels
        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        // Load image
        ByteBuffer data = STBImage.stbi_load(path, width, height, channels, 4);
        if (data == null) {
            throw new RuntimeException("Failed to load a texture file!"
                    + System.lineSeparator() + STBImage.stbi_failure_reason());
        }

        // Create a new OpenGL texture
        int textureId = GL30.glGenTextures();
        GL30.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        // Upload the texture data
        GL30.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width.get(0), height.get(0), 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);

        // Generate mipmaps
        GL30.glGenerateMipmap(GL30.GL_TEXTURE_2D);

        // Set texture parameters
        GL30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        GL30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        // Free the loaded image data
        STBImage.stbi_image_free(data);

        return textureId;
    }

}