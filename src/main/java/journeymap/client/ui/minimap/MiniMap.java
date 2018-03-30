package journeymap.client.ui.minimap;

import journeymap.client.render.map.*;
import net.minecraft.client.*;
import java.awt.geom.*;
import net.minecraft.client.entity.*;
import net.minecraftforge.fml.client.*;
import journeymap.common.*;
import journeymap.client.api.util.*;
import net.minecraft.entity.player.*;
import journeymap.client.feature.*;
import journeymap.common.api.feature.*;
import journeymap.client.api.impl.*;
import net.minecraft.client.renderer.*;
import journeymap.client.log.*;
import journeymap.client.render.draw.*;
import java.util.*;
import org.lwjgl.opengl.*;
import journeymap.client.data.*;
import net.minecraft.world.*;
import journeymap.client.model.*;
import journeymap.client.forge.event.*;
import journeymap.client.render.texture.*;
import journeymap.client.properties.*;
import net.minecraft.util.math.*;

public class MiniMap
{
    private static final MapState state;
    private static final float lightmapS = 240.0f;
    private static final float lightmapT = 240.0f;
    private static final GridRenderer gridRenderer;
    private final Minecraft mc;
    private final WaypointDrawStepFactory waypointRenderer;
    private final RadarDrawStepFactory radarRenderer;
    private TextureImpl playerArrowFg;
    private TextureImpl playerArrowBg;
    private int playerArrowColor;
    private MiniMapProperties miniMapProperties;
    private StatTimer drawTimer;
    private StatTimer refreshStateTimer;
    private DisplayVars dv;
    private boolean minimapAllowed;
    private boolean mapViewAllowed;
    private Point2D.Double centerPoint;
    private Rectangle2D.Double centerRect;
    private long initTime;
    private long lastAutoDayNightTime;
    private Boolean lastPlayerUnderground;
    private EntityPlayerSP player;
    
    public MiniMap(final MiniMapProperties miniMapProperties) {
        this.mc = FMLClientHandler.instance().getClient();
        this.waypointRenderer = new WaypointDrawStepFactory();
        this.radarRenderer = new RadarDrawStepFactory();
        this.lastAutoDayNightTime = -1L;
        this.player = Journeymap.clientPlayer();
        this.initTime = System.currentTimeMillis();
        this.setMiniMapProperties(miniMapProperties);
    }
    
    public static synchronized MapState state() {
        return MiniMap.state;
    }
    
    public static synchronized UIState uiState() {
        return MiniMap.gridRenderer.getUIState();
    }
    
    public static void updateUIState(final boolean isActive) {
        if (Journeymap.clientWorld() != null) {
            MiniMap.gridRenderer.updateUIState(isActive);
        }
    }
    
    private void initGridRenderer() {
        MiniMap.gridRenderer.clear();
        MiniMap.state.requireRefresh();
        this.player = Journeymap.clientPlayer();
        if (this.player == null || this.player.field_70128_L) {
            return;
        }
        MiniMap.state.refresh(this.mc, (EntityPlayer)this.player, this.miniMapProperties);
        final MapView mapView = MiniMap.state.getMapView();
        final int gridSize = (this.miniMapProperties.getSize() <= 768) ? 3 : 5;
        MiniMap.gridRenderer.setGridSize(gridSize);
        MiniMap.gridRenderer.setContext(MiniMap.state.getWorldDir(), mapView);
        MiniMap.gridRenderer.center(MiniMap.state.getWorldDir(), mapView, this.player.field_70165_t, this.player.field_70161_v, this.miniMapProperties.zoomLevel.get());
        final boolean highQuality = Journeymap.getClient().getCoreProperties().tileHighDisplayQuality.get();
        MiniMap.gridRenderer.updateTiles(MiniMap.state.getMapView(), MiniMap.state.getZoom(), highQuality, this.mc.field_71443_c, this.mc.field_71440_d, true, 0.0, 0.0);
    }
    
