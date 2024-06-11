package de.kostari.cloud.core.utils.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import de.kostari.cloud.core.utils.files.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Shader {

    private final int programId;
    private final Map<String, Integer> uniforms;

    public Shader() {
        programId = GL20.glCreateProgram();
        uniforms = new HashMap<>();
    }

    public void attachShaderFromFile(int type, String filePath) {
        // try {
        int shaderId = GL20.glCreateShader(type);
        // String shaderCode = FileUtils.loadAsString(filePath);
        String shaderCode = readShaderFile(filePath);
        GL20.glShaderSource(shaderId, shaderCode);
        GL20.glCompileShader(shaderId);
        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Error compiling shader: " + GL20.glGetShaderInfoLog(shaderId));
        }
        GL20.glAttachShader(programId, shaderId);
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
    }

    public void link() {
        GL20.glLinkProgram(programId);
        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Error linking shader program: " + GL20.glGetProgramInfoLog(programId));
        }
    }

    public void bind() {
        GL20.glUseProgram(programId);
    }

    public void unbind() {
        GL20.glUseProgram(0);
    }

    public void createUniform(String name) {
        int location = GL20.glGetUniformLocation(programId, name);
        if (location < 0) {
            throw new RuntimeException("Could not find uniform:" + name);
        }
        uniforms.put(name, location);
    }

    public void setUniform(String name, int value) {
        GL20.glUniform1i(uniforms.get(name), value);
    }

    public void setUniform(String name, float value) {
        GL20.glUniform1f(uniforms.get(name), value);
    }

    public void setUniform(String name, float[] matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(matrix.length);
            buffer.put(matrix).flip();
            GL20.glUniformMatrix4fv(uniforms.get(name), false, buffer);
        }
    }

    public void cleanup() {
        GL20.glUseProgram(0);
        if (programId != 0) {
            GL20.glDeleteProgram(programId);
        }
    }

    private String readShaderFile(String filePath) {
        StringBuilder shaderSource = new StringBuilder();
        try (InputStream inputStream = getClass().getResourceAsStream(filePath);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load shader file: " + filePath, e);
        }
        return shaderSource.toString();
    }
}
