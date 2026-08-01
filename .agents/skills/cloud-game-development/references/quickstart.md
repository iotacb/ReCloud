# Minimal Standalone Game

Use this structure as the first compiling vertical slice. It imports only the exported Cloud API.

## `Main.java`

```java
package game;

import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.window.Window;

public final class Main {
    public static void main(String[] args) {
        Window window = Window.create(1280, 720, "My Cloud Game");
        window.setResizable(true);
        SceneManager.setScene(GameScene.class);
        window.show();
    }
}
```

## `GameScene.java`

```java
package game;

import de.kostari.cloud.core.scene.Scene;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.window.Window;

public final class GameScene extends Scene {
    @Override
    public void init() {
        Window.get().setClearColor(0.03f, 0.05f, 0.09f, 1f);
        new Player(640, 360);
        super.init();
    }

    @Override
    public void draw() {
        Render.drawRect(0, 0, Window.get().getWidth(), Window.get().getHeight(), Colors.CHARCOAL);
        super.draw();
    }
}
```

This first slice has no file assets. Add UI after supplying a font inside the consumer project:

```java
UI.setDefaultFont(new Font("assets/fonts/game.ttf", 38).load());
Canvas canvas = new Canvas();
Text status = new Text("Move with WASD");
canvas.append(status, 16, 16, 260, Canvas.AUTO);
```

## `Player.java`

```java
package game;

import de.kostari.cloud.core.objects.GameObject;
import de.kostari.cloud.core.utils.Colors;
import de.kostari.cloud.core.utils.input.Controllings;
import de.kostari.cloud.core.utils.render.Render;

public final class Player extends GameObject {
    private static final float SIZE = 48;

    public Player(float x, float y) {
        transform.position.set(x, y);
    }

    @Override
    public void update() {
        Controllings.moveWithWASD(280, this); // applies Time.delta internally
        super.update();
    }

    @Override
    public void draw() {
        Render.drawRect(transform.position.x, transform.position.y,
                SIZE, SIZE, true, Colors.CYAN);
        super.draw();
    }
}
```

## Lifecycle model

`Window.show()` initializes GLFW, OpenGL, OpenAL, Cloud rendering, and then the current scene. Each frame it updates time/input/UI/tweens, calls the scene update, draws the scene through Cloud's batch, applies post-processing, draws canvases in screen space, and swaps buffers.

`new GameObject()` auto-adds the object to the current scene. `addComponent(...)` immediately assigns `component.gameObject`, calls `Component.init()`, and returns the component. `Scene.update()` calls object/component updates before stepping its physics world. `Scene.draw()` draws objects/components and flushes the render batch.

Switch scenes with `SceneManager.setScene(new OtherScene())` or `SceneManager.setScene(OtherScene.class)`. The old scene's `dispose()` runs first. Store scene-specific canvases and resources on the scene and release application-owned audio or other resources in an overriding `dispose()` before calling `super.dispose()`.
