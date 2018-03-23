package journeymap.client.api.model;

import journeymap.client.api.display.*;
import com.google.common.base.*;

public class ShapeProperties
{
    private int strokeColor;
    private int fillColor;
    private float strokeOpacity;
    private float fillOpacity;
    private float strokeWidth;
    
    public ShapeProperties() {
        this.strokeColor = 0;
        this.fillColor = 0;
        this.strokeOpacity = 1.0f;
        this.fillOpacity = 0.5f;
        this.strokeWidth = 2.0f;
    }
    
    public int getStrokeColor() {
        return this.strokeColor;
    }
    
    public ShapeProperties setStrokeColor(final int strokeColor) {
        this.strokeColor = Displayable.clampRGB(strokeColor);
        return this;
    }
    
    public int getFillColor() {
        return this.fillColor;
    }
    
    public ShapeProperties setFillColor(final int fillColor) {
        this.fillColor = Displayable.clampRGB(fillColor);
        return this;
    }
    
    public float getStrokeOpacity() {
        return this.strokeOpacity;
    }
    
    public ShapeProperties setStrokeOpacity(final float strokeOpacity) {
        this.strokeOpacity = Displayable.clampOpacity(strokeOpacity);
        return this;
    }
    
    public float getFillOpacity() {
        return this.fillOpacity;
    }
    
    public ShapeProperties setFillOpacity(final float fillOpacity) {
        this.fillOpacity = Displayable.clampOpacity(fillOpacity);
        return this;
    }
    
    public float getStrokeWidth() {
        return this.strokeWidth;
    }
    
    public ShapeProperties setStrokeWidth(final float strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShapeProperties)) {
            return false;
        }
        final ShapeProperties that = (ShapeProperties)o;
        return Objects.equal((Object)this.strokeColor, (Object)that.strokeColor) && Objects.equal((Object)this.fillColor, (Object)that.fillColor) && Objects.equal((Object)this.strokeOpacity, (Object)that.strokeOpacity) && Objects.equal((Object)this.fillOpacity, (Object)that.fillOpacity) && Objects.equal((Object)this.strokeWidth, (Object)that.strokeWidth);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(new Object[] { this.strokeColor, this.fillColor, this.strokeOpacity, this.fillOpacity, this.strokeWidth });
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("fillColor", this.fillColor).add("fillOpacity", this.fillOpacity).add("strokeColor", this.strokeColor).add("strokeOpacity", this.strokeOpacity).add("strokeWidth", this.strokeWidth).toString();
    }
}
