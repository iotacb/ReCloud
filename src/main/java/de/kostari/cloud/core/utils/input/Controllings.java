package de.kostari.cloud.core.utils.input;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.math.Vector2;
import de.kostari.cloud.core.window.Input;
import de.kostari.cloud.core.window.Time;

public class Controllings {

    /**
     * Moves the specified object with the specified keys.
     * Inputs are normalized, so the object will move with the same speed in every
     * direction.
     * Delta time is automatically applied.
     * 
     * @see Input
     * @see Vector2
     * @see Time#delta
     * 
     * @param keyUp    The key to move up
     * @param keyRight The key to move right
     * @param keyDown  The key to move down
     * @param keyLeft  The key to move left
     * @param speed    The speed to move with
     * @param object   The object to move
     */
    public static void moveWithKeys(int keyUp, int keyRight, int keyDown, int keyLeft, float speed,
            GameObject object) {
        float x = Input.keyState(keyRight) - Input.keyState(keyLeft);
        float y = Input.keyState(keyDown) - Input.keyState(keyUp);
        Vector2 movement = new Vector2(x, y).normalize().mul(speed * Time.delta);
        object.transform.position.x += movement.x;
        object.transform.position.y += movement.y;
    }

    /**
     * Moves the specified object with the arrow keys.
     * Inputs are normalized, so the object will move with the same speed in every
     * direction.
     * Delta time is automatically applied.
     * 
     * @see Time#delta
     * 
     * @param speed  The speed to move with
     * @param object The object to move
     */
    public static void moveWithArrows(float speed, GameObject object) {
        moveWithKeys(Keys.KEY_UP, Keys.KEY_RIGHT, Keys.KEY_DOWN, Keys.KEY_LEFT, speed, object);
    }

    /**
     * Moves the specified object with the WASD keys.
     * Inputs are normalized, so the object will move with the same speed in every
     * direction.
     * Delta time is automatically applied.
     * 
     * @see Time#delta
     * 
     * @param speed  The speed to move with
     * @param object The object to move
     */
    public static void moveWithWASD(float speed, GameObject object) {
        moveWithKeys(Keys.KEY_W, Keys.KEY_D, Keys.KEY_S, Keys.KEY_A, speed, object);
    }

    /**
     * Moves the specified object into the direction of the given position.
     * Delta time is automatically applied.
     * 
     * Use the gap parameter to stop the object from moving when it is close enough,
     * otherwise it could
     * move past the target position.
     * 
     * @see Vector2
     * @see Time#delta
     * 
     * @param x      The x position to move to
     * @param y      The y position to move to
     * @param speed  The speed to move with
     * @param gap    The gap to stop moving
     * @param object The object to move
     */
    public static void moveToward(float x, float y, float speed, float gap, GameObject object) {
        Vector2 objectPositon = object.transform.position;
        Vector2 targetPosition = new Vector2(x, y);
        Vector2 direction = targetPosition.sub(objectPositon).normalize();
        Vector2 movement = direction.mul(speed * Time.delta);
        if (objectPositon.distance(targetPosition) > gap) {
            object.transform.position.x += movement.x;
            object.transform.position.y += movement.y;
        }
    }

    /**
     * Moves the specified object into the direction of the given position.
     * Delta time is automatically applied.
     * 
     * Use the gap parameter to stop the object from moving when it is close enough,
     * otherwise it could
     * move past the target position.
     * 
     * @see Vector2
     * @see Time#delta
     * 
     * @param targetPosition The position to move to
     * @param speed          The speed to move with
     * @param gap            The gap to stop moving
     * @param object         The object to move
     */
    public static void moveToward(Vector2 targetPosition, float speed, float gap, GameObject object) {
        moveToward(targetPosition.x, targetPosition.y, speed, gap, object);
    }

    /**
     * Moves the specified object away from the given position.
     * Delta time is automatically applied.
     * 
     * Use the gap parameter to stop the object from moving when it is close enough,
     * otherwise it could
     * move past the target position.
     * 
     * @see Vector2
     * @see Time#delta
     * 
     * @param x      The x to move away from
     * @param y      The y to move away from
     * @param speed  The speed to move with
     * @param gap    The gap to stop moving
     * @param object The object to move
     */
    public static void moveFrom(float x, float y, float speed, float gap, GameObject object) {
        Vector2 objectPositon = object.transform.position;
        Vector2 targetPosition = new Vector2(x, y);
        Vector2 direction = targetPosition.sub(objectPositon).normalize();
        Vector2 movement = direction.mul(speed * Time.delta);
        if (objectPositon.distance(targetPosition) > gap) {
            object.transform.position.x -= movement.x;
            object.transform.position.y -= movement.y;
        }
    }

    /**
     * Moves the specified object away from the given position.
     * Delta time is automatically applied.
     * 
     * Use the gap parameter to stop the object from moving when it is close enough,
     * otherwise it could
     * move past the target position.
     * 
     * @see Vector2
     * @see Time#delta
     * 
     * @param targetPosition The position to move away from
     * @param speed          The speed to move with
     * @param gap            The gap to stop moving
     * @param object         The object to move
     */
    public static void moveFrom(Vector2 targetPosition, float speed, float gap, GameObject object) {
        moveFrom(targetPosition.x, targetPosition.y, speed, gap, object);
    }

}
