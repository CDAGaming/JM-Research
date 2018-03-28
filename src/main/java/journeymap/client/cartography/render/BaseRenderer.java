package journeymap.client.cartography.render;

import journeymap.client.cartography.IChunkRenderer;
import journeymap.client.cartography.Stratum;
import journeymap.client.cartography.color.RGB;
import journeymap.client.data.DataCache;
import journeymap.client.model.BlockCoordIntPair;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.MapType;
import journeymap.client.properties.CoreProperties;
import journeymap.common.Journeymap;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

public abstract class BaseRenderer implements IChunkRenderer {
    public static final int COLOR_BLACK;
    protected static final float[] DEFAULT_FOG;
    private static final String PROP_SLOPES = "slopes";
    private static final String PROP_HEIGHTS = "heights";
    private static final String PROP_WATER_HEIGHTS = "waterHeights";
    public static volatile AtomicLong badBlockCount;

    static {
        COLOR_BLACK = Color.black.getRGB();
        BaseRenderer.badBlockCount = new AtomicLong(0L);
        DEFAULT_FOG = new float[]{0.0f, 0.0f, 0.1f};
    }

    protected final DataCache dataCache;
    protected CoreProperties coreProperties;
    protected boolean mapBathymetry;
    protected boolean mapTransparency;
    protected boolean mapCaveLighting;
    protected boolean mapAntialiasing;
    protected float[] ambientColor;
    protected long lastPropFileUpdate;
    protected ArrayList<BlockCoordIntPair> primarySlopeOffsets;
    protected ArrayList<BlockCoordIntPair> secondarySlopeOffsets;
    protected float shadingSlopeMin;
    protected float shadingSlopeMax;
    protected float shadingPrimaryDownslopeMultiplier;
    protected float shadingPrimaryUpslopeMultiplier;
    protected float shadingSecondaryDownslopeMultiplier;
    protected float shadingSecondaryUpslopeMultiplier;
    protected float tweakMoonlightLevel;
    protected float tweakBrightenDaylightDiff;
    protected float tweakBrightenLightsourceBlock;
    protected float tweakBlendShallowWater;
    protected float tweakMinimumDarkenNightWater;
    protected float tweakWaterColorBlend;
    protected int tweakSurfaceAmbientColor;
    protected int tweakCaveAmbientColor;
    protected int tweakNetherAmbientColor;
    protected int tweakEndAmbientColor;
    private MapType currentMapType;

    public BaseRenderer() {
        this.dataCache = DataCache.INSTANCE;
        this.primarySlopeOffsets = new ArrayList<BlockCoordIntPair>(3);
        this.secondarySlopeOffsets = new ArrayList<BlockCoordIntPair>(4);
        this.updateOptions(null, null);
        this.shadingSlopeMin = 0.2f;
        this.shadingSlopeMax = 1.7f;
        this.shadingPrimaryDownslopeMultiplier = 0.65f;
        this.shadingPrimaryUpslopeMultiplier = 1.2f;
        this.shadingSecondaryDownslopeMultiplier = 0.95f;
        this.shadingSecondaryUpslopeMultiplier = 1.05f;
        this.tweakMoonlightLevel = 3.5f;
        this.tweakBrightenDaylightDiff = 0.06f;
        this.tweakBrightenLightsourceBlock = 1.2f;
        this.tweakBlendShallowWater = 0.15f;
        this.tweakMinimumDarkenNightWater = 0.25f;
        this.tweakWaterColorBlend = 0.5f;
        this.tweakSurfaceAmbientColor = 26;
        this.tweakCaveAmbientColor = 0;
        this.tweakNetherAmbientColor = 3344392;
        this.tweakEndAmbientColor = 26;
        this.primarySlopeOffsets.add(new BlockCoordIntPair(0, -1));
        this.primarySlopeOffsets.add(new BlockCoordIntPair(-1, -1));
        this.primarySlopeOffsets.add(new BlockCoordIntPair(-1, 0));
        this.secondarySlopeOffsets.add(new BlockCoordIntPair(-1, -2));
        this.secondarySlopeOffsets.add(new BlockCoordIntPair(-2, -1));
        this.secondarySlopeOffsets.add(new BlockCoordIntPair(-2, -2));
        this.secondarySlopeOffsets.add(new BlockCoordIntPair(-2, 0));
        this.secondarySlopeOffsets.add(new BlockCoordIntPair(0, -2));
    }

