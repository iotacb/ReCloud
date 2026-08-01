# Consumer Setup

## Contents

- [Requirements](#requirements)
- [Use a published or locally installed artifact](#use-a-published-or-locally-installed-artifact)
- [Maven consumer](#maven-consumer)
- [Gradle consumer](#gradle-consumer)
- [Runtime flags](#runtime-flags)
- [Assets and working directory](#assets-and-working-directory)
- [Failure guide](#failure-guide)

## Requirements

Cloud 3 is a Java 25 library built on LWJGL 3.4.1 and JOML 1.10.5. A plain `cloud-3.0.jar` is not a fat JAR; the consumer must also resolve LWJGL modules, platform natives, and JOML unless a published dependency descriptor already provides them.

Use one native classifier matching the runtime machine:

- Windows x64: `natives-windows`
- macOS Apple Silicon: `natives-macos-arm64`
- macOS Intel: `natives-macos`
- Linux x64: `natives-linux`

Do not put `src/demos` on the consumer's source path.

## Use a published or locally installed artifact

Prefer the repository and coordinates supplied with the exported library. The current project coordinates are `de.kostari:cloud:3.0`.

If only a JAR is supplied, install it in the local Maven repository and declare the LWJGL/JOML dependencies explicitly:

```shell
mvn install:install-file \
  -Dfile=/absolute/path/to/cloud-3.0.jar \
  -DgroupId=de.kostari \
  -DartifactId=cloud \
  -Dversion=3.0 \
  -Dpackaging=jar
```

For a distributable project, prefer a private/package repository over committing machine-specific Maven cache state. A Gradle project may keep the exported JAR under `libs/` and use a file dependency.

## Maven consumer

Adapt this dependency section to the platform. Do not redeclare LWJGL when the provided Cloud artifact metadata already resolves the same modules and natives.

```xml
<properties>
  <maven.compiler.release>25</maven.compiler.release>
  <lwjgl.version>3.4.1</lwjgl.version>
  <joml.version>1.10.5</joml.version>
  <lwjgl.natives>natives-windows</lwjgl.natives>
</properties>

<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.lwjgl</groupId>
      <artifactId>lwjgl-bom</artifactId>
      <version>${lwjgl.version}</version>
      <scope>import</scope>
      <type>pom</type>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>de.kostari</groupId>
    <artifactId>cloud</artifactId>
    <version>3.0</version>
  </dependency>
  <dependency><groupId>org.lwjgl</groupId><artifactId>lwjgl</artifactId></dependency>
  <dependency><groupId>org.lwjgl</groupId><artifactId>lwjgl-glfw</artifactId></dependency>
  <dependency><groupId>org.lwjgl</groupId><artifactId>lwjgl-openal</artifactId></dependency>
  <dependency><groupId>org.lwjgl</groupId><artifactId>lwjgl-opengl</artifactId></dependency>
  <dependency><groupId>org.lwjgl</groupId><artifactId>lwjgl-stb</artifactId></dependency>
  <dependency><groupId>org.joml</groupId><artifactId>joml</artifactId><version>${joml.version}</version></dependency>

  <dependency><groupId>org.lwjgl</groupId><artifactId>lwjgl</artifactId><classifier>${lwjgl.natives}</classifier><scope>runtime</scope></dependency>
  <dependency><groupId>org.lwjgl</groupId><artifactId>lwjgl-glfw</artifactId><classifier>${lwjgl.natives}</classifier><scope>runtime</scope></dependency>
  <dependency><groupId>org.lwjgl</groupId><artifactId>lwjgl-openal</artifactId><classifier>${lwjgl.natives}</classifier><scope>runtime</scope></dependency>
  <dependency><groupId>org.lwjgl</groupId><artifactId>lwjgl-opengl</artifactId><classifier>${lwjgl.natives}</classifier><scope>runtime</scope></dependency>
  <dependency><groupId>org.lwjgl</groupId><artifactId>lwjgl-stb</artifactId><classifier>${lwjgl.natives}</classifier><scope>runtime</scope></dependency>
</dependencies>
```

Use OS-activated Maven profiles when the project must run cross-platform; set only `lwjgl.natives` in each profile.

## Gradle consumer

For an exported JAR at `libs/cloud-3.0.jar`, a Kotlin DSL build can use:

```kotlin
plugins { java }

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

val lwjglVersion = "3.4.1"
val lwjglNatives = providers.gradleProperty("lwjglNatives")
    .orElse("natives-windows")
    .get()

dependencies {
    implementation(files("libs/cloud-3.0.jar"))
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-openal")
    implementation("org.lwjgl:lwjgl-opengl")
    implementation("org.lwjgl:lwjgl-stb")
    implementation("org.joml:joml:1.10.5")

    runtimeOnly("org.lwjgl:lwjgl::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-glfw::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-openal::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-opengl::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-stb::$lwjglNatives")
}
```

Run with the matching property, for example `-PlwjglNatives=natives-macos-arm64`.

## Runtime flags

On macOS, GLFW must start on the first thread:

```text
-XstartOnFirstThread
--enable-native-access=ALL-UNNAMED
```

Use `--enable-native-access=ALL-UNNAMED` on current JDKs on other platforms too to avoid restricted-native-access warnings.

## Assets and working directory

`Texture`, `Font`, `Audio`, and external shader paths use normal file paths. Resolve relative paths from the process working directory. Keep a predictable layout such as:

```text
game/
├── pom.xml or build.gradle.kts
├── assets/
│   ├── fonts/game.ttf
│   ├── sprites/player.png
│   ├── audio/jump.ogg
│   └── shaders/my_effect.glsl
└── src/main/java/...
```

Run from `game/`, or configure the IDE/application task working directory accordingly.

Cloud's UI fallback font is `./arial.ttf`. Do not rely on that implicit file in a standalone game. Set an application font during `Scene.init()` before rendering text:

```java
UI.setDefaultFont(new Font("assets/fonts/game.ttf", 38).load());
```

Keep Cloud's packaged core `.glsl` resources inside the exported JAR. Application-owned custom shader paths may be filesystem paths or packaged classpath resources supported by `Shader`.

## Failure guide

- `UnsupportedClassVersionError`: run and compile with JDK 25.
- `NoClassDefFoundError: org/lwjgl/...` or `org/joml/...`: add the non-native LWJGL modules or JOML dependency.
- `Failed to locate library` / native extraction error: add the correct runtime classifier for every used LWJGL module and avoid mixing architectures.
- macOS main-thread assertion or blank/crashing GLFW startup: add `-XstartOnFirstThread`.
- `No scene set!`: call `SceneManager.setScene(...)` before `Window.show()`.
- Texture, font, audio, or shader load failure: check the working directory and exact file case.
- UI text fails while shapes render: provide `./arial.ttf` or call `UI.setDefaultFont(...)` with an existing TTF.
- Engine shaders missing from an exported JAR: rebuild/export Cloud with `de/kostari/cloud/core/shader/*.glsl` included as resources.
