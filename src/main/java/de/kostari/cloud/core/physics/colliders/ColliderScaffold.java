package de.kostari.cloud.core.physics.colliders;

import de.kostari.cloud.core.components.Transform;
import de.kostari.cloud.core.utils.math.Mathf;
import de.kostari.cloud.core.utils.math.Vector2f;

public abstract class ColliderScaffold {

    protected Transform transform;
    protected ColliderTypes type;

    protected Face[] faces;
    protected Vector2f[] relatives;
    protected Vector2f[] absolutes;

    protected Vector2f relativeCentroid;
    protected Vector2f absoluteCentroid;

    protected int vertices;

    protected CircleCollider boundingCircle;

    public ColliderScaffold(Vector2f... relatives) {
        this(ColliderTypes.POLYGON, relatives);
    }

    public ColliderScaffold(ColliderTypes type, Vector2f... relatives) {
        this.type = type;
        if (type == ColliderTypes.CIRCLE) {
            this.vertices = 0;
            this.relatives = new Vector2f[0];
            this.absolutes = new Vector2f[0];
            this.faces = new Face[0];
        } else {
            this.vertices = relatives.length;
            this.relatives = relatives;
            this.absolutes = new Vector2f[vertices];
            this.faces = new Face[vertices];
        }
    }

    protected void initializeCollider() {
        for (int i = 0; i < vertices; i++) {
            faces[i] = new Face(relatives[i], relatives[(i + 1) % vertices].sub(relatives[i]));
        }
    }

    protected void initializeSphere() {
        this.boundingCircle = new CircleCollider(relativeCentroid, Mathf.boundingSphere(relativeCentroid, relatives));
    }

    protected void updateCollider() {
        for (int i = 0; i < vertices; i++) {
            absolutes[i] = transform.position.add(relatives[i]);
        }
    }

    public void setPosition(float x, float y) {
        transform.position.set(x, y);
    }

    public int getVertices() {
        return vertices;
    }

    public Vector2f[] getRelatives() {
        return relatives;
    }

    public Vector2f[] getAbsolutes() {
        return absolutes;
    }

    public Face[] getFaces() {
        return faces;
    }

    public ColliderTypes getType() {
        return type;
    }

    public Vector2f support(Vector2f vector) {
        return Mathf.maxDotPoint(absolutes, vector);
    }

}