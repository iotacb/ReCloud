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

    public static Texture addTexture(Texture texture) {
        Atlas.textures.add(texture);
        return texture;
    }

    public static Texture addTexture(String path) {
        Texture texture = new Texture(path);
        Atlas.textures.add(texture);
        return texture;
    }

    public static Audio addAudio(Audio audio) {
        Atlas.audios.add(audio);
        return audio;
    }

    public static Audio addAudio(String path) {
        Audio audio = new Audio(path);
        Atlas.audios.add(audio);
        return audio;
    }

    public static Font addFont(Font font) {
        Atlas.fonts.add(font);
        return font;
    }

    public static Font addFont() {
        Font font = new Font();
        fonts.add(font);
        return font;
    }

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
