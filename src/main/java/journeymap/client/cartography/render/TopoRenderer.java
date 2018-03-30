package journeymap.client.cartography.render;

import journeymap.client.cartography.*;
import journeymap.client.log.*;
import journeymap.client.properties.*;
import journeymap.common.*;
import java.io.*;
import journeymap.client.render.*;
import journeymap.client.feature.*;
import journeymap.common.api.feature.*;
import java.awt.image.*;
import org.apache.logging.log4j.*;
import journeymap.common.log.*;
import journeymap.client.model.*;
import net.minecraft.util.math.*;
import journeymap.client.cartography.color.*;

public class TopoRenderer extends BaseRenderer implements IChunkRenderer
{
    private static final String PROP_SHORE = "isShore";
    private Integer[] waterPalette;
    private Integer[] landPalette;
    private int waterPaletteRange;
    private int landPaletteRange;
    private long lastTopoFileUpdate;
    protected StatTimer renderTopoTimer;
    private Integer landContourColor;
    private Integer waterContourColor;
    private double waterContourInterval;
    private double landContourInterval;
    TopoProperties topoProperties;
    
    public TopoRenderer() {
        this.renderTopoTimer = StatTimer.get("TopoRenderer.renderSurface");
        this.primarySlopeOffsets.clear();
        this.secondarySlopeOffsets.clear();
        this.primarySlopeOffsets.add(new BlockCoordIntPair(0, -1));
        this.primarySlopeOffsets.add(new BlockCoordIntPair(-1, 0));
        this.primarySlopeOffsets.add(new BlockCoordIntPair(0, 1));
        this.primarySlopeOffsets.add(new BlockCoordIntPair(1, 0));
    }
    
    @Override
    protected boolean updateOptions(final ChunkMD chunkMd, final MapView mapView) {
        boolean needUpdate = false;
        if (super.updateOptions(chunkMd, mapView)) {
            double worldHeight = 256.0;
            if (chunkMd != null) {
                worldHeight = chunkMd.getWorld().func_72800_K();
            }
            this.topoProperties = Journeymap.getClient().getTopoProperties();
            if (System.currentTimeMillis() - this.lastTopoFileUpdate > 5000L && this.lastTopoFileUpdate < this.topoProperties.lastModified()) {
                needUpdate = true;
                Journeymap.getLogger().info("Loading " + this.topoProperties.getFileName());
                this.topoProperties.load();
                this.lastTopoFileUpdate = this.topoProperties.lastModified();
                this.landContourColor = this.topoProperties.getLandContourColor();
                this.waterContourColor = this.topoProperties.getWaterContourColor();
                this.waterPalette = this.topoProperties.getWaterColors();
                this.waterPaletteRange = this.waterPalette.length - 1;
                this.waterContourInterval = worldHeight / Math.max(1, this.waterPalette.length);
                this.landPalette = this.topoProperties.getLandColors();
                this.landPaletteRange = this.landPalette.length - 1;
                this.landContourInterval = worldHeight / Math.max(1, this.landPalette.length);
            }
            if (chunkMd != null) {
                final Long lastUpdate = (Long)chunkMd.getProperty("lastTopoPropFileUpdate", this.lastTopoFileUpdate);
                if (needUpdate || lastUpdate < this.lastTopoFileUpdate) {
                    needUpdate = true;
                    chunkMd.resetBlockData(this.getCurrentMapView());
                }
                chunkMd.setProperty("lastTopoPropFileUpdate", this.lastTopoFileUpdate);
            }
        }
        return needUpdate;
    }
    
