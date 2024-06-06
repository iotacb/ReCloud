package de.kostari.cloud.demo2;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

public class ShapeRenderer {
    private int program;
    private int vao;
    private int vbo;

    public ShapeRenderer() {
        String vertexShaderSource = "#version 330 core\n" +
                "layout(location = 0) in vec2 position;\n" +
                "void main() {\n" +
                "    gl_Position = vec4(position, 0.0, 1.0);\n" +
                "}";
        String fragmentShaderSource = "#version 330 core\n" +
                "out vec4 color;\n" +
                "void main() {\n" +
                "    color = vec4(1.0, 1.0, 1.0, 1.0);\n" +
                "}";

        int vertexShader = compileShader(GL20.GL_VERTEX_SHADER, vertexShaderSource);
        int fragmentShader = compileShader(GL20.GL_FRAGMENT_SHADER, fragmentShaderSource);

        program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertexShader);
        GL20.glAttachShader(program, fragmentShader);
        GL20.glLinkProgram(program);
        GL20.glValidateProgram(program);

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);

        vao = GL30.glGenVertexArrays();
        vbo = GL20.glGenBuffers();
    }

    private int compileShader(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL20.GL_FALSE) {
            throw new RuntimeException("Failed to compile shader: " + GL20.glGetShaderInfoLog(shader));
        }
        return shader;
    }

    public void render(float[] vertices) {
        GL20.glUseProgram(program);

        GL30.glBindVertexArray(vao);
        GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, vbo);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        GL20.glBufferData(GL20.GL_ARRAY_BUFFER, buffer, GL20.GL_DYNAMIC_DRAW);

        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL20.GL_FLOAT, false, 0, 0);

        GL20.glDrawArrays(GL20.GL_TRIANGLES, 0, vertices.length / 2);

        GL20.glDisableVertexAttribArray(0);
        GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);
    }

    public void cleanup() {
        GL20.glDeleteBuffers(vbo);
        GL30.glDeleteVertexArrays(vao);
        GL20.glDeleteProgram(program);
    }
}
