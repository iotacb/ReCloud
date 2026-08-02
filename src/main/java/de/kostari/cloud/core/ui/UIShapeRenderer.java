package de.kostari.cloud.core.ui;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.Shader;
import de.kostari.cloud.core.utils.types.Color4f;
import de.kostari.cloud.core.window.Time;
import de.kostari.cloud.core.window.Window;

final class UIShapeRenderer {

    private static Shader shader;
    private static int vaoId;
    private static int vboId;
    private static boolean initialized;

    private UIShapeRenderer() {
    }

    static void draw(UIMaterial material, UIRect bounds, float opacity) {
        if (material == null || bounds.width <= 0 || bounds.height <= 0 || opacity <= 0) {
            return;
        }
        if (!initialized) {
            init();
        }

        Render.flush();
        float expansion = material.glowSize() * 1.5f;
        shader.bind();
        shader.setUniform("viewportSize", Window.get().getWidth(), Window.get().getHeight());
        shader.setUniform("quadBounds", bounds.x - expansion, bounds.y - expansion,
                bounds.width + expansion * 2, bounds.height + expansion * 2);
        shader.setUniform("shapeBounds", bounds.x, bounds.y, bounds.width, bounds.height);
        setColor("topColor", material.topColor(), opacity);
        setColor("bottomColor", material.bottomColor(), opacity);
        setColor("borderColor", material.borderColor(), opacity);
        setColor("glowColor", material.glowColor(), opacity);
        setColor("sheenColor", material.sheenColor(), opacity);
        shader.setUniform("cornerRadius", material.radius());
        shader.setUniform("edgeSoftness", material.softness());
        shader.setUniform("borderWidth", material.borderWidth());
        shader.setUniform("glowSize", material.glowSize());
        shader.setUniform("glowIntensity", material.glowIntensity());
        shader.setUniform("sheenWidth", material.sheenWidth());
        shader.setUniform("sheenSpeed", material.sheenSpeed());
        shader.setUniform("sheenIntensity", material.sheenIntensity());
        shader.setUniform("pulseSpeed", material.pulseSpeed());
        shader.setUniform("pulseAmount", material.pulseAmount());
        shader.setUniform("time", Time.timePassed);

        GL30.glBindVertexArray(vaoId);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        GL30.glBindVertexArray(0);
        shader.unbind();
    }

    static void cleanup() {
        if (!initialized) {
            return;
        }
        shader.cleanup();
        GL15.glDeleteBuffers(vboId);
        GL30.glDeleteVertexArrays(vaoId);
        shader = null;
        vboId = 0;
        vaoId = 0;
        initialized = false;
    }

    private static void init() {
        float[] vertices = {
                0, 0,
                1, 0,
                1, 1,
                0, 0,
                1, 1,
                0, 1
        };

        vaoId = GL30.glGenVertexArrays();
        vboId = GL15.glGenBuffers();
        GL30.glBindVertexArray(vaoId);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        FloatBuffer buffer = MemoryUtil.memAllocFloat(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        MemoryUtil.memFree(buffer);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 2 * Float.BYTES, 0L);
        GL20.glEnableVertexAttribArray(0);
        GL30.glBindVertexArray(0);

        shader = new Shader();
        shader.attachShaderFromFile(GL20.GL_VERTEX_SHADER, "../../shader/ui_material_vertex.glsl");
        shader.attachShaderFromFile(GL20.GL_FRAGMENT_SHADER, "../../shader/ui_material_fragment.glsl");
        shader.link();
        shader.bind();
        shader.createUniforms(
                "viewportSize", "quadBounds", "shapeBounds",
                "topColor", "bottomColor", "borderColor", "glowColor", "sheenColor",
                "cornerRadius", "edgeSoftness", "borderWidth", "glowSize", "glowIntensity",
                "sheenWidth", "sheenSpeed", "sheenIntensity", "pulseSpeed", "pulseAmount", "time");
        shader.unbind();
        initialized = true;
    }

    private static void setColor(String uniform, Color4f color, float opacity) {
        Color4f safe = color == null ? new Color4f(0, 0, 0, 0) : color;
        shader.setUniform(uniform, safe.r, safe.g, safe.b, safe.a * Math.clamp(opacity, 0, 1));
    }
}
