package de.kostari.cloud.core.physics.colliders;

import de.kostari.cloud.core.utils.math.Vector2f;

public class LineCollider extends ColliderScaffold {

    public LineCollider(Vector2f relativeA, Vector2f relativeB) {
        super(ColliderTypes.LINE, relativeA, relativeB);
        initializeCollider();
    }

    @Override
    public Vector2f support(Vector2f vector) {
        return absolutes[0].dot(vector) > absolutes[1].dot(vector) ? absolutes[0] : absolutes[1];
    }

}
