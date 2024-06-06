package de.kostari.cloud.core.physics.colliders;

import de.kostari.cloud.core.components.Transform;
import de.kostari.cloud.core.utils.math.Vector2f;

public class PointCollider extends ColliderScaffold {

    public PointCollider(Transform relative) {
        super(ColliderTypes.POINT, relative.position);
        initializeCollider();
    }

    @Override
    public Vector2f support(Vector2f vector) {
        return absolutes[0];
    }

}