    protected boolean updateOptions(final ChunkMD chunkMd, final MapType mapType) {
        this.currentMapType = mapType;
        boolean updateNeeded = false;
        this.coreProperties = Journeymap.getClient().getCoreProperties();
        final long lastUpdate = Journeymap.getClient().getCoreProperties().lastModified();
        if (lastUpdate == 0L || this.lastPropFileUpdate != lastUpdate) {
            updateNeeded = true;
            this.lastPropFileUpdate = lastUpdate;
            this.mapBathymetry = this.coreProperties.mapBathymetry.get();
            this.mapTransparency = this.coreProperties.mapTransparency.get();
            this.mapAntialiasing = this.coreProperties.mapAntialiasing.get();
            this.mapCaveLighting = this.coreProperties.mapCaveLighting.get();
            this.ambientColor = new float[]{0.0f, 0.0f, 0.0f};
        }
        if (chunkMd != null) {
            final Long lastChunkUpdate = (Long) chunkMd.getProperty("lastPropFileUpdate", this.lastPropFileUpdate);
            updateNeeded = true;
            chunkMd.resetBlockData(this.getCurrentMapType());
            chunkMd.setProperty("lastPropFileUpdate", lastChunkUpdate);
        }
        return updateNeeded;
    }

    @Override
    public float[] getAmbientColor() {
        return BaseRenderer.DEFAULT_FOG;
    }

    @Override
    public void setStratumColors(final Stratum stratum, final int lightAttenuation, final Integer waterColor, final boolean waterAbove, final boolean underground, final boolean mapCaveLighting) {
        if (stratum.isUninitialized()) {
            throw new IllegalStateException("Stratum wasn't initialized for setStratumColors");
        }
        float dayAmbient = 15.0f;
        final boolean noSky = stratum.getWorldHasNoSky();
        float nightLightDiff;
        float daylightDiff;
        if (noSky) {
            dayAmbient = stratum.getWorldAmbientLight();
            daylightDiff = (nightLightDiff = Math.max(1.0f, Math.max(stratum.getLightLevel(), dayAmbient - lightAttenuation)) / 15.0f);
        } else {
            daylightDiff = Math.max(1.0f, Math.max(stratum.getLightLevel(), dayAmbient - lightAttenuation)) / 15.0f;
            daylightDiff += this.tweakBrightenDaylightDiff;
            nightLightDiff = Math.max(this.tweakMoonlightLevel, Math.max(stratum.getLightLevel(), this.tweakMoonlightLevel - lightAttenuation)) / 15.0f;
        }
        int basicColor = stratum.getBlockMD().getBlockColor(stratum.getChunkMd(), stratum.getBlockPos());
        final Block block = stratum.getBlockMD().getBlockState().getBlock();
        if (block == Blocks.GLOWSTONE || block == Blocks.LIT_REDSTONE_LAMP) {
            basicColor = RGB.adjustBrightness(basicColor, this.tweakBrightenLightsourceBlock);
        }
        if (waterAbove && waterColor != null) {
            final int adjustedWaterColor = waterColor;
            final int adjustedBasicColor = RGB.adjustBrightness(basicColor, Math.max(daylightDiff, nightLightDiff));
            stratum.setDayColor(RGB.blendWith(adjustedBasicColor, adjustedWaterColor, this.tweakWaterColorBlend));
            if (noSky) {
                stratum.setNightColor(stratum.getDayColor());
            } else {
                stratum.setNightColor(RGB.adjustBrightness(stratum.getDayColor(), Math.max(nightLightDiff, this.tweakMinimumDarkenNightWater)));
            }
        } else {
            stratum.setDayColor(RGB.adjustBrightness(basicColor, daylightDiff));
            if (noSky) {
                stratum.setNightColor(stratum.getDayColor());
            } else {
                stratum.setNightColor(RGB.darkenAmbient(basicColor, nightLightDiff, this.getAmbientColor()));
            }
        }
        if (underground) {
            stratum.setCaveColor(mapCaveLighting ? stratum.getNightColor() : stratum.getDayColor());
        }
    }

