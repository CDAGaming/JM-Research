package journeymap.client.api.model;

import java.util.*;
import journeymap.client.api.display.*;
import journeymap.client.api.util.*;
import com.google.common.base.*;

public class TextProperties
{
    protected EnumSet<Context.UI> activeUIs;
    protected EnumSet<Context.MapType> activeMapTypes;
    protected float scale;
    protected int color;
    protected int backgroundColor;
    protected float opacity;
    protected float backgroundOpacity;
    protected boolean fontShadow;
    protected int minZoom;
    protected int maxZoom;
    protected int offsetX;
    protected int offsetY;
    
    public TextProperties() {
        this.activeUIs = EnumSet.of(Context.UI.Any);
        this.activeMapTypes = EnumSet.of(Context.MapType.Any);
        this.scale = 1.0f;
        this.color = 16777215;
        this.backgroundColor = 0;
        this.opacity = 1.0f;
        this.backgroundOpacity = 0.5f;
        this.fontShadow = true;
        this.minZoom = 0;
        this.maxZoom = 8;
        this.offsetX = 0;
        this.offsetY = 0;
    }
    
    public float getScale() {
        return this.scale;
    }
    
    public TextProperties setScale(final float scale) {
        this.scale = Math.max(1.0f, Math.min(scale, 8.0f));
        return this;
    }
    
    public int getColor() {
        return this.color;
    }
    
    public TextProperties setColor(final int color) {
        this.color = Displayable.clampRGB(color);
        return this;
    }
    
    public int getBackgroundColor() {
        return this.backgroundColor;
    }
    
    public TextProperties setBackgroundColor(final int backgroundColor) {
        this.backgroundColor = Displayable.clampRGB(backgroundColor);
        return this;
    }
    
    public float getOpacity() {
        return this.opacity;
    }
    
    public TextProperties setOpacity(final float opacity) {
        this.opacity = Displayable.clampOpacity(opacity);
        return this;
    }
    
    public float getBackgroundOpacity() {
        return this.backgroundOpacity;
    }
    
    public TextProperties setBackgroundOpacity(final float backgroundOpacity) {
        this.backgroundOpacity = Displayable.clampOpacity(backgroundOpacity);
        return this;
    }
    
    public boolean hasFontShadow() {
        return this.fontShadow;
    }
    
    public TextProperties setFontShadow(final boolean fontShadow) {
        this.fontShadow = fontShadow;
        return this;
    }
    
    public EnumSet<Context.UI> getActiveUIs() {
        return this.activeUIs;
    }
    
    public TextProperties setActiveUIs(EnumSet<Context.UI> activeUIs) {
        if (activeUIs.contains(Context.UI.Any)) {
            activeUIs = EnumSet.of(Context.UI.Any);
        }
        this.activeUIs = activeUIs;
        return this;
    }
    
    public EnumSet<Context.MapType> getActiveMapTypes() {
        return this.activeMapTypes;
    }
    
    public TextProperties setActiveMapTypes(EnumSet<Context.MapType> activeMapTypes) {
        if (activeMapTypes.contains(Context.MapType.Any)) {
            activeMapTypes = EnumSet.of(Context.MapType.Any);
        }
        this.activeMapTypes = activeMapTypes;
        return this;
    }
    
    public boolean isActiveIn(final UIState uiState) {
        return uiState.active && (this.activeUIs.contains(Context.UI.Any) || this.activeUIs.contains(uiState.ui)) && (this.activeMapTypes.contains(Context.MapType.Any) || this.activeMapTypes.contains(uiState.mapType)) && this.minZoom <= uiState.zoom && this.maxZoom >= uiState.zoom;
    }
    
    public int getMinZoom() {
        return this.minZoom;
    }
    
    public TextProperties setMinZoom(final int minZoom) {
        this.minZoom = Math.max(0, minZoom);
        return this;
    }
    
    public int getMaxZoom() {
        return this.maxZoom;
    }
    
    public TextProperties setMaxZoom(final int maxZoom) {
        this.maxZoom = Math.min(8, maxZoom);
        return this;
    }
    
    public int getOffsetX() {
        return this.offsetX;
    }
    
    public TextProperties setOffsetX(final int offsetX) {
        this.offsetX = offsetX;
        return this;
    }
    
    public int getOffsetY() {
        return this.offsetY;
    }
    
    public TextProperties setOffsetY(final int offsetY) {
        this.offsetY = offsetY;
        return this;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("activeMapTypes", (Object)this.activeMapTypes).add("activeUIs", (Object)this.activeUIs).add("backgroundColor", this.backgroundColor).add("backgroundOpacity", this.backgroundOpacity).add("color", this.color).add("opacity", this.opacity).add("fontShadow", this.fontShadow).add("maxZoom", this.maxZoom).add("minZoom", this.minZoom).add("offsetX", this.offsetX).add("offsetY", this.offsetY).add("scale", this.scale).toString();
    }
}
