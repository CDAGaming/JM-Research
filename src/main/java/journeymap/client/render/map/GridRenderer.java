package journeymap.client.render.map;

import org.apache.logging.log4j.*;
import journeymap.common.api.feature.*;
import journeymap.client.log.*;
import journeymap.client.api.util.*;
import java.awt.geom.*;
import java.io.*;
import java.nio.*;
import journeymap.common.*;
import net.minecraftforge.fml.client.*;
import org.lwjgl.*;
import java.awt.image.*;
import net.minecraft.client.*;
import java.awt.*;
import net.minecraft.util.math.*;
import journeymap.client.data.*;
import java.util.*;
import journeymap.client.render.draw.*;
import journeymap.client.model.*;
import org.lwjgl.opengl.*;
import journeymap.client.api.event.*;
import journeymap.client.api.impl.*;
import org.lwjgl.util.glu.*;

public class GridRenderer
{
    private static boolean enabled;
    private static HashMap<String, String> messages;
    private final TilePos centerPos;
    private final Logger logger;
    private final boolean debug;
    private final TreeMap<TilePos, Tile> grid;
    private final Point2D.Double centerPixelOffset;
    private final int maxGlErrors = 20;
    private final Feature.Display contextUi;
    StatTimer updateTilesTimer1;
    StatTimer updateTilesTimer2;
    private UIState uiState;
    private int glErrors;
    private int gridSize;
    private double srcSize;
    private Rectangle2D.Double viewPort;
    private Rectangle2D.Double screenBounds;
    private AxisAlignedBB blockBounds;
    private int lastHeight;
    private int lastWidth;
    private MapView mapView;
    private String centerTileKey;
    private int zoom;
    private double centerBlockX;
    private double centerBlockZ;
    private File worldDir;
    private double currentRotation;
    private IntBuffer viewportBuf;
    private FloatBuffer modelMatrixBuf;
    private FloatBuffer projMatrixBuf;
    private FloatBuffer winPosBuf;
    private FloatBuffer objPosBuf;
    
    public GridRenderer(final Feature.Display contextUi, final int gridSize) {
        this.centerPos = new TilePos(0, 0);
        this.logger = Journeymap.getLogger();
        this.debug = this.logger.isDebugEnabled();
        this.grid = new TreeMap<TilePos, Tile>();
        this.centerPixelOffset = new Point2D.Double();
        this.updateTilesTimer1 = StatTimer.get("GridRenderer.updateTiles(1)", 5, 500);
        this.updateTilesTimer2 = StatTimer.get("GridRenderer.updateTiles(2)", 5, 500);
        this.glErrors = 0;
        this.viewPort = null;
        this.screenBounds = null;
        this.blockBounds = null;
        this.lastHeight = -1;
        this.lastWidth = -1;
        this.centerTileKey = "";
        this.contextUi = contextUi;
        this.uiState = UIState.newInactive(contextUi, FMLClientHandler.instance().getClient());
        this.viewportBuf = BufferUtils.createIntBuffer(16);
        this.modelMatrixBuf = BufferUtils.createFloatBuffer(16);
        this.projMatrixBuf = BufferUtils.createFloatBuffer(16);
        this.winPosBuf = BufferUtils.createFloatBuffer(16);
        this.objPosBuf = BufferUtils.createFloatBuffer(16);
        this.setGridSize(gridSize);
    }
    
    public static void addDebugMessage(final String key, final String message) {
        GridRenderer.messages.put(key, message);
    }
    
    public static void removeDebugMessage(final String key, final String message) {
        GridRenderer.messages.remove(key);
    }
    
    public static void clearDebugMessages() {
        GridRenderer.messages.clear();
    }
    
    public static void setEnabled(final boolean enabled) {
        if (!(GridRenderer.enabled = enabled)) {
            TileDrawStepCache.clear();
        }
    }
    
    public Feature.Display getDisplay() {
        return this.contextUi;
    }
    
    public void setViewPort(final Rectangle2D.Double viewPort) {
        this.viewPort = viewPort;
        this.screenBounds = null;
        this.updateBounds(this.lastWidth, this.lastHeight);
    }
    
    private void populateGrid(final Tile centerTile) {
        final int endRow = (this.gridSize - 1) / 2;
        final int endCol = (this.gridSize - 1) / 2;
        final int startRow = -endRow;
        final int startCol = -endCol;
        for (int z = startRow; z <= endRow; ++z) {
            for (int x = startCol; x <= endCol; ++x) {
                final TilePos pos = new TilePos(x, z);
                final Tile tile = this.findNeighbor(centerTile, pos);
                this.grid.put(pos, tile);
            }
        }
    }
    
