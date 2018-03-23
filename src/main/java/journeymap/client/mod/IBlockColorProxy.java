package journeymap.client.mod;

import javax.annotation.*;
import journeymap.client.model.*;
import net.minecraft.util.math.*;

public interface IBlockColorProxy
{
    @Nullable
    int deriveBlockColor(final BlockMD p0);
    
    int getBlockColor(final ChunkMD p0, final BlockMD p1, final BlockPos p2);
}
