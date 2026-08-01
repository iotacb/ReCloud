package tower_climber;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.physics.PhysicsBody;
import de.kostari.cloud.core.utils.input.Keys;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.window.Input;
import de.kostari.cloud.core.window.Time;

public class Player extends GameObject {

    public enum AnimationState {
        IDLE,
        RUN,
        JUMP,
        FALL
    }

    private static final float DRAW_SIZE = 176;
    public static final float COLLIDER_WIDTH = 32;
    public static final float COLLIDER_HEIGHT = 52;
    private static final float SPRITE_Y_OFFSET = 4;
    private static final float BASE_MOVE_SPEED = 300;
    private static final float GROUND_ACCELERATION = 2_200;
    private static final float AIR_ACCELERATION = 1_250;
    private static final float BASE_JUMP_IMPULSE = 650;
    private static final float COYOTE_TIME = 0.1f;
    private static final float JUMP_BUFFER_TIME = 0.12f;
    private static final float DASH_DURATION = 0.14f;
    private static final float DASH_COOLDOWN = 0.58f;
    private static final float BASE_DASH_SPEED = 610;

    private final PhysicsBody body;
    private final Texture[] idleFrames;
    private final Texture[] runFrames;
    private final Texture[] jumpFrames;
    private final Texture[] fallFrames;

    private AnimationState animationState = AnimationState.IDLE;
    private float animationTime;
    private int frameIndex;
    private boolean facingRight = true;
    private float coyoteTimer;
    private float jumpBufferTimer;
    private float invulnerabilityTimer;
    private float moveSpeed = BASE_MOVE_SPEED;
    private float jumpImpulse = BASE_JUMP_IMPULSE;
    private boolean controlsEnabled = true;
    private boolean jumped;
    private boolean doubleJumped;
    private boolean wasGrounded;
    private boolean airborneSinceLanding;
    private float previousVerticalSpeed;
    private float landingStrength;
    private float dashTimer;
    private float dashCooldown;
    private float dashDirection = 1;
    private float dashSpeed = BASE_DASH_SPEED;
    private boolean dashRequested;
    private boolean airDashAvailable = true;
    private boolean slingshotMode;
    private boolean slingshotCharging;
    private boolean slingshotLaunchActive;
    private boolean doubleJumpUnlocked;
    private int airJumpsRemaining;
    private boolean channeling;
    private boolean debugCollider;

    public Player(float x, float y) {
        transform.position.set(x, y);

        idleFrames = TowerSprites.playerIdle();
        runFrames = TowerSprites.playerRun();
        jumpFrames = TowerSprites.playerJump();
        fallFrames = TowerSprites.playerFall();

        body = addComponent(PhysicsBody.dynamic(COLLIDER_WIDTH, COLLIDER_HEIGHT)
                .friction(0)
                .linearDamping(0));
    }

    @Override
    public void update() {
        float delta = Math.min(Time.delta, 0.05f);
        invulnerabilityTimer = Math.max(0, invulnerabilityTimer - delta);
        detectLanding();
        if (channeling) {
            body.gravityScale(0);
            body.velocity.set(0, 0);
            updateAnimation(delta);
            previousVerticalSpeed = 0;
            super.update();
            return;
        }
        updateDash(delta);
        if (!isDashing()) {
            updateMovement(delta);
        }
        updateAnimation(delta);
        previousVerticalSpeed = body.velocity.y;
        super.update();
    }

