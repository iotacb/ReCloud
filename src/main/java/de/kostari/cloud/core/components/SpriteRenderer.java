package de.kostari.cloud.core.components;

import de.kostari.cloud.core.utils.Atlas;
import de.kostari.cloud.core.utils.render.Render;
import de.kostari.cloud.core.utils.render.Texture;
import de.kostari.cloud.core.utils.types.Color4f;

public class SpriteRenderer extends Component {

    protected Color4f color;
    protected Texture texture;

    public SpriteRenderer(Color4f color, Texture texture) {
        this.color = color;
        this.texture = texture;
    }

    public SpriteRenderer(Texture texture) {
        this(new Color4f(1, 1, 1, 1), texture);
    }

    public SpriteRenderer(String path) {
        this.texture = new Texture(path);
        Atlas.addTexture(texture);
        this.color = new Color4f(1, 1, 1, 1);
    }

    @Override
    public void draw() {
        Render.color(color.r, color.g, color.b);
        Render.drawTexture(gameObject.transform.position.x,
                gameObject.transform.position.y,
                gameObject.transform.scale.x, gameObject.transform.scale.y,
                texture.getTextureId());
        super.draw();
    }

    public Texture getTexture() {
        return texture;
    }

}
