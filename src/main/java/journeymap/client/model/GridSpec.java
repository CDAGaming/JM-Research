package journeymap.client.model;

import journeymap.client.Constants;
import journeymap.client.cartography.color.RGB;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class GridSpec {
    public final Style style;
    public final float red;
    public final float green;
    public final float blue;
    public final float alpha;
    private int colorX;
    private int colorY;
    private transient TextureImpl texture;

    public GridSpec(final Style style, final Color color, float alpha) {
        this.colorX = -1;
        this.colorY = -1;
        this.texture = null;
        this.style = style;
        final float[] rgb = RGB.floats(color.getRGB());
        this.red = rgb[0];
        this.green = rgb[1];
        this.blue = rgb[2];
        if (alpha < 0.0f) {
            alpha = 0.0f;
        }
        while (alpha > 1.0f) {
            alpha /= 100.0f;
        }
        this.alpha = alpha;
        assert alpha <= 1.0f;
    }

    public GridSpec(final Style style, final float red, final float green, final float blue, final float alpha) {
        this.colorX = -1;
        this.colorY = -1;
        this.texture = null;
        this.style = style;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        assert alpha <= 1.0f;
    }

    public GridSpec setColorCoords(final int x, final int y) {
        this.colorX = x;
        this.colorY = y;
        return this;
    }

    public void beginTexture(final int textureFilter, final int textureWrap, final float mapAlpha) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(this.getTexture().getGlTextureId());
        GlStateManager.color(this.red, this.green, this.blue, this.alpha * mapAlpha);
        GL11.glTexParameteri(3553, 10241, textureFilter);
        GL11.glTexParameteri(3553, 10240, textureFilter);
        GL11.glTexParameteri(3553, 10242, textureWrap);
        GL11.glTexParameteri(3553, 10243, textureWrap);
    }

    public TextureImpl getTexture() {
        if (this.texture == null || this.texture.isDefunct()) {
            this.texture = TextureCache.getTexture(this.style.textureLocation);
        }
        return this.texture;
    }

    public GridSpec clone() {
        return new GridSpec(this.style, this.red, this.green, this.blue, this.alpha).setColorCoords(this.colorX, this.colorY);
    }

    public void finishTexture() {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.clearColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public Integer getColor() {
        return RGB.toInteger(this.red, this.green, this.blue);
    }

    public int getColorX() {
        return this.colorX;
    }

    public int getColorY() {
        return this.colorY;
    }

    public enum Style {
        Squares("jm.common.grid_style_squares", TextureCache.GridSquares),
        Dots("jm.common.grid_style_dots", TextureCache.GridDots),
        Checkers("jm.common.grid_style_checkers", TextureCache.GridCheckers);

        private final String key;
        private final ResourceLocation textureLocation;

        private Style(final String key, final ResourceLocation textureLocation) {
            this.key = key;
            this.textureLocation = textureLocation;
        }

        public String displayName() {
            return Constants.getString(this.key);
        }
    }
}
