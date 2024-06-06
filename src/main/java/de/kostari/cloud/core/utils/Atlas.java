package de.kostari.cloud.core.utils;

import java.util.ArrayList;

import de.kostari.cloud.core.utils.audio.Audio;
import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.utils.render.font.Font;

public class Atlas {

    private static ArrayList<Texture> textures = new ArrayList<>();
    private static ArrayList<Audio> audios = new ArrayList<>();
    private static ArrayList<Font> fonts = new ArrayList<>();

    private static boolean hasLoaded = false;

    /**
     * Add a texture to the Atlas that will be loaded when the engine is started
     * 
     * @param texture
     * @return
     */
    public static Texture addTexture(Texture texture) {
        Atlas.textures.add(texture);
        return texture;
    }

    /**
     * Add a texture to the Atlas that will be loaded when the engine is started
     * 
     * @param path
     * @return
     */
    public static Texture addTexture(String path) {
        Texture texture = new Texture(path);
        Atlas.textures.add(texture);
        return texture;
    }

    /**
     * Add an audio to the Atlas that will be loaded when the engine is started
     * 
     * @param audio
     * @return
     */
    public static Audio addAudio(Audio audio) {
        Atlas.audios.add(audio);
        return audio;
    }

    /**
     * Add an audio to the Atlas that will be loaded when the engine is started
     * 
     * @param path
     * @return
     */
    public static Audio addAudio(String path) {
        Audio audio = new Audio(path);
        Atlas.audios.add(audio);
        return audio;
    }

    /**
     * Add a font to the Atlas that will be loaded when the engine is started
     * 
     * @param font
     * @return
     */
    public static Font addFont(Font font) {
        Atlas.fonts.add(font);
        return font;
    }

    /**
     * Add a font to the Atlas that will be loaded when the engine is started
     * 
     * @return
     */
    public static Font addFont() {
        Font font = new Font();
        fonts.add(font);
        return font;
    }

    /**
     * Will load all the textures, audios and fonts that have been added to the
     * Atlas
     * DO NOT CALL THIS METHOD YOURSELF! IT WILL BE CALLED BY THE ENGINE
     * AUTOMATICALLY!
     */
    public static void loadAtlas() {
        if (!hasLoaded) {
            hasLoaded = true;
        } else {
            System.out.println("Files already have been loaded by the engine! Please don't load textures yourself!");
        }
        Atlas.textures.forEach(tex -> {
            tex.load();
        });
        Atlas.audios.forEach(aud -> {
            aud.load();
        });
        System.out.println("Loading " + fonts.size() + " fonts");
        Atlas.fonts.forEach(fon -> {
            fon.create();
            System.out.println(fon.toString());
        });
    }

}
