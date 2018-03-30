package journeymap.client.api.display;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.gson.annotations.Since;
import journeymap.client.api.model.MapImage;
import journeymap.client.api.model.MapText;
import journeymap.client.api.model.WaypointBase;
import journeymap.common.api.util.CachedDimPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@ParametersAreNonnullByDefault
public class Waypoint extends WaypointBase<Waypoint> {
    public static final double VERSION = 1.6;
    @Since(1.4)
    protected final double version = 1.6;
    private final transient CachedDimPosition dimPositions;
    @Since(1.4)
    protected int dim;
    @Since(1.4)
    protected BlockPos pos;
    @Since(1.4)
    protected WaypointGroup group;
    @Since(1.4)
    protected boolean editable;
    private transient boolean persistent;

    private Waypoint() {
        this.editable = true;
        this.persistent = true;
        this.dimPositions = new CachedDimPosition(this::getInternalPosition);
    }

    public Waypoint(final String modId, final String name, final int dimension, final BlockPos position) {
        super(modId, name);
        this.editable = true;
        this.persistent = true;
        this.dimPositions = new CachedDimPosition(this::getInternalPosition);
        this.setPosition(dimension, position);
    }

    public Waypoint(final String modId, final String id, final String name, final int dimension, final BlockPos position) {
        super(modId, id, name);
        this.editable = true;
        this.persistent = true;
        this.dimPositions = new CachedDimPosition(this::getInternalPosition);
        this.setPosition(dimension, position);
    }

    public Waypoint(final Waypoint other) {
        super(other.getModId(), other.getName());
        this.editable = true;
        this.persistent = true;
        this.dimPositions = new CachedDimPosition(this::getInternalPosition);
        this.updateFrom(other);
    }

    public Waypoint updateFrom(final Waypoint other) {
        this.setName(other.getName());
        this.dim = other.dim;
        this.pos = other.pos;
        this.group = other.group;
        this.editable = other.editable;
        this.persistent = other.persistent;
        this.displayDims = new HashSet<>(other.getDisplayDimensions());
        final MapImage icon = other.getIcon();
        if (icon != null) {
            this.icon = new MapImage(icon);
        }
        final MapText label = other.getLabel();
        if (label != null) {
            this.label = new MapText(label);
        }
        return this;
    }

    public final WaypointGroup getGroup() {
        return this.group;
    }

    public Waypoint setGroup(@Nullable final WaypointGroup group) {
        this.group = group;
        return this.setDirty();
    }

    public final int getDimension() {
        return this.dim;
    }

    public final BlockPos getPosition() {
        return this.pos;
    }

    public BlockPos getPosition(final int targetDimension) {
        return this.dimPositions.getPosition(targetDimension);
    }

    private BlockPos getInternalPosition(final int targetDimension) {
        if (this.dim != targetDimension) {
            if (this.dim == -1) {
                this.pos = new BlockPos(this.pos.getX() * 8, this.pos.getY(), this.pos.getZ() * 8);
            } else if (targetDimension == -1) {
                this.pos = new BlockPos(this.pos.getX() / 8.0, (double) this.pos.getY(), this.pos.getZ() / 8.0);
            }
        }
        return this.pos;
    }

    public Waypoint setPosition(final int dimension, final BlockPos position) {
        if (position == null) {
            throw new IllegalArgumentException("position may not be null");
        }
        this.dim = dimension;
        this.pos = position;
        this.dimPositions.reset();
        return this.setDirty();
    }

    public Vec3d getVec(final int dimension) {
        return this.dimPositions.getVec(dimension);
    }

    public Vec3d getCenteredVec(final int dimension) {
        return this.dimPositions.getCenteredVec(dimension);
    }

    public final boolean isPersistent() {
        return this.persistent;
    }

    public final Waypoint setPersistent(final boolean persistent) {
        if (!(this.persistent = persistent)) {
            this.dirty = false;
        }
        return this.setDirty();
    }

    public final boolean isEditable() {
        return this.editable;
    }

    public final Waypoint setEditable(final boolean editable) {
        this.editable = editable;
        return this.setDirty();
    }

    @Override
    protected WaypointGroup getDelegate() {
        return this.getGroup();
    }

    @Override
    protected boolean hasDelegate() {
        return this.group != null;
    }

    @Override
    public Set<Integer> getDisplayDimensions() {
        if (this.displayDims == null && !this.hasDelegate()) {
            this.setDisplayDimensions(this.dim);
        }
        return super.getDisplayDimensions();
    }

    @Override
    public int getDisplayOrder() {
        return (this.group != null) ? this.group.getDisplayOrder() : 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Waypoint)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final Waypoint that = (Waypoint) o;
        return this.isPersistent() == that.isPersistent() && this.isEditable() == that.isEditable() && Objects.equal(this.getDimension(), that.getDimension()) && Objects.equal(this.getLabel(), that.getLabel()) && Objects.equal(this.getName(), that.getName()) && Objects.equal(this.getPosition(), that.getPosition()) && Objects.equal(this.getIcon(), that.getIcon()) && Arrays.equals(this.getDisplayDimensions().toArray(), that.getDisplayDimensions().toArray());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), this.getName());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", this.name).add("dim", this.dim).add("pos", this.pos).add("group", this.group).add("icon", this.icon).add("label", this.label).add("displayDims", this.displayDims).add("editable", this.editable).add("persistent", this.persistent).add("dirty", this.dirty).toString();
    }
}