    @Override
    public void draw() {
        Texture frame = currentFrames()[frameIndex];
        float drawWidth = facingRight ? DRAW_SIZE : -DRAW_SIZE;
        float channelOffset = channeling
                ? (float) Math.sin(Time.timePassed * 7.5f) * 4 - 3
                : 0;
        float drawY = transform.position.y + SPRITE_Y_OFFSET + channelOffset;
        if (isDashing()) {
            float dashProgress = 1 - dashTimer / DASH_DURATION;
            for (int i = 3; i >= 1; i--) {
                float alpha = (1 - dashProgress) * (0.16f - i * 0.025f);
                Render.drawTexture(frame,
                        transform.position.x - dashDirection * (i * 18 + dashProgress * 10),
                        drawY,
                        drawWidth,
                        DRAW_SIZE,
                        true,
                        new de.kostari.cloud.core.utils.types.Color4f(0.45f, 1f, 0.92f, alpha));
            }
        }
        boolean visible = invulnerabilityTimer <= 0
                || ((int) (invulnerabilityTimer * 14) & 1) == 0;
        if (visible) {
            Render.drawTexture(frame,
                    transform.position.x,
                    drawY,
                    drawWidth,
                    DRAW_SIZE,
                    true);
        }
        if (debugCollider) {
            drawCollider();
        }
        super.draw();
    }

    public void reset(float x, float y) {
        transform.position.set(x, y);
        body.velocity.set(0, 0);
        coyoteTimer = 0;
        jumpBufferTimer = 0;
        wasGrounded = false;
        airborneSinceLanding = false;
        previousVerticalSpeed = 0;
        landingStrength = 0;
        doubleJumped = false;
        dashTimer = 0;
        dashCooldown = 0;
        dashDirection = facingRight ? 1 : -1;
        dashRequested = false;
        airDashAvailable = true;
        slingshotMode = false;
        slingshotCharging = false;
        slingshotLaunchActive = false;
        airJumpsRemaining = doubleJumpUnlocked ? 1 : 0;
        channeling = false;
        body.gravityScale(1);
        setAnimation(AnimationState.IDLE);
    }

    public void respawn(float x, float y) {
        reset(x, y);
        invulnerabilityTimer = 1.25f;
    }

    public void bounceFromStomp() {
        setChanneling(false);
        slingshotLaunchActive = false;
        body.velocity.y = -jumpImpulse * 0.72f;
        coyoteTimer = 0;
        jumpBufferTimer = 0;
    }

    public void launchFromSlingshot(float velocityX, float velocityY) {
        body.velocity.set(velocityX, velocityY);
        coyoteTimer = 0;
        jumpBufferTimer = 0;
        airborneSinceLanding = true;
        slingshotMode = false;
        slingshotCharging = false;
        slingshotLaunchActive = true;
    }

    public void applyUpgrade(int level) {
        int upgrades = Math.max(0, level - 1);
        moveSpeed = BASE_MOVE_SPEED + Math.min(90, upgrades * 9);
        jumpImpulse = BASE_JUMP_IMPULSE + Math.min(85, upgrades * 8);
        dashSpeed = BASE_DASH_SPEED + Math.min(90, upgrades * 8);
    }

    public void knockBack(float sourceX) {
        setChanneling(false);
        slingshotLaunchActive = false;
        float direction = transform.position.x < sourceX ? -1 : 1;
        body.velocity.set(direction * 285, -310);
        invulnerabilityTimer = 1.15f;
    }

    public void setControlsEnabled(boolean enabled) {
        controlsEnabled = enabled;
        if (!enabled) {
            body.velocity.x = 0;
        }
    }

    public boolean isInvulnerable() {
        return invulnerabilityTimer > 0 || isDashing() || channeling;
    }

    public void setDebugCollider(boolean debugCollider) {
        this.debugCollider = debugCollider;
    }

    public PhysicsBody getBody() {
        return body;
    }

    public boolean consumeJumped() {
        boolean result = jumped;
        jumped = false;
        return result;
    }

    public boolean consumeDoubleJumped() {
        boolean result = doubleJumped;
        doubleJumped = false;
        return result;
    }

    public float consumeLandingStrength() {
        float result = landingStrength;
        landingStrength = 0;
        return result;
    }

    public AnimationState getAnimationState() {
        return animationState;
    }

    public boolean consumeDashed() {
        boolean result = dashRequested;
        dashRequested = false;
        return result;
    }

    public boolean isDashing() {
        return dashTimer > 0;
    }

    public float dashDirection() {
        return dashDirection;
    }

    public float dashCooldownRatio() {
        return Math.max(0, Math.min(1, dashCooldown / DASH_COOLDOWN));
    }

