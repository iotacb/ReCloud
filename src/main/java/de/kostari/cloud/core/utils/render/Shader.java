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
import java.nio.file.Path;
import java.nio.file.Paths;
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

    /**
     * Attaches a shader loaded from either the classpath or the filesystem.
     * Filesystem paths are resolved relative to the application's working
     * directory.
     */
    public void attachShaderFromFile(int type, String filePath) {
        attachShaderFromSource(type, readShaderFile(filePath));
    }

    /**
     * Attaches a shader loaded only from the filesystem.
     */
    public void attachShaderFromExternalFile(int type, String filePath) {
        attachShaderFromSource(type, readExternalShaderFile(filePath));
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

    /**
     * Uploads one or more tightly packed {@code vec4} values. Create the uniform
     * using the first array element (for example {@code lights[0]}) before
     * calling this method.
     */
    public void setUniformVec4Array(String name, float[] values) {
        if (values == null || values.length == 0 || values.length % 4 != 0) {
            throw new IllegalArgumentException("vec4 uniform data must contain a positive multiple of four values");
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(values.length);
            buffer.put(values).flip();
            GL20.glUniform4fv(location(name), buffer);
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
        validateFilePath(filePath);

        InputStream inputStream = getClass().getResourceAsStream(filePath);
        if (inputStream == null) {
            String packagePath = "/" + getClass().getPackageName().replace('.', '/');
            String normalizedResourcePath = Paths.get(packagePath)
                    .resolve(filePath)
                    .normalize()
                    .toString()
                    .replace('\\', '/');
            inputStream = getClass().getResourceAsStream(normalizedResourcePath);
        }
        if (inputStream == null) {
            return readExternalShaderFile(filePath);
        }

        StringBuilder shaderSource = new StringBuilder();
        try (InputStream stream = inputStream;
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read classpath shader: " + filePath, e);
        }
        return shaderSource.toString();
    }

    private String readExternalShaderFile(String filePath) {
        validateFilePath(filePath);

        Path resolvedPath = Paths.get(filePath).toAbsolutePath().normalize();
        try {
            return FileUtils.loadAsString(resolvedPath.toString());
        } catch (IOException e) {
            throw new RuntimeException(
                    "Shader was not found on the classpath or filesystem: " + filePath
                            + " (resolved filesystem path: " + resolvedPath + ")",
                    e);
        }
    }

    private void validateFilePath(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Shader file path cannot be null or blank");
        }
    }
}
