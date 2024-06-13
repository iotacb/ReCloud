package de.kostari.cloud.core.window;

import org.joml.Vector2f;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.Platform;

import de.kostari.cloud.Cloud;
import de.kostari.cloud.core.Clogger;
import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.utils.math.Vector2;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.types.Color4f;

public class Window {

    private static Window instance;

    private String windowTitle;

    private int windowWidth;
    private int windowHeight;

    private int windowPosX;
    private int windowPosY;

    private long windowId;
    private long mainMonitorId;

    private long audioContext;
    private long audioDevice;

    private boolean resizable = false;
    private boolean fullscreen = false;
    private boolean centerOnStart = true;
    private boolean vsync = true;

    private boolean initialized = false;

    private float fpsCounter = 0;
    private float fps = 0;

    private Color4f clearColor = new Color4f(0, 0, 0, 0);

    private Window(
            int windowWidth,
            int windowHeight, String windowTitle) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.windowTitle = windowTitle;
    }

    public static Window create(int windowWidth, int windowHeight, String windowTitle) {
        if (instance == null) {
            instance = new Window(windowWidth, windowHeight, windowTitle);
        }
        return instance;
    }

    private void setWindowHints() {
        GLFW.glfwDefaultWindowHints(); // optional, but recommended
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE); // hide the window initially
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, resizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE); // allow window resizing

        if (Platform.get() == Platform.MACOSX) {
            GLFW.glfwWindowHint(GLFW.GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW.GLFW_FALSE); // disable retina
        }
    }

    private void setupCallbacks() {
        GLFW.glfwSetWindowSizeCallback(windowId, (id, newWidth, newHeight) -> {
            this.windowWidth = newWidth;
            this.windowHeight = newHeight;
            WindowEvents.onWindowResize.call(windowWidth, windowHeight);
        });

        GLFW.glfwSetWindowFocusCallback(windowId, (id, focus) -> {
            WindowEvents.onWindowFocus.call(focus);
        });

        GLFW.glfwSetScrollCallback(windowId, (id, scrollX, scrollY) -> {
            Input.scrollX = (int) scrollX;
            Input.scrollY = (int) scrollY;
            WindowEvents.onMouseScroll.call((float) scrollX, (float) scrollY);
        });

        GLFW.glfwSetKeyCallback(windowId, (id, key, scancode, action, mods) -> {
            Input.listenKeys(id, key, scancode, action, mods);
            WindowEvents.onKeyType.call(key, action);
        });

        GLFW.glfwSetCursorPosCallback(windowId, (id, x, y) -> {
            Input.mouseX = (int) x;
            Input.mouseY = (int) y;
            WindowEvents.onMouseMove.call((int) x, (int) y);
        });

        GLFW.glfwSetWindowPosCallback(windowId, (id, x, y) -> {
            WindowEvents.onWindowDrag.call(x, y);
        });

        GLFW.glfwSetMouseButtonCallback(windowId, (id, button, action, mods) -> {
            Input.listenMouseButtons(id, button, action, mods);
            WindowEvents.onMouseClick.call(button, action);
        });

        GLFW.glfwSetDropCallback(windowId, (id, count, names) -> {
            StringBuilder files = new StringBuilder();
            for (int i = 0; i < count; i++) {
                files.append(GLFWDropCallback.getName(names, i) + Cloud.PATH_DELIMETER); // using space as delimiter
            }
            WindowEvents.onFileDrop.call(files.toString());
        });
    }

    private void createWindow() {
        mainMonitorId = GLFW.glfwGetPrimaryMonitor();
        GLFWVidMode videoMode = GLFW.glfwGetVideoMode(mainMonitorId);

        this.windowPosX = 20;
        this.windowPosY = 60;

        if (fullscreen) {
            this.windowWidth = videoMode.width();
            this.windowHeight = videoMode.height();
        }

        windowId = GLFW.glfwCreateWindow(windowWidth, windowHeight, windowTitle, fullscreen ? mainMonitorId : 0, 0);
        if (windowId == 0) {
            GLFW.glfwTerminate();
            throw new RuntimeException("Failed to create the GLFW window");
        }

        if (centerOnStart) {
            int centerX = (videoMode.width() - windowWidth) / 2;
            int centerY = (videoMode.height() - windowHeight) / 2;
            this.windowPosX = centerX;
            this.windowPosY = centerY;
        }
        GLFW.glfwSetWindowPos(windowId, windowPosX, windowPosY);

    }

    private void setupGLFW() {
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE);
        GLFW.glfwMakeContextCurrent(windowId);
        GLFW.glfwSwapInterval(vsync ? 1 : 0);
        GLFW.glfwShowWindow(windowId);
        GL.createCapabilities();
    }

    private void setupALC() {
        String defaultDeviceName = ALC11.alcGetString(0, ALC11.ALC_DEFAULT_DEVICE_SPECIFIER);

        audioDevice = ALC11.alcOpenDevice(defaultDeviceName);

        int[] attributes = { 0 };
        audioContext = ALC11.alcCreateContext(audioDevice, attributes);
        ALC11.alcMakeContextCurrent(audioContext);

        ALCCapabilities alcCapabilities = ALC.createCapabilities(audioDevice);
        ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);

        if (!alCapabilities.OpenAL10) {
            assert false : "Audio library not supported.";
        }
    }

    private void setupDrawing() {
        GL11.glOrtho(0, windowWidth, Cloud.ENGINE_UP == -1 ? windowHeight : 0,
                Cloud.ENGINE_UP == 1 ? windowHeight : 0,
                1, -1);
        // GL11.glMatrixMode(GL11.GL_MODELVIEW);
        // GL11.glLoadIdentity();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Render.init(windowWidth, windowHeight);
    }

    private void initialize() {
        Clogger.log("Initializing...");
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        Clogger.log("Setting hints");
        setWindowHints();
        Clogger.log("Creating basic window");
        createWindow();
        Clogger.log("Setting up callbacks");
        setupCallbacks();
        Clogger.log("Setting up GLFW");
        setupGLFW();
        Clogger.log("Setting up ALC");
        setupALC();
        Clogger.log("Setting up drawing");
        setupDrawing();
        Clogger.log("Loading Atlas files");

        this.initialized = true;
        if (!SceneManager.current().isInitialized)
            SceneManager.current().init();
    }

    private void destroy() {
        Render.cleanup();
        Callbacks.glfwFreeCallbacks(windowId);
        GLFW.glfwDestroyWindow(windowId);
        GLFW.glfwSetErrorCallback(null).free();
        GLFW.glfwTerminate();
    }

    private void updateWindow() {
        fpsCounter++;
        if (Time.getTime() >= 1) {
            fps = fpsCounter;
            fpsCounter = 0;
            Time.reset();
        }
        Input.update();
        GLFW.glfwPollEvents();
        updateGlobalMousePosition();
        SceneManager.current().update();
    }

    private void updateGlobalMousePosition() {
        Vector2f globalMousePos = SceneManager.current().getCamera().screenToWorld(Input.mouseX,
                Input.mouseY);
        Input.worldMouseX = (int) globalMousePos.x;
        Input.worldMouseY = (int) globalMousePos.y;
    }

    private void drawWindow() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
        SceneManager.current().draw();
        GLFW.glfwSwapBuffers(windowId);
    }

    public void setClearColor(float r, float g, float b, float a) {
        clearColor.r = r;
        clearColor.g = g;
        clearColor.b = b;
        clearColor.a = a;
    }

    public void show() {
        initialize();
        while (!GLFW.glfwWindowShouldClose(windowId)) {
            Time.updateDelta();
            updateWindow();
            drawWindow();
        }
        destroy();
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    public void setCenterOnStart(boolean centerOnStart) {
        this.centerOnStart = centerOnStart;
    }

    public void setWindowPosX(int windowPosX) {
        this.windowPosX = windowPosX;
    }

    public void setWindowPosY(int windowPosY) {
        this.windowPosY = windowPosY;
    }

    public void setWindowPos(int x, int y) {
        this.windowPosX = x;
        this.windowPosY = y;
    }

    public int getWidth() {
        return windowWidth;
    }

    public int getHeight() {
        return windowHeight;
    }

    public Vector2 getCenter() {
        return new Vector2(windowWidth / 2, windowHeight / 2);
    }

    public Vector2 getSize() {
        return new Vector2(windowWidth, windowHeight);
    }

    public float getFPS() {
        return fps;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setTitle(String windowTitle) {
        this.windowTitle = windowTitle;
        GLFW.glfwSetWindowTitle(windowId, windowTitle);
    }

    public static Window get() {
        assert instance != null : "Window not created!";
        return instance;
    }
}
