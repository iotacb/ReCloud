package de.kostari.cloud.core.components;

import de.kostari.cloud.core.utils.math.Vector2;

public class Transform extends Component {

    public Vector2 position;
    public Vector2 localPosition;
    public Vector2 scale;
    public float rotation;

    public Transform(Vector2 position, Vector2 scale) {
        this.position = position;
        this.localPosition = Vector2.ZERO.clone();
        this.scale = scale;
    }

    public Transform() {
        this(Vector2.ZERO.clone(), Vector2.ONE.clone());
    }

    public Transform(Vector2 position) {
        this(position, Vector2.ONE.clone());
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