    public void resetInitTime() {
        this.initTime = System.currentTimeMillis();
    }
    
    public void setMiniMapProperties(final MiniMapProperties miniMapProperties) {
        this.miniMapProperties = miniMapProperties;
        state().requireRefresh();
        this.reset();
    }
    
    public MiniMapProperties getCurrentMinimapProperties() {
        return this.miniMapProperties;
    }
    
    public void drawMap() {
        this.drawMap(false);
    }
    
    public void drawMap(final boolean preview) {
        StatTimer timer = this.drawTimer;
        RenderHelper.func_74518_a();
        if (this.player == null || this.player.field_70128_L) {
            this.player = Journeymap.clientPlayer();
            return;
        }
        if (!(this.minimapAllowed = ClientFeatures.instance().isAllowed(Feature.Display.Minimap, this.player.field_71093_bK))) {
            return;
        }
        try {
            MiniMap.gridRenderer.clearGlErrors(false);
            final boolean doStateRefresh = MiniMap.state.shouldRefresh(this.mc, this.miniMapProperties);
            if (doStateRefresh) {
                timer = this.refreshStateTimer.start();
                this.autoDayNight();
                MiniMap.gridRenderer.setContext(MiniMap.state.getWorldDir(), MiniMap.state.getMapView());
                if (!preview) {
                    MiniMap.state.refresh(this.mc, (EntityPlayer)this.player, this.miniMapProperties);
                }
                MapView mapView = MiniMap.state.getMapView();
                if (!mapView.isAllowed() || mapView.isNone()) {
                    mapView = MiniMap.state.toggleMapType();
                }
                this.mapViewAllowed = (!mapView.isNone() && mapView.isAllowed());
                ClientAPI.INSTANCE.flagOverlaysForRerender();
            }
            else {
                timer.start();
            }
            if (!this.minimapAllowed) {
                return;
            }
            final boolean moved = MiniMap.gridRenderer.center(MiniMap.state.getWorldDir(), MiniMap.state.getMapView(), this.player.field_70165_t, this.player.field_70161_v, this.miniMapProperties.zoomLevel.get());
            if (moved || doStateRefresh) {
                MiniMap.gridRenderer.updateTiles(MiniMap.state.getMapView(), MiniMap.state.getZoom(), MiniMap.state.isHighQuality(), this.mc.field_71443_c, this.mc.field_71440_d, doStateRefresh || preview, 0.0, 0.0);
            }
            if (doStateRefresh) {
                final boolean checkWaypointDistance = Journeymap.getClient().getWaypointProperties().maxDistance.get() > 0;
                MiniMap.state.generateDrawSteps(this.mc, MiniMap.gridRenderer, this.waypointRenderer, this.radarRenderer, this.miniMapProperties, checkWaypointDistance);
                MiniMap.state.updateLastRefresh();
            }
            this.updateDisplayVars(false);
            final long now = System.currentTimeMillis();
            DrawUtil.sizeDisplay(this.mc.field_71443_c, this.mc.field_71440_d);
            OpenGlHelper.func_77475_a(OpenGlHelper.field_77476_b, 240.0f, 240.0f);
            GlStateManager.func_179147_l();
            GlStateManager.func_179112_b(770, 0);
            GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.func_179126_j();
            this.beginStencil();
            double rotation = 0.0;
            switch (this.dv.orientation) {
                case North: {
                    rotation = 0.0;
                    break;
                }
                case OldNorth: {
                    rotation = 90.0;
                    break;
                }
                case PlayerHeading: {
                    if (this.dv.shape == Shape.Circle) {
                        rotation = 180.0f - this.player.field_70759_as;
                        break;
                    }
                    break;
                }
            }
            this.startMapRotation(rotation);
            try {
                GlStateManager.func_179109_b((float)this.dv.translateX, (float)this.dv.translateY, 0.0f);
                MiniMap.gridRenderer.draw(this.dv.terrainAlpha, 0.0, 0.0, this.miniMapProperties.showGrid.get());
                MiniMap.gridRenderer.draw(MiniMap.state.getDrawSteps(), 0.0, 0.0, this.dv.fontScale, rotation);
                this.centerPoint = MiniMap.gridRenderer.getPixel(this.player.field_70165_t, this.player.field_70161_v);
                if (this.centerPoint == null) {
                    return;
                }
                this.centerRect = new Rectangle2D.Double(this.centerPoint.x - this.dv.minimapWidth / 2, this.centerPoint.y - this.dv.minimapHeight / 2, this.dv.minimapWidth, this.dv.minimapHeight);
                this.drawOnMapWaypoints(rotation);
                if (this.miniMapProperties.showSelf.get() && this.playerArrowFg != null && this.centerPoint != null) {
                    DrawUtil.drawColoredEntity(this.centerPoint.getX(), this.centerPoint.getY(), this.playerArrowBg, 16777215, 1.0f, 1.0f, this.player.field_70759_as);
                    DrawUtil.drawColoredEntity(this.centerPoint.getX(), this.centerPoint.getY(), this.playerArrowFg, this.playerArrowColor, 1.0f, 1.0f, this.player.field_70759_as);
                }
                GlStateManager.func_179109_b((float)(-this.dv.translateX), (float)(-this.dv.translateY), 0.0f);
                ReticleOrientation reticleOrientation = null;
                if (this.dv.showReticle) {
                    reticleOrientation = this.dv.minimapFrame.getReticleOrientation();
                    if (reticleOrientation == ReticleOrientation.Compass) {
                        this.dv.minimapFrame.drawReticle();
                    }
                    else {
                        this.startMapRotation(this.player.field_70759_as);
                        this.dv.minimapFrame.drawReticle();
                        this.stopMapRotation(this.player.field_70759_as);
                    }
                }
                if (this.mapViewAllowed) {
                    final long lastMapChangeTime = MiniMap.state.getLastMapTypeChange();
                    if (now - lastMapChangeTime <= 1000L) {
                        this.stopMapRotation(rotation);
                        GlStateManager.func_179109_b((float)this.dv.translateX, (float)this.dv.translateY, 0.0f);
                        final float alpha = Math.min(255L, Math.max(0L, 1100L - (now - lastMapChangeTime))) / 255.0f;
                        final Point2D.Double windowCenter = MiniMap.gridRenderer.getWindowPosition(this.centerPoint);
                        this.dv.getMapTypeStatus(MiniMap.state.getMapView()).draw(windowCenter, alpha, 0.0);
                        GlStateManager.func_179109_b((float)(-this.dv.translateX), (float)(-this.dv.translateY), 0.0f);
                        this.startMapRotation(rotation);
                    }
                }
                if (now - this.initTime <= 1000L) {
                    this.stopMapRotation(rotation);
                    GlStateManager.func_179109_b((float)this.dv.translateX, (float)this.dv.translateY, 0.0f);
                    final float alpha2 = Math.min(255L, Math.max(0L, 1100L - (now - this.initTime))) / 255.0f;
                    final Point2D.Double windowCenter2 = MiniMap.gridRenderer.getWindowPosition(this.centerPoint);
                    this.dv.getMapPresetStatus(MiniMap.state.getMapView(), this.miniMapProperties.getId()).draw(windowCenter2, alpha2, 0.0);
                    GlStateManager.func_179109_b((float)(-this.dv.translateX), (float)(-this.dv.translateY), 0.0f);
                    this.startMapRotation(rotation);
                }
                this.endStencil();
                if (!this.dv.frameRotates && rotation != 0.0) {
                    this.stopMapRotation(rotation);
                }
                this.dv.minimapFrame.drawFrame();
                if (!this.dv.frameRotates && rotation != 0.0) {
                    this.startMapRotation(rotation);
                }
                if (this.dv.showCompass) {
                    this.dv.minimapCompassPoints.drawPoints(rotation);
                }
                GlStateManager.func_179109_b((float)this.dv.translateX, (float)this.dv.translateY, 0.0f);
                this.drawOffMapWaypoints(rotation);
                if (this.dv.showCompass) {
                    GlStateManager.func_179109_b((float)(-this.dv.translateX), (float)(-this.dv.translateY), 0.0f);
                    this.dv.minimapCompassPoints.drawLabels(rotation);
                }
            }
            finally {
                GlStateManager.func_179121_F();
            }
            this.dv.drawInfoLabels(now);
            DrawUtil.sizeDisplay(this.dv.scaledResolution.func_78327_c(), this.dv.scaledResolution.func_78324_d());
        }
        catch (Throwable t) {
            JMLogger.logOnce("Error during MiniMap.drawMap(): " + t.getMessage(), t);
        }
        finally {
            this.cleanup();
            timer.stop();
            MiniMap.gridRenderer.clearGlErrors(true);
        }
    }
    
