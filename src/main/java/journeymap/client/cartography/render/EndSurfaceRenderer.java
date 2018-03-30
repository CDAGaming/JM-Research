package journeymap.client.cartography.render;

import journeymap.client.cartography.*;
import journeymap.client.model.*;
import journeymap.client.cartography.color.*;

public class EndSurfaceRenderer extends SurfaceRenderer implements IChunkRenderer
{
    @Override
    protected boolean updateOptions(final ChunkMD chunkMd, final MapView mapView) {
        if (super.updateOptions(chunkMd, mapView)) {
            this.ambientColor = RGB.floats(this.tweakEndAmbientColor);
            this.tweakMoonlightLevel = 5.0f;
            return true;
        }
        return false;
    }
}
