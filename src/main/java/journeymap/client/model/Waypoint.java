package journeymap.client.model;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Since;
import journeymap.client.Constants;
import journeymap.client.cartography.color.RGB;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.waypoint.WaypointGroupStore;
import journeymap.client.waypoint.WaypointParser;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.util.Strings;

import java.awt.*;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.*;
import java.util.List;

public class Waypoint implements Serializable {
    public static final int VERSION = 3;
    public static final Gson GSON;
    protected static final String ICON_NORMAL = "waypoint-normal.png";
    protected static final String ICON_DEATH = "waypoint-death.png";

    static {
        GSON = new GsonBuilder().setVersion(3.0).create();
    }

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
    protected transient WaypointGroup group;
    protected transient boolean dirty;
    protected transient Minecraft mc;

    public Waypoint() {
        this.mc = FMLClientHandler.instance().getClient();
    }

    public Waypoint(final Waypoint original) {
        this(original.name, original.x, original.y, original.z, original.enable, original.r, original.g, original.b, original.type, original.origin, original.dimensions.first(), original.dimensions);
        this.x = original.x;
        this.y = original.y;
        this.z = original.z;
    }

    public Waypoint(final journeymap.client.api.display.Waypoint modWaypoint) {
        this(modWaypoint.getName(), modWaypoint.getPosition(), (modWaypoint.getColor() == null) ? Color.WHITE : new Color(modWaypoint.getColor()), Type.Normal, modWaypoint.getDimension());
        final int[] prim = modWaypoint.getDisplayDimensions();
        final ArrayList<Integer> dims = new ArrayList<>(prim.length);
        for (final int aPrim : prim) {
            dims.add(aPrim);
        }
        this.setDimensions(dims);
        this.setOrigin(modWaypoint.getModId());
        this.displayId = modWaypoint.getId();
        this.setPersistent(modWaypoint.isPersistent());
        if (modWaypoint.getGroup() != null) {
            this.setGroupName(modWaypoint.getGroup().getName());
        }
    }

    public Waypoint(final String name, final BlockPos pos, final Color color, final Type type, final Integer currentDimension) {
        this(name, pos.getX(), pos.getY(), pos.getZ(), true, color.getRed(), color.getGreen(), color.getBlue(), type, "journeymap", currentDimension, Arrays.asList(currentDimension));
    }

