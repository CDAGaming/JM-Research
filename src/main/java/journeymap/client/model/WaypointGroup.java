package journeymap.client.model;

import javax.annotation.*;
import com.google.gson.annotations.*;
import journeymap.client.cartography.color.*;
import com.google.common.base.*;
import journeymap.client.waypoint.*;
import journeymap.client.*;
import com.google.gson.*;

@ParametersAreNonnullByDefault
public class WaypointGroup implements Comparable<WaypointGroup>
{
    public static final WaypointGroup DEFAULT;
    public static final double VERSION = 5.2;
    public static final Gson GSON;
    @Since(5.2)
    protected String name;
    @Since(5.2)
    protected String origin;
    @Since(5.2)
    protected String icon;
    @Since(5.2)
    protected String color;
    @Since(5.2)
    protected boolean enable;
    @Since(5.2)
    protected int order;
    protected transient boolean dirty;
    protected transient Integer colorInt;
    
    public WaypointGroup(final String origin, final String name) {
        this.setOrigin(origin).setName(name);
    }
    
    public String getName() {
        return this.name;
    }
    
    public WaypointGroup setName(final String name) {
        this.name = name;
        return this.setDirty();
    }
    
    public String getOrigin() {
        return this.origin;
    }
    
    public WaypointGroup setOrigin(final String origin) {
        this.origin = origin;
        return this.setDirty();
    }
    
    public String getIcon() {
        return this.icon;
    }
    
    public WaypointGroup setIcon(final String icon) {
        this.icon = icon;
        return this.setDirty();
    }
    
    public int getColor() {
        if (this.colorInt == null) {
            if (this.color == null) {
                this.color = RGB.toHexString(RGB.randomColor());
            }
            this.colorInt = RGB.hexToInt(this.color);
        }
        return this.colorInt;
    }
    
    public WaypointGroup setColor(final String color) {
        this.colorInt = RGB.hexToInt(color);
        this.color = RGB.toHexString(this.colorInt);
        return this.setDirty();
    }
    
    public WaypointGroup setColor(final int color) {
        this.color = RGB.toHexString(color);
        this.colorInt = color;
        return this.setDirty();
    }
    
    public boolean isEnable() {
        return this.enable;
    }
    
    public WaypointGroup setEnable(final boolean enable) {
        this.enable = enable;
        return this.setDirty();
    }
    
    public boolean isDirty() {
        return this.dirty;
    }
    
    public WaypointGroup setDirty() {
        return this.setDirty(true);
    }
    
    public WaypointGroup setDirty(final boolean dirty) {
        this.dirty = dirty;
        return this;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final WaypointGroup group = (WaypointGroup)o;
        return this.name.equals(group.name) && this.origin.equals(group.origin);
    }
    
    @Override
    public int hashCode() {
        int result = this.name.hashCode();
        result = 31 * result + this.origin.hashCode();
        return result;
    }
    
    @Override
    public int compareTo(final WaypointGroup o) {
        int result = Integer.compare(this.order, o.order);
        if (result == 0) {
            result = this.name.compareTo(o.name);
        }
        return result;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("name", (Object)this.name).add("origin", (Object)this.origin).toString();
    }
    
    public String getKey() {
        return String.format("%s:%s", this.origin, this.name);
    }
    
    public static WaypointGroup getNamedGroup(final String origin, final String groupName) {
        return WaypointGroupStore.INSTANCE.get(origin, groupName);
    }
    
    static {
        DEFAULT = new WaypointGroup("journeymap", Constants.getString("jm.config.category.waypoint")).setEnable(true);
        GSON = new GsonBuilder().setVersion(5.2).create();
    }
}
