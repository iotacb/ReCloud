package de.kostari.cloud.core.scene;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.math.Vector2;
import de.kostari.cloud.core.window.Input;
import de.kostari.cloud.core.window.Window;

public class Camera extends GameObject {

    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    private Matrix4f combinedMatrix;
    private float zoom;

    private int lastMouseX;
    private int lastMouseY;
    private boolean dragging;

    public Camera() {
        super();

        this.projectionMatrix = new Matrix4f().ortho2D(0, Window.get().getWidth(), Window.get().getHeight(), 0);
        this.viewMatrix = new Matrix4f();
        this.combinedMatrix = new Matrix4f();
        this.zoom = 1.0f;
        // WindowEvents.onMouseScroll.join(this, "handleScrollZoom");
        updateViewMatrix();
    }

    public void updateViewMatrix() {
        viewMatrix.identity();
        float scaledX = -transform.position.x * zoom;
        float scaledY = -transform.position.y * zoom;
        viewMatrix.translate(scaledX, scaledY, 0);
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

    public void zoomTo(float x, float y, float zoom) {
        Vector2f worldPosBeforeZoom = screenToWorld(x, y);

        setZoom(zoom);

        Vector2f worldPosAfterZoom = screenToWorld(x, y);

        Vector2f offset = new Vector2f(
                worldPosBeforeZoom.x - worldPosAfterZoom.x,
                worldPosBeforeZoom.y - worldPosAfterZoom.y);

        transform.position.x += offset.x;
        transform.position.y += offset.y;
    }

    public void drag(int dragButton) {
        if (Input.mouseButtonPressed(dragButton)) {
            lastMouseX = (int) Input.getMousePosition().x;
            lastMouseY = (int) Input.getMousePosition().y;
            dragging = true;
        }

        if (Input.mouseButtonDown(dragButton)) {
            int currentMouseX = (int) Input.getMousePosition().x;
            int currentMouseY = (int) Input.getMousePosition().y;

            float deltaX = currentMouseX - lastMouseX;
            float deltaY = currentMouseY - lastMouseY;

            transform.position.x -= deltaX;
            transform.position.y -= deltaY;

            lastMouseX = currentMouseX;
            lastMouseY = currentMouseY;
        }

        if (Input.mouseButtonReleased(dragButton)) {
            dragging = false;
        }
    }

    public void handleScrolling(float zoomStrength) {
        if (!dragging)
            zoomTo(Input.getMousePosition(), zoom + Input.getScrollY() * zoomStrength);
    }

    public void zoomTo(Vector2 position, float zoom) {
        zoomTo(position.x, position.y, zoom);
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

    public boolean isDragging() {
        return dragging;
    }

}
