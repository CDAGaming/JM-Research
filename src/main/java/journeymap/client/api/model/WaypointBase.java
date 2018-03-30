package journeymap.client.api.model;

import journeymap.client.api.display.*;
import com.google.gson.annotations.*;
import com.google.common.base.*;
import java.util.*;
import java.util.function.*;
import javax.annotation.*;

public abstract class WaypointBase<T extends WaypointBase> extends Displayable implements IWaypointDisplay
{
    @Since(1.4)
    protected String name;
    @Since(1.4)
    protected MapImage icon;
    @Since(1.6)
    protected MapText label;
    @Since(1.4)
    protected HashSet<Integer> displayDims;
    @Since(1.4)
    protected transient boolean dirty;
    
    protected WaypointBase() {
    }
    
    protected WaypointBase(final String modId, final String name) {
        super(modId);
        this.setName(name);
    }
    
    protected WaypointBase(final String modId, final String id, final String name) {
        super(modId, id);
        this.setName(name);
    }
    
    protected abstract IWaypointDisplay getDelegate();
    
    protected abstract boolean hasDelegate();
    
    public final String getName() {
        return this.name;
    }
    
    public final T setName(final String name) {
        if (Strings.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("name may not be blank");
        }
        this.name = name;
        return this.setDirty();
    }
    
    @Override
    public final MapText getLabel() {
        if (this.label == null && this.hasDelegate()) {
            return this.getDelegate().getLabel();
        }
        return this.label;
    }
    
    public final int getOrDefaultIconColor(final int defaultRgb) {
        final MapImage icon = this.getIcon();
        return (icon == null) ? defaultRgb : icon.getColor();
    }
    
    public final T setIconColor(final int rgb) {
        MapImage mapImage = this.getIcon();
        if (mapImage == null) {
            mapImage = new MapImage();
            this.setIcon(mapImage);
        }
        mapImage.setColor(rgb);
        return this.setDirty();
    }
    
    public final int getOrDefaultLabelColor(final int defaultRgb) {
        final MapText label = this.getLabel();
        return (label == null) ? defaultRgb : label.getColor();
    }
    
    public final T setLabelColor(final int rgb) {
        MapText mapText = this.getLabel();
        if (mapText == null) {
            mapText = new MapText();
            this.setLabel(mapText);
        }
        mapText.setColor(rgb);
        return this.setDirty();
    }
    
    public final T setLabel(final int color, final float opacity) {
        return this.setLabel(new MapText<MapText>().setColor(color).setOpacity(opacity));
    }
    
    public final T setLabel(final MapText label) {
        this.label = label;
        return this.setDirty();
    }
    
    public final T clearLabel() {
        this.label = null;
        return this.setDirty();
    }
    
    @Override
    public Set<Integer> getDisplayDimensions() {
        if (this.displayDims != null) {
            return new HashSet<Integer>(this.displayDims);
        }
        if (this.hasDelegate()) {
            return this.getDelegate().getDisplayDimensions();
        }
        return Collections.emptySet();
    }
    
    public final T setDisplayDimensions(final Integer... dimensions) {
        return this.setDisplayDimensions(Arrays.asList(dimensions));
    }
    
    public final T setDisplayDimensions(final Collection<Integer> dimensions) {
        HashSet<Integer> temp = null;
        if (dimensions != null && dimensions.size() > 0) {
            temp = new HashSet<Integer>(dimensions.size());
            dimensions.stream().filter(Objects::nonNull).forEach(temp::add);
            if (temp.size() == 0) {
                temp = null;
            }
        }
        this.displayDims = temp;
        return this.setDirty();
    }
    
    public final T clearDisplayDimensions() {
        this.displayDims = null;
        return this.setDirty();
    }
    
    public void setDisplayed(final int dimension, final boolean displayed) {
        final Set<Integer> dimSet = this.getDisplayDimensions();
        if (displayed && dimSet.add(dimension)) {
            this.setDisplayDimensions(dimSet);
        }
        else if (!displayed && dimSet.remove(dimension)) {
            this.setDisplayDimensions(dimSet);
        }
    }
    
    public final boolean isDisplayed(final int dimension) {
        return this.getDisplayDimensions().contains(dimension);
    }
    
    @Override
    public MapImage getIcon() {
        if (this.icon == null && this.hasDelegate()) {
            return this.getDelegate().getIcon();
        }
        return this.icon;
    }
    
    public final T setIcon(@Nullable final MapImage icon) {
        this.icon = icon;
        return this.setDirty();
    }
    
    public final T clearIcon() {
        this.icon = null;
        return this.setDirty();
    }
    
    public boolean isDirty() {
        return this.dirty;
    }
    
    public T setDirty(final boolean dirty) {
        this.dirty = dirty;
        return (T)this;
    }
    
    public T setDirty() {
        return this.setDirty(true);
    }
    
    public boolean hasIcon() {
        return this.icon != null;
    }
    
    public boolean hasColor() {
        return this.getLabel() != null;
    }
    
    public boolean hasBackgroundColor() {
        return this.getLabel() != null;
    }
    
    public boolean hasDisplayDimensions() {
        return this.displayDims != null;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WaypointBase)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final WaypointBase<?> that = (WaypointBase<?>)o;
        return com.google.common.base.Objects.equal((Object)this.getGuid(), (Object)that.getGuid()) && com.google.common.base.Objects.equal((Object)this.getIcon(), (Object)that.getIcon()) && com.google.common.base.Objects.equal((Object)this.getLabel(), (Object)that.getLabel()) && Arrays.equals(this.getDisplayDimensions().toArray(), that.getDisplayDimensions().toArray());
    }
}