    @Override
    public boolean render(final ComparableBufferedImage chunkImage, final ChunkMD chunkMd, final Integer vSlice) {
        final StatTimer timer = this.renderTopoTimer;
        if (this.landPalette == null || this.landPalette.length < 1 || this.waterPalette == null || this.waterPalette.length < 1) {
            return false;
        }
        final int dimension = chunkMd.getDimension();
        if (!ClientFeatures.instance().isAllowed(Feature.MapType.Topo, dimension)) {
            return false;
        }
        try {
            timer.start();
            this.updateOptions(chunkMd, MapView.from(Feature.MapType.Topo, null, chunkMd.getDimension()));
            if (!this.hasSlopes(chunkMd, null)) {
                this.populateSlopes(chunkMd);
            }
            return this.renderSurface(chunkImage, chunkMd, vSlice, false);
        }
        catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
        finally {
            timer.stop();
        }
    }
    
    protected boolean renderSurface(final BufferedImage chunkImage, final ChunkMD chunkMd, final Integer vSlice, final boolean cavePrePass) {
        boolean chunkOk = false;
        try {
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    BlockMD topBlockMd = null;
                    int y = Math.max(0, this.getBlockHeight(chunkMd, x, null, z, null, null));
                    if (this.mapBathymetry) {
                        final Integer[][] waterHeights = this.getFluidHeights(chunkMd, null);
                        if (waterHeights[z] != null && waterHeights[z][x] != null) {
                            y = this.getFluidHeights(chunkMd, null)[z][x];
                        }
                    }
                    topBlockMd = chunkMd.getBlockMD(x, y, z);
                    if (topBlockMd == null) {
                        this.paintBadBlock(chunkImage, x, y, z);
                    }
                    else {
                        chunkOk = (this.paintContour(chunkImage, chunkMd, topBlockMd, x, y, z) || chunkOk);
                    }
                }
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().log(Level.WARN, "Error in renderSurface: " + LogFormatter.toString(t));
        }
        return chunkOk;
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
        try {
            BlockMD blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, localX, y, localZ);
            while (y > 0) {
                if (blockMD.isWater() || blockMD.isIce()) {
                    if (!this.mapBathymetry) {
                        break;
                    }
                    this.getFluidHeights(chunkMd, null)[localZ][localX] = y;
                }
                else if (!blockMD.hasAnyFlag(BlockMD.FlagsPlantAndCrop)) {
                    if (!blockMD.isIgnore() && !blockMD.hasFlag(BlockFlag.NoTopo)) {
                        break;
                    }
                }
                --y;
                blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, localX, y, localZ);
            }
        }
        catch (Exception e) {
            Journeymap.getLogger().debug("Couldn't get safe surface block height at " + localX + "," + localZ + ": " + e);
        }
        y = Math.max(0, y);
        return heights[localX][localZ] = y;
    }
    
    protected Float[][] populateSlopes(final ChunkMD chunkMd) {
        final Float[][] slopes = this.getSlopes(chunkMd, null);
        final float nearZero = 1.0E-4f;
        for (int z = 0; z < 16; ++z) {
            for (int x = 0; x < 16; ++x) {
                float h = this.getBlockHeight(chunkMd, x, null, z, null, null);
                final BlockMD blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, (int)h, z);
                boolean isWater = false;
                double contourInterval;
                if (blockMD.isWater() || blockMD.isIce() || (this.mapBathymetry && this.getFluidHeights(chunkMd, null)[z][x] != null)) {
                    isWater = true;
                    contourInterval = this.waterContourInterval;
                }
                else {
                    contourInterval = this.landContourInterval;
                }
                final float[] heights = new float[this.primarySlopeOffsets.size()];
                Float lastOffsetHeight = null;
                boolean flatOffsets = true;
                boolean isShore = false;
                for (int i = 0; i < heights.length; ++i) {
                    final BlockCoordIntPair offset = this.primarySlopeOffsets.get(i);
                    float offsetHeight = this.getOffsetBlockHeight(chunkMd, x, null, z, null, null, offset, (int)h);
                    if (isWater && !isShore) {
                        final ChunkMD targetChunkMd = this.getOffsetChunk(chunkMd, x, z, offset);
                        final int newX = (chunkMd.getCoord().field_77276_a << 4) + (x + offset.x) & 0xF;
                        final int newZ = (chunkMd.getCoord().field_77275_b << 4) + (z + offset.z) & 0xF;
                        if (targetChunkMd != null) {
                            if (this.mapBathymetry && this.mapBathymetry && this.getFluidHeights(chunkMd, null)[z][x] == null) {
                                isShore = true;
                            }
                            else {
                                final int ceiling = targetChunkMd.ceiling(newX, newZ);
                                final BlockMD offsetBlock = targetChunkMd.getBlockMD(newX, ceiling, newZ);
                                if (!offsetBlock.isWater() && !offsetBlock.isIce()) {
                                    isShore = true;
                                }
                            }
                        }
                    }
                    offsetHeight = (float)Math.max(nearZero, offsetHeight - offsetHeight % contourInterval);
                    heights[i] = offsetHeight;
                    if (lastOffsetHeight == null) {
                        lastOffsetHeight = offsetHeight;
                    }
                    else if (flatOffsets) {
                        flatOffsets = (lastOffsetHeight == offsetHeight);
                    }
                }
                if (isWater) {
                    this.getShore(chunkMd)[z][x] = isShore;
                }
                h = (float)Math.max(nearZero, h - h % contourInterval);
                Float slope;
                if (flatOffsets) {
                    slope = 1.0f;
                }
                else {
                    slope = 0.0f;
                    for (final float offsetHeight2 : heights) {
                        slope += h / offsetHeight2;
                    }
                    slope /= (Float)heights.length;
                }
                if (slope.isNaN() || slope.isInfinite()) {
                    slope = 1.0f;
                }
                slopes[x][z] = slope;
            }
        }
        return slopes;
    }
    
    @Override
    public int getBlockHeight(final ChunkMD chunkMd, final BlockPos blockPos) {
        return Journeymap.clientWorld().func_175726_f(blockPos).func_177440_h(blockPos).func_177956_o();
    }
    
    protected boolean paintContour(final BufferedImage chunkImage, final ChunkMD chunkMd, final BlockMD topBlockMd, final int x, final int y, final int z) {
        if (!chunkMd.hasChunk()) {
            return false;
        }
        try {
            final float slope = this.getSlope(chunkMd, x, null, z);
            final boolean isWater = topBlockMd.isWater() || topBlockMd.isIce();
            int color;
            if (slope > 1.0f) {
                color = (isWater ? this.waterContourColor : this.landContourColor);
            }
            else if (topBlockMd.isLava()) {
                color = topBlockMd.getTextureColor();
            }
            else if (isWater) {
                if (this.getShore(chunkMd)[z][x] == Boolean.TRUE) {
                    color = this.waterContourColor;
                }
                else {
                    int index = (int)Math.floor((y - y % this.waterContourInterval) / this.waterContourInterval);
                    index = Math.max(0, Math.min(index, this.waterPaletteRange));
                    color = this.waterPalette[index];
                    if (slope < 1.0f) {
                        color = RGB.adjustBrightness(color, 0.9f);
                    }
                }
            }
            else {
                int index = (int)Math.floor((y - y % this.landContourInterval) / this.landContourInterval);
                index = Math.max(0, Math.min(index, this.landPaletteRange));
                color = this.landPalette[index];
                if (slope < 1.0f) {
                    color = RGB.adjustBrightness(color, 0.85f);
                }
            }
            this.paintBlock(chunkImage, x, z, color);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    
    protected final Boolean[][] getShore(final ChunkMD chunkMd) {
        return chunkMd.getBlockDataBooleans(this.getCurrentMapView()).get("isShore");
    }
    
    protected final boolean hasShore(final ChunkMD chunkMd) {
        return chunkMd.getBlockDataBooleans(this.getCurrentMapView()).has("isShore");
    }
    
    protected final void resetShore(final ChunkMD chunkMd) {
        chunkMd.getBlockDataBooleans(this.getCurrentMapView()).clear("isShore");
    }
}
