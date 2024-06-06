package de.kostari.cloud.core.components;

import de.kostari.cloud.core.utils.math.Vector2f;

public class Transform extends Component {

    public Vector2f position;
    public Vector2f localPosition;
    public Vector2f scale;
    public float rotation;

    public Transform(Vector2f position, Vector2f scale) {
        this.position = position;
        this.localPosition = Vector2f.ZERO.clone();
        this.scale = scale;
    }

    public Transform() {
        this(Vector2f.ZERO.clone(), Vector2f.ONE.clone());
    }

    public Transform(Vector2f position) {
        this(position, Vector2f.ONE.clone());
    }

    public Transform(Transform transform) {
        this(transform.position, transform.scale);
    }

    /**
     * Returns a new Transform instance of the current transform
     */
    public Transform clone() {
        return new Transform(this.position.clone(), this.scale.clone());
    }

}