    public void move(final int deltaBlockX, final int deltaBlockZ) {
        this.center(this.worldDir, this.mapView, this.centerBlockX + deltaBlockX, this.centerBlockZ + deltaBlockZ, this.zoom);
    }
    
    public boolean center() {
        return this.center(this.worldDir, this.mapView, this.centerBlockX, this.centerBlockZ, this.zoom);
    }
    
    public boolean hasUnloadedTile() {
        return this.hasUnloadedTile(false);
    }
    
    public int getGridSize() {
        return this.gridSize;
    }
    
    public void setGridSize(final int gridSize) {
        this.gridSize = gridSize;
        this.srcSize = gridSize * 512;
    }
    
    public boolean hasUnloadedTile(final boolean preview) {
        for (final Map.Entry<TilePos, Tile> entry : this.grid.entrySet()) {
            if (this.isOnScreen(entry.getKey())) {
                final Tile tile = entry.getValue();
                if (tile == null || !tile.hasTexture(this.mapView)) {
                    return true;
                }
                continue;
            }
        }
        return false;
    }
    
    public boolean center(final File worldDir, final MapView mapView, final double blockX, final double blockZ, final int zoom) {
        final boolean mapTypeChanged = !Objects.equals(worldDir, this.worldDir) || !Objects.equals(mapView, this.mapView);
        if (!Objects.equals(worldDir, this.worldDir)) {
            this.worldDir = worldDir;
        }
        if (blockX == this.centerBlockX && blockZ == this.centerBlockZ && zoom == this.zoom && !mapTypeChanged && !this.grid.isEmpty()) {
            if (!Objects.equals(mapView.mapType, this.uiState.mapType)) {
                this.updateUIState(true);
            }
            return false;
        }
        this.centerBlockX = blockX;
        this.centerBlockZ = blockZ;
        this.zoom = zoom;
        final int tileX = Tile.blockPosToTile((int)Math.floor(blockX), this.zoom);
        final int tileZ = Tile.blockPosToTile((int)Math.floor(blockZ), this.zoom);
        final String newCenterKey = Tile.toCacheKey(tileX, tileZ, zoom);
        final boolean centerTileChanged = !newCenterKey.equals(this.centerTileKey);
        this.centerTileKey = newCenterKey;
        if (mapTypeChanged || centerTileChanged || this.grid.isEmpty()) {
            final Tile newCenterTile = this.findTile(tileX, tileZ, zoom);
            this.populateGrid(newCenterTile);
            if (this.debug) {
                this.logger.debug("Centered on " + newCenterTile + " with pixel offsets of " + this.centerPixelOffset.x + "," + this.centerPixelOffset.y);
                final Minecraft mc = FMLClientHandler.instance().getClient();
                final BufferedImage tmp = new BufferedImage(mc.field_71443_c, mc.field_71440_d, 2);
                final Graphics2D g = tmp.createGraphics();
                g.setStroke(new BasicStroke(1.0f));
                g.setColor(Color.GREEN);
                g.drawLine(mc.field_71443_c / 2, 0, mc.field_71443_c / 2, mc.field_71440_d);
                g.drawLine(0, mc.field_71440_d / 2, mc.field_71443_c, mc.field_71440_d / 2);
            }
        }
        this.updateUIState(true);
        return true;
    }
    