    public void setSlingshotMode(boolean slingshotMode) {
        this.slingshotMode = slingshotMode;
        if (!slingshotMode) {
            slingshotCharging = false;
        }
    }

    public void setSlingshotCharging(boolean slingshotCharging) {
        this.slingshotCharging = slingshotMode && slingshotCharging;
    }

    public float facingDirection() {
        return facingRight ? 1 : -1;
    }

    public void faceAim(float directionX) {
        if (Math.abs(directionX) > 0.05f) {
            facingRight = directionX > 0;
        }
    }

    public void setDoubleJumpUnlocked(boolean unlocked) {
        doubleJumpUnlocked = unlocked;
        if (unlocked && body.isGrounded()) {
            airJumpsRemaining = 1;
        }
    }

    public boolean isDoubleJumpUnlocked() {
        return doubleJumpUnlocked;
    }

    public int airJumpsRemaining() {
        return airJumpsRemaining;
    }

    public void setChanneling(boolean channeling) {
        this.channeling = channeling;
        if (channeling) {
            dashTimer = 0;
            slingshotLaunchActive = false;
            slingshotCharging = false;
            body.velocity.set(0, 0);
            body.gravityScale(0);
        } else {
            body.gravityScale(1);
        }
    }

    public boolean isChanneling() {
        return channeling;
    }

    private void updateDash(float delta) {
        dashCooldown = Math.max(0, dashCooldown - delta);
        if (body.isGrounded()) {
            airDashAvailable = true;
        }

        if (dashTimer > 0) {
            dashTimer = Math.max(0, dashTimer - delta);
            body.gravityScale(0.08f);
            body.velocity.x = dashDirection * dashSpeed;
            if (dashTimer <= 0) {
                body.gravityScale(1);
            }
            return;
        }
        body.gravityScale(1);

        boolean dashPressed = Input.keyPressed(Keys.KEY_SHIFT1)
                || Input.keyPressed(Keys.KEY_SHIFT2)
                || Input.keyPressed(Keys.KEY_K);
        if (!controlsEnabled || slingshotCharging || dashCooldown > 0 || !dashPressed
                || (!body.isGrounded() && !airDashAvailable)) {
            return;
        }

        float input = horizontalInput();
        dashDirection = input == 0 ? facingDirection() : Math.signum(input);
        facingRight = dashDirection > 0;
        dashTimer = DASH_DURATION;
        dashCooldown = DASH_COOLDOWN;
        slingshotLaunchActive = false;
        if (!body.isGrounded()) {
            airDashAvailable = false;
        }
        body.velocity.set(dashDirection * dashSpeed, 0);
        dashRequested = true;
    }

    private void updateMovement(float delta) {
        if (!controlsEnabled) {
            return;
        }
        if (slingshotLaunchActive && body.velocity.y >= 0) {
            slingshotLaunchActive = false;
        }
        float direction = horizontalInput();
        if (direction != 0) {
            facingRight = direction > 0;
        }

        float targetVelocity = direction * moveSpeed;
        if (slingshotCharging) {
            targetVelocity = 0;
        }
        float acceleration = body.isGrounded() ? GROUND_ACCELERATION : AIR_ACCELERATION;
        body.velocity.x = moveTowards(body.velocity.x, targetVelocity, acceleration * delta);

        if (body.isGrounded()) {
            coyoteTimer = COYOTE_TIME;
        } else {
            coyoteTimer = Math.max(0, coyoteTimer - delta);
        }

        if (slingshotMode) {
            jumpBufferTimer = 0;
            coyoteTimer = 0;
        } else if (Input.keyPressed(Keys.KEY_SPACE)
                || Input.keyPressed(Keys.KEY_W)
                || Input.keyPressed(Keys.KEY_UP)) {
            jumpBufferTimer = JUMP_BUFFER_TIME;
        } else {
            jumpBufferTimer = Math.max(0, jumpBufferTimer - delta);
        }

        if (!slingshotMode && jumpBufferTimer > 0 && coyoteTimer > 0) {
            slingshotLaunchActive = false;
            body.velocity.y = -jumpImpulse;
            jumped = true;
            jumpBufferTimer = 0;
            coyoteTimer = 0;
        } else if (!slingshotMode && jumpBufferTimer > 0
                && !body.isGrounded() && doubleJumpUnlocked && airJumpsRemaining > 0) {
            slingshotLaunchActive = false;
            body.velocity.y = -jumpImpulse * 0.92f;
            airJumpsRemaining--;
            jumped = true;
            doubleJumped = true;
            jumpBufferTimer = 0;
            coyoteTimer = 0;
        }

        boolean jumpHeld = Input.keyDown(Keys.KEY_SPACE)
                || Input.keyDown(Keys.KEY_W)
                || Input.keyDown(Keys.KEY_UP);
        if (!slingshotLaunchActive && !jumpHeld && body.velocity.y < -180) {
            body.velocity.y = -180;
        }

        if (!body.isGrounded()
                && body.velocity.y > 0
                && (Input.keyDown(Keys.KEY_S) || Input.keyDown(Keys.KEY_DOWN))) {
            body.velocity.y = Math.min(760, body.velocity.y + 900 * delta);
        }
    }