    private void drawOnMapWaypoints(final double rotation) {
        final boolean showLabel = this.miniMapProperties.showWaypointLabels.get();
        for (final DrawStep.Pass pass : DrawStep.Pass.values()) {
            for (final DrawWayPointStep drawWayPointStep : MiniMap.state.getDrawWaypointSteps()) {
                boolean onScreen = false;
                if (pass == DrawStep.Pass.Object) {
                    final Point2D.Double waypointPos = drawWayPointStep.getPosition(0.0, 0.0, MiniMap.gridRenderer, true);
                    onScreen = this.isOnScreen(waypointPos, this.centerPoint, this.centerRect);
                    drawWayPointStep.setOnScreen(onScreen);
                }
                else {
                    onScreen = drawWayPointStep.isOnScreen();
                }
                if (onScreen) {
                    drawWayPointStep.setShowLabel(showLabel);
                    drawWayPointStep.draw(pass, 0.0, 0.0, MiniMap.gridRenderer, this.dv.fontScale, rotation);
                }
            }
        }
    }
    
    private void drawOffMapWaypoints(final double rotation) {
        for (final DrawWayPointStep drawWayPointStep : MiniMap.state.getDrawWaypointSteps()) {
            if (!drawWayPointStep.isOnScreen()) {
                final Point2D.Double point = this.getPointOnFrame(drawWayPointStep.getPosition(0.0, 0.0, MiniMap.gridRenderer, false), this.centerPoint, this.dv.minimapSpec.waypointOffset);
                drawWayPointStep.drawOffscreen(DrawStep.Pass.Object, point, rotation);
            }
        }
    }
    
