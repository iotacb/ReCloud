# Cloud 3

## Running demos

This project includes runnable LWJGL demos in `src/demos`.

On Windows PowerShell:

```powershell
.\scripts\run-demo.ps1 first_window
```

Available demos:

```powershell
.\scripts\run-demo.ps1 -List
```

You can run any demo by name:

```powershell
.\scripts\run-demo.ps1 balls_demo
.\scripts\run-demo.ps1 camera_demo
.\scripts\run-demo.ps1 custom_shader
.\scripts\run-demo.ps1 drawing_stuff
.\scripts\run-demo.ps1 flappy_bird_clone
.\scripts\run-demo.ps1 particle_system_demo
.\scripts\run-demo.ps1 physics_demo
.\scripts\run-demo.ps1 ui_system
```

The first run downloads a local JDK 25 and Maven 3.9.9 into `.tools`, then uses
them only for this project. Demo assets are loaded with relative paths, so run
the script from this repository.

On macOS, open the demo `Main.java` file in VS Code and select
`Launch current demo` in the Run and Debug view. The checked-in launch
configuration adds `-XstartOnFirstThread`, which GLFW requires on macOS, and
enables native access for LWJGL on current JDKs. The configuration is scoped to
macOS, so Windows launches are unchanged.

## Rendering helpers

The renderer batches rectangles, textures, and text automatically. You can pass
`Texture` objects directly instead of manually pulling texture ids:

```java
Texture sprite = new Texture("./demo_assets/player.png").load();

Render.drawTexture(sprite, playerX, playerY, 64, 64, true);
Render.drawRect(20, 20, 120, 32, Colors.CYAN);
```

## Custom shaders

Run `custom_shader` for an interactive post-processing shader driven by time,
window size, mouse position, mouse buttons, and keyboard input. Its Java effect
extends `ShaderPostEffect`, declares uniforms once, and updates them every frame:

Project-owned shader files live in
`src/main/java/de/kostari/cloud/core/shader`. Maven includes that core directory
as a project resource, so effects load them through the core-relative
`../../shader/...` path rather than from a demo package.

```java
public class MyEffect extends ShaderPostEffect {
    protected String fragmentShaderPath() {
        return "demo_assets/custom_shader/my_fragment.glsl";
    }

    protected void createUniforms() {
        shader.createUniforms("resolution", "mouse", "time");
    }

    protected void updateUniforms(PostProcessingContext context) {
        shader.setUniform("resolution", (float) context.getWidth(), (float) context.getHeight());
        shader.setUniform("mouse", (float) Input.getMouseX(), (float) Input.getMouseY());
        shader.setUniform("time", context.getTime());
    }
}
```

Register an instance after the window has initialized rendering, such as from
`Scene.init()`:

```java
MyEffect effect = Render.postProcessing().add(new MyEffect());
```

Shader paths can point to packaged classpath resources or regular files.
Relative asset paths are resolved from the application's working directory, so
run the demo from the repository root when using `demo_assets/...`.

## AABB physics

Add a `PhysicsBody` component to opt a game object into physics. Bodies are
discovered and simulated by the current scene automatically:

```java
PhysicsBody playerBody = player.addComponent(PhysicsBody.dynamic(48, 48));
floor.addComponent(PhysicsBody.fixed(600, 32));

playerBody.velocity.set(180, 0);
playerBody.applyImpulse(0, -450);
```

Configure the whole scene and individual bodies with chainable methods:

```java
physics().gravity(0, 980).substeps(4);

playerBody.mass(2)
        .friction(0.8f)
        .bounce(0.15f)
        .linearDamping(0.2f);

playerBody.onCollision(hit -> {
    System.out.println("Hit " + hit.other().gameObject);
});
```

Dynamic bodies react to gravity and collisions. Fixed bodies make floors and
walls. Kinematic bodies move using their velocity but are not pushed by the
solver. Sensors report overlaps without moving either object. Collision layers,
masks, gravity scale, forces, grounded checks, collider offsets, and direct
`AABB` overlap/containment queries are also available.

Run `physics_demo` to try a small platform scene using A/D and Space.

## UI overlays

The UI package provides `Canvas`, `Flex`, `Grid`, `Text`, `Button`, and `Panel`.
Canvas instances render automatically after the scene and post-processing, so UI
stays on top of the game world.

```java
Canvas canvas = new Canvas();

Flex hud = new Flex(FlexDirection.COLUMN);
hud.style().css("padding: 14px; gap: 10px; background: #111827cc; border: 1px solid #ffffff33;");

Text score = new Text("Score: 0");
score.style().css("line-height: 1.3;");
Button restart = new Button("Restart").onClick(() -> restartGame());
hud.add(score, restart);

canvas.append(hud, 16, 16, 280, Canvas.AUTO);

score.text("Score: " + points);
```

## Tweens

Use `Tweens` for small UI, object, and value animations. Tweens update
automatically each frame after the window has been created.

```java
import de.kostari.cloud.core.utils.tween.Ease;
import de.kostari.cloud.core.utils.tween.Tweens;

Tweens.move(player, 320, 180, 0.45f)
        .ease(Ease.OUT_BACK);

Tweens.size(panel, 280, 96, 0.25f)
        .ease(Ease.OUT_CUBIC);

Tweens.value(0, 1, 0.2f, alpha -> titleColor.a = alpha);
```

Post-processing effects are opt-in. Add them once during scene setup:

```java
Render.postProcessing().enableBloom(0.65f, 0.55f, 3.0f);
Render.postProcessing().enableVignette(0.45f, 0.45f, 0.35f);
Render.postProcessing().enableColorGrading()
        .contrast(1.08f)
        .saturation(1.12f)
        .temperature(0.05f);
```

## Particle systems

`Particles` is a pooled 2D component with Unity-style configuration modules.
Configure it before adding it to a game object:

```java
GameObject emitter = new GameObject();
emitter.transform.position.set(640, 500);

Particles fire = new Particles();
fire.main.maxParticles = 500;
fire.main.startLifetime.set(0.6f, 1.2f);
fire.main.startSpeed.set(60, 130);
fire.main.startSize.set(6, 14);
fire.main.gravity.set(0, 35);

fire.emission.rateOverTime = 80;
fire.emission.addBurst(0, 30);
fire.shape.shape = Particles.ShapeType.CONE;
fire.shape.angle = 30;
fire.shape.direction.set(0, -1);

fire.colorOverLifetime.enabled = true;
fire.colorOverLifetime.color.set(
        new Color4f(1, 0.85f, 0.2f, 1),
        new Color4f(1, 0.1f, 0, 0));
fire.sizeOverLifetime.enabled = true;
fire.sizeOverLifetime.multiplier = time -> 1 - time;

emitter.addComponent(fire);
```

The main, emission, shape, velocity, color, size, rotation, and renderer modules
can be changed at runtime. Use `play()`, `pause()`, `stop()`, `clear()`, or
`emit(count)` for explicit control. Assign `fire.renderer.texture` to render
textured particles instead of colored quads.

Run `particle_system_demo` to compare cone, box, and circle emitters; local and
world simulation; gravity, damping, bursts, lifetime curves, and manual
explosions. Click to emit fireworks, press Space to pause, and press R to reset.