    public void updateTiles(final MapView mapView, final int zoom, final boolean highQuality, final int width, final int height, final boolean fullUpdate, final double xOffset, final double yOffset) {
        this.updateTilesTimer1.start();
        this.mapView = mapView;
        this.zoom = zoom;
        this.updateBounds(width, height);
        Tile centerTile = this.grid.get(this.centerPos);
        if (centerTile == null || centerTile.zoom != this.zoom) {
            final int tileX = Tile.blockPosToTile((int)Math.floor(this.centerBlockX), this.zoom);
            final int tileZ = Tile.blockPosToTile((int)Math.floor(this.centerBlockZ), this.zoom);
            centerTile = this.findTile(tileX, tileZ, this.zoom);
            this.populateGrid(centerTile);
        }
        final Point2D blockPixelOffset = centerTile.blockPixelOffsetInTile(this.centerBlockX, this.centerBlockZ);
        final double blockSizeOffset = Math.pow(2.0, zoom) / 2.0;
        final int magic = ((this.gridSize == 5) ? 2 : 1) * 512;
        double displayOffsetX = xOffset + magic - (this.srcSize - this.lastWidth) / 2.0;
        if (this.centerBlockX < 0.0) {
            displayOffsetX -= blockSizeOffset;
        }
        else {
            displayOffsetX += blockSizeOffset;
        }
        double displayOffsetY = yOffset + magic - (this.srcSize - this.lastHeight) / 2.0;
        if (this.centerBlockZ < 0.0) {
            displayOffsetY -= blockSizeOffset;
        }
        else {
            displayOffsetY += blockSizeOffset;
        }
        this.centerPixelOffset.setLocation(displayOffsetX + blockPixelOffset.getX(), displayOffsetY + blockPixelOffset.getY());
        this.updateTilesTimer1.stop();
        if (!fullUpdate) {
            return;
        }
        if (mapView.isNone()) {
            return;
        }
        this.updateTilesTimer2.start();
        for (final Map.Entry<TilePos, Tile> entry : this.grid.entrySet()) {
            final TilePos pos = entry.getKey();
            Tile tile = entry.getValue();
            if (tile == null) {
                tile = this.findNeighbor(centerTile, pos);
                this.grid.put(pos, tile);
            }
            if (!tile.hasTexture(this.mapView)) {
                tile.updateTexture(this.worldDir, this.mapView, highQuality);
            }
        }
        this.updateTilesTimer2.stop();
    }
    
    public Point2D.Double getCenterPixelOffset() {
        return this.centerPixelOffset;
    }
    
    public AxisAlignedBB getBlockBounds() {
        return this.blockBounds;
    }
    
    public BlockPos getBlockAtPixel(final Point2D.Double pixel) {
        final double centerPixelX = this.lastWidth / 2.0;
        final double centerPixelZ = this.lastHeight / 2.0;
        final double deltaX = (centerPixelX - pixel.x) / this.uiState.blockSize;
        final double deltaZ = (centerPixelZ - (this.lastHeight - pixel.y)) / this.uiState.blockSize;
        final int x = MathHelper.func_76128_c(this.centerBlockX - deltaX);
        final int z = MathHelper.func_76128_c(this.centerBlockZ + deltaZ);
        int y = 0;
        if (DataCache.getPlayer().underground) {
            y = MathHelper.func_76128_c(DataCache.getPlayer().posY);
        }
        else {
            y = Journeymap.clientWorld().func_181545_F();
        }
        return new BlockPos(x, y, z);
    }
    
    public Point2D.Double getBlockPixelInGrid(final BlockPos pos) {
        return this.getBlockPixelInGrid(pos.func_177958_n(), pos.func_177952_p());
    }
    
    public Point2D.Double getBlockPixelInGrid(final double blockX, final double blockZ) {
        final Minecraft mc = FMLClientHandler.instance().getClient();
        final double localBlockX = blockX - this.centerBlockX;
        final double localBlockZ = blockZ - this.centerBlockZ;
        final int blockSize = (int)Math.pow(2.0, this.zoom);
        final double pixelOffsetX = mc.field_71443_c / 2.0 + localBlockX * blockSize;
        final double pixelOffsetZ = mc.field_71440_d / 2.0 + localBlockZ * blockSize;
        return new Point2D.Double(pixelOffsetX, pixelOffsetZ);
    }
    
    public void draw(final List<? extends DrawStep> drawStepList, final double xOffset, final double yOffset, final double fontScale, final double rotation) {
        if (!GridRenderer.enabled || drawStepList == null || drawStepList.isEmpty()) {
            return;
        }
        this.draw(xOffset, yOffset, fontScale, rotation, (DrawStep[])drawStepList.toArray(new DrawStep[drawStepList.size()]));
    }
    
    public void draw(final double xOffset, final double yOffset, final double fontScale, final double rotation, final DrawStep... drawSteps) {
        if (GridRenderer.enabled) {
            for (final DrawStep.Pass pass : DrawStep.Pass.values()) {
                for (final DrawStep drawStep : drawSteps) {
                    drawStep.draw(pass, xOffset, yOffset, this, fontScale, rotation);
                }
            }
        }
    }
    
