package journeymap.client.cartography.render;

import journeymap.client.cartography.IChunkRenderer;
import journeymap.client.cartography.color.RGB;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.MapView;

public class EndCaveRenderer extends CaveRenderer implements IChunkRenderer {
    public EndCaveRenderer(final SurfaceRenderer endSurfaceRenderer) {
        super(endSurfaceRenderer);
    }

    @Override
    protected boolean updateOptions(final ChunkMD chunkMd, final MapView mapView) {
        if (super.updateOptions(chunkMd, mapView)) {
            this.ambientColor = RGB.floats(this.tweakEndAmbientColor);
            return true;
        }
        return false;
    }

    @Override
    protected int getSliceLightLevel(final ChunkMD chunkMd, final int x, final int y, final int z, final boolean adjusted) {
        return this.mapCaveLighting ? Math.max(adjusted ? ((int) this.surfaceRenderer.tweakMoonlightLevel) : 0, chunkMd.getSavedLightValue(x, y + 1, z)) : 15;
    }
}
