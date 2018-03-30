package journeymap.client.api.display;

import journeymap.common.api.feature.*;
import journeymap.client.api.model.*;
import javax.annotation.*;
import java.util.*;
import journeymap.client.api.util.*;
import com.google.common.base.*;

@ParametersAreNonnullByDefault
public abstract class Overlay extends Displayable
{
    protected String overlayGroupName;
    protected String title;
    protected String label;
    protected int dimension;
    protected int minZoom;
    protected int maxZoom;
    protected int displayOrder;
    protected EnumSet<Feature.Display> activeUIs;
    protected EnumSet<Feature.MapType> activeMapTypes;
    protected TextProperties textProperties;
    protected IOverlayListener overlayListener;
    protected boolean needsRerender;
    
    Overlay(final String modId, final String displayId) {
        super(modId, displayId);
        this.minZoom = 0;
        this.maxZoom = 8;
        this.activeUIs = EnumSet.allOf(Feature.Display.class);
        this.activeMapTypes = EnumSet.allOf(Feature.MapType.class);
        this.textProperties = new TextProperties();
        this.needsRerender = true;
    }
    
    public int getDimension() {
        return this.dimension;
    }
    
    public Overlay setDimension(final int dimension) {
        this.dimension = dimension;
        return this;
    }
    
    public String getOverlayGroupName() {
        return this.overlayGroupName;
    }
    
    public Overlay setOverlayGroupName(final String overlayGroupName) {
        this.overlayGroupName = overlayGroupName;
        return this;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public Overlay setTitle(@Nullable final String title) {
        this.title = title;
        return this;
    }
    
    public String getLabel() {
        return this.label;
    }
    
    public Overlay setLabel(@Nullable final String label) {
        this.label = label;
        return this;
    }
    
    public int getMinZoom() {
        return this.minZoom;
    }
    
    public Overlay setMinZoom(final int minZoom) {
        this.minZoom = Math.max(0, minZoom);
        return this;
    }
    
    public int getMaxZoom() {
        return this.maxZoom;
    }
    
    public Overlay setMaxZoom(final int maxZoom) {
        this.maxZoom = Math.min(8, maxZoom);
        return this;
    }
    
    @Override
    public int getDisplayOrder() {
        return this.displayOrder;
    }
    
    public Overlay setDisplayOrder(final int zIndex) {
        this.displayOrder = zIndex;
        return this;
    }
    
    public TextProperties getTextProperties() {
        return this.textProperties;
    }
    
    public Overlay setTextProperties(final TextProperties textProperties) {
        this.textProperties = textProperties;
        return this;
    }
    
    public EnumSet<Feature.Display> getActiveUIs() {
        return this.activeUIs;
    }
    
    public Overlay setActiveUIs(final EnumSet<Feature.Display> activeUIs) {
        (this.activeUIs = EnumSet.noneOf(Feature.Display.class)).addAll((Collection<?>)activeUIs);
        return this;
    }
    
    public EnumSet<Feature.MapType> getActiveMapTypes() {
        return this.activeMapTypes;
    }
    
    public Overlay setActiveMapTypes(final EnumSet<Feature.MapType> activeMapTypes) {
        (this.activeMapTypes = EnumSet.noneOf(Feature.MapType.class)).addAll((Collection<?>)activeMapTypes);
        return this;
    }
    
    public boolean isActiveIn(final UIState uiState) {
        return uiState.active && this.dimension == uiState.dimension && this.activeUIs.contains(uiState.ui) && this.activeMapTypes.contains(uiState.mapType) && this.minZoom <= uiState.zoom && this.maxZoom >= uiState.zoom;
    }
    
    public IOverlayListener getOverlayListener() {
        return this.overlayListener;
    }
    
    public Overlay setOverlayListener(@Nullable final IOverlayListener overlayListener) {
        this.overlayListener = overlayListener;
        return this;
    }
    
    public void flagForRerender() {
        this.needsRerender = true;
    }
    
    public void clearFlagForRerender() {
        this.needsRerender = false;
    }
    
    public boolean getNeedsRerender() {
        return this.needsRerender;
    }
    
    protected final MoreObjects.ToStringHelper toStringHelper(final Overlay instance) {
        return MoreObjects.toStringHelper((Object)this).add("label", (Object)this.label).add("title", (Object)this.title).add("overlayGroupName", (Object)this.overlayGroupName).add("activeMapTypes", (Object)this.activeMapTypes).add("activeUIs", (Object)this.activeUIs).add("dimension", this.dimension).add("displayOrder", this.displayOrder).add("maxZoom", this.maxZoom).add("minZoom", this.minZoom).add("textProperties", (Object)this.textProperties).add("hasOverlayListener", this.overlayListener != null);
    }
}
