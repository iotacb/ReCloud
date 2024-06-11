package de.kostari.cloud.core.physics.colliders;

import de.kostari.cloud.core.utils.math.Vector2;

public class LineCollider extends ColliderScaffold {

    public LineCollider(Vector2 relativeA, Vector2 relativeB) {
        super(ColliderTypes.LINE, relativeA, relativeB);
        initializeCollider();
    }

    @Override
    public Vector2 support(Vector2 vector) {
        return absolutes[0].dot(vector) > absolutes[1].dot(vector) ? absolutes[0] : absolutes[1];
    }

}
