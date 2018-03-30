package journeymap.client.api.util;

import journeymap.client.api.model.*;
import net.minecraft.util.math.*;

public class PolygonHelper
{
    public static MapPolygon createChunkPolygonForWorldCoords(final int x, final int y, final int z) {
        return createChunkPolygon(x >> 4, y, z >> 4);
    }
    
    public static MapPolygon createChunkPolygon(final int chunkX, final int y, final int chunkZ) {
        final int x = chunkX << 4;
        final int z = chunkZ << 4;
        final BlockPos sw = new BlockPos(x, y, z + 16);
        final BlockPos se = new BlockPos(x + 16, y, z + 16);
        final BlockPos ne = new BlockPos(x + 16, y, z);
        final BlockPos nw = new BlockPos(x, y, z);
        return new MapPolygon(new BlockPos[] { sw, se, ne, nw });
    }
}