    public Waypoint(String name, final int x, final int y, final int z, final boolean enable, final int red, final int green, final int blue, final Type type, final String origin, final Integer currentDimension, Collection<Integer> dimensions) {
        this.mc = FMLClientHandler.instance().getClient();
        if (name == null) {
            name = createName(x, z);
        }
        if (dimensions == null || dimensions.size() == 0) {
            dimensions = new TreeSet<>();
            dimensions.add(FMLClientHandler.instance().getClient().player.world.provider.getDimension());
        }
        (this.dimensions = new TreeSet<>(dimensions)).add(currentDimension);
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

    public static Waypoint of(final EntityPlayer player) {
        final BlockPos blockPos = new BlockPos(MathHelper.floor(player.posX), MathHelper.floor(player.posY), MathHelper.floor(player.posZ));
        return at(blockPos, Type.Normal, FMLClientHandler.instance().getClient().player.world.provider.getDimension());
    }

    public static Waypoint at(final BlockPos blockPos, final Type type, final int dimension) {
        String name;
        if (type == Type.Death) {
            final Date now = new Date();
            name = String.format("%s %s %s", Constants.getString("jm.waypoint.deathpoint"), DateFormat.getTimeInstance().format(now), DateFormat.getDateInstance(3).format(now));
        } else {
            name = createName(blockPos.getX(), blockPos.getZ());
        }
        final Waypoint waypoint = new Waypoint(name, blockPos, Color.white, type, dimension);
        waypoint.setRandomColor();
        return waypoint;
    }

    private static String createName(final int x, final int z) {
        return String.format("%s, %s", x, z);
    }

    public static Waypoint fromString(final String json) {
        return Waypoint.GSON.fromJson(json, Waypoint.class);
    }

    public Waypoint setLocation(final int x, final int y, final int z, final int currentDimension) {
        this.x = ((currentDimension == -1) ? (x * 8) : x);
        this.y = y;
        this.z = ((currentDimension == -1) ? (z * 8) : z);
        this.updateId();
        return this.setDirty();
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

    public Waypoint setGroupName(final String groupName) {
        final WaypointGroup group = WaypointGroupStore.INSTANCE.get(this.origin, groupName);
        this.setGroup(group);
        return this;
    }

    public WaypointGroup getGroup() {
        if (this.group == null) {
            if (Strings.isEmpty(this.origin) || Strings.isEmpty(this.groupName)) {
                this.setGroup(WaypointGroup.DEFAULT);
            } else {
                this.setGroup(WaypointGroupStore.INSTANCE.get(this.origin, this.groupName));
            }
        }
        return this.group;
    }

    public Waypoint setGroup(final WaypointGroup group) {
        this.setOrigin(group.getOrigin());
        this.groupName = group.getName();
        this.group = group;
        return this.setDirty();
    }

    public Waypoint setRandomColor() {
        return this.setColor(RGB.randomColor());
    }

    public Integer getColor() {
        return RGB.toInteger(this.r, this.g, this.b);
    }

    public Waypoint setColor(final Integer color) {
        final int[] c = RGB.ints(color);
        this.r = c[0];
        this.g = c[1];
        this.b = c[2];
        return this.setDirty();
    }

    public Integer getSafeColor() {
        if (this.r + this.g + this.b >= 100) {
            return this.getColor();
        }
        return 4210752;
    }

    public Collection<Integer> getDimensions() {
        return this.dimensions;
    }

    public Waypoint setDimensions(final Collection<Integer> dims) {
        this.dimensions = new TreeSet<>(dims);
        return this.setDirty();
    }

    public boolean isTeleportReady() {
        return this.y >= 0 && this.isInPlayerDimension();
    }

    public boolean isInPlayerDimension() {
        return this.dimensions.contains(FMLClientHandler.instance().getClient().player.world.provider.getDimension());
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

    public Waypoint setName(final String name) {
        this.name = name;
        return this.setDirty();
    }

    public String getIcon() {
        return this.icon;
    }

    public Waypoint setIcon(final String icon) {
        this.icon = icon;
        return this.setDirty();
    }

    public int getX() {
        if (this.mc != null && this.mc.player != null && this.mc.player.dimension == -1) {
            return this.x / 8;
        }
        return this.x;
    }

    public double getBlockCenteredX() {
        return this.getX() + 0.5;
    }

    public int getY() {
        return this.y;
    }

    public double getBlockCenteredY() {
        return this.getY() + 0.5;
    }

    public int getZ() {
        if (this.mc != null && this.mc.player != null && this.mc.player.dimension == -1) {
            return this.z / 8;
        }
        return this.z;
    }

    public double getBlockCenteredZ() {
        return this.getZ() + 0.5;
    }

    public Vec3d getPosition() {
        return new Vec3d(this.getBlockCenteredX(), this.getBlockCenteredY(), this.getBlockCenteredZ());
    }

    public BlockPos getBlockPos() {
        return new BlockPos(this.getX(), this.getY(), this.getZ());
    }

    public int getR() {
        return this.r;
    }

    public Waypoint setR(final int r) {
        this.r = r;
        return this.setDirty();
    }

    public int getG() {
        return this.g;
    }

    public Waypoint setG(final int g) {
        this.g = g;
        return this.setDirty();
    }

    public int getB() {
        return this.b;
    }

    public Waypoint setB(final int b) {
        this.b = b;
        return this.setDirty();
    }

    public boolean isEnable() {
        return this.enable;
    }

    public Waypoint setEnable(final boolean enable) {
        if (enable != this.enable) {
            this.enable = enable;
            this.setDirty();
        }
        return this;
    }

    public Type getType() {
        return this.type;
    }

    public Waypoint setType(final Type type) {
        this.type = type;
        return this.setDirty();
    }

    public String getOrigin() {
        return this.origin;
    }

    public Waypoint setOrigin(final String origin) {
        this.origin = origin;
        return this.setDirty();
    }

    public String getFileName() {
        String fileName = this.id.replaceAll("[\\\\/:\"*?<>|]", "_").concat(".json");
        if (fileName.equals("waypoint_groups.json")) {
            fileName = "_" + fileName;
        }
        return fileName;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public Waypoint setDirty(final boolean dirty) {
        if (this.isPersistent()) {
            this.dirty = dirty;
        }
        return this;
    }

    public Waypoint setDirty() {
        return this.setDirty(true);
    }

    public boolean isPersistent() {
        return this.persistent;
    }

    public Waypoint setPersistent(final boolean persistent) {
        this.persistent = persistent;
        this.dirty = persistent;
        return this;
    }

    public String toChatString() {
        final boolean useName = !this.getName().equals(String.format("%s, %s", this.getX(), this.getZ()));
        return this.toChatString(useName);
    }

    public String toChatString(final boolean useName) {
        final boolean useDim = this.dimensions.first() != 0;
        final List<String> parts = new ArrayList<>();
        final List<Object> args = new ArrayList<>();
        if (useName) {
            parts.add("name:\"%s\"");
            args.add(this.getName().replaceAll("\"", " "));
        }
        parts.add("x:%s, y:%s, z:%s");
        args.add(this.getX());
        args.add(this.getY());
        args.add(this.getZ());
        if (useDim) {
            parts.add("dim:%s");
            args.add(this.dimensions.first());
        }
        final String format = "[" + Joiner.on(", ").join(parts) + "]";
        final String result = String.format(format, args.toArray());
        if (WaypointParser.parse(result) == null) {
            Journeymap.getLogger().warn("Couldn't produce parsable chat string from Waypoint: " + this);
            if (useName) {
                return this.toChatString(false);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return Waypoint.GSON.toJson(this);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Waypoint waypoint = (Waypoint) o;
        return this.b == waypoint.b && this.enable == waypoint.enable && this.g == waypoint.g && this.r == waypoint.r && this.x == waypoint.x && this.y == waypoint.y && this.z == waypoint.z && this.dimensions.equals(waypoint.dimensions) && this.icon.equals(waypoint.icon) && this.id.equals(waypoint.id) && this.name.equals(waypoint.name) && this.origin.equals(waypoint.origin) && this.type == waypoint.type;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public enum Type {
        Normal,
        Death
    }
}
