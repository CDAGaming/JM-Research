package journeymap.client.render.map;

import org.apache.logging.log4j.*;
import journeymap.client.log.*;
import java.util.concurrent.*;
import journeymap.client.model.*;
import journeymap.client.render.draw.*;
import net.minecraft.client.renderer.*;
import org.lwjgl.opengl.*;
import journeymap.common.*;
import journeymap.client.task.main.*;
import com.google.common.base.*;
import journeymap.client.render.texture.*;
import journeymap.client.io.*;
import java.awt.image.*;
import java.awt.*;
import net.minecraft.util.math.*;
import java.util.*;

public class TileDrawStep implements TextureImpl.Listener<RegionTextureImpl>
{
    private static final Integer bgColor;
    private static final Logger logger;
    private static final RegionImageCache regionImageCache;
    private boolean debug;
    private final RegionCoord regionCoord;
    private final MapView mapView;
    private final Integer zoom;
    private final boolean highQuality;
    private final StatTimer drawTimer;
    private final StatTimer updateRegionTimer;
    private final StatTimer updateScaledTimer;
    private final int theHashCode;
    private final String theCacheKey;
    private final RegionImageSet.Key regionImageSetKey;
    private int sx1;
    private int sy1;
    private int sx2;
    private int sy2;
    private volatile TextureImpl scaledTexture;
    private volatile Future<RegionTextureImpl> regionFuture;
    private volatile Future<TextureImpl> scaledFuture;
    private volatile boolean needsScaledUpdate;
    private int lastTextureFilter;
    private int lastTextureWrap;
    
    public TileDrawStep(final RegionCoord regionCoord, final MapView mapView, final Integer zoom, final boolean highQuality, final int sx1, final int sy1, final int sx2, final int sy2) {
        this.debug = false;
        this.updateRegionTimer = StatTimer.get("TileDrawStep.updateRegionTexture", 5, 50);
        this.updateScaledTimer = StatTimer.get("TileDrawStep.updateScaledTexture", 5, 50);
        this.mapView = mapView;
        this.regionCoord = regionCoord;
        this.regionImageSetKey = RegionImageSet.Key.from(regionCoord);
        this.zoom = zoom;
        this.sx1 = sx1;
        this.sx2 = sx2;
        this.sy1 = sy1;
        this.sy2 = sy2;
        this.highQuality = (highQuality && zoom != 0);
        this.drawTimer = (this.highQuality ? StatTimer.get("TileDrawStep.draw(high)") : StatTimer.get("TileDrawStep.draw(low)"));
        this.theCacheKey = toCacheKey(regionCoord, mapView, zoom, highQuality, sx1, sy1, sx2, sy2);
        this.theHashCode = this.theCacheKey.hashCode();
        this.updateRegionTexture();
        if (highQuality) {
            this.updateScaledTexture();
        }
    }
    
    public static String toCacheKey(final RegionCoord regionCoord, final MapView mapView, final Integer zoom, final boolean highQuality, final int sx1, final int sy1, final int sx2, final int sy2) {
        return regionCoord.cacheKey() + mapView.toCacheKey() + zoom + highQuality + sx1 + "," + sy1 + "," + sx2 + "," + sy2;
    }
    
    ImageHolder getRegionTextureHolder() {
        return TileDrawStep.regionImageCache.getRegionImageSet(this.regionImageSetKey).getHolder(this.mapView);
    }
    
