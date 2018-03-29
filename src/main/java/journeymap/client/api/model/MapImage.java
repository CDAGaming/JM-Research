package journeymap.client.api.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.gson.annotations.Since;
import journeymap.client.api.display.Displayable;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;

public final class MapImage {
    @Since(1.1)
    private transient BufferedImage image;
    @Since(1.1)
    private ResourceLocation imageLocation;
    @Since(1.1)
    private Integer color;
    @Since(1.1)
    private Float opacity;
    @Since(1.1)
    private Integer textureX;
    @Since(1.1)
    private Integer textureY;
    @Since(1.1)
    private Integer textureWidth;
    @Since(1.1)
    private Integer textureHeight;
    @Since(1.1)
    private Integer rotation;
    @Since(1.1)
    private Double displayWidth;
    @Since(1.1)
    private Double displayHeight;
    @Since(1.1)
    private Double anchorX;
    @Since(1.1)
    private Double anchorY;

    public MapImage(final BufferedImage image) {
        this(image, 0, 0, image.getWidth(), image.getHeight(), 16777215, 1.0f);
    }

    public MapImage(final BufferedImage image, final int textureX, final int textureY, final int textureWidth, final int textureHeight, final int color, final float opacity) {
        this.color = 16777215;
        this.opacity = 1.0f;
        this.textureX = 0;
        this.textureY = 0;
        this.image = image;
        this.textureX = textureX;
        this.textureY = textureY;
        this.textureWidth = Math.max(1, textureWidth);
        this.textureHeight = Math.max(1, textureHeight);
        this.setDisplayWidth(this.textureWidth);
        this.setDisplayHeight(this.textureHeight);
        this.setColor(color);
        this.setOpacity(opacity);
    }

    public MapImage(final ResourceLocation imageLocation, final int textureWidth, final int textureHeight) {
        this(imageLocation, 0, 0, textureWidth, textureHeight, 16777215, 1.0f);
    }

    public MapImage(final ResourceLocation imageLocation, final int textureX, final int textureY, final int textureWidth, final int textureHeight, final int color, final float opacity) {
        this.color = 16777215;
        this.opacity = 1.0f;
        this.textureX = 0;
        this.textureY = 0;
        this.imageLocation = imageLocation;
        this.textureX = textureX;
        this.textureY = textureY;
        this.textureWidth = Math.max(1, textureWidth);
        this.textureHeight = Math.max(1, textureHeight);
        this.setDisplayWidth(this.textureWidth);
        this.setDisplayHeight(this.textureHeight);
        this.setColor(color);
        this.setOpacity(opacity);
    }

    public int getColor() {
        return this.color;
    }

    public MapImage setColor(final int color) {
        this.color = Displayable.clampRGB(color);
        return this;
    }

    public float getOpacity() {
        return this.opacity;
    }

    public MapImage setOpacity(final float opacity) {
        this.opacity = Displayable.clampOpacity(opacity);
        return this;
    }

    public int getTextureX() {
        return this.textureX;
    }

    public int getTextureY() {
        return this.textureY;
    }

    public double getAnchorX() {
        return this.anchorX;
    }

    public MapImage setAnchorX(final double anchorX) {
        this.anchorX = anchorX;
        return this;
    }

    public double getAnchorY() {
        return this.anchorY;
    }

    public MapImage setAnchorY(final double anchorY) {
        this.anchorY = anchorY;
        return this;
    }

    public MapImage centerAnchors() {
        this.setAnchorX(this.displayWidth / 2.0);
        this.setAnchorY(this.displayHeight / 2.0);
        return this;
    }

    public int getTextureWidth() {
        return this.textureWidth;
    }

    public int getTextureHeight() {
        return this.textureHeight;
    }

    @Nullable
    public ResourceLocation getImageLocation() {
        return this.imageLocation;
    }

    @Nullable
    public BufferedImage getImage() {
        return this.image;
    }

    public int getRotation() {
        return this.rotation;
    }

    public MapImage setRotation(final int rotation) {
        this.rotation = rotation % 360;
        return this;
    }

    public double getDisplayWidth() {
        return this.displayWidth;
    }

    public MapImage setDisplayWidth(final double displayWidth) {
        this.displayWidth = displayWidth;
        return this;
    }

    public double getDisplayHeight() {
        return this.displayHeight;
    }

    public MapImage setDisplayHeight(final double displayHeight) {
        this.displayHeight = displayHeight;
        return this;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final MapImage mapImage = (MapImage) o;
        return Objects.equal(this.color, mapImage.color) && Objects.equal(this.opacity, mapImage.opacity) && Objects.equal(this.anchorX, mapImage.anchorX) && Objects.equal(this.anchorY, mapImage.anchorY) && Objects.equal(this.textureX, mapImage.textureX) && Objects.equal(this.textureY, mapImage.textureY) && Objects.equal(this.textureWidth, mapImage.textureWidth) && Objects.equal(this.textureHeight, mapImage.textureHeight) && Objects.equal(this.imageLocation, mapImage.imageLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.imageLocation, this.color, this.opacity, this.anchorX, this.anchorY, this.textureX, this.textureY, this.textureWidth, this.textureHeight);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("imageLocation", this.imageLocation).add("anchorX", this.anchorX).add("anchorY", this.anchorY).add("color", this.color).add("textureHeight", this.textureHeight).add("opacity", this.opacity).add("textureX", this.textureX).add("textureY", this.textureY).add("textureWidth", this.textureWidth).toString();
    }
}
