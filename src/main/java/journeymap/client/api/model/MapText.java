package journeymap.client.api.model;

import journeymap.client.api.display.*;
import com.google.common.base.*;

public class MapText<T extends MapText>
{
    public static float DEFAULT_SCALE;
    public static int DEFAULT_COLOR;
    public static float DEFAULT_OPACITY;
    public static int DEFAULT_BACKGROUND_COLOR;
    public static float DEFAULT_BACKGROUND_OPACITY;
    public static int DEFAULT_MIN_ZOOM;
    public static int DEFAULT_MAX_ZOOM;
    public static int DEFAULT_OFFSET_X;
    public static int DEFAULT_OFFSET_Y;
    public static boolean DEFAULT_FONT_SHADOW;
    protected Float scale;
    protected Integer color;
    protected Integer backgroundColor;
    protected Float opacity;
    protected Float backgroundOpacity;
    protected Boolean fontShadow;
    protected Integer minZoom;
    protected Integer maxZoom;
    protected Integer offsetX;
    protected Integer offsetY;
    
    public MapText() {
    }
    
    public MapText(final MapText other) {
        this.scale = other.scale;
        this.color = other.color;
        this.backgroundColor = other.backgroundColor;
        this.opacity = other.opacity;
        this.backgroundOpacity = other.backgroundOpacity;
        this.fontShadow = other.fontShadow;
        this.minZoom = other.minZoom;
        this.maxZoom = other.maxZoom;
        this.offsetX = other.offsetX;
        this.offsetY = other.offsetY;
    }
    
    public float getScale() {
        return (this.scale == null) ? MapText.DEFAULT_SCALE : this.scale;
    }
    
    public T setScale(final float scale) {
        this.scale = Math.max(1.0f, Math.min(scale, 8.0f));
        return (T)this;
    }
    
    public int getColor() {
        return (this.color == null) ? MapText.DEFAULT_COLOR : this.color;
    }
    
    public T setColor(final int color) {
        this.color = Displayable.clampRGB(color);
        return (T)this;
    }
    
    public int getBackgroundColor() {
        return (this.backgroundColor == null) ? MapText.DEFAULT_BACKGROUND_COLOR : this.backgroundColor;
    }
    
    public T setBackgroundColor(final int backgroundColor) {
        this.backgroundColor = Displayable.clampRGB(backgroundColor);
        return (T)this;
    }
    
    public float getOpacity() {
        return (this.opacity == null) ? MapText.DEFAULT_OPACITY : this.opacity;
    }
    
    public T setOpacity(final float opacity) {
        this.opacity = Displayable.clampOpacity(opacity);
        return (T)this;
    }
    
    public float getBackgroundOpacity() {
        return (this.backgroundOpacity == null) ? MapText.DEFAULT_BACKGROUND_OPACITY : this.backgroundOpacity;
    }
    
    public T setBackgroundOpacity(final float backgroundOpacity) {
        this.backgroundOpacity = Displayable.clampOpacity(backgroundOpacity);
        return (T)this;
    }
    
    public boolean hasFontShadow() {
        return (this.fontShadow == null) ? MapText.DEFAULT_FONT_SHADOW : this.fontShadow;
    }
    
    public T setFontShadow(final boolean fontShadow) {
        this.fontShadow = fontShadow;
        return (T)this;
    }
    
    public int getMinZoom() {
        return (this.minZoom == null) ? MapText.DEFAULT_MIN_ZOOM : this.minZoom;
    }
    
    public T setMinZoom(final int minZoom) {
        this.minZoom = Math.max(0, minZoom);
        return (T)this;
    }
    
    public int getMaxZoom() {
        return (this.maxZoom == null) ? MapText.DEFAULT_MAX_ZOOM : this.maxZoom;
    }
    
    public T setMaxZoom(final int maxZoom) {
        this.maxZoom = Math.min(8, maxZoom);
        return (T)this;
    }
    
    public int getOffsetX() {
        return (this.offsetX == null) ? MapText.DEFAULT_OFFSET_X : this.offsetX;
    }
    
    public T setOffsetX(final int offsetX) {
        this.offsetX = offsetX;
        return (T)this;
    }
    
    public int getOffsetY() {
        return (this.offsetY == null) ? MapText.DEFAULT_OFFSET_Y : this.offsetY;
    }
    
    public T setOffsetY(final int offsetY) {
        this.offsetY = offsetY;
        return (T)this;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("backgroundColor", (Object)this.backgroundColor).add("backgroundOpacity", (Object)this.backgroundOpacity).add("color", (Object)this.color).add("opacity", (Object)this.opacity).add("fontShadow", (Object)this.fontShadow).add("maxZoom", (Object)this.maxZoom).add("minZoom", (Object)this.minZoom).add("offsetX", (Object)this.offsetX).add("offsetY", (Object)this.offsetY).add("scale", (Object)this.scale).toString();
    }
    
    static {
        MapText.DEFAULT_SCALE = 1.0f;
        MapText.DEFAULT_COLOR = 16777215;
        MapText.DEFAULT_OPACITY = 1.0f;
        MapText.DEFAULT_BACKGROUND_COLOR = 0;
        MapText.DEFAULT_BACKGROUND_OPACITY = 0.7f;
        MapText.DEFAULT_MIN_ZOOM = 0;
        MapText.DEFAULT_MAX_ZOOM = 0;
        MapText.DEFAULT_OFFSET_X = 0;
        MapText.DEFAULT_OFFSET_Y = 0;
        MapText.DEFAULT_FONT_SHADOW = true;
    }
}
