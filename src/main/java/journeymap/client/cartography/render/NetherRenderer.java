package journeymap.client.cartography.render;

import journeymap.client.cartography.*;
import journeymap.client.cartography.color.*;
import journeymap.client.model.*;
import journeymap.common.*;

public class NetherRenderer extends CaveRenderer implements IChunkRenderer
{
    public NetherRenderer() {
        super(null);
    }
    
    @Override
    protected boolean updateOptions(final ChunkMD chunkMd, final MapView mapView) {
        if (super.updateOptions(chunkMd, mapView)) {
            this.ambientColor = RGB.floats(this.tweakNetherAmbientColor);
            this.mapSurfaceAboveCaves = false;
            return true;
        }
        return false;
    }
    
    @Override
    protected Integer getBlockHeight(final ChunkMD chunkMd, final int x, final Integer vSlice, final int z, final Integer sliceMinY, final Integer sliceMaxY) {
        final Integer[][] blockSliceHeights = this.getHeights(chunkMd, vSlice);
        if (blockSliceHeights == null) {
            return null;
        }
        final Integer intY = blockSliceHeights[x][z];
        if (intY != null) {
            return intY;
        }
        int y;
        try {
            y = sliceMaxY;
            BlockMD blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, y, z);
            BlockMD blockMDAbove = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, Math.min(y + 1, sliceMaxY), z);
            while (y > 0) {
                if (blockMD.isLava()) {
                    break;
                }
                if (blockMDAbove.isIgnore() || blockMDAbove.hasTransparency() || blockMDAbove.hasFlag(BlockFlag.OpenToSky)) {
                    if (!blockMD.isIgnore() && !blockMD.hasTransparency() && !blockMD.hasFlag(BlockFlag.OpenToSky)) {
                        break;
                    }
                }
                else if (y == sliceMinY) {
                    y = sliceMaxY;
                    break;
                }
                --y;
                blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, y, z);
                blockMDAbove = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, y + 1, z);
            }
        }
        catch (Exception e) {
            Journeymap.getLogger().warn("Couldn't get safe slice block height at " + x + "," + z + ": " + e);
            y = sliceMaxY;
        }
        y = Math.max(0, y);
        blockSliceHeights[x][z] = y;
        return y;
    }
    
    @Override
    protected int getSliceLightLevel(final ChunkMD chunkMd, final int x, final int y, final int z, final boolean adjusted) {
        if (y + 1 >= chunkMd.getWorldActualHeight()) {
            return 0;
        }
        return this.mapCaveLighting ? Math.max(adjusted ? 2 : 0, chunkMd.getSavedLightValue(x, y + 1, z)) : 15;
    }
    
    @Override
    public float[] getAmbientColor() {
        return this.ambientColor;
    }
}
