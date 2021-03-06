package journeymap.client.api.util;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import journeymap.client.api.display.Context;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.awt.geom.Rectangle2D;

public final class UIState {
    public final Context.UI ui;
    public final boolean active;
    public final int dimension;
    public final int zoom;
    public final Context.MapType mapType;
    public final BlockPos mapCenter;
    public final Integer chunkY;
    public final AxisAlignedBB blockBounds;
    public final Rectangle2D.Double displayBounds;
    public final double blockSize;

    public UIState(final Context.UI ui, final boolean active, final int dimension, final int zoom, @Nullable final Context.MapType mapType, @Nullable final BlockPos mapCenter, @Nullable final Integer chunkY, @Nullable final AxisAlignedBB blockBounds, @Nullable final Rectangle2D.Double displayBounds) {
        this.ui = ui;
        this.active = active;
        this.dimension = dimension;
        this.zoom = zoom;
        this.mapType = mapType;
        this.mapCenter = mapCenter;
        this.chunkY = chunkY;
        this.blockBounds = blockBounds;
        this.displayBounds = displayBounds;
        this.blockSize = Math.pow(2.0, zoom);
    }

    public static UIState newInactive(final Context.UI ui, final Minecraft minecraft) {
        final BlockPos center = (minecraft.world == null) ? new BlockPos(0, 68, 0) : minecraft.world.getSpawnPoint();
        return new UIState(ui, false, 0, 0, Context.MapType.Day, center, null, null, null);
    }

    public static UIState newInactive(final UIState priorState) {
        return new UIState(priorState.ui, false, priorState.dimension, priorState.zoom, priorState.mapType, priorState.mapCenter, priorState.chunkY, priorState.blockBounds, priorState.displayBounds);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final UIState mapState = (UIState) o;
        return Objects.equal(this.active, mapState.active) && Objects.equal(this.dimension, mapState.dimension) && Objects.equal(this.zoom, mapState.zoom) && Objects.equal(this.ui, mapState.ui) && Objects.equal(this.mapType, mapState.mapType) && Objects.equal(this.displayBounds, mapState.displayBounds);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.ui, this.active, this.dimension, this.zoom, this.mapType, this.displayBounds);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("ui", this.ui).add("active", this.active).add("dimension", this.dimension).add("mapType", this.mapType).add("zoom", this.zoom).add("mapCenter", this.mapCenter).add("chunkY", this.chunkY).add("blockBounds", this.blockBounds).add("displayBounds", this.displayBounds).add("blockSize", this.blockSize).toString();
    }
}
