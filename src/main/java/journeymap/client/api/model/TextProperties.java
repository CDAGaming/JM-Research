package journeymap.client.api.model;

import java.util.*;
import journeymap.common.api.feature.*;
import journeymap.client.api.util.*;
import com.google.common.base.*;

public class TextProperties extends MapText<TextProperties>
{
    protected EnumSet<Feature.Display> activeUIs;
    protected EnumSet<Feature.MapType> activeMapTypes;
    
    public TextProperties() {
        this.activeUIs = EnumSet.allOf(Feature.Display.class);
        this.activeMapTypes = EnumSet.allOf(Feature.MapType.class);
        this.activeUIs = EnumSet.allOf(Feature.Display.class);
        this.activeMapTypes = EnumSet.allOf(Feature.MapType.class);
    }
    
    public TextProperties(final TextProperties other) {
        super(other);
        this.activeUIs = EnumSet.allOf(Feature.Display.class);
        this.activeMapTypes = EnumSet.allOf(Feature.MapType.class);
        this.setActiveUIs(other.activeUIs);
        this.setActiveMapTypes(other.activeMapTypes);
    }
    
    public EnumSet<Feature.Display> getActiveUIs() {
        return this.activeUIs;
    }
    
    public TextProperties setActiveUIs(final EnumSet<Feature.Display> activeUIs) {
        this.activeUIs = EnumSet.copyOf(activeUIs);
        return this;
    }
    
    public EnumSet<Feature.MapType> getActiveMapTypes() {
        return this.activeMapTypes;
    }
    
    public TextProperties setActiveMapTypes(final EnumSet<Feature.MapType> activeMapTypes) {
        this.activeMapTypes = EnumSet.copyOf(activeMapTypes);
        return this;
    }
    
    public boolean isActiveIn(final UIState uiState) {
        return uiState.active && this.activeUIs.contains(uiState.ui) && this.activeMapTypes.contains(uiState.mapType) && this.getMinZoom() <= uiState.zoom && this.getMaxZoom() >= uiState.zoom;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("activeMapTypes", (Object)this.activeMapTypes).add("activeUIs", (Object)this.activeUIs).add("backgroundColor", (Object)this.backgroundColor).add("backgroundOpacity", (Object)this.backgroundOpacity).add("color", (Object)this.color).add("opacity", (Object)this.opacity).add("fontShadow", (Object)this.fontShadow).add("maxZoom", (Object)this.maxZoom).add("minZoom", (Object)this.minZoom).add("offsetX", (Object)this.offsetX).add("offsetY", (Object)this.offsetY).add("scale", (Object)this.scale).toString();
    }
}
