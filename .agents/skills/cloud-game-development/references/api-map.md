# Public API Map

## Contents

- [Application and lifecycle](#application-and-lifecycle)
- [Objects and components](#objects-and-components)
- [Time and input](#time-and-input)
- [Rendering and assets](#rendering-and-assets)
- [Camera](#camera)
- [Physics](#physics)
- [UI](#ui)
- [Particles](#particles)
- [Tweens](#tweens)
- [Post-processing and shaders](#post-processing-and-shaders)
- [Audio and utilities](#audio-and-utilities)

## Application and lifecycle

- `Window.create(width, height, title)`: create/get the singleton. Configure resizable, fullscreen, centering, position, clear color, and then call `show()`.
- `Window.get()`: access size, center, FPS, title, initialized state, and `close()`.
- `SceneManager.setScene(Scene|Class<? extends Scene>)`: transition; `current()` and `hasScene()` inspect state.
- `Scene`: override `init`, `update`, `draw`, `dispose`; use `getGameObjects`, `addGameObjects`, `removeGameObjects`, `getCamera`, and `physics`.

## Objects and components

- `GameObject`: owns public `transform`, auto-registers by default, and supports `addComponent`, `getComponent`, `removeComponent`, parenting, `destroy`, and lifecycle overrides.
- `Transform`: public mutable `position`, `localPosition`, `scale`, and `rotation` (degrees).
- `Component`: override `init`, `update`, `draw`, `dispose`; access the owning `gameObject`.
- `SpriteRenderer`: construct from `Texture` or a path and add as a component. Direct `Render` calls offer more control.

## Time and input

- `Time.delta`: frame delta seconds. `Time.timePassed`: elapsed engine time.
- `Input.keyDown`, `keyPressed`, `keyReleased`, `keyState`; equivalent mouse button queries.
- `Input.getMousePosition()`: screen coordinates. `getWorldMousePosition()`: camera-transformed coordinates. Scroll is reset per frame.
- `Keys`: Cloud aliases for keyboard codes. GLFW button constants/indices may be used for mouse buttons.
- `Controllings.moveWithWASD`, `moveWithArrows`, `moveWithKeys`, `moveToward`, `moveFrom`: delta-scaled movement helpers.
- `WindowEvents`: reflection-backed events for resize, drag, focus, mouse, keys, scroll, and file drop. Prefer per-frame `Input` unless callback behavior is required.

## Rendering and assets

- Coordinates use top-left origin with +y downward.
- `Render.drawRect`, `drawRotatedRect`, `drawTexture`, `drawRotatedTexture`, `drawText`, `drawTextShadow` enqueue batched work.
- The `centered` argument selects whether `(x,y)` is the quad center (`true`) or top-left (`false`). Angles are degrees.
- `Render.screenSpace(Runnable)` temporarily bypasses the camera; retained `Canvas` UI already does this.
- `Texture(path).load()` uploads an image. `TextureSheet(path, cellWidth, cellHeight)` loads once and exposes row-major shared-UV `Texture` cells.
- `AnimatedTexture(paths, frameTime)` uses manual `update()` and frame-count timing, not seconds; prefer explicit `TextureSheet` animation driven by `Time.delta` for predictable timing.
- `Font(path, pixelHeight).load()` creates an ASCII font texture.
- `Colors`, `Color3f`, and `Color4f` provide named and RGBA colors. Color floats are `0..1`.

## Camera

- Every initialized scene gets a `Camera` unless `initCamera()` is deliberately customized.
- `setPosition` addresses the viewport's world-space top-left. `centerOn` and `followObject` center on a target.
- Use `setZoom`, `setZoomLimits`, `zoomAt`, `handleScrolling`, `drag`, `pan`, and `panScreen`.
- Convert with `screenToWorld` / `worldToScreen`; use `getViewportWorldSize` for culling and placement.

## Physics

- `PhysicsBody.dynamic`, `fixed`, `kinematic` create centered AABB colliders. Add one to a `GameObject`; the scene discovers it automatically.
- Configure `size`, `offset`, `mass`, `gravityScale`, `bounce`, `friction`, `linearDamping`, `sensor`, `oneWayPlatform`, `layer`, `collisionMask`, and `enabled`.
- Set public `velocity`, call `velocity(x,y)`, accumulate `applyForce`, or immediately change velocity with `applyImpulse`.
- Query `bounds`, `isGrounded`, `collisions`, `isTouching`, `overlaps`, `contains`; subscribe with `onCollision`.
- `Scene.physics()` configures gravity, substeps, maximum delta, and enabled state.
- `AABB.fromCenter` / `fromTopLeft` supports standalone overlap and point containment.

## UI

- `Canvas` is a retained screen-space root rendered after post-processing. `Canvas.AUTO` sizes to content; `Canvas.FILL` fills remaining space.
- `Panel` groups and paints; `Flex` lays out rows/columns; `Grid` uses equal fixed columns; `Absolute` anchors overlay children; `Text` wraps and updates via `text(...)`.
- `Button`, `TextBox`, and `Slider` are composed controls. Their primitive panels and text elements are publicly accessible for custom skins.
- Every `UIElement` exposes typed `layout()` properties, children, visibility, clipping, translation, opacity, bounds, measurement, and arrangement. Flex/grid configuration uses typed methods such as `gap`, `align`, and `justify`; there is no CSS parser.
- Panels accept solid, gradient, texture, nine-slice, layered, or shader-backed `UIMaterial` drawables. `UIMaterial` supports rounded SDF corners, borders, glow, pulse, and animated sheen.
- Use `UI.setDefaultFont(...)` in standalone games. Canvases automatically register with the active scene and are disposed during scene disposal.

## Particles

- Add `Particles` as a component after configuring its public modules: `main`, `emission`, `shape`, `velocityOverLifetime`, `colorOverLifetime`, `sizeOverLifetime`, `rotationOverLifetime`, `renderer`.
- Shapes: `POINT`, `CIRCLE`, `BOX`, `CONE`; spaces: `LOCAL`, `WORLD`.
- Control with `play`, `pause`, `stop`, `clear`, `emit`, and deterministic `setRandomSeed`.
- `main.playOnAwake` defaults true. Configure before `addComponent`, because adding immediately calls `init()`.

## Tweens

- `Tweens.value`, `vector`, `color`, `move`, `localMove`, `scale`, `rotate`, UI `width`/`height`/`size`, `custom`, and `delay` create automatically updated tweens.
- Chain `ease(Ease...)`, `delay`, `repeat`, `loop`, `yoyo`, and callbacks. Use `target(...)` plus `Tweens.cancel(target)` for lifecycle cleanup.
- Do not call `Tweens.update()` from game code; `Window` does it once per frame.

## Post-processing and shaders

- `Render.postProcessing().enableBloom(...)`, `enableVignette(...)`, and `enableColorGrading()` add ordered effects.
- Keep returned effects to adjust parameters, toggle them, or remove them. Effect order equals add order.
- Extend `ShaderPostEffect` for custom fullscreen effects: provide `fragmentShaderPath`, create uniforms once, update uniforms per frame, and add it during `Scene.init()`.
- `PostProcessingContext` supplies width, height, time, and fullscreen drawing. `Shader` supports scalar/vector arrays and JOML matrix uniforms.

## Audio and utilities

- `Audio(path).load()` supports `.ogg` and `.wav`; call `play`, adjust gain/pitch/looping, optionally `update`, and `cleanUp` when ownership ends.
- `Vector2` is mutable for `set`, `add`, `sub`, multiply/divide, normalize, rotate, reflect, dot/cross, lerp, distance, and clones.
- Avoid mutating shared `Vector2.ZERO/ONE/direction` or named `Colors` constants; clone/copy when persistent mutable state is needed.
- `MathUtil`, `Lerping`, `Timer`, and `Physics.isColliding` provide small helpers.
