package journeymap.client.cartography.render;

import journeymap.client.log.*;
import journeymap.common.*;
import net.minecraft.util.math.*;
import journeymap.client.render.*;
import journeymap.client.feature.*;
import journeymap.common.api.feature.*;
import java.awt.image.*;
import journeymap.common.log.*;
import journeymap.client.model.*;
import net.minecraft.world.*;
import journeymap.client.cartography.color.*;
import journeymap.client.cartography.*;

public class CaveRenderer extends BaseRenderer implements IChunkRenderer
{
    protected SurfaceRenderer surfaceRenderer;
    protected StatTimer renderCaveTimer;
    protected Strata strata;
    protected float defaultDim;
    protected boolean mapSurfaceAboveCaves;
    
    public CaveRenderer(final SurfaceRenderer surfaceRenderer) {
        this.renderCaveTimer = StatTimer.get("CaveRenderer.render");
        this.strata = new Strata("Cave", 40, 8, true);
        this.defaultDim = 0.2f;
        this.surfaceRenderer = surfaceRenderer;
        this.updateOptions(null, null);
        this.shadingSlopeMin = 0.2f;
        this.shadingSlopeMax = 1.1f;
        this.shadingPrimaryDownslopeMultiplier = 0.7f;
        this.shadingPrimaryUpslopeMultiplier = 1.05f;
        this.shadingSecondaryDownslopeMultiplier = 0.99f;
        this.shadingSecondaryUpslopeMultiplier = 1.01f;
    }
    
    @Override
    protected boolean updateOptions(final ChunkMD chunkMd, final MapView mapView) {
        if (super.updateOptions(chunkMd, mapView)) {
            this.mapSurfaceAboveCaves = Journeymap.getClient().getCoreProperties().mapSurfaceAboveCaves.get();
            return true;
        }
        return false;
    }
    
    @Override
    public int getBlockHeight(final ChunkMD chunkMd, final BlockPos blockPos) {
        final Integer vSlice = blockPos.func_177956_o() >> 4;
        final int[] sliceBounds = this.getVSliceBounds(chunkMd, vSlice);
        final int sliceMinY = sliceBounds[0];
        final int sliceMaxY = sliceBounds[1];
        final Integer y = this.getBlockHeight(chunkMd, blockPos.func_177958_n() & 0xF, vSlice, blockPos.func_177952_p() & 0xF, sliceMinY, sliceMaxY);
        return (y == null) ? blockPos.func_177956_o() : y;
    }
    
    @Override
    public synchronized boolean render(final ComparableBufferedImage chunkImage, final ChunkMD chunkMd, final Integer vSlice) {
        if (vSlice == null) {
            Journeymap.getLogger().warn("ChunkOverworldCaveRenderer is for caves. vSlice can't be null");
            return false;
        }
        final int dimension = chunkMd.getDimension();
        if (!ClientFeatures.instance().isAllowed(Feature.MapType.Underground, dimension)) {
            return false;
        }
        this.updateOptions(chunkMd, MapView.underground(vSlice, dimension));
        this.renderCaveTimer.start();
        try {
            if (!this.hasSlopes(chunkMd, vSlice)) {
                this.populateSlopes(chunkMd, vSlice, this.getSlopes(chunkMd, vSlice));
            }
            BufferedImage chunkSurfaceImage = null;
            if (this.mapSurfaceAboveCaves) {
                final MapView mapView = MapView.day(chunkMd.getDimension());
                final RegionImageSet ris = RegionImageCache.INSTANCE.getRegionImageSet(chunkMd, mapView);
                if (ris != null && ris.getHolder(mapView).hasTexture()) {
                    chunkSurfaceImage = ris.getChunkImage(chunkMd, mapView);
                }
            }
            return this.renderUnderground(chunkSurfaceImage, chunkImage, chunkMd, vSlice);
        }
        finally {
            this.renderCaveTimer.stop();
        }
    }
    
