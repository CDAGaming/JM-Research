package journeymap.client.cartography.render;

import journeymap.client.log.*;
import journeymap.client.cartography.color.*;
import net.minecraft.util.math.*;
import journeymap.client.render.*;
import java.awt.image.*;
import journeymap.client.feature.*;
import journeymap.common.api.feature.*;
import journeymap.common.*;
import journeymap.common.log.*;
import journeymap.client.model.*;
import journeymap.client.cartography.*;

public class SurfaceRenderer extends BaseRenderer implements IChunkRenderer
{
    protected StatTimer renderSurfaceTimer;
    protected StatTimer renderSurfacePrepassTimer;
    protected Strata strata;
    protected float maxDepth;
    
    public SurfaceRenderer() {
        this.renderSurfaceTimer = StatTimer.get("SurfaceRenderer.renderSurface");
        this.renderSurfacePrepassTimer = StatTimer.get("SurfaceRenderer.renderSurface.CavePrepass");
        this.strata = new Strata("Surface", 40, 8, false);
        this.maxDepth = 8.0f;
        this.updateOptions(null, null);
    }
    
    @Override
    protected boolean updateOptions(final ChunkMD chunkMd, final MapView mapView) {
        if (super.updateOptions(chunkMd, mapView)) {
            this.ambientColor = RGB.floats(this.tweakSurfaceAmbientColor);
            return true;
        }
        return false;
    }
    
    @Override
    public int getBlockHeight(final ChunkMD chunkMd, final BlockPos blockPos) {
        final Integer y = this.getBlockHeight(chunkMd, blockPos.func_177958_n() & 0xF, null, blockPos.func_177952_p() & 0xF, null, null);
        return (y == null) ? blockPos.func_177956_o() : y;
    }
    
    @Override
    public boolean render(final ComparableBufferedImage dayChunkImage, final ChunkMD chunkMd, final Integer ignored) {
        return this.render(dayChunkImage, null, chunkMd, null, false);
    }
    
    public boolean render(final ComparableBufferedImage dayChunkImage, final BufferedImage nightChunkImage, final ChunkMD chunkMd) {
        return this.render(dayChunkImage, nightChunkImage, chunkMd, null, false);
    }
    
    public synchronized boolean render(ComparableBufferedImage dayChunkImage, BufferedImage nightChunkImage, final ChunkMD chunkMd, final Integer vSlice, final boolean cavePrePass) {
        final StatTimer timer = cavePrePass ? this.renderSurfacePrepassTimer : this.renderSurfaceTimer;
        try {
            timer.start();
            Feature.MapType mapType = null;
            final int dimension = chunkMd.getDimension();
            final boolean renderDay = ClientFeatures.instance().isAllowed(Feature.MapType.Day, dimension);
            final boolean renderNight = ClientFeatures.instance().isAllowed(Feature.MapType.Night, dimension);
            if (renderDay) {
                mapType = Feature.MapType.Day;
            }
            else {
                if (!renderNight) {
                    return false;
                }
                mapType = Feature.MapType.Night;
            }
            this.updateOptions(chunkMd, MapView.from(mapType, null, dimension));
            if (!this.hasSlopes(chunkMd, vSlice)) {
                this.populateSlopes(chunkMd, vSlice, this.getSlopes(chunkMd, vSlice));
            }
            if (!renderDay) {
                dayChunkImage = null;
            }
            if (!renderNight) {
                nightChunkImage = null;
            }
            return this.renderSurface(dayChunkImage, nightChunkImage, chunkMd, vSlice, cavePrePass);
        }
        catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
        finally {
            this.strata.reset();
            timer.stop();
        }
    }
    
