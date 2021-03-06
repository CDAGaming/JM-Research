package journeymap.client.api.display;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.primitives.Ints;
import com.google.gson.annotations.Since;
import journeymap.client.api.model.WaypointBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.Arrays;

public class Waypoint extends WaypointBase<Waypoint> {
    public static final double VERSION = 1.4;
    protected final transient CachedDimPosition cachedDimPosition;
    @Since(1.4)
    protected final double version = 1.4;
    @Since(1.4)
    protected int dim;
    @Since(1.4)
    protected BlockPos pos;
    @Since(1.4)
    protected WaypointGroup group;
    @Since(1.4)
    protected boolean persistent;
    @Since(1.4)
    protected boolean editable;

    public Waypoint(final String modId, final String name, final int dimension, final BlockPos position) {
        super(modId, name);
        this.cachedDimPosition = new CachedDimPosition();
        this.persistent = true;
        this.editable = true;
        this.setPosition(dimension, position);
    }

    public Waypoint(final String modId, final String id, final String name, final int dimension, final BlockPos position) {
        super(modId, id, name);
        this.cachedDimPosition = new CachedDimPosition();
        this.persistent = true;
        this.editable = true;
        this.setPosition(dimension, position);
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
        return this.cachedDimPosition.getPosition(targetDimension);
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
        this.cachedDimPosition.reset();
        return this.setDirty();
    }

    public Vec3d getVec(final int dimension) {
        return this.cachedDimPosition.getVec(dimension);
    }

    public Vec3d getCenteredVec(final int dimension) {
        return this.cachedDimPosition.getCenteredVec(dimension);
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

    public final boolean isTeleportReady(final int targetDimension) {
        final BlockPos pos = this.getPosition(targetDimension);
        return pos != null && pos.getY() >= 0;
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
    public int[] getDisplayDimensions() {
        final int[] dims = super.getDisplayDimensions();
        if (dims == null) {
            this.setDisplayDimensions(this.dim);
        }
        return this.displayDims;
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
        return this.isPersistent() == that.isPersistent() && this.isEditable() == that.isEditable() && Objects.equal(this.getDimension(), that.getDimension()) && Objects.equal(this.getColor(), that.getColor()) && Objects.equal(this.getBackgroundColor(), that.getBackgroundColor()) && Objects.equal(this.getName(), that.getName()) && Objects.equal(this.getPosition(), that.getPosition()) && Objects.equal(this.getIcon(), that.getIcon()) && Arrays.equals(this.getDisplayDimensions(), that.getDisplayDimensions());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), this.getName());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", this.name).add("dim", this.dim).add("pos", this.pos).add("group", this.group).add("icon", this.icon).add("color", this.color).add("bgColor", this.bgColor).add("displayDims", (this.displayDims == null) ? null : Ints.asList(this.displayDims)).add("editable", this.editable).add("persistent", this.persistent).add("dirty", this.dirty).toString();
    }

    class CachedDimPosition {
        Integer cachedDim;
        BlockPos cachedPos;
        Vec3d cachedVec;
        Vec3d cachedCenteredVec;

        CachedDimPosition reset() {
            this.cachedDim = null;
            this.cachedPos = null;
            this.cachedVec = null;
            this.cachedCenteredVec = null;
            return this;
        }

        private CachedDimPosition ensure(final int dimension) {
            if (this.cachedDim != dimension) {
                this.cachedDim = dimension;
                this.cachedPos = Waypoint.this.getInternalPosition(dimension);
                this.cachedVec = new Vec3d((double) this.cachedPos.getX(), (double) this.cachedPos.getY(), (double) this.cachedPos.getZ());
                this.cachedCenteredVec = this.cachedVec.addVector(0.5, 0.5, 0.5);
            }
            return this;
        }

        public BlockPos getPosition(final int dimension) {
            return this.ensure(dimension).cachedPos;
        }

        public Vec3d getVec(final int dimension) {
            return this.ensure(dimension).cachedVec;
        }

        public Vec3d getCenteredVec(final int dimension) {
            return this.ensure(dimension).cachedCenteredVec;
        }
    }
}
