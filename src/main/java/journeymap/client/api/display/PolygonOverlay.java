package journeymap.client.api.display;

import journeymap.client.api.model.*;
import javax.annotation.*;
import java.util.*;

@ParametersAreNonnullByDefault
public final class PolygonOverlay extends Overlay
{
    private MapPolygon outerArea;
    private List<MapPolygon> holes;
    private ShapeProperties shapeProperties;
    
    public PolygonOverlay(final String modId, final String displayId, final int dimension, final ShapeProperties shapeProperties, final MapPolygon outerArea) {
        this(modId, displayId, dimension, shapeProperties, outerArea, null);
    }
    
    public PolygonOverlay(final String modId, final String displayId, final int dimension, final ShapeProperties shapeProperties, final MapPolygon outerArea, @Nullable final List<MapPolygon> holes) {
        super(modId, displayId);
        this.setDimension(dimension);
        this.setShapeProperties(shapeProperties);
        this.setOuterArea(outerArea);
        this.setHoles(holes);
    }
    
    public MapPolygon getOuterArea() {
        return this.outerArea;
    }
    
    public PolygonOverlay setOuterArea(final MapPolygon outerArea) {
        this.outerArea = outerArea;
        return this;
    }
    
    public List<MapPolygon> getHoles() {
        return this.holes;
    }
    
    public PolygonOverlay setHoles(@Nullable final List<MapPolygon> holes) {
        if (holes == null) {
            this.holes = null;
        }
        else {
            this.holes = new ArrayList<MapPolygon>(holes);
        }
        return this;
    }
    
    public ShapeProperties getShapeProperties() {
        return this.shapeProperties;
    }
    
    public PolygonOverlay setShapeProperties(final ShapeProperties shapeProperties) {
        this.shapeProperties = shapeProperties;
        return this;
    }
    
    @Override
    public String toString() {
        return this.toStringHelper(this).add("holes", (Object)this.holes).add("outerArea", (Object)this.outerArea).add("shapeProperties", (Object)this.shapeProperties).toString();
    }
}