    private void detectLanding() {
        boolean grounded = body.isGrounded();
        if (grounded) {
            airJumpsRemaining = doubleJumpUnlocked ? 1 : 0;
        }
        if (!grounded) {
            airborneSinceLanding = true;
        } else if (!wasGrounded && airborneSinceLanding) {
            landingStrength = Math.max(0, previousVerticalSpeed);
            airborneSinceLanding = false;
        }
        wasGrounded = grounded;
    }

    private void updateAnimation(float delta) {
        AnimationState nextState;
        if (!body.isGrounded()) {
            nextState = body.velocity.y < 20 ? AnimationState.JUMP : AnimationState.FALL;
        } else if (Math.abs(body.velocity.x) > 15) {
            nextState = AnimationState.RUN;
        } else {
            nextState = AnimationState.IDLE;
        }

        setAnimation(nextState);

        Texture[] frames = currentFrames();
        animationTime += delta;
        float frameDuration = frameDuration();
        while (animationTime >= frameDuration) {
            animationTime -= frameDuration;
            if (animationState == AnimationState.JUMP) {
                frameIndex = Math.min(frameIndex + 1, frames.length - 1);
            } else {
                frameIndex = (frameIndex + 1) % frames.length;
            }
        }
    }

    private void setAnimation(AnimationState nextState) {
        if (animationState == nextState) {
            return;
        }
        animationState = nextState;
        animationTime = 0;
        frameIndex = 0;
    }

    private Texture[] currentFrames() {
        return switch (animationState) {
            case IDLE -> idleFrames;
            case RUN -> runFrames;
            case JUMP -> jumpFrames;
            case FALL -> fallFrames;
        };
    }

    private float frameDuration() {
        return switch (animationState) {
            case IDLE -> 0.1f;
            case RUN -> 0.065f;
            case JUMP -> 0.075f;
            case FALL -> 0.11f;
        };
    }

    private float horizontalInput() {
        int right = Math.max(Input.keyState(Keys.KEY_D), Input.keyState(Keys.KEY_RIGHT));
        int left = Math.max(Input.keyState(Keys.KEY_A), Input.keyState(Keys.KEY_LEFT));
        return right - left;
    }

    private float moveTowards(float current, float target, float maximumChange) {
        if (current < target) {
            return Math.min(current + maximumChange, target);
        }
        return Math.max(current - maximumChange, target);
    }

    private void drawCollider() {
        float left = body.bounds().left();
        float top = body.bounds().top();
        float width = body.bounds().width();
        float height = body.bounds().height();
        var color = Colors.from255(255, 90, 90, 230);
        Render.drawRect(left, top, width, 1, color);
        Render.drawRect(left, top + height - 1, width, 1, color);
        Render.drawRect(left, top, 1, height, color);
        Render.drawRect(left + width - 1, top, 1, height, color);
    }
}
