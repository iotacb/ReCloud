package de.kostari.cloud.core.physics.colliders;

import de.kostari.cloud.core.components.Transform;
import de.kostari.cloud.core.utils.math.MathUtil;
import de.kostari.cloud.core.utils.math.Vector2;

public abstract class ColliderScaffold {

    protected Transform transform;
    protected ColliderTypes type;

    protected Face[] faces;
    protected Vector2[] relatives;
    protected Vector2[] absolutes;

    protected Vector2 relativeCentroid;
    protected Vector2 absoluteCentroid;

    protected int vertices;

    protected CircleCollider boundingCircle;

    public ColliderScaffold(Vector2... relatives) {
        this(ColliderTypes.POLYGON, relatives);
    }

    public ColliderScaffold(ColliderTypes type, Vector2... relatives) {
        this.type = type;
        if (type == ColliderTypes.CIRCLE) {
            this.vertices = 0;
            this.relatives = new Vector2[0];
            this.absolutes = new Vector2[0];
            this.faces = new Face[0];
        } else {
            this.vertices = relatives.length;
            this.relatives = relatives;
            this.absolutes = new Vector2[vertices];
            this.faces = new Face[vertices];
        }
    }

    protected void initializeCollider() {
        for (int i = 0; i < vertices; i++) {
            faces[i] = new Face(relatives[i], relatives[(i + 1) % vertices].sub(relatives[i]));
        }
    }

    protected void initializeSphere() {
        this.boundingCircle = new CircleCollider(relativeCentroid,
                MathUtil.boundingSphere(relativeCentroid, relatives));
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

    public Vector2[] getRelatives() {
        return relatives;
    }

    public Vector2[] getAbsolutes() {
        return absolutes;
    }

    public Face[] getFaces() {
        return faces;
    }

    public ColliderTypes getType() {
        return type;
    }

    public Vector2 support(Vector2 vector) {
        return MathUtil.maxDotPoint(absolutes, vector);
    }

}