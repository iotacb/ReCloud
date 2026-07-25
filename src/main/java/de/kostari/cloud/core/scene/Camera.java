package de.kostari.cloud.core.scene;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.math.Vector2;
import de.kostari.cloud.core.window.Input;
import de.kostari.cloud.core.window.Window;

public class Camera extends GameObject {

    private final Matrix4f projectionMatrix;
    private final Matrix4f viewMatrix;
    private final Matrix4f combinedMatrix;

    private float zoom;
    private float minZoom = 0.05f;
    private float maxZoom = 20.0f;

    private int lastMouseX;
    private int lastMouseY;
    private boolean dragging;

    public Camera() {
        super(false);

        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();
        this.combinedMatrix = new Matrix4f();
        this.zoom = 1.0f;

        resize(Window.get().getWidth(), Window.get().getHeight());
    }

    public void resize(int width, int height) {
        projectionMatrix.identity().ortho2D(0, Math.max(1, width), Math.max(1, height), 0);
        updateViewMatrix();
    }

    public void updateViewMatrix() {
        viewMatrix.identity();
        viewMatrix.translate(-transform.position.x * zoom, -transform.position.y * zoom, 0);
        viewMatrix.scale(zoom, zoom, 1.0f);
        projectionMatrix.mul(viewMatrix, combinedMatrix);
    }

    @Override
    public void update() {
        updateViewMatrix();
        super.update();
    }

    public void followObject(GameObject object, float lerpingFactor) {
        if (object == null) {
            return;
        }
        follow(object.transform.position, lerpingFactor);
    }

    public void follow(Vector2 worldPosition, float lerpingFactor) {
        Vector2 targetPosition = topLeftForCenter(worldPosition.x, worldPosition.y);
        Vector2 lerped = transform.position.lerp(targetPosition, lerpingFactor);
        transform.position.set(lerped);
        updateViewMatrix();
    }

    public void centerOn(GameObject object) {
        if (object != null) {
            centerOn(object.transform.position);
        }
    }

    public void centerOn(Vector2 worldPosition) {
        transform.position.set(topLeftForCenter(worldPosition.x, worldPosition.y));
        updateViewMatrix();
    }

    public void setPosition(float x, float y) {
        transform.position.set(x, y);
        updateViewMatrix();
    }

    public void pan(float worldDeltaX, float worldDeltaY) {
        transform.position.add(worldDeltaX, worldDeltaY);
        updateViewMatrix();
    }

    public void panScreen(float screenDeltaX, float screenDeltaY) {
        pan(screenDeltaX / zoom, screenDeltaY / zoom);
    }

    public void setZoom(float zoom) {
        this.zoom = clamp(zoom, minZoom, maxZoom);
        updateViewMatrix();
    }

    public void setZoomLimits(float minZoom, float maxZoom) {
        this.minZoom = Math.max(0.0001f, Math.min(minZoom, maxZoom));
        this.maxZoom = Math.max(this.minZoom, Math.max(minZoom, maxZoom));
        setZoom(zoom);
    }

    public void zoom(float factor) {
        if (factor <= 0) {
            return;
        }
        setZoom(this.zoom * factor);
    }

    public void zoomTo(float x, float y, float zoom) {
        Vector2f worldPosBeforeZoom = screenToWorld(x, y);
        setZoom(zoom);
        Vector2f worldPosAfterZoom = screenToWorld(x, y);

        transform.position.x += worldPosBeforeZoom.x - worldPosAfterZoom.x;
        transform.position.y += worldPosBeforeZoom.y - worldPosAfterZoom.y;
        updateViewMatrix();
    }

    public void zoomTo(Vector2 position, float zoom) {
        zoomTo(position.x, position.y, zoom);
    }

    public void zoomAt(float screenX, float screenY, float scrollAmount, float zoomStrength) {
        if (scrollAmount == 0) {
            return;
        }

        float factor = 1.0f + scrollAmount * zoomStrength;
        if (factor <= 0.01f) {
            factor = 0.01f;
        }
        zoomTo(screenX, screenY, zoom * factor);
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

            panScreen(-deltaX, -deltaY);

            lastMouseX = currentMouseX;
            lastMouseY = currentMouseY;
        }

        if (Input.mouseButtonReleased(dragButton)) {
            dragging = false;
        }
    }

    public void handleScrolling(float zoomStrength) {
        if (!dragging) {
            zoomAt(Input.getMousePosition().x, Input.getMousePosition().y, Input.getScrollY(), zoomStrength);
        }
    }

    public Vector2f screenToWorld(float screenX, float screenY) {
        updateViewMatrix();

        float ndcX = (2.0f * screenX) / Window.get().getWidth() - 1.0f;
        float ndcY = 1.0f - (2.0f * screenY) / Window.get().getHeight();

        Vector4f worldPos = new Vector4f(ndcX, ndcY, 0, 1);
        new Matrix4f(combinedMatrix).invert().transform(worldPos);
        return new Vector2f(worldPos.x, worldPos.y);
    }

    public Vector2f worldToScreen(float worldX, float worldY) {
        updateViewMatrix();

        Vector4f clipPos = new Vector4f(worldX, worldY, 0, 1);
        combinedMatrix.transform(clipPos);

        if (clipPos.w != 0) {
            clipPos.div(clipPos.w);
        }

        float screenX = (clipPos.x + 1.0f) * 0.5f * Window.get().getWidth();
        float screenY = (1.0f - clipPos.y) * 0.5f * Window.get().getHeight();
        return new Vector2f(screenX, screenY);
    }

    public Vector2 getViewportWorldSize() {
        return new Vector2(Window.get().getWidth() / zoom, Window.get().getHeight() / zoom);
    }

    public float getZoom() {
        return zoom;
    }

    public float getMinZoom() {
        return minZoom;
    }

    public float getMaxZoom() {
        return maxZoom;
    }

    public Matrix4f getCombinedMatrix() {
        updateViewMatrix();
        return combinedMatrix;
    }

    public boolean isDragging() {
        return dragging;
    }

    private Vector2 topLeftForCenter(float worldX, float worldY) {
        Vector2 viewportWorldSize = getViewportWorldSize();
        return new Vector2(worldX - viewportWorldSize.x * 0.5f, worldY - viewportWorldSize.y * 0.5f);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
