package de.kostari.cloud.core.window;

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
import de.kostari.cloud.core.scene.SceneManager;
import de.kostari.cloud.core.utils.Atlas;
import de.kostari.cloud.core.utils.math.Vector2f;
import de.kostari.cloud.core.utils.types.Float4;

public class Window {

    public static Window instance;

    private String windowTitle;

    private int windowWidth;
    private int windowHeight;

    private long windowId;
    private long mainMonitorId;

    private long audioContext;
    private long audioDevice;

    private boolean resizable = false;
    private boolean fullscreen = false;
    private boolean centerOnStart = true;
    private boolean vsync = true;

    private Float4 clearColor = new Float4(0, 0, 0, 0);

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
            Input.scrollX = (float) scrollX;
            Input.scrollY = (float) scrollY;
            WindowEvents.onMouseScroll.call((float) scrollX, (float) scrollY);
        });

        GLFW.glfwSetKeyCallback(windowId, (id, key, scancode, action, mods) -> {
            Input.listenKeys(id, key, scancode, action, mods);
            WindowEvents.onKeyType.call(key, action);
        });

        GLFW.glfwSetCursorPosCallback(windowId, (id, x, y) -> {
            Input.mouseX = (float) x;
            Input.mouseY = (float) y;
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
            GLFW.glfwSetWindowPos(windowId, centerX, centerY);
        }

    }

    private void setupGLFW() {
        GLFW.glfwMakeContextCurrent(windowId);
        GLFW.glfwSwapInterval(vsync ? 1 : 0);
        GLFW.glfwShowWindow(windowId);
        // TODO: Add callbacks
        // TODO: Init audio
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
    }

    private void initialize() {
        System.out.println("Initializing...");
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        System.out.println("Setting hints");
        setWindowHints();
        System.out.println("Creating basic window");
        createWindow();
        System.out.println("Setting up callbacks");
        setupCallbacks();
        System.out.println("Setting up GLFW");
        setupGLFW();
        System.out.println("Setting up ALC");
        setupALC();
        System.out.println("Setting up drawing");
        setupDrawing();

        System.out.println("Loading Atlas files");
        Atlas.loadAtlas();
    }

    private void destroy() {
        Callbacks.glfwFreeCallbacks(windowId);
        GLFW.glfwDestroyWindow(windowId);
        GLFW.glfwSetErrorCallback(null).free();
        GLFW.glfwTerminate();
    }

    private void updateWindow() {
        Input.update();
        GLFW.glfwPollEvents();
        SceneManager.current().update();
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

    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public Vector2f getWindowSize() {
        return new Vector2f(windowWidth, windowHeight);
    }
}
