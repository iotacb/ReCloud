---
name: cloud-game-development
description: Build, extend, and debug standalone Java 2D games that consume the exported Cloud 3 game library (`de.kostari.cloud`) rather than copying its demos or engine source. Use for setting up a consumer project or implementing Cloud scenes, game objects, components, rendering, textures, input, cameras, AABB physics, retained UI, particles, audio, tweens, post-processing, shaders, and game-loop behavior.
---

# Develop Games with Cloud

Build the requested game against the exported Cloud library. Keep application code independent of `src/demos` and engine internals.

## Select references

- Read [consumer-setup.md](references/consumer-setup.md) when creating a project, adding the exported JAR/dependency, configuring native libraries, or diagnosing startup/resource failures.
- Read [quickstart.md](references/quickstart.md) before scaffolding a new game or when lifecycle behavior is unclear.
- Read [api-map.md](references/api-map.md) to select public Cloud types and verify method names.
- Read only the relevant sections of [gameplay-patterns.md](references/gameplay-patterns.md) for rendering, camera/input, physics, UI, particles, tweens, audio, or post-processing.

If a supplied Cloud JAR differs from this documented API, inspect its public classes with `jar tf` and `javap -public`; treat the actual exported artifact as authoritative.

## Follow this workflow

1. Inspect the consumer project's build files, JDK, asset layout, and Cloud dependency. Do not assume Maven when another build system is already present.
2. Establish one runnable vertical slice: `Main`, one `Scene`, and the smallest useful `GameObject` classes.
3. Add mechanics in cohesive objects/components. Keep scene code responsible for orchestration, world settings, UI roots, and transitions.
4. Add presentation after the core loop works. Prefer Cloud's batched `Render`, retained `Canvas` UI, `Particles`, `Tweens`, and post-processing APIs over parallel custom systems.
5. Compile the consumer without demo sources. Run a smoke test when the environment supports a graphical window; otherwise report the exact untested runtime boundary.

## Preserve Cloud lifecycle invariants

- Create the singleton `Window`, then set a scene with `SceneManager`, then call `window.show()`.
- Create textures, fonts, audio, shaders, canvases, particles, and other GPU/OpenAL-backed resources inside `Scene.init()` or later. The window establishes native contexts before calling the scene's `init()`.
- Call `super.init()` exactly once, normally at the end of an overriding `Scene.init()`.
- Call `super.update()` from overriding `Scene.update()` so game objects, components, physics, and the camera advance.
- Call `super.draw()` from overriding `Scene.draw()` so game objects/components render and the batch flushes. Draw scene backgrounds before it.
- Construct normal `GameObject` instances while their scene is current; they auto-register. Use `new GameObject(false)` only when intentionally managing scene registration yourself.
- Call parent lifecycle methods from overridden `GameObject` methods so attached components still work.
- Multiply manual kinematic movement by `Time.delta`. Do not multiply `PhysicsBody.velocity` by delta; the physics world integrates it.
- Use world coordinates with +x right and +y down. Pass `centered=true` when `(x, y)` represents a sprite or collider center.
- Use `Input.keyDown` for held actions and `Input.keyPressed` for one-shot actions. Use world mouse coordinates for gameplay and screen mouse coordinates for UI.

## Keep the exported-library boundary

- Import public `de.kostari.cloud...` types only. Import LWJGL constants or JOML types only where a public Cloud API requires them.
- Never import demo packages such as `tower_climber`, `physics_demo`, or `ui_system`.
- Never copy engine source, shaders, or demo classes into the game to bypass a missing dependency. Fix dependency/resource packaging or implement application-owned behavior instead.
- Do not call `Render.init`, `Render.beginFrame`, `Render.endFrame`, `UI.beginFrame`, `UI.flush`, `Tweens.update`, or physics stepping manually; `Window` and `Scene` own those operations.
- Prefer public APIs documented in the references. Do not depend on reflection or package-private fields.

## Verify the result

- Confirm the consumer compiles with its declared Cloud dependency and without `src/demos` on the source path.
- Confirm the initial scene is set before `show()` and each scene override preserves parent lifecycle calls.
- Confirm every file asset exists relative to the configured working directory or is packaged in the expected classpath location.
- Confirm the correct LWJGL native classifier is present and macOS launches on the first thread.
- Exercise input, resize behavior, scene transitions, collision behavior, and cleanup paths relevant to the game.
