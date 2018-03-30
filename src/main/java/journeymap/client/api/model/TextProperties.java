package journeymap.client.api.model;

import com.google.common.base.MoreObjects;
import journeymap.client.api.util.UIState;
import journeymap.common.api.feature.Feature;

import java.util.EnumSet;

public class TextProperties extends MapText<TextProperties> {
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
        return MoreObjects.toStringHelper(this).add("activeMapTypes", this.activeMapTypes).add("activeUIs", this.activeUIs).add("backgroundColor", this.backgroundColor).add("backgroundOpacity", this.backgroundOpacity).add("color", this.color).add("opacity", this.opacity).add("fontShadow", this.fontShadow).add("maxZoom", this.maxZoom).add("minZoom", this.minZoom).add("offsetX", this.offsetX).add("offsetY", this.offsetY).add("scale", this.scale).toString();
    }
}
