package journeymap.client.api.util;

import journeymap.common.api.feature.*;
import net.minecraft.util.math.*;
import java.awt.geom.*;
import javax.annotation.*;
import net.minecraft.client.*;
import com.google.common.base.*;

public final class UIState
{
    public final Feature.Display ui;
    public final boolean active;
    public final int dimension;
    public final int zoom;
    public final Feature.MapType mapType;
    public final BlockPos mapCenter;
    public final Integer chunkY;
    public final AxisAlignedBB blockBounds;
    public final Rectangle2D.Double displayBounds;
    public final double blockSize;
    
    public UIState(final Feature.Display ui, final boolean active, final int dimension, final int zoom, @Nullable final Feature.MapType mapType, @Nullable final BlockPos mapCenter, @Nullable final Integer chunkY, @Nullable final AxisAlignedBB blockBounds, @Nullable final Rectangle2D.Double displayBounds) {
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
    
    public static UIState newInactive(final Feature.Display ui, final Minecraft minecraft) {
        final BlockPos center = (minecraft.field_71441_e == null) ? new BlockPos(0, 68, 0) : minecraft.field_71441_e.func_175694_M();
        return new UIState(ui, false, 0, 0, Feature.MapType.Day, center, null, null, null);
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
        final UIState mapState = (UIState)o;
        return Objects.equal((Object)this.active, (Object)mapState.active) && Objects.equal((Object)this.dimension, (Object)mapState.dimension) && Objects.equal((Object)this.zoom, (Object)mapState.zoom) && Objects.equal((Object)this.ui, (Object)mapState.ui) && Objects.equal((Object)this.mapType, (Object)mapState.mapType) && Objects.equal((Object)this.displayBounds, (Object)mapState.displayBounds);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(new Object[] { this.ui, this.active, this.dimension, this.zoom, this.mapType, this.displayBounds });
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("ui", (Object)this.ui).add("active", this.active).add("dimension", this.dimension).add("mapType", (Object)this.mapType).add("zoom", this.zoom).add("mapCenter", (Object)this.mapCenter).add("chunkY", (Object)this.chunkY).add("blockBounds", (Object)this.blockBounds).add("displayBounds", (Object)this.displayBounds).add("blockSize", this.blockSize).toString();
    }
}
