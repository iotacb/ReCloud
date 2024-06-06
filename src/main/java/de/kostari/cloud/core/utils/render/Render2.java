package de.kostari.cloud.core.utils.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import de.kostari.cloud.core.utils.types.Color4f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Render2 {

    private static final int MAX_BATCH_SIZE = 1000;
    private static final int VERTEX_SIZE = 2 + 4;
    private static final int RECT_VERTICES = 4;
    private static final int TRI_VERTICES = 3;
    private static final int CIRCLE_SEGMENTS = 30;

    private static int vboId;
    private static int eboId;
    private static FloatBuffer vertexBuffer;
    private static IntBuffer indexBuffer;
    private static List<Shape> shapes;
    private static boolean initialized = false;

    private static class Shape {
        float[] vertices;
        int[] indices;
    }

    public static void init() {
        if (initialized)
            return;

        vboId = GL15.glGenBuffers();
        eboId = GL15.glGenBuffers();

        // Adjust the buffer sizes to account for the largest possible batch
        vertexBuffer = MemoryUtil.memAllocFloat(MAX_BATCH_SIZE * RECT_VERTICES * VERTEX_SIZE);
        indexBuffer = MemoryUtil.memAllocInt(MAX_BATCH_SIZE * RECT_VERTICES * 6); // 6 indices per rectangle (2
                                                                                  // triangles)

        shapes = new ArrayList<>();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, VERTEX_SIZE * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, VERTEX_SIZE * Float.BYTES, 2 * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboId);

        initialized = true;
    }

    public static void cleanup() {
        if (!initialized)
            return;

        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(vboId);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(eboId);

        MemoryUtil.memFree(vertexBuffer);
        MemoryUtil.memFree(indexBuffer);

        initialized = false;
    }

    public static void beginBatch() {
        shapes.clear();
    }

    private static void endBatch() {
        if (shapes.isEmpty())
            return;

        vertexBuffer.clear();
        indexBuffer.clear();

        int vertexIndex = 0;
        int indexIndex = 0;

        for (Shape shape : shapes) {
            vertexBuffer.put(shape.vertices);
            for (int i = 0; i < shape.indices.length; i++) {
                indexBuffer.put(shape.indices[i] + vertexIndex);
            }
            vertexIndex += shape.vertices.length / VERTEX_SIZE;
        }

        vertexBuffer.flip();
        indexBuffer.flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_DYNAMIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboId);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_DYNAMIC_DRAW);

        GL11.glDrawElements(GL11.GL_TRIANGLES, indexBuffer.limit(), GL11.GL_UNSIGNED_INT, 0);
    }

    private static void addShape(Shape shape) {
        if (shapes.size() >= MAX_BATCH_SIZE) {
            flush();
        }
        shapes.add(shape);
    }

    public static void drawRect(float x, float y, float width, float height, Color4f color) {
        Shape shape = new Shape();
        shape.vertices = new float[] {
                x, y, color.r, color.g, color.b, color.a,
                x + width, y, color.r, color.g, color.b, color.a,
                x + width, y + height, color.r, color.g, color.b, color.a,
                x, y + height, color.r, color.g, color.b, color.a
        };
        shape.indices = new int[] { 0, 1, 2, 2, 3, 0 };
        addShape(shape);
    }

    public static void drawTriangle(float x1, float y1, float x2, float y2, float x3, float y3, Color4f color) {
        Shape shape = new Shape();
        shape.vertices = new float[] {
                x1, y1, color.r, color.g, color.b, color.a,
                x2, y2, color.r, color.g, color.b, color.a,
                x3, y3, color.r, color.g, color.b, color.a
        };
        shape.indices = new int[] { 0, 1, 2 };
        addShape(shape);
    }

    public static void drawCircle(float x, float y, float radius, Color4f color) {
        Shape shape = new Shape();
        List<Float> verticesList = new ArrayList<>();
        List<Integer> indicesList = new ArrayList<>();
        verticesList.add(x);
        verticesList.add(y);
        verticesList.add(color.r);
        verticesList.add(color.g);
        verticesList.add(color.b);
        verticesList.add(color.a);

        for (int i = 0; i <= CIRCLE_SEGMENTS; i++) {
            double angle = 2 * Math.PI * i / CIRCLE_SEGMENTS;
            float px = x + (float) Math.cos(angle) * radius;
            float py = y + (float) Math.sin(angle) * radius;
            verticesList.add(px);
            verticesList.add(py);
            verticesList.add(color.r);
            verticesList.add(color.g);
            verticesList.add(color.b);
            verticesList.add(color.a);

            if (i > 0) {
                indicesList.add(0);
                indicesList.add(i);
                indicesList.add(i + 1);
            }
        }

        shape.vertices = new float[verticesList.size()];
        for (int i = 0; i < verticesList.size(); i++) {
            shape.vertices[i] = verticesList.get(i);
        }

        shape.indices = new int[indicesList.size()];
        for (int i = 0; i < indicesList.size(); i++) {
            shape.indices[i] = indicesList.get(i);
        }

        addShape(shape);
    }

    public static void flush() {
        endBatch();
        beginBatch();
    }
}