    protected Float[][] populateSlopes(final ChunkMD chunkMd, final Integer vSlice, final Float[][] slopes) {
        int y = 0;
        int sliceMinY = 0;
        int sliceMaxY = 0;
        final boolean isSurface = vSlice == null;
        if (!isSurface) {
            final int[] sliceBounds = this.getVSliceBounds(chunkMd, vSlice);
            sliceMinY = sliceBounds[0];
            sliceMaxY = sliceBounds[1];
        }
        for (int z = 0; z < 16; ++z) {
            for (int x = 0; x < 16; ++x) {
                y = this.getBlockHeight(chunkMd, x, vSlice, z, sliceMinY, sliceMaxY);
                Float slope;
                final Float primarySlope = slope = this.calculateSlope(chunkMd, this.primarySlopeOffsets, x, y, z, isSurface, vSlice, sliceMinY, sliceMaxY);
                if (slope < 1.0f) {
                    slope *= this.shadingPrimaryDownslopeMultiplier;
                } else if (slope > 1.0f) {
                    slope *= this.shadingPrimaryUpslopeMultiplier;
                }
                if (this.mapAntialiasing && primarySlope == 1.0f) {
                    final Float secondarySlope = this.calculateSlope(chunkMd, this.secondarySlopeOffsets, x, y, z, isSurface, vSlice, sliceMinY, sliceMaxY);
                    if (secondarySlope > primarySlope) {
                        slope *= this.shadingSecondaryUpslopeMultiplier;
                    } else if (secondarySlope < primarySlope) {
                        slope *= this.shadingSecondaryDownslopeMultiplier;
                    }
                }
                if (slope.isNaN()) {
                    slope = 1.0f;
                }
                slopes[x][z] = Math.min(this.shadingSlopeMax, Math.max(this.shadingSlopeMin, slope));
            }
        }
        return slopes;
    }

    protected MapType getCurrentMapType() {
        return this.currentMapType;
    }

    public abstract int getBlockHeight(final ChunkMD p0, final BlockPos p1);

    protected abstract Integer getBlockHeight(final ChunkMD p0, final int p1, final Integer p2, final int p3, final Integer p4, final Integer p5);

    protected int getOffsetBlockHeight(final ChunkMD chunkMd, final int x, final Integer vSlice, final int z, final Integer sliceMinY, final Integer sliceMaxY, final BlockCoordIntPair offset, final int defaultVal) {
        final int blockX = (chunkMd.getCoord().x << 4) + (x + offset.x);
        final int blockZ = (chunkMd.getCoord().z << 4) + (z + offset.z);
        final ChunkPos targetCoord = new ChunkPos(blockX >> 4, blockZ >> 4);
        ChunkMD targetChunkMd = null;
        if (targetCoord.equals((Object) chunkMd.getCoord())) {
            targetChunkMd = chunkMd;
        } else {
            targetChunkMd = this.dataCache.getChunkMD(targetCoord);
        }
        if (targetChunkMd != null) {
            return this.getBlockHeight(targetChunkMd, blockX & 0xF, vSlice, blockZ & 0xF, sliceMinY, sliceMaxY);
        }
        return defaultVal;
    }

    protected float calculateSlope(final ChunkMD chunkMd, final Collection<BlockCoordIntPair> offsets, final int x, final int y, final int z, final boolean isSurface, final Integer vSlice, final int sliceMinY, final int sliceMaxY) {
        if (y <= 0) {
            return 1.0f;
        }
        float slopeSum = 0.0f;
        for (final BlockCoordIntPair offset : offsets) {
            final float offsetHeight = this.getOffsetBlockHeight(chunkMd, x, vSlice, z, sliceMinY, sliceMaxY, offset, y);
            slopeSum += y * 1.0f / offsetHeight;
        }
        Float slope = slopeSum / offsets.size();
        if (slope.isNaN()) {
            slope = 1.0f;
        }
        return slope;
    }

    protected int[] getVSliceBounds(final ChunkMD chunkMd, final Integer vSlice) {
        if (vSlice == null) {
            return null;
        }
        final int sliceMinY = Math.max(vSlice << 4, 0);
        final int hardSliceMaxY = (vSlice + 1 << 4) - 1;
        int sliceMaxY = Math.min(hardSliceMaxY, chunkMd.getWorld().getActualHeight());
        if (sliceMinY >= sliceMaxY) {
            sliceMaxY = sliceMinY + 2;
        }
        return new int[]{sliceMinY, sliceMaxY};
    }