    public void draw(final float alpha, final double offsetX, final double offsetZ, final boolean showGrid) {
        if (GridRenderer.enabled && !this.grid.isEmpty()) {
            final double centerX = offsetX + this.centerPixelOffset.x;
            final double centerZ = offsetZ + this.centerPixelOffset.y;
            final GridSpec gridSpec = showGrid ? Journeymap.getClient().getCoreProperties().gridSpecs.getSpec(this.mapView) : null;
            boolean somethingDrew = false;
            for (final Map.Entry<TilePos, Tile> entry : this.grid.entrySet()) {
                final TilePos pos = entry.getKey();
                final Tile tile = entry.getValue();
                if (tile == null) {
                    continue;
                }
                if (!tile.draw(pos, centerX, centerZ, alpha, gridSpec)) {
                    continue;
                }
                somethingDrew = true;
            }
            if (!somethingDrew) {
                RegionImageCache.INSTANCE.clear();
            }
        }
        if (!GridRenderer.messages.isEmpty()) {
            final double centerX = offsetX + this.centerPixelOffset.x + (this.centerPos.endX - this.centerPos.startX) / 2.0;
            double centerZ = offsetZ + this.centerPixelOffset.y + (this.centerPos.endZ - this.centerPos.startZ) / 2.0 - 60.0;
            for (final String message : GridRenderer.messages.values()) {
                DrawUtil.drawLabel(message, centerX, centerZ += 20.0, DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, 0, 1.0f, 16777215, 1.0f, 1.0, true);
            }
        }
    }
    
    public void clearGlErrors(final boolean report) {
        int err;
        while ((err = GL11.glGetError()) != 0) {
            if (report && this.glErrors <= 20) {
                ++this.glErrors;
                if (this.glErrors < 20) {
                    this.logger.warn("GL Error occurred during JourneyMap draw: " + err);
                }
                else {
                    this.logger.warn("GL Error reporting during JourneyMap will be suppressed after max errors: 20");
                }
            }
        }
    }
    
    public Point2D.Double getPixel(final double blockX, final double blockZ) {
        final Point2D.Double pixel = this.getBlockPixelInGrid(blockX, blockZ);
        if (this.isOnScreen(pixel)) {
            return pixel;
        }
        return null;
    }
    
    public void ensureOnScreen(final Point2D pixel) {
        if (this.screenBounds == null) {
            return;
        }
        double x = pixel.getX();
        if (x < this.screenBounds.x) {
            x = this.screenBounds.x;
        }
        else if (x > this.screenBounds.getMaxX()) {
            x = this.screenBounds.getMaxX();
        }
        double y = pixel.getY();
        if (y < this.screenBounds.y) {
            y = this.screenBounds.y;
        }
        else if (y > this.screenBounds.getMaxY()) {
            y = this.screenBounds.getMaxY();
        }
        pixel.setLocation(x, y);
    }
    
    private boolean isOnScreen(final TilePos pos) {
        return true;
    }
    
    public boolean isOnScreen(final Point2D.Double pixel) {
        return this.screenBounds.contains(pixel);
    }
    
    public boolean isOnScreen(final Rectangle2D.Double bounds) {
        return this.screenBounds.intersects(bounds);
    }
    
    public boolean isOnScreen(final double x, final double y) {
        return this.screenBounds.contains(x, y);
    }
    
    public boolean isOnScreen(final double startX, final double startY, final int width, final int height) {
        return this.screenBounds != null && this.screenBounds.intersects(startX, startY, width, height);
    }
    
    private void updateBounds(final int width, final int height) {
        if (this.screenBounds == null || this.lastWidth != width || this.lastHeight != height || this.blockBounds == null) {
            this.lastWidth = width;
            this.lastHeight = height;
            if (this.viewPort == null) {
                final int pad = 32;
                this.screenBounds = new Rectangle2D.Double(-pad, -pad, width + pad, height + pad);
            }
            else {
                this.screenBounds = new Rectangle2D.Double((width - this.viewPort.width) / 2.0, (height - this.viewPort.height) / 2.0, this.viewPort.width, this.viewPort.height);
            }
            ClientAPI.INSTANCE.flagOverlaysForRerender();
        }
    }
    