    protected void mask(final BufferedImage chunkSurfaceImage, final BufferedImage chunkImage, final ChunkMD chunkMd, final int x, final int y, final int z) {
        if (chunkSurfaceImage == null || !this.mapSurfaceAboveCaves) {
            this.paintBlackBlock(chunkImage, x, z);
        }
        else {
            final int surfaceY = Math.max(0, chunkMd.getChunk().func_76611_b(x, z));
            final int distance = Math.max(0, surfaceY - y);
            if (distance > 16) {
                final int minY = this.getBlockHeight(chunkMd, new BlockPos(x, y - 1, z));
                if (y > 0 && minY > 0) {
                    this.paintBlackBlock(chunkImage, x, z);
                }
                else {
                    this.paintVoidBlock(chunkImage, x, z);
                }
            }
            else {
                this.paintDimOverlay(chunkSurfaceImage, chunkImage, x, z, this.defaultDim);
            }
        }
    }
    
    protected boolean renderUnderground(final BufferedImage chunkSurfaceImage, final BufferedImage chunkSliceImage, final ChunkMD chunkMd, final int vSlice) {
        final int[] sliceBounds = this.getVSliceBounds(chunkMd, vSlice);
        final int sliceMinY = sliceBounds[0];
        final int sliceMaxY = sliceBounds[1];
        boolean chunkOk = false;
        for (int z = 0; z < 16; ++z) {
            for (int x = 0; x < 16; ++x) {
                this.strata.reset();
                try {
                    final int ceiling = this.getBlockHeight(chunkMd, x, vSlice, z, sliceMinY, sliceMaxY);
                    if (ceiling < 0) {
                        this.paintVoidBlock(chunkSliceImage, x, z);
                        chunkOk = true;
                    }
                    else {
                        final int y = Math.min(ceiling, sliceMaxY);
                        this.buildStrata(this.strata, sliceMinY - 1, chunkMd, x, y, z);
                        if (this.strata.isEmpty()) {
                            this.mask(chunkSurfaceImage, chunkSliceImage, chunkMd, x, y, z);
                            chunkOk = true;
                        }
                        else {
                            chunkOk = (this.paintStrata(this.strata, chunkSliceImage, chunkMd, vSlice, x, ceiling, z) || chunkOk);
                        }
                    }
                }
                catch (Throwable t) {
                    this.paintBadBlock(chunkSliceImage, x, vSlice, z);
                    final String error = "CaveRenderer error at x,vSlice,z = " + x + "," + vSlice + "," + z + " : " + LogFormatter.toString(t);
                    Journeymap.getLogger().error(error);
                }
            }
        }
        this.strata.reset();
        return chunkOk;
    }
    