    protected boolean renderSurface(final BufferedImage dayChunkImage, final BufferedImage nightChunkImage, final ChunkMD chunkMd, final Integer vSlice, final boolean cavePrePass) {
        boolean chunkOk = false;
        try {
            int sliceMaxY = 0;
            if (cavePrePass) {
                final int[] sliceBounds = this.getVSliceBounds(chunkMd, vSlice);
                sliceMaxY = sliceBounds[1];
            }
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    this.strata.reset();
                    int upperY = Math.max(0, chunkMd.getPrecipitationHeight(x, z));
                    final int lowerY = Math.max(0, this.getBlockHeight(chunkMd, x, null, z, null, null));
                    if (lowerY == 0 && chunkMd.getBlockMD(x, lowerY, z).isIgnore()) {
                        this.paintVoidBlock(dayChunkImage, x, z);
                        if (!cavePrePass && nightChunkImage != null) {
                            this.paintVoidBlock(nightChunkImage, x, z);
                        }
                        chunkOk = true;
                    }
                    else if (cavePrePass && upperY > sliceMaxY && upperY - sliceMaxY > this.maxDepth) {
                        chunkOk = true;
                        this.paintBlackBlock(dayChunkImage, x, z);
                    }
                    else {
                        final boolean showSlope = !chunkMd.getBlockMD(x, lowerY, z).hasNoShadow();
                        if (this.mapBathymetry) {
                            final Integer[][] waterHeights = this.getFluidHeights(chunkMd, null);
                            final Integer waterHeight = waterHeights[z][x];
                            if (waterHeight != null) {
                                upperY = waterHeight;
                            }
                        }
                        this.buildStrata(this.strata, upperY, chunkMd, x, lowerY, z);
                        chunkOk = (this.paintStrata(this.strata, dayChunkImage, nightChunkImage, chunkMd, x, z, showSlope, cavePrePass) || chunkOk);
                    }
                }
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().warn(LogFormatter.toString(t));
        }
        finally {
            this.strata.reset();
        }
        return chunkOk;
    }
    
    public int getSurfaceBlockHeight(final ChunkMD chunkMd, final int x, final int z, final BlockCoordIntPair offset, final int defaultVal) {
        final ChunkMD targetChunkMd = this.getOffsetChunk(chunkMd, x, z, offset);
        final int newX = (chunkMd.getCoord().field_77276_a << 4) + (x + offset.x) & 0xF;
        final int newZ = (chunkMd.getCoord().field_77275_b << 4) + (z + offset.z) & 0xF;
        if (targetChunkMd == null) {
            return defaultVal;
        }
        final Integer height = this.getBlockHeight(targetChunkMd, newX, null, newZ, null, null);
        if (height == null) {
            return defaultVal;
        }
        return height;
    }
    
    public Integer getBlockHeight(final ChunkMD chunkMd, final int localX, final Integer vSlice, final int localZ, final Integer sliceMinY, final Integer sliceMaxY) {
        final Integer[][] heights = this.getHeights(chunkMd, null);
        if (heights == null) {
            return null;
        }
        Integer y = heights[localX][localZ];
        if (y != null) {
            return y;
        }
        y = Math.max(0, chunkMd.getPrecipitationHeight(localX, localZ));
        if (y == 0) {
            return 0;
        }
        boolean setFluidHeight = true;
        try {
            while (y > 0) {
                final BlockMD blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, localX, y, localZ);
                if (blockMD.isIgnore()) {
                    --y;
                }
                else if (blockMD.isWater() || blockMD.isFluid()) {
                    if (!this.mapBathymetry) {
                        break;
                    }
                    if (setFluidHeight) {
                        this.getFluidHeights(chunkMd, null)[localZ][localX] = y;
                        setFluidHeight = false;
                    }
                    --y;
                }
                else if (blockMD.hasTransparency() && this.mapTransparency) {
                    --y;
                }
                else {
                    if (blockMD.isLava()) {
                        break;
                    }
                    if (blockMD.hasNoShadow()) {
                        --y;
                        break;
                    }
                    break;
                }
            }
        }
        catch (Exception e) {
            Journeymap.getLogger().warn(String.format("Couldn't get safe surface block height for %s coords %s,%s: %s", chunkMd, localX, localZ, LogFormatter.toString(e)));
        }
        y = Math.max(0, y);
        return heights[localX][localZ] = y;
    }
    
    protected void buildStrata(final Strata strata, int upperY, final ChunkMD chunkMd, final int x, int lowerY, final int z) {
        while (upperY > lowerY) {
            final BlockMD blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, upperY, z);
            if (!blockMD.isIgnore()) {
                if (blockMD.hasTransparency()) {
                    strata.push(chunkMd, blockMD, x, upperY, z);
                    if (!this.mapTransparency) {
                        break;
                    }
                }
                if (blockMD.hasNoShadow()) {
                    lowerY = upperY;
                    break;
                }
            }
            --upperY;
        }
        if (this.mapTransparency || strata.isEmpty()) {
            while (lowerY >= 0) {
                if (upperY - lowerY >= this.maxDepth) {
                    break;
                }
                final BlockMD blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, lowerY, z);
                if (!blockMD.isIgnore()) {
                    strata.push(chunkMd, blockMD, x, lowerY, z);
                    if (!blockMD.hasTransparency()) {
                        break;
                    }
                    if (!this.mapTransparency) {
                        break;
                    }
                }
                --lowerY;
            }
        }
    }
    
    protected boolean paintStrata(final Strata strata, final BufferedImage dayChunkImage, final BufferedImage nightChunkImage, final ChunkMD chunkMd, final int x, final int z, final boolean showSlope, final boolean cavePrePass) {
        final int y = strata.getTopY();
        if (strata.isEmpty()) {
            if (dayChunkImage != null) {
                this.paintBadBlock(dayChunkImage, x, y, z);
            }
            if (nightChunkImage != null) {
                this.paintBadBlock(nightChunkImage, x, y, z);
            }
            return false;
        }
        try {
            while (!strata.isEmpty()) {
                final Stratum stratum = strata.nextUp(this, true);
                if (strata.getRenderDayColor() == null || strata.getRenderNightColor() == null) {
                    strata.setRenderDayColor(stratum.getDayColor());
                    if (!cavePrePass) {
                        strata.setRenderNightColor(stratum.getNightColor());
                    }
                }
                else {
                    strata.setRenderDayColor(RGB.blendWith(strata.getRenderDayColor(), stratum.getDayColor(), stratum.getBlockMD().getAlpha()));
                    if (!cavePrePass) {
                        strata.setRenderNightColor(RGB.blendWith(strata.getRenderNightColor(), stratum.getNightColor(), stratum.getBlockMD().getAlpha()));
                    }
                }
                strata.release(stratum);
            }
            if (strata.getRenderDayColor() == null) {
                this.paintBadBlock(dayChunkImage, x, y, z);
                this.paintBadBlock(nightChunkImage, x, y, z);
                return false;
            }
            if (nightChunkImage != null && strata.getRenderNightColor() == null) {
                this.paintBadBlock(nightChunkImage, x, y, z);
                return false;
            }
            if (showSlope) {
                final float slope = this.getSlope(chunkMd, x, null, z);
                if (slope != 1.0f) {
                    strata.setRenderDayColor(RGB.bevelSlope(strata.getRenderDayColor(), slope));
                    if (!cavePrePass) {
                        strata.setRenderNightColor(RGB.bevelSlope(strata.getRenderNightColor(), slope));
                    }
                }
            }
            this.paintBlock(dayChunkImage, x, z, strata.getRenderDayColor());
            if (nightChunkImage != null) {
                this.paintBlock(nightChunkImage, x, z, strata.getRenderNightColor());
            }
        }
        catch (RuntimeException e) {
            throw e;
        }
        return true;
    }
}
