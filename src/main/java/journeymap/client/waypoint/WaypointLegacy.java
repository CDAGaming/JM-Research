package journeymap.client.waypoint;

import java.io.*;
import com.google.gson.annotations.*;
import java.awt.*;
import journeymap.common.*;
import journeymap.client.api.model.*;
import java.util.*;
import net.minecraft.util.*;
import journeymap.client.render.texture.*;
import net.minecraft.util.math.*;
import journeymap.client.api.display.*;
import journeymap.client.cartography.color.*;
import com.google.gson.*;

@Deprecated
public class WaypointLegacy implements Serializable
{
    public static final int VERSION = 3;
    public static final Gson GSON;
    protected static final String ICON_NORMAL = "waypoint-normal.png";
    protected static final String ICON_DEATH = "waypoint-death.png";
    @Since(1.0)
    protected String id;
    @Since(1.0)
    protected String name;
    @Since(3.0)
    protected String groupName;
    @Since(2.0)
    protected String displayId;
    @Since(1.0)
    protected String icon;
    @Since(1.0)
    protected int x;
    @Since(1.0)
    protected int y;
    @Since(1.0)
    protected int z;
    @Since(1.0)
    protected int r;
    @Since(1.0)
    protected int g;
    @Since(1.0)
    protected int b;
    @Since(1.0)
    protected boolean enable;
    @Since(1.0)
    protected Type type;
    @Since(1.0)
    protected String origin;
    @Since(1.0)
    protected TreeSet<Integer> dimensions;
    @Since(2.0)
    protected boolean persistent;
    
    public WaypointLegacy() {
    }
    
    public WaypointLegacy(final String name, final BlockPos pos, final Color color, final Type type, final Integer currentDimension) {
        this(name, pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p(), true, color.getRed(), color.getGreen(), color.getBlue(), type, "journeymap", currentDimension, Arrays.asList(currentDimension));
    }
    
    public WaypointLegacy(String name, final int x, final int y, final int z, final boolean enable, final int red, final int green, final int blue, final Type type, final String origin, final Integer currentDimension, Collection<Integer> dimensions) {
        if (name == null) {
            name = createName(x, z);
        }
        if (dimensions == null || dimensions.size() == 0) {
            dimensions = new TreeSet<Integer>();
            dimensions.add(Journeymap.clientWorld().field_73011_w.getDimension());
        }
        (this.dimensions = new TreeSet<Integer>(dimensions)).add(currentDimension);
        this.name = name;
        this.setLocation(x, y, z, currentDimension);
        this.r = red;
        this.g = green;
        this.b = blue;
        this.enable = enable;
        this.type = type;
        this.origin = origin;
        this.persistent = true;
        switch (type) {
            case Normal: {
                this.icon = "waypoint-normal.png";
                break;
            }
            case Death: {
                this.icon = "waypoint-death.png";
                break;
            }
        }
    }
    
    private static String createName(final int x, final int z) {
        return String.format("%s, %s", x, z);
    }
    
    public static WaypointLegacy fromString(final String json) {
        return (WaypointLegacy)WaypointLegacy.GSON.fromJson(json, (Class)WaypointLegacy.class);
    }
    
    public Waypoint toWaypoint() {
        final Waypoint wp = new Waypoint(this.getOrigin(), this.getId(), this.getName(), this.getDimension(), this.getBlockPos());
        final Integer color = this.getColor();
        if (color != null) {
            wp.setIconColor(color);
        }
        if (this.type == Type.Death) {
            wp.setIcon(new MapImage(TextureCache.Deathpoint, 16, 16));
        }
        for (final int dim : this.getDimensions()) {
            wp.setDisplayed(dim, true);
        }
        return wp;
    }
    
    public static WaypointLegacy toLegacy(final Waypoint waypoint) {
        final ResourceLocation iconLocation = waypoint.getIcon().getImageLocation();
        final Type type = TextureCache.Deathpoint.equals((Object)iconLocation) ? Type.Death : Type.Normal;
        final WaypointLegacy legacy = new WaypointLegacy(waypoint.getName(), waypoint.getPosition(), new Color(waypoint.getOrDefaultLabelColor(16777215)), type, waypoint.getDimension());
        legacy.enable = waypoint.isDisplayed(waypoint.getDimension());
        legacy.dimensions.addAll(waypoint.getDisplayDimensions());
        legacy.origin = waypoint.getModId();
        if (waypoint.getGroup() != null) {
            legacy.groupName = waypoint.getGroup().getName();
        }
        return legacy;
    }
    
    public void setLocation(final int x, final int y, final int z, final int currentDimension) {
        this.x = ((currentDimension == -1) ? (x * 8) : x);
        this.y = y;
        this.z = ((currentDimension == -1) ? (z * 8) : z);
        this.updateId();
    }
    
    public String updateId() {
        final String oldId = this.id;
        this.id = String.format("%s_%s,%s,%s", this.name, this.x, this.y, this.z);
        return oldId;
    }
    
    public boolean isDeathPoint() {
        return this.type == Type.Death;
    }
    
    public TextureImpl getTexture() {
        return this.isDeathPoint() ? TextureCache.getTexture(TextureCache.Deathpoint) : TextureCache.getTexture(TextureCache.Waypoint);
    }
    
    public ChunkPos getChunkCoordIntPair() {
        return new ChunkPos(this.x >> 4, this.z >> 4);
    }
    
    public WaypointGroup getGroup() {
        return WaypointGroupStore.DEFAULT;
    }
    
    public Integer getColor() {
        return RGB.toInteger(this.r, this.g, this.b);
    }
    
    public int getDimension() {
        return this.dimensions.first();
    }
    
    public Collection<Integer> getDimensions() {
        return this.dimensions;
    }
    
    public String getId() {
        return (this.displayId != null) ? this.getGuid() : this.id;
    }
    
    public String getGuid() {
        return this.origin + ":" + this.displayId;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getIcon() {
        return this.icon;
    }
    
    public int getX() {
        return this.x;
    }
    
    public double getBlockCenteredX() {
        return this.getX() + 0.5;
    }
    
    public int getY() {
        return this.y;
    }
    
    public int getZ() {
        return this.z;
    }
    
    public BlockPos getBlockPos() {
        return new BlockPos(this.getX(), this.getY(), this.getZ());
    }
    
    public int getR() {
        return this.r;
    }
    
    public int getG() {
        return this.g;
    }
    
    public int getB() {
        return this.b;
    }
    
    public boolean isEnable() {
        return this.enable;
    }
    
    public Type getType() {
        return this.type;
    }
    
    public String getOrigin() {
        return this.origin;
    }
    
    public boolean isPersistent() {
        return this.persistent;
    }
    
    @Override
    public String toString() {
        return WaypointLegacy.GSON.toJson((Object)this);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final WaypointLegacy waypoint = (WaypointLegacy)o;
        return this.b == waypoint.b && this.enable == waypoint.enable && this.g == waypoint.g && this.r == waypoint.r && this.x == waypoint.x && this.y == waypoint.y && this.z == waypoint.z && this.dimensions.equals(waypoint.dimensions) && this.icon.equals(waypoint.icon) && this.id.equals(waypoint.id) && this.name.equals(waypoint.name) && this.origin == waypoint.origin && this.type == waypoint.type;
    }
    
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
    
    static {
        GSON = new GsonBuilder().setVersion(3.0).create();
    }
    
    public enum Type
    {
        Normal, 
        Death;
    }
}