    protected void buildStrata(final Strata strata, final int minY, final ChunkMD chunkMd, final int x, final int topY, final int z) {
        BlockMD lavaBlockMD = null;
        try {
            for (int y = this.getBlockHeight(chunkMd, x, topY >> 4, z, minY, topY); y >= 0; --y) {
                final BlockMD blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, y, z);
                if (!blockMD.isIgnore() && !blockMD.hasFlag(BlockFlag.OpenToSky)) {
                    strata.setBlocksFound(true);
                    final BlockMD blockAboveMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, y + 1, z);
                    if (blockMD.isLava() && blockAboveMD.isLava()) {
                        lavaBlockMD = blockMD;
                    }
                    if (blockAboveMD.isIgnore() || blockAboveMD.hasFlag(BlockFlag.OpenToSky)) {
                        if (chunkMd.hasNoSky() || !chunkMd.canBlockSeeTheSky(x, y + 1, z)) {
                            final int lightLevel = this.getSliceLightLevel(chunkMd, x, y, z, true);
                            if (lightLevel > 0) {
                                strata.push(chunkMd, blockMD, x, y, z, lightLevel);
                                if (!blockMD.hasTransparency()) {
                                    break;
                                }
                                if (!this.mapTransparency) {
                                    break;
                                }
                            }
                            else if (y < minY) {
                                break;
                            }
                        }
                    }
                    else if (strata.isEmpty() && y < minY) {
                        break;
                    }
                }
            }
        }
        finally {
            if (strata.isEmpty() && lavaBlockMD != null && chunkMd.getWorld().field_73011_w instanceof WorldProviderHell) {
                strata.push(chunkMd, lavaBlockMD, x, topY, z, 14);
            }
        }
    }
    
    protected boolean paintStrata(final Strata strata, final BufferedImage chunkSliceImage, final ChunkMD chunkMd, final Integer vSlice, final int x, final int y, final int z) {
        if (strata.isEmpty()) {
            this.paintBadBlock(chunkSliceImage, x, y, z);
            return false;
        }
        try {
            Stratum stratum = null;
            BlockMD blockMD = null;
            while (!strata.isEmpty()) {
                stratum = strata.nextUp(this, true);
                if (strata.getRenderCaveColor() == null) {
                    strata.setRenderCaveColor(stratum.getCaveColor());
                }
                else {
                    strata.setRenderCaveColor(RGB.blendWith(strata.getRenderCaveColor(), stratum.getCaveColor(), stratum.getBlockMD().getAlpha()));
                }
                blockMD = stratum.getBlockMD();
                strata.release(stratum);
            }
            if (strata.getRenderCaveColor() == null) {
                this.paintBadBlock(chunkSliceImage, x, y, z);
                return false;
            }
            if (!blockMD.hasNoShadow()) {
                final float slope = this.getSlope(chunkMd, x, vSlice, z);
                if (slope != 1.0f) {
                    strata.setRenderCaveColor(RGB.bevelSlope(strata.getRenderCaveColor(), slope));
                }
            }
            this.paintBlock(chunkSliceImage, x, z, strata.getRenderCaveColor());
        }
        catch (RuntimeException e) {
            this.paintBadBlock(chunkSliceImage, x, y, z);
            throw e;
        }
        return true;
    }
    
    @Override
    protected Integer getBlockHeight(final ChunkMD chunkMd, final int x, final Integer vSlice, final int z, final Integer sliceMinY, final Integer sliceMaxY) {
        final Integer[][] blockSliceHeights = this.getHeights(chunkMd, vSlice);
        if (blockSliceHeights == null) {
            return null;
        }
        Integer y = blockSliceHeights[x][z];
        if (y != null) {
            return y;
        }
        try {
            y = Math.min(chunkMd.getHeight(new BlockPos(x, 0, z)), sliceMaxY) - 1;
            if (y <= sliceMinY) {
                return y;
            }
            if (y + 1 < sliceMaxY) {
                while (y > 0 && y > sliceMinY) {
                    final BlockMD blockMDAbove = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, y + 1, z);
                    if (!blockMDAbove.isIgnore() && !blockMDAbove.hasFlag(BlockFlag.OpenToSky)) {
                        break;
                    }
                    --y;
                }
            }
            BlockMD blockMDAbove = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, y + 1, z);
            BlockMD blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, y, z);
            boolean inAirPocket = false;
            while (y > 0 && y > sliceMinY) {
                if (this.mapBathymetry && blockMD.isWater()) {
                    --y;
                }
                inAirPocket = blockMD.isIgnore();
                if (blockMDAbove.isIgnore() || blockMDAbove.hasTransparency() || blockMDAbove.hasFlag(BlockFlag.OpenToSky)) {
                    if (!blockMD.isIgnore() || !blockMD.hasTransparency()) {
                        break;
                    }
                    if (!blockMD.hasFlag(BlockFlag.OpenToSky)) {
                        break;
                    }
                }
                --y;
                blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, y, z);
                blockMDAbove = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, y + 1, z);
                if (y < sliceMinY && !inAirPocket) {
                    break;
                }
            }
        }
        catch (Exception e) {
            Journeymap.getLogger().warn("Couldn't get safe slice block height at " + x + "," + z + ": " + e);
            y = sliceMaxY;
        }
        y = Math.max(0, y);
        return blockSliceHeights[x][z] = y;
    }
    
    protected int getSliceLightLevel(final ChunkMD chunkMd, final int x, final int y, final int z, final boolean adjusted) {
        return this.mapCaveLighting ? chunkMd.getSavedLightValue(x, y + 1, z) : 15;
    }
}
