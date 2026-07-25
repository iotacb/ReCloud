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
.\scripts\run-demo.ps1 drawing_stuff
.\scripts\run-demo.ps1 flappy_bird_clone
.\scripts\run-demo.ps1 ui_system
```

The first run downloads a local JDK 21 and Maven 3.9.9 into `.tools`, then uses
them only for this project. Demo assets are loaded with relative paths, so run
the script from this repository.

## Rendering helpers

The renderer batches rectangles, textures, and text automatically. You can pass
`Texture` objects directly instead of manually pulling texture ids:

```java
Texture sprite = new Texture("./demo_assets/player.png").load();

Render.drawTexture(sprite, playerX, playerY, 64, 64, true);
Render.drawRect(20, 20, 120, 32, Colors.CYAN);
```

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
