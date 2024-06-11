package de.kostari.cloud.core.physics.colliders;

import de.kostari.cloud.core.components.Transform;
import de.kostari.cloud.core.utils.math.Vector2;

public class PointCollider extends ColliderScaffold {

    public PointCollider(Transform relative) {
        super(ColliderTypes.POINT, relative.position);
        initializeCollider();
    }

    @Override
    public Vector2 support(Vector2 vector) {
        return absolutes[0];
    }

}