    protected float getSlope(final ChunkMD chunkMd, final int x, final Integer vSlice, final int z) {
        final Float[][] slopes = this.getSlopes(chunkMd, vSlice);
        Float slope = slopes[x][z];
        if (slope == null) {
            this.populateSlopes(chunkMd, vSlice, slopes);
            slope = slopes[x][z];
        }
        if (slope == null || slope.isNaN()) {
            Journeymap.getLogger().warn(String.format("Bad slope for %s at %s,%s: %s", chunkMd.getCoord(), x, z, slope));
            slope = 1.0f;
        }
        return slope;
    }

    protected final String getKey(final String propName, final Integer vSlice) {
        return (vSlice == null) ? propName : (propName + vSlice);
    }

    protected final Integer[][] getHeights(final ChunkMD chunkMd, final Integer vSlice) {
        return chunkMd.getBlockDataInts(this.getCurrentMapType()).get(this.getKey("heights", vSlice));
    }

    protected final boolean hasHeights(final ChunkMD chunkMd, final Integer vSlice) {
        return chunkMd.getBlockDataInts(this.getCurrentMapType()).has(this.getKey("heights", vSlice));
    }

    protected final void resetHeights(final ChunkMD chunkMd, final Integer vSlice) {
        chunkMd.getBlockDataInts(this.getCurrentMapType()).clear(this.getKey("heights", vSlice));
    }

    protected final Float[][] getSlopes(final ChunkMD chunkMd, final Integer vSlice) {
        return chunkMd.getBlockDataFloats(this.getCurrentMapType()).get(this.getKey("slopes", vSlice));
    }

    protected final boolean hasSlopes(final ChunkMD chunkMd, final Integer vSlice) {
        return chunkMd.getBlockDataFloats(this.getCurrentMapType()).has(this.getKey("slopes", vSlice));
    }

    protected final void resetSlopes(final ChunkMD chunkMd, final Integer vSlice) {
        chunkMd.getBlockDataFloats(this.getCurrentMapType()).clear(this.getKey("slopes", vSlice));
    }

    protected final Integer[][] getFluidHeights(final ChunkMD chunkMd, final Integer vSlice) {
        return chunkMd.getBlockDataInts(this.getCurrentMapType()).get(this.getKey("waterHeights", vSlice));
    }

    protected final boolean hasWaterHeights(final ChunkMD chunkMd, final Integer vSlice) {
        return chunkMd.getBlockDataInts(this.getCurrentMapType()).has(this.getKey("waterHeights", vSlice));
    }

    protected final void resetWaterHeights(final ChunkMD chunkMd, final Integer vSlice) {
        chunkMd.getBlockDataInts(this.getCurrentMapType()).clear(this.getKey("waterHeights", vSlice));
    }

    public ChunkMD getOffsetChunk(final ChunkMD chunkMd, final int x, final int z, final BlockCoordIntPair offset) {
        final int blockX = (chunkMd.getCoord().x << 4) + (x + offset.x);
        final int blockZ = (chunkMd.getCoord().z << 4) + (z + offset.z);
        final ChunkPos targetCoord = new ChunkPos(blockX >> 4, blockZ >> 4);
        if (targetCoord.equals((Object) chunkMd.getCoord())) {
            return chunkMd;
        }
        return this.dataCache.getChunkMD(targetCoord);
    }

    public void paintDimOverlay(final BufferedImage image, final int x, final int z, final float alpha) {
        final Integer color = image.getRGB(x, z);
        this.paintBlock(image, x, z, RGB.adjustBrightness(color, alpha));
    }

    public void paintDimOverlay(final BufferedImage sourceImage, final BufferedImage targetImage, final int x, final int z, final float alpha) {
        final Integer color = sourceImage.getRGB(x, z);
        this.paintBlock(targetImage, x, z, RGB.adjustBrightness(color, alpha));
    }

    public void paintBlock(final BufferedImage image, final int x, final int z, final int color) {
        image.setRGB(x, z, 0xFF000000 | color);
    }

    public void paintVoidBlock(final BufferedImage image, final int x, final int z) {
        this.paintBlock(image, x, z, RGB.toInteger(this.getAmbientColor()));
    }

    public void paintBlackBlock(final BufferedImage image, final int x, final int z) {
        this.paintBlock(image, x, z, BaseRenderer.COLOR_BLACK);
    }

    public void paintBadBlock(final BufferedImage image, final int x, final int y, final int z) {
        final long count = BaseRenderer.badBlockCount.incrementAndGet();
        if (count == 1L || count % 2046L == 0L) {
            Journeymap.getLogger().warn("Bad block at " + x + "," + y + "," + z + ". Total bad blocks: " + count);
        }
    }
}
