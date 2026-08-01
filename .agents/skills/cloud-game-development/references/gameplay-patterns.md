# Gameplay Patterns

## Contents

- [Sprite object](#sprite-object)
- [Input and camera](#input-and-camera)
- [Platform physics](#platform-physics)
- [HUD](#hud)
- [Particle burst](#particle-burst)
- [Tweens](#tweens)
- [Audio](#audio)
- [Post-processing](#post-processing)
- [Custom shader effect](#custom-shader-effect)

## Sprite object

Load textures during scene initialization, then pass them into objects instead of loading per object:

```java
Texture playerTexture = new Texture("assets/sprites/player.png").load();
new Player(playerTexture);
```

```java
final class Player extends GameObject {
    private final Texture texture;

    Player(Texture texture) {
        this.texture = texture;
        transform.position.set(320, 240);
    }

    @Override public void draw() {
        Render.drawTexture(texture, transform.position.x, transform.position.y,
                64, 64, true);
        super.draw();
    }
}
```

For sprite sheets, construct one `TextureSheet` and cache the returned cell textures. Cell `(0,0)` is top-left; linear indices are row-major.

## Input and camera

```java
float x = Input.keyState(Keys.KEY_D) - Input.keyState(Keys.KEY_A);
float y = Input.keyState(Keys.KEY_S) - Input.keyState(Keys.KEY_W);
Vector2 direction = new Vector2(x, y).normalize();
transform.position.add(direction.multiply(240 * Time.delta));

if (Input.keyPressed(Keys.KEY_ESCAPE)) {
    Window.get().close();
}
```

For an editor-like camera:

```java
getCamera().drag(2);                // middle mouse button index
getCamera().handleScrolling(0.1f); // zoom around cursor
```

For a follow camera, update its target before `super.update()` and then call:

```java
getCamera().followObject(player, 0.08f);
```

Use `Input.getWorldMousePosition()` for aiming/spawning and `Input.getMousePosition()` for screen-space hit tests.

## Platform physics

```java
physics().gravity(0, 1100).substeps(4);

GameObject floor = new GameObject();
floor.transform.position.set(480, 610);
floor.addComponent(PhysicsBody.fixed(960, 60));

GameObject player = new GameObject();
player.transform.position.set(160, 120);
PhysicsBody body = player.addComponent(PhysicsBody.dynamic(44, 56)
        .friction(0)
        .linearDamping(0.5f));
```

Implement player control in the object's `update()`:

```java
body.velocity.x = (Input.keyState(Keys.KEY_D) - Input.keyState(Keys.KEY_A)) * 260;
if (Input.keyPressed(Keys.KEY_SPACE) && body.isGrounded()) {
    body.applyImpulse(0, -650);
}
```

The physics solver moves transforms after object updates. Draw the same centered dimensions as the collider. Use a sensor for triggers. Layer values are bit flags; both `(a.mask & b.layer)` and `(b.mask & a.layer)` must be nonzero. A one-way platform only accepts a descending dynamic body at its top.

## HUD

Create retained UI once, then update element state:

```java
Canvas canvas = new Canvas();
Flex hud = new Flex(FlexDirection.COLUMN);
hud.style().css("padding: 14px; gap: 8px; background: #111827dd; border: 1px solid #ffffff33;");

Text score = new Text("Score: 0");
Button restart = new Button("Restart").onClick(this::restart);
hud.add(score, restart);
canvas.append(hud, 16, 16, 280, Canvas.AUTO);
```

Do not manually draw a `Canvas`. It renders after world post-processing. For responsive fixed slots, update `canvas.setBounds(...)` when the window dimensions change or each update.

## Particle burst

Configure before `addComponent`:

```java
GameObject emitter = new GameObject();
emitter.transform.position.set(hitX, hitY);

Particles burst = new Particles();
burst.main.playOnAwake = false;
burst.main.maxParticles = 200;
burst.main.simulationSpace = Particles.SimulationSpace.WORLD;
burst.main.startLifetime.set(0.3f, 0.7f);
burst.main.startSpeed.set(80, 220);
burst.main.startSize.set(4, 10);
burst.emission.enabled = false;
burst.shape.shape = Particles.ShapeType.CIRCLE;
burst.colorOverLifetime.enabled = true;
burst.colorOverLifetime.color.set(
        new Color4f(1, 0.8f, 0.2f, 1),
        new Color4f(1, 0.1f, 0, 0));
burst.sizeOverLifetime.enabled = true;
burst.sizeOverLifetime.multiplier = age -> 1 - age;

emitter.addComponent(burst);
burst.emit(80);
```

Reuse pooled emitters for repeated effects instead of creating one per frame.

## Tweens

```java
Tweens.move(player, 320, 180, 0.45f)
        .ease(Ease.OUT_BACK)
        .onComplete(() -> playerReady = true);

Tweens.value(0, 1, 0.2f, value -> overlayColor.a = value);
Tweens.delay(0.15f, this::spawnNextWave);
```

Cancel target-owned tweens before discarding a target: `Tweens.cancel(player)`.

## Audio

Load after OpenAL initialization in `Scene.init()`:

```java
Audio jump = new Audio("assets/audio/jump.ogg").load();
jump.setGain(0.7f);
jump.play(true); // slightly randomized pitch
```

Keep long-lived audio as a field. Call `stop()` when changing state and `cleanUp()` from the owning scene/system's disposal path.

## Post-processing

Add effects once during `Scene.init()`:

```java
Render.postProcessing().enableBloom(0.65f, 0.55f, 3f);
Render.postProcessing().enableVignette(0.35f, 0.6f, 0.3f);
Render.postProcessing().enableColorGrading()
        .contrast(1.08f)
        .saturation(1.12f)
        .temperature(0.04f);
```

Canvases render afterward and remain crisp. Remove scene-specific effects during scene disposal if they should not survive a transition because `Render.postProcessing()` is global.

## Custom shader effect

```java
final class MyEffect extends ShaderPostEffect {
    @Override protected String fragmentShaderPath() {
        return "assets/shaders/my_effect.glsl";
    }

    @Override protected void createUniforms() {
        shader.createUniforms("resolution", "time", "mouse");
    }

    @Override protected void updateUniforms(PostProcessingContext context) {
        shader.setUniform("resolution", (float) context.getWidth(), (float) context.getHeight());
        shader.setUniform("time", context.getTime());
        shader.setUniform("mouse", (float) Input.getMouseX(), (float) Input.getMouseY());
    }
}
```

The fragment shader receives the source frame as `uniform sampler2D inputTexture`. Add the effect with `Render.postProcessing().add(new MyEffect())` during `Scene.init()`.
