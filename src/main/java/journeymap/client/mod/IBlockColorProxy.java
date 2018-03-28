package journeymap.client.mod;

import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public interface IBlockColorProxy {
    @Nullable
    int deriveBlockColor(final BlockMD p0);

    int getBlockColor(final ChunkMD p0, final BlockMD p1, final BlockPos p2);
}
