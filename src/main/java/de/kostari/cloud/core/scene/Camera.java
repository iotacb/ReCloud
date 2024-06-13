package de.kostari.cloud.core.scene;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.math.Vector2;
import de.kostari.cloud.core.window.Window;

public class Camera extends GameObject {

    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    private Matrix4f combinedMatrix;
    private float zoom;

    private float deadZoneX = 0;
    private float deadZoneY = 0;

    public Camera() {
        super();

        this.projectionMatrix = new Matrix4f().ortho2D(0, Window.get().getWidth(), Window.get().getHeight(), 0);
        this.viewMatrix = new Matrix4f();
        this.combinedMatrix = new Matrix4f();
        this.zoom = 1.0f;
        updateViewMatrix();
    }

    public void updateViewMatrix() {
        viewMatrix.identity();
        viewMatrix.translate(-transform.position.x, -transform.position.y, 0);
        viewMatrix.scale(zoom, zoom, 1.0f);
        projectionMatrix.mul(viewMatrix, combinedMatrix);
    }

    @Override
    public void update() {
        updateViewMatrix();
        super.update();
    }

    public void followObject(GameObject object, float lerpingFactor) {
        Vector2 targetPosition = object.transform.position.clone().sub(Window.get().getCenter());
        Vector2 currentPosition = transform.position;
        Vector2 lerped = new Vector2(currentPosition).lerp(targetPosition, lerpingFactor);
        if (Math.abs(targetPosition.x) > deadZoneX) {
            lerped.x = targetPosition.x;
        }
        if (Math.abs(targetPosition.y) > deadZoneY) {
            lerped.y = targetPosition.y;
        }
        transform.position.set(lerped);
    }

    public void setZoom(float zoom) {
        if (zoom > 0) {
            this.zoom = zoom;
            updateViewMatrix();
        }
    }

    public void zoom(float factor) {
        setZoom(this.zoom * factor);
    }

    public Vector2f screenToWorld(float screenX, float screenY) {
        float ndcX = (2.0f * screenX) / Window.get().getWidth() - 1.0f;
        float ndcY = 1.0f - (2.0f * screenY) / Window.get().getHeight();

        Vector4f ndcPos = new Vector4f(ndcX, ndcY, 0, 1);
        Matrix4f inverse = new Matrix4f(combinedMatrix).invert();
        ndcPos.mul(inverse);

        return new Vector2f(ndcPos.x, ndcPos.y);
    }

    public Vector2f worldToScreen(float worldX, float worldY) {
        Vector4f worldPos = new Vector4f(worldX, worldY, 0, 1);
        worldPos.mul(combinedMatrix);
        return new Vector2f(worldPos.x, worldPos.y);
    }

    public float getZoom() {
        return zoom;
    }

    public Matrix4f getCombinedMatrix() {
        return combinedMatrix;
    }

}
