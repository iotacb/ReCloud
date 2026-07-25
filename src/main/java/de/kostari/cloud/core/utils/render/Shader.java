package de.kostari.cloud.core.utils.render;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import de.kostari.cloud.core.utils.files.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Shader {

    private final int programId;
    private final Map<String, Integer> uniforms;
    private final List<Integer> shaderIds;

    public Shader() {
        programId = GL20.glCreateProgram();
        uniforms = new HashMap<>();
        shaderIds = new ArrayList<>();
    }

    public void attachShaderFromFile(int type, String filePath) {
        attachShaderFromSource(type, readShaderFile(filePath));
    }

    public void attachShaderFromExternalFile(int type, String filePath) {
        try {
            attachShaderFromSource(type, FileUtils.loadAsString(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void attachShaderFromSource(int type, String shaderCode) {
        int shaderId = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderId, shaderCode);
        GL20.glCompileShader(shaderId);
        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Error compiling shader: " + GL20.glGetShaderInfoLog(shaderId));
        }
        GL20.glAttachShader(programId, shaderId);
        shaderIds.add(shaderId);
    }

    public void link() {
        GL20.glLinkProgram(programId);
        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Error linking shader program: " + GL20.glGetProgramInfoLog(programId));
        }

        for (int shaderId : shaderIds) {
            GL20.glDetachShader(programId, shaderId);
            GL20.glDeleteShader(shaderId);
        }
        shaderIds.clear();
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

    public void createUniforms(String... names) {
        for (String name : names) {
            createUniform(name);
        }
    }

    public void setUniform(String name, int value) {
        GL20.glUniform1i(location(name), value);
    }

    public void setUniform(String name, int[] values) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.mallocInt(values.length);
            buffer.put(values).flip();
            GL20.glUniform1iv(location(name), buffer);
        }
    }

    public void setUniform(String name, float value) {
        GL20.glUniform1f(location(name), value);
    }

    public void setUniform(String name, float x, float y) {
        GL20.glUniform2f(location(name), x, y);
    }

    public void setUniform(String name, float x, float y, float z) {
        GL20.glUniform3f(location(name), x, y, z);
    }

    public void setUniform(String name, float x, float y, float z, float w) {
        GL20.glUniform4f(location(name), x, y, z, w);
    }

    public void setUniform(String name, Matrix4f matrix4f) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            matrix4f.get(buffer);
            GL20.glUniformMatrix4fv(location(name), false, buffer);
        }
    }

    public void setUniform(String name, float[] matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(matrix.length);
            buffer.put(matrix).flip();
            GL20.glUniformMatrix4fv(location(name), false, buffer);
        }
    }

    public void cleanup() {
        GL20.glUseProgram(0);
        for (int shaderId : shaderIds) {
            GL20.glDeleteShader(shaderId);
        }
        shaderIds.clear();
        if (programId != 0) {
            GL20.glDeleteProgram(programId);
        }
    }

    public int getProgramId() {
        return programId;
    }

    private int location(String name) {
        Integer location = uniforms.get(name);
        if (location == null) {
            throw new RuntimeException("Uniform has not been created:" + name);
        }
        return location;
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
