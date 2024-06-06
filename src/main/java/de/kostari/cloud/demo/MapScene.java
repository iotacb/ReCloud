package de.kostari.cloud.demo;

import de.kostari.cloud.core.scene.Scene;
import de.kostari.cloud.core.utils.Atlas;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.window.Window;
import de.kostari.cloud.demo.editor.EditorOverlay;

public class MapScene extends Scene {

    private Texture mapTexture;
    private EditorOverlay editorOverlay;

    @Override
    public void init() {
        this.mapTexture = Atlas.addTexture("map.png");
        this.editorOverlay = new EditorOverlay();
        super.init();
    }

    @Override
    public void draw() {
        Render.drawTexture(0, 0, Window.instance.getWindowWidth(), Window.instance.getWindowHeight(),
                mapTexture.getTextureId());
        editorOverlay.draw();
        super.draw();
    }

    @Override
    public void update() {
        editorOverlay.update();
        super.update();
    }

}
