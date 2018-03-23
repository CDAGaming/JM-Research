package journeymap.client.cartography.render;

import journeymap.client.cartography.*;
import journeymap.client.model.*;
import journeymap.client.cartography.color.*;

public class EndCaveRenderer extends CaveRenderer implements IChunkRenderer
{
    public EndCaveRenderer(final SurfaceRenderer endSurfaceRenderer) {
        super(endSurfaceRenderer);
    }
    
    @Override
    protected boolean updateOptions(final ChunkMD chunkMd, final MapType mapType) {
        if (super.updateOptions(chunkMd, mapType)) {
            this.ambientColor = RGB.floats(this.tweakEndAmbientColor);
            return true;
        }
        return false;
    }
    
    @Override
    protected int getSliceLightLevel(final ChunkMD chunkMd, final int x, final int y, final int z, final boolean adjusted) {
        return this.mapCaveLighting ? Math.max(adjusted ? ((int)this.surfaceRenderer.tweakMoonlightLevel) : 0, chunkMd.getSavedLightValue(x, y + 1, z)) : 15;
    }
}