    boolean draw(final TilePos pos, final double offsetX, final double offsetZ, final float alpha, final int textureFilter, final int textureWrap, final GridSpec gridSpec) {
        final boolean regionUpdatePending = this.updateRegionTexture();
        if (this.highQuality && !regionUpdatePending) {
            this.updateScaledTexture();
        }
        Integer textureId = -1;
        boolean useScaled = false;
        if (this.highQuality && this.scaledTexture != null) {
            textureId = this.scaledTexture.func_110552_b();
            useScaled = true;
        }
        else if (!regionUpdatePending) {
            textureId = this.getRegionTextureHolder().getTexture().func_110552_b();
        }
        else {
            textureId = -1;
        }
        if (textureFilter != this.lastTextureFilter) {
            this.lastTextureFilter = textureFilter;
        }
        if (textureWrap != this.lastTextureWrap) {
            this.lastTextureWrap = textureWrap;
        }
        this.drawTimer.start();
        final double startX = offsetX + pos.startX;
        final double startY = offsetZ + pos.startZ;
        final double endX = offsetX + pos.endX;
        final double endY = offsetZ + pos.endZ;
        final double z = 0.0;
        final double size = 512.0;
        final double startU = useScaled ? 0.0 : (this.sx1 / 512.0);
        final double startV = useScaled ? 0.0 : (this.sy1 / 512.0);
        final double endU = useScaled ? 1.0 : (this.sx2 / 512.0);
        final double endV = useScaled ? 1.0 : (this.sy2 / 512.0);
        DrawUtil.drawRectangle(startX, startY, endX - startX, endY - startY, TileDrawStep.bgColor, 0.8f);
        GlStateManager.func_179147_l();
        GlStateManager.func_179120_a(770, 771, 1, 0);
        GlStateManager.func_179098_w();
        if (textureId != -1) {
            GlStateManager.func_179144_i((int)textureId);
            GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, alpha);
            GL11.glTexParameteri(3553, 10241, textureFilter);
            GL11.glTexParameteri(3553, 10240, textureFilter);
            GL11.glTexParameteri(3553, 10242, textureWrap);
            GL11.glTexParameteri(3553, 10243, textureWrap);
            DrawUtil.drawBoundTexture(startU, startV, startX, startY, 0.0, endU, endV, endX, endY);
        }
        if (gridSpec != null) {
            gridSpec.beginTexture(9728, 33071, alpha);
            DrawUtil.drawBoundTexture(this.sx1 / 512.0, this.sy1 / 512.0, startX, startY, 0.0, this.sx2 / 512.0, this.sy2 / 512.0, endX, endY);
            gridSpec.finishTexture();
        }
        if (this.debug) {
            final int debugX = (int)startX;
            final int debugY = (int)startY;
            DrawUtil.drawRectangle(debugX, debugY, 3.0, endV * 512.0, 65280, 0.8f);
            DrawUtil.drawRectangle(debugX, debugY, endU * 512.0, 3.0, 16711680, 0.8f);
            DrawUtil.drawLabel(this.toString(), debugX + 5, debugY + 10, DrawUtil.HAlign.Right, DrawUtil.VAlign.Below, 16777215, 255.0f, 255, 255.0f, 1.0, false);
            DrawUtil.drawLabel(String.format("Tile Render Type: %s, Scaled: %s", Tile.debugGlSettings, useScaled), debugX + 5, debugY + 20, DrawUtil.HAlign.Right, DrawUtil.VAlign.Below, 16777215, 255.0f, 255, 255.0f, 1.0, false);
            final long imageTimestamp = useScaled ? this.scaledTexture.getLastImageUpdate() : this.getRegionTextureHolder().getImageTimestamp();
            final long age = (System.currentTimeMillis() - imageTimestamp) / 1000L;
            DrawUtil.drawLabel(this.mapView + " tile age: " + age + " seconds old", debugX + 5, debugY + 30, DrawUtil.HAlign.Right, DrawUtil.VAlign.Below, 16777215, 255.0f, 255, 255.0f, 1.0, false);
        }
        GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.func_179082_a(1.0f, 1.0f, 1.0f, 1.0f);
        this.drawTimer.stop();
        final int glErr = GL11.glGetError();
        if (glErr != 0) {
            Journeymap.getLogger().warn("GL Error in TileDrawStep: " + glErr);
            this.clearTexture();
        }
        return textureId != 1;
    }
    
    public void clearTexture() {
        ExpireTextureTask.queue(this.scaledTexture);
        this.scaledTexture = null;
        if (this.scaledFuture != null && !this.scaledFuture.isDone()) {
            this.scaledFuture.cancel(true);
        }
        this.scaledFuture = null;
        if (this.regionFuture != null && !this.regionFuture.isDone()) {
            this.regionFuture.cancel(true);
        }
        this.regionFuture = null;
    }
    
    public MapView getMapType() {
        return this.mapView;
    }
    
    public Integer getZoom() {
        return this.zoom;
    }
    
    public String cacheKey() {
        return this.theCacheKey;
    }
    
    @Override
    public int hashCode() {
        return this.theHashCode;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("rc", (Object)this.regionCoord).add("type", (Object)this.mapView).add("high", this.highQuality).add("zoom", (Object)this.zoom).add("sx1", this.sx1).add("sy1", this.sy1).toString();
    }
    
    boolean hasTexture(final MapView mapView) {
        if (!Objects.equal((Object)this.mapView, (Object)mapView)) {
            return false;
        }
        if (this.highQuality) {
            return this.scaledTexture != null && this.scaledTexture.isBound();
        }
        return this.getRegionTextureHolder().getTexture().isBound();
    }
    
    private boolean updateRegionTexture() {
        this.updateRegionTimer.start();
        if (this.regionFuture != null) {
            if (!this.regionFuture.isDone()) {
                this.updateRegionTimer.stop();
                return true;
            }
            this.regionFuture = null;
        }
        final ImageHolder imageHolder = this.getRegionTextureHolder();
        if (imageHolder.hasTexture()) {
            final RegionTextureImpl tex = imageHolder.getTexture();
            tex.addListener(this);
            if (tex.isBindNeeded()) {
                tex.bindTexture();
            }
            this.updateRegionTimer.stop();
            return false;
        }
        final RegionTextureImpl tex2;
        this.regionFuture = TextureCache.scheduleTextureTask(() -> {
            tex2 = this.getRegionTextureHolder().getTexture();
            tex2.addListener(this);
            return tex2;
        });
        this.updateRegionTimer.stop();
        return true;
    }
    
    private boolean updateScaledTexture() {
        this.updateScaledTimer.start();
        if (this.scaledFuture == null) {
            if (this.scaledTexture == null) {
                this.needsScaledUpdate = false;
                final TextureImpl temp;
                this.scaledFuture = TextureCache.scheduleTextureTask(() -> {
                    temp = new TextureImpl(null, this.getScaledRegionArea(), false, false);
                    temp.setDescription("Scaled " + this);
                    return temp;
                });
            }
            else if (this.needsScaledUpdate) {
                this.needsScaledUpdate = false;
                final TextureImpl temp2 = this.scaledTexture;
                final TextureImpl textureImpl;
                this.scaledFuture = TextureCache.scheduleTextureTask(() -> {
                    textureImpl.setImage(this.getScaledRegionArea(), false);
                    return textureImpl;
                });
            }
            this.updateScaledTimer.stop();
            return true;
        }
        if (!this.scaledFuture.isDone()) {
            this.updateScaledTimer.stop();
            return true;
        }
        try {
            (this.scaledTexture = this.scaledFuture.get()).bindTexture();
        }
        catch (Throwable e) {
            TileDrawStep.logger.error((Object)e);
        }
        this.scaledFuture = null;
        this.updateScaledTimer.stop();
        return false;
    }
    
    public BufferedImage getScaledRegionArea() {
        final int scale = (int)Math.pow(2.0, this.zoom);
        final int scaledSize = 512 / scale;
        try {
            final BufferedImage subImage = this.getRegionTextureHolder().getTexture().getImage().getSubimage(this.sx1, this.sy1, scaledSize, scaledSize);
            final BufferedImage scaledImage = new BufferedImage(512, 512, 2);
            final Graphics2D g = RegionImageHandler.initRenderingHints(scaledImage.createGraphics());
            g.drawImage(subImage, 0, 0, 512, 512, null);
            g.dispose();
            return scaledImage;
        }
        catch (Throwable e) {
            TileDrawStep.logger.error((Object)e);
            return null;
        }
    }
    
    @Override
    public void textureImageUpdated(final RegionTextureImpl textureImpl) {
        if (this.highQuality && this.zoom > 0) {
            final Set<ChunkPos> dirtyAreas = textureImpl.getDirtyAreas();
            if (dirtyAreas.isEmpty()) {
                this.needsScaledUpdate = true;
            }
            else {
                for (final ChunkPos area : dirtyAreas) {
                    if (area.field_77276_a >= this.sx1 && area.field_77275_b >= this.sy1 && area.field_77276_a + 16 <= this.sx2 && area.field_77275_b + 16 <= this.sy2) {
                        this.needsScaledUpdate = true;
                    }
                }
            }
        }
    }
    
    static {
        bgColor = 2236962;
        logger = Journeymap.getLogger();
        regionImageCache = RegionImageCache.INSTANCE;
    }
}
