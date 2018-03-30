package journeymap.client.render.map;

import net.minecraft.util.math.*;
import java.awt.*;
import org.apache.logging.log4j.*;
import journeymap.common.*;
import java.io.*;
import journeymap.client.*;
import journeymap.client.log.*;
import journeymap.client.properties.*;
import journeymap.client.ui.minimap.*;
import journeymap.client.ui.fullscreen.*;
import journeymap.client.io.*;
import java.util.*;
import java.awt.geom.*;
import journeymap.client.model.*;

public class Tile
{
    public static final int TILESIZE = 512;
    public static final int LOAD_RADIUS = 768;
    static String debugGlSettings;
    final int zoom;
    final int tileX;
    final int tileZ;
    final ChunkPos ulChunk;
    final ChunkPos lrChunk;
    final Point ulBlock;
    final Point lrBlock;
    final ArrayList<TileDrawStep> drawSteps;
    private final Logger logger;
    private final int theHashCode;
    private final String theCacheKey;
    int renderType;
    int textureFilter;
    int textureWrap;
    
    private Tile(final int tileX, final int tileZ, final int zoom) {
        this.drawSteps = new ArrayList<TileDrawStep>();
        this.logger = Journeymap.getLogger();
        this.renderType = 0;
        this.textureFilter = 0;
        this.textureWrap = 0;
        this.tileX = tileX;
        this.tileZ = tileZ;
        this.zoom = zoom;
        this.theCacheKey = toCacheKey(tileX, tileZ, zoom);
        this.theHashCode = this.theCacheKey.hashCode();
        final int distance = 32 / (int)Math.pow(2.0, zoom);
        this.ulChunk = new ChunkPos(tileX * distance, tileZ * distance);
        this.lrChunk = new ChunkPos(this.ulChunk.field_77276_a + distance - 1, this.ulChunk.field_77275_b + distance - 1);
        this.ulBlock = new Point(this.ulChunk.field_77276_a * 16, this.ulChunk.field_77275_b * 16);
        this.lrBlock = new Point(this.lrChunk.field_77276_a * 16 + 15, this.lrChunk.field_77275_b * 16 + 15);
        this.updateRenderType();
    }
    
    public static Tile create(final int tileX, final int tileZ, final int zoom, final File worldDir, final MapView mapView, final boolean highQuality) {
        final Tile tile = new Tile(tileX, tileZ, zoom);
        tile.updateTexture(worldDir, mapView, highQuality);
        return tile;
    }
    
    public static int blockPosToTile(final int b, final int zoom) {
        final int tile = b >> 9 - zoom;
        return tile;
    }
    
    public static int tileToBlock(final int t, final int zoom) {
        return t << 9 - zoom;
    }
    
    public static String toCacheKey(final int tileX, final int tileZ, final int zoom) {
        return "" + tileX + "," + tileZ + "@" + zoom;
    }
    
    public static void switchTileRenderType() {
        final CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
        int type = coreProperties.tileRenderType.incrementAndGet();
        if (type > 4) {
            type = 1;
            coreProperties.tileRenderType.set(type);
        }
        coreProperties.save();
        final String msg = String.format("%s: %s (%s)", Constants.getString("jm.advanced.tile_render_type"), type, Tile.debugGlSettings);
        ChatLog.announceError(msg);
        resetTileDisplay();
    }
    
    public static void switchTileDisplayQuality() {
        final CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
        final boolean high = !coreProperties.tileHighDisplayQuality.get();
        coreProperties.tileHighDisplayQuality.set(Boolean.valueOf(high));
        coreProperties.save();
        ChatLog.announceError(Constants.getString("jm.common.tile_display_quality") + ": " + (high ? Constants.getString("jm.common.on") : Constants.getString("jm.common.off")));
        resetTileDisplay();
    }
    
    private static void resetTileDisplay() {
        TileDrawStepCache.instance().invalidateAll();
        RegionImageCache.INSTANCE.clear();
        MiniMap.state().requireRefresh();
        Fullscreen.state().requireRefresh();
    }
    
    public boolean updateTexture(final File worldDir, final MapView mapView, final boolean highQuality) {
        this.updateRenderType();
        this.drawSteps.clear();
        this.drawSteps.addAll(RegionImageHandler.getTileDrawSteps(worldDir, this.ulChunk, this.lrChunk, mapView, this.zoom, highQuality));
        return this.drawSteps.size() > 1;
    }
    
    public boolean hasTexture(final MapView mapView) {
        if (this.drawSteps.isEmpty()) {
            return false;
        }
        for (final TileDrawStep tileDrawStep : this.drawSteps) {
            if (tileDrawStep.hasTexture(mapView)) {
                return true;
            }
        }
        return false;
    }
    
    public void clear() {
        this.drawSteps.clear();
    }
    
    private void updateRenderType() {
        switch (this.renderType = Journeymap.getClient().getCoreProperties().tileRenderType.get()) {
            case 4: {
                this.textureFilter = 9728;
                this.textureWrap = 33071;
                Tile.debugGlSettings = "GL_NEAREST, GL_CLAMP_TO_EDGE";
                break;
            }
            case 3: {
                this.textureFilter = 9728;
                this.textureWrap = 33648;
                Tile.debugGlSettings = "GL_NEAREST, GL_MIRRORED_REPEAT";
                break;
            }
            case 2: {
                this.textureFilter = 9729;
                this.textureWrap = 33071;
                Tile.debugGlSettings = "GL_LINEAR, GL_CLAMP_TO_EDGE";
                break;
            }
            default: {
                this.textureFilter = 9729;
                this.textureWrap = 33648;
                Tile.debugGlSettings = "GL_LINEAR, GL_MIRRORED_REPEAT";
                break;
            }
        }
    }
    
    @Override
    public String toString() {
        return "Tile [ r" + this.tileX + ", r" + this.tileZ + " (zoom " + this.zoom + ") ]";
    }
    
    public String cacheKey() {
        return this.theCacheKey;
    }
    
    @Override
    public int hashCode() {
        return this.theHashCode;
    }
    
    public Point2D blockPixelOffsetInTile(final double x, final double z) {
        if (x < this.ulBlock.x || Math.floor(x) > this.lrBlock.x || z < this.ulBlock.y || Math.floor(z) > this.lrBlock.y) {
            throw new RuntimeException("Block " + x + "," + z + " isn't in " + this);
        }
        double localBlockX = this.ulBlock.x - x;
        if (x < 0.0) {
            ++localBlockX;
        }
        double localBlockZ = this.ulBlock.y - z;
        if (z < 0.0) {
            ++localBlockZ;
        }
        final int blockSize = (int)Math.pow(2.0, this.zoom);
        final double pixelOffsetX = 256.0 + localBlockX * blockSize - blockSize / 2;
        final double pixelOffsetZ = 256.0 + localBlockZ * blockSize - blockSize / 2;
        return new Point2D.Double(pixelOffsetX, pixelOffsetZ);
    }
    
    boolean draw(final TilePos pos, final double offsetX, final double offsetZ, final float alpha, final GridSpec gridSpec) {
        boolean somethingDrew = false;
        for (final TileDrawStep tileDrawStep : this.drawSteps) {
            final boolean ok = tileDrawStep.draw(pos, offsetX, offsetZ, alpha, this.textureFilter, this.textureWrap, gridSpec);
            if (ok) {
                somethingDrew = true;
            }
        }
        return somethingDrew;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Tile tile = (Tile)o;
        return this.tileX == tile.tileX && this.tileZ == tile.tileZ && this.zoom == tile.zoom;
    }
    
    static {
        Tile.debugGlSettings = "";
    }
}
