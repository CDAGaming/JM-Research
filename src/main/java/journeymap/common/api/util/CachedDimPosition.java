package journeymap.common.api.util;

import net.minecraft.util.math.*;
import java.util.function.*;

public class CachedDimPosition
{
    private Integer cachedDim;
    private BlockPos cachedPos;
    private Vec3d cachedVec;
    private Vec3d cachedCenteredVec;
    private final Function<Integer, BlockPos> valueSupplier;
    
    public CachedDimPosition(final Function<Integer, BlockPos> valueSupplier) {
        this.valueSupplier = valueSupplier;
    }
    
    public void reset() {
        this.cachedDim = null;
        this.cachedPos = null;
        this.cachedVec = null;
        this.cachedCenteredVec = null;
    }
    
    private void ensure(final int dimension) {
        if (this.cachedDim == null || this.cachedDim != dimension) {
            this.cachedDim = dimension;
            this.cachedPos = this.valueSupplier.apply(dimension);
            this.cachedVec = new Vec3d((double)this.cachedPos.func_177958_n(), (double)this.cachedPos.func_177956_o(), (double)this.cachedPos.func_177952_p());
            this.cachedCenteredVec = this.cachedVec.func_72441_c(0.5, 0.5, 0.5);
        }
    }
    
    public BlockPos getPosition(final int dimension) {
        this.ensure(dimension);
        return this.cachedPos;
    }
    
    public Vec3d getVec(final int dimension) {
        this.ensure(dimension);
        return this.cachedVec;
    }
    
    public Vec3d getCenteredVec(final int dimension) {
        this.ensure(dimension);
        return this.cachedCenteredVec;
    }
}
