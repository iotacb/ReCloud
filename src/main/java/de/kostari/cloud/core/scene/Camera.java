package de.kostari.cloud.core.scene;

import org.lwjgl.opengl.GL11;

import de.kostari.cloud.core.components.Transform;

public class Camera {

    public Transform transform;
    private float zoom;

    public Camera(float x, float y) {
        this.transform = new Transform();
        this.transform.position.set(x, y);
        this.zoom = 1f;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public void translate(float dx, float dy) {
        transform.position.x += dx;
        transform.position.y += dy;
    }

    public void applyTransforms() {
        GL11.glTranslatef(-transform.position.x, -transform.position.y, 0);
        GL11.glScalef(zoom, zoom, 1f);
    }

    public void resetTransforms() {
        GL11.glLoadIdentity();
    }

}
