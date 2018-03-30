package journeymap.client.cartography;

import journeymap.client.cartography.render.*;
import journeymap.common.*;
import journeymap.common.log.*;
import journeymap.client.model.*;
import java.awt.image.*;
import journeymap.client.render.*;

public class ChunkRenderController
{
    private final SurfaceRenderer overWorldSurfaceRenderer;
    private final BaseRenderer netherRenderer;
    private final SurfaceRenderer endSurfaceRenderer;
    private final BaseRenderer endCaveRenderer;
    private final BaseRenderer topoRenderer;
    private final BaseRenderer overWorldCaveRenderer;
    
    public ChunkRenderController() {
        this.overWorldSurfaceRenderer = new SurfaceRenderer();
        this.overWorldCaveRenderer = new CaveRenderer(this.overWorldSurfaceRenderer);
        this.netherRenderer = new NetherRenderer();
        this.endSurfaceRenderer = new EndSurfaceRenderer();
        this.endCaveRenderer = new EndCaveRenderer(this.endSurfaceRenderer);
        this.topoRenderer = new TopoRenderer();
    }
    
    public BaseRenderer getRenderer(final RegionCoord rCoord, final MapView mapView, final ChunkMD chunkMd) {
        try {
            final RegionImageSet regionImageSet = RegionImageCache.INSTANCE.getRegionImageSet(rCoord);
            if (!mapView.isUnderground()) {
                return this.overWorldSurfaceRenderer;
            }
            final BufferedImage image = regionImageSet.getChunkImage(chunkMd, mapView);
            if (image != null) {
                switch (rCoord.dimension) {
                    case -1: {
                        return this.netherRenderer;
                    }
                    case 1: {
                        return this.endCaveRenderer;
                    }
                    default: {
                        return this.overWorldCaveRenderer;
                    }
                }
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("Unexpected error in ChunkRenderController: " + LogFormatter.toPartialString(t));
        }
        return null;
    }
    
    public boolean renderChunk(final RegionCoord rCoord, final MapView mapView, final ChunkMD chunkMd) {
        if (!Journeymap.getClient().isMapping()) {
            return false;
        }
        boolean renderOkay = false;
        try {
            final RegionImageSet regionImageSet = RegionImageCache.INSTANCE.getRegionImageSet(rCoord);
            if (mapView.isUnderground()) {
                final ComparableBufferedImage chunkSliceImage = regionImageSet.getChunkImage(chunkMd, mapView);
                if (chunkSliceImage != null) {
                    switch (rCoord.dimension) {
                        case -1: {
                            renderOkay = this.netherRenderer.render(chunkSliceImage, chunkMd, mapView.vSlice);
                            break;
                        }
                        case 1: {
                            renderOkay = this.endCaveRenderer.render(chunkSliceImage, chunkMd, mapView.vSlice);
                            break;
                        }
                        default: {
                            renderOkay = this.overWorldCaveRenderer.render(chunkSliceImage, chunkMd, mapView.vSlice);
                            break;
                        }
                    }
                    if (renderOkay) {
                        regionImageSet.setChunkImage(chunkMd, mapView, chunkSliceImage);
                    }
                }
            }
            else if (mapView.isTopo()) {
                final ComparableBufferedImage imageTopo = regionImageSet.getChunkImage(chunkMd, MapView.topo(rCoord.dimension));
                renderOkay = this.topoRenderer.render(imageTopo, chunkMd, null);
                if (renderOkay) {
                    regionImageSet.setChunkImage(chunkMd, MapView.topo(rCoord.dimension), imageTopo);
                }
            }
            else {
                final ComparableBufferedImage imageDay = regionImageSet.getChunkImage(chunkMd, MapView.day(rCoord.dimension));
                final ComparableBufferedImage imageNight = regionImageSet.getChunkImage(chunkMd, MapView.night(rCoord.dimension));
                renderOkay = this.overWorldSurfaceRenderer.render(imageDay, imageNight, chunkMd);
                if (renderOkay) {
                    regionImageSet.setChunkImage(chunkMd, MapView.day(rCoord.dimension), imageDay);
                    regionImageSet.setChunkImage(chunkMd, MapView.night(rCoord.dimension), imageNight);
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            Journeymap.getLogger().warn(LogFormatter.toString(e));
            return false;
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("Unexpected error in ChunkRenderController: " + LogFormatter.toString(t));
        }
        if (!renderOkay && Journeymap.getLogger().isDebugEnabled()) {
            Journeymap.getLogger().debug(String.format("Chunk %s render failed for %s", chunkMd.getCoord(), mapView));
        }
        return renderOkay;
    }
}
