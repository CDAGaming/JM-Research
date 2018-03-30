package journeymap.client.api.model;

import java.awt.image.*;
import com.google.gson.annotations.*;
import net.minecraft.util.*;
import journeymap.client.api.display.*;
import javax.annotation.*;
import com.google.common.base.*;

public final class MapImage
{
    public static final int DEFAULT_COLOR = 16777215;
    public static final float DEFAULT_OPACITY = 1.0f;
    public static final int DEFAUlT_TEXTURE_X = 0;
    public static final int DEFAUlT_TEXTURE_Y = 0;
    public static final int DEFAULT_TEXTURE_WIDTH = 1;
    public static final int DEFAULT_ROTATION = 0;
    public static final int DEFAULT_TEXTURE_HEIGHT = 1;
    public static final int DEFAULT_BACKGROUND_COLOR = 0;
    public static final float DEFAULT_BACKGROUND_OPACITY = 0.7f;
    public static final int DEFAUlT_ANCHOR_X = 0;
    public static final int DEFAUlT_ANCHOR_Y = 0;
    public static final int DEFAULT_DISPLAY_WIDTH = 0;
    public static final int DEFAULT_DISPLAY_HEIGHT = 0;
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
    
    public MapImage() {
        this.color = 16777215;
        this.opacity = 1.0f;
        this.textureX = 0;
        this.textureY = 0;
    }
    
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
    
    public MapImage(final MapImage other) {
        this.color = 16777215;
        this.opacity = 1.0f;
        this.textureX = 0;
        this.textureY = 0;
        this.image = other.image;
        this.imageLocation = other.imageLocation;
        this.color = other.color;
        this.opacity = other.opacity;
        this.textureX = other.textureX;
        this.textureY = other.textureY;
        this.textureWidth = other.textureWidth;
        this.textureHeight = other.textureHeight;
        this.rotation = other.rotation;
        this.displayWidth = other.displayWidth;
        this.displayHeight = other.displayHeight;
        this.anchorX = other.anchorX;
        this.anchorY = other.anchorY;
    }
    
    public int getColor() {
        return (this.color == null) ? 16777215 : this.color;
    }
    
    public MapImage setColor(final int color) {
        this.color = Displayable.clampRGB(color);
        return this;
    }
    
    public float getOpacity() {
        return (this.opacity == null) ? 1.0f : this.opacity;
    }
    
    public MapImage setOpacity(final float opacity) {
        this.opacity = Displayable.clampOpacity(opacity);
        return this;
    }
    
    public int getTextureX() {
        return (this.textureX == null) ? 0 : this.textureX;
    }
    
    public int getTextureY() {
        return (this.textureY == null) ? 0 : this.textureY;
    }
    
    public double getAnchorX() {
        return (this.anchorX == null) ? 0.0 : this.anchorX;
    }
    
    public MapImage setAnchorX(final double anchorX) {
        this.anchorX = anchorX;
        return this;
    }
    
    public double getAnchorY() {
        return (this.anchorY == null) ? 0.0 : this.anchorY;
    }
    
    public MapImage setAnchorY(final double anchorY) {
        this.anchorY = anchorY;
        return this;
    }
    
    public MapImage centerAnchors() {
        this.setAnchorX(this.getDisplayWidth() / 2.0);
        this.setAnchorY(this.getDisplayHeight() / 2.0);
        return this;
    }
    
    public int getTextureWidth() {
        return (this.textureWidth == null) ? 1 : this.textureWidth;
    }
    
    public int getTextureHeight() {
        return (this.textureHeight == null) ? 1 : this.textureHeight;
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
        return (this.rotation == null) ? 0 : this.rotation;
    }
    
    public MapImage setRotation(final int rotation) {
        this.rotation = rotation % 360;
        return this;
    }
    
    public double getDisplayWidth() {
        return (this.displayWidth == null) ? 0.0 : this.displayWidth;
    }
    
    public MapImage setDisplayWidth(final double displayWidth) {
        this.displayWidth = displayWidth;
        return this;
    }
    
    public double getDisplayHeight() {
        return (this.displayHeight == null) ? 0.0 : this.displayHeight;
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
        final MapImage mapImage = (MapImage)o;
        return Objects.equal((Object)this.color, (Object)mapImage.color) && Objects.equal((Object)this.opacity, (Object)mapImage.opacity) && Objects.equal((Object)this.anchorX, (Object)mapImage.anchorX) && Objects.equal((Object)this.anchorY, (Object)mapImage.anchorY) && Objects.equal((Object)this.textureX, (Object)mapImage.textureX) && Objects.equal((Object)this.textureY, (Object)mapImage.textureY) && Objects.equal((Object)this.textureWidth, (Object)mapImage.textureWidth) && Objects.equal((Object)this.textureHeight, (Object)mapImage.textureHeight) && Objects.equal((Object)this.imageLocation, (Object)mapImage.imageLocation);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(new Object[] { this.imageLocation, this.color, this.opacity, this.anchorX, this.anchorY, this.textureX, this.textureY, this.textureWidth, this.textureHeight });
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("imageLocation", (Object)this.imageLocation).add("anchorX", (Object)this.anchorX).add("anchorY", (Object)this.anchorY).add("color", (Object)this.color).add("textureHeight", (Object)this.textureHeight).add("opacity", (Object)this.opacity).add("textureX", (Object)this.textureX).add("textureY", (Object)this.textureY).add("textureWidth", (Object)this.textureWidth).toString();
    }
}