    private void startMapRotation(final double rotation) {
        GlStateManager.func_179094_E();
        if (rotation % 360.0 != 0.0) {
            final double width = this.dv.displayWidth / 2 + this.dv.translateX;
            final double height = this.dv.displayHeight / 2 + this.dv.translateY;
            GlStateManager.func_179137_b(width, height, 0.0);
            GlStateManager.func_179114_b((float)rotation, 0.0f, 0.0f, 1.0f);
            GlStateManager.func_179137_b(-width, -height, 0.0);
        }
        MiniMap.gridRenderer.updateRotation(rotation);
    }
    
    private void stopMapRotation(final double rotation) {
        GlStateManager.func_179121_F();
        MiniMap.gridRenderer.updateRotation(rotation);
    }
    
    private boolean isOnScreen(final Point2D.Double objectPixel, final Point2D centerPixel, final Rectangle2D.Double centerRect) {
        if (this.dv.shape == Shape.Circle) {
            return centerPixel.distance(objectPixel) < this.dv.minimapWidth / 2;
        }
        return centerRect.contains(MiniMap.gridRenderer.getWindowPosition(objectPixel));
    }
    
    private Point2D.Double getPointOnFrame(final Point2D.Double objectPixel, final Point2D centerPixel, final double offset) {
        if (this.dv.shape == Shape.Circle) {
            final double bearing = Math.atan2(objectPixel.getY() - centerPixel.getY(), objectPixel.getX() - centerPixel.getX());
            final Point2D.Double framePos = new Point2D.Double(this.dv.minimapWidth / 2 * Math.cos(bearing) + centerPixel.getX(), this.dv.minimapHeight / 2 * Math.sin(bearing) + centerPixel.getY());
            return framePos;
        }
        final Rectangle2D.Double rect = new Rectangle2D.Double(this.dv.textureX - this.dv.translateX, this.dv.textureY - this.dv.translateY, this.dv.minimapWidth, this.dv.minimapHeight);
        if (objectPixel.x > rect.getMaxX()) {
            objectPixel.x = rect.getMaxX();
        }
        else if (objectPixel.x < rect.getMinX()) {
            objectPixel.x = rect.getMinX();
        }
        if (objectPixel.y > rect.getMaxY()) {
            objectPixel.y = rect.getMaxY();
        }
        else if (objectPixel.y < rect.getMinY()) {
            objectPixel.y = rect.getMinY();
        }
        return objectPixel;
    }
    
