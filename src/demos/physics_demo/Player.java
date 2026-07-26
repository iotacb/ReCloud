package physics_demo;

import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.input.Keys;
import de.kostari.cloud.core.window.Input;

public class Player extends PhysicsBox {

    private static final float SPEED = 260;
    private static final float JUMP_IMPULSE = 650;

    private boolean jumpHeld;

    public Player(float x, float y) {
        super(x, y, 44, 56, false, Colors.CYAN);
        // Horizontal movement is controlled directly, so contact friction would only
        // make the player cling to vertical platform edges.
        body.friction(0).linearDamping(0.5f);
    }

    @Override
    public void update() {
        float direction = Input.keyState(Keys.KEY_D) - Input.keyState(Keys.KEY_A);
        body.velocity.x = direction * SPEED;

        boolean jumpDown = Input.keyDown(Keys.KEY_SPACE);
        if (jumpDown && !jumpHeld && body.isGrounded()) {
            body.applyImpulse(0, -JUMP_IMPULSE);
        }
        jumpHeld = jumpDown;

        super.update();
    }
}