    public void updateUIState(final boolean isActive) {
        if (isActive && this.screenBounds == null) {
            return;
        }
        UIState newState = null;
        if (isActive) {
            final int worldHeight = Journeymap.clientWorld().func_72940_L();
            final int pad = 32;
            final BlockPos upperLeft = this.getBlockAtPixel(new Point2D.Double(this.screenBounds.getMinX(), this.screenBounds.getMinY()));
            final BlockPos lowerRight = this.getBlockAtPixel(new Point2D.Double(this.screenBounds.getMaxX(), this.screenBounds.getMaxY()));
            this.blockBounds = new AxisAlignedBB(upperLeft.func_177982_a(-pad, 0, -pad), lowerRight.func_177982_a(pad, worldHeight, pad));
            try {
                newState = new UIState(this.contextUi, true, this.mapView.dimension, this.zoom, this.mapView.mapType, new BlockPos(this.centerBlockX, 0.0, this.centerBlockZ), this.mapView.vSlice, this.blockBounds, this.screenBounds);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            newState = UIState.newInactive(this.uiState);
        }
        if (!newState.equals(this.uiState)) {
            this.uiState = newState;
            final ClientEventManager clientEventManager = ClientAPI.INSTANCE.getClientEventManager();
            if (clientEventManager.canFireClientEvent(ClientEvent.Type.DISPLAY_UPDATE)) {
                clientEventManager.fireDisplayUpdateEvent(new DisplayUpdateEvent(this.uiState));
            }
        }
    }
    
    private Tile findNeighbor(final Tile tile, final TilePos pos) {
        if (pos.deltaX == 0 && pos.deltaZ == 0) {
            return tile;
        }
        return this.findTile(tile.tileX + pos.deltaX, tile.tileZ + pos.deltaZ, tile.zoom);
    }
    
    private Tile findTile(final int tileX, final int tileZ, final int zoom) {
        return Tile.create(tileX, tileZ, zoom, this.worldDir, this.mapView, Journeymap.getClient().getCoreProperties().tileHighDisplayQuality.get());
    }
    
    public void setContext(final File worldDir, final MapView mapView) {
        TileDrawStepCache.setContext(this.worldDir = worldDir, this.mapView = mapView);
    }
    
    public void updateRotation(final double rotation) {
        this.currentRotation = rotation;
        GL11.glGetInteger(2978, this.viewportBuf);
        GL11.glGetFloat(2982, this.modelMatrixBuf);
        GL11.glGetFloat(2983, this.projMatrixBuf);
    }
    
    public Point2D shiftWindowPosition(final double x, final double y, final int shiftX, final int shiftY) {
        if (this.currentRotation % 360.0 == 0.0) {
            return new Point2D.Double(x + shiftX, y + shiftY);
        }
        GLU.gluProject((float)x, (float)y, 0.0f, this.modelMatrixBuf, this.projMatrixBuf, this.viewportBuf, this.winPosBuf);
        GLU.gluUnProject(this.winPosBuf.get(0) + shiftX, this.winPosBuf.get(1) + shiftY, 0.0f, this.modelMatrixBuf, this.projMatrixBuf, this.viewportBuf, this.objPosBuf);
        return new Point2D.Float(this.objPosBuf.get(0), this.objPosBuf.get(1));
    }
    
    public Point2D.Double getWindowPosition(final Point2D.Double matrixPixel) {
        if (this.currentRotation % 360.0 == 0.0) {
            return matrixPixel;
        }
        GLU.gluProject((float)matrixPixel.getX(), (float)matrixPixel.getY(), 0.0f, this.modelMatrixBuf, this.projMatrixBuf, this.viewportBuf, this.winPosBuf);
        return new Point2D.Double(this.winPosBuf.get(0), this.winPosBuf.get(1));
    }
    
    public Point2D.Double getMatrix(final Point2D.Double windowPixel) {
        GLU.gluUnProject((float)windowPixel.x, (float)windowPixel.y, 0.0f, this.modelMatrixBuf, this.projMatrixBuf, this.viewportBuf, this.objPosBuf);
        return new Point2D.Double(this.objPosBuf.get(0), this.objPosBuf.get(1));
    }
    
    public double getCenterBlockX() {
        return this.centerBlockX;
    }
    
    public double getCenterBlockZ() {
        return this.centerBlockZ;
    }
    
    public File getWorldDir() {
        return this.worldDir;
    }
    
    public MapView getMapView() {
        return this.mapView;
    }
    
    public int getZoom() {
        return this.zoom;
    }
    
    public boolean setZoom(final int zoom) {
        return this.center(this.worldDir, this.mapView, this.centerBlockX, this.centerBlockZ, zoom);
    }
    
    public int getRenderSize() {
        return this.gridSize * 512;
    }
    
    public void clear() {
        this.grid.clear();
        GridRenderer.messages.clear();
    }
    
    public int getWidth() {
        return this.lastWidth;
    }
    
    public int getHeight() {
        return this.lastHeight;
    }
    
    public UIState getUIState() {
        return this.uiState;
    }
    
    static {
        GridRenderer.enabled = true;
        GridRenderer.messages = new HashMap<String, String>();
    }
}