    private void beginStencil() {
        try {
            this.cleanup();
            DrawUtil.zLevel = 1000.0;
            GlStateManager.func_179135_a(false, false, false, false);
            this.dv.minimapFrame.drawMask();
            GlStateManager.func_179135_a(true, true, true, true);
            DrawUtil.zLevel = 0.0;
            GlStateManager.func_179132_a(false);
            GlStateManager.func_179143_c(516);
        }
        catch (Throwable t) {
            JMLogger.logOnce("Error during MiniMap.beginStencil()", t);
        }
    }
    
    private void endStencil() {
        try {
            GlStateManager.func_179097_i();
        }
        catch (Throwable t) {
            JMLogger.logOnce("Error during MiniMap.endStencil()", t);
        }
    }
    
    private void cleanup() {
        try {
            DrawUtil.zLevel = 0.0;
            GlStateManager.func_179132_a(true);
            GL11.glClear(256);
            GlStateManager.func_179126_j();
            GlStateManager.func_179143_c(515);
            GlStateManager.func_179141_d();
            GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.func_179082_a(1.0f, 1.0f, 1.0f, 1.0f);
        }
        catch (Throwable t) {
            JMLogger.logOnce("Error during MiniMap.cleanup()", t);
        }
    }
    
    private void autoDayNight() {
        final World world = Journeymap.clientWorld();
        if (world != null) {
            final boolean wasInCaves = false;
            if (this.miniMapProperties.showCaves.get()) {
                final EntityDTO player = DataCache.getPlayer();
                final boolean neverChecked = this.lastPlayerUnderground == null;
                final boolean playerUnderground = player.underground;
                if (neverChecked || playerUnderground != this.lastPlayerUnderground) {
                    this.lastPlayerUnderground = playerUnderground;
                    if (playerUnderground) {
                        MiniMap.state.setMapType(MapView.underground(player));
                    }
                }
                final MapView currentMapType = MiniMap.state.getMapView();
                if (playerUnderground && currentMapType.isUnderground() && currentMapType.vSlice != player.chunkCoordY) {
                    MiniMap.state.setMapType(MapView.underground(player));
                }
            }
            if (this.miniMapProperties.showDayNight.get() && (wasInCaves || MiniMap.state.getMapView().isDayOrNight())) {
                final long NIGHT = 13800L;
                final long worldTime = world.func_72820_D() % 24000L;
                final boolean neverChecked2 = this.lastAutoDayNightTime == -1L;
                if (worldTime >= 13800L && (neverChecked2 || this.lastAutoDayNightTime < 13800L)) {
                    this.lastAutoDayNightTime = worldTime;
                    MiniMap.state.setMapType(MapView.night(world.field_73011_w.getDimension()));
                }
                else if (worldTime < 13800L && (neverChecked2 || this.lastAutoDayNightTime >= 13800L)) {
                    this.lastAutoDayNightTime = worldTime;
                    MiniMap.state.setMapType(MapView.day(world.field_73011_w.getDimension()));
                }
            }
        }
    }
    
    public void reset() {
        this.initTime = System.currentTimeMillis();
        this.lastAutoDayNightTime = -1L;
        this.initGridRenderer();
        this.updateDisplayVars(this.miniMapProperties.shape.get(), this.miniMapProperties.position.get(), true);
        MiniMapOverlayHandler.checkEventConfig();
        GridRenderer.clearDebugMessages();
        final CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
        this.playerArrowColor = coreProperties.getColor(coreProperties.colorSelf);
        if (this.miniMapProperties.playerDisplay.get().isLarge()) {
            this.playerArrowBg = TextureCache.getTexture(TextureCache.PlayerArrowBG_Large);
            this.playerArrowFg = TextureCache.getTexture(TextureCache.PlayerArrow_Large);
        }
        else {
            this.playerArrowBg = TextureCache.getTexture(TextureCache.PlayerArrowBG);
            this.playerArrowFg = TextureCache.getTexture(TextureCache.PlayerArrow);
        }
    }
    
    public void updateDisplayVars(final boolean force) {
        if (this.dv != null) {
            this.updateDisplayVars(this.dv.shape, this.dv.position, force);
        }
    }
    
    public void updateDisplayVars(Shape shape, Position position, final boolean force) {
        if (this.dv != null && !force && this.mc.field_71440_d == this.dv.displayHeight && this.mc.field_71443_c == this.dv.displayWidth && this.dv.shape == shape && this.dv.position == position && this.dv.fontScale == this.miniMapProperties.fontScale.get()) {
            return;
        }
        this.initGridRenderer();
        if (force) {
            shape = this.miniMapProperties.shape.get();
            position = this.miniMapProperties.position.get();
        }
        this.miniMapProperties.shape.set(shape);
        this.miniMapProperties.position.set(position);
        this.miniMapProperties.save();
        final DisplayVars oldDv = this.dv;
        this.dv = new DisplayVars(this.mc, this.miniMapProperties);
        if (oldDv == null || oldDv.shape != this.dv.shape) {
            final String timerName = String.format("MiniMap%s.%s", this.miniMapProperties.getId(), shape.name());
            (this.drawTimer = StatTimer.get(timerName, 100)).reset();
            (this.refreshStateTimer = StatTimer.get(timerName + "+refreshState", 5)).reset();
        }
        final double xpad = 0.0;
        final double ypad = 0.0;
        final Rectangle2D.Double viewPort = new Rectangle2D.Double(this.dv.textureX + xpad, this.dv.textureY + ypad, this.dv.minimapWidth - 2.0 * xpad, this.dv.minimapHeight - 2.0 * ypad);
        MiniMap.gridRenderer.setViewPort(viewPort);
        updateUIState(true);
    }
    
    public String getLocation() {
        final int playerX = MathHelper.func_76128_c(this.player.field_70165_t);
        final int playerZ = MathHelper.func_76128_c(this.player.field_70161_v);
        final int playerY = MathHelper.func_76128_c(this.player.func_174813_aQ().field_72338_b);
        return this.dv.locationFormatKeys.format(this.dv.locationFormatVerbose, playerX, playerZ, playerY, this.player.field_70162_ai);
    }
    
    public String getBiome() {
        return MiniMap.state.getPlayerBiome();
    }
    
    static {
        state = new MapState();
        gridRenderer = new GridRenderer(Feature.Display.Minimap, 3);
    }
}
