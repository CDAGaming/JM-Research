package journeymap.client.render.draw;

import journeymap.client.api.display.*;
import journeymap.client.api.model.*;
import java.awt.geom.*;
import journeymap.client.waypoint.*;
import journeymap.client.render.texture.*;
import journeymap.client.render.map.*;
import net.minecraft.util.math.*;
import com.google.common.cache.*;

public class DrawWayPointStep implements DrawStep
{
    public final Waypoint waypoint;
    final TextureImpl texture;
    final boolean isEdit;
    final MapImage icon;
    final MapText label;
    Point2D.Double lastPosition;
    boolean lastOnScreen;
    boolean showLabel;
    
    public DrawWayPointStep(final Waypoint waypoint) {
        this(waypoint, false);
    }
    
    public DrawWayPointStep(final Waypoint waypoint, final boolean isEdit) {
        this.waypoint = waypoint;
        this.isEdit = isEdit;
        this.icon = WaypointStore.getWaypointIcon(waypoint);
        this.label = WaypointStore.getWaypointLabel(waypoint);
        this.texture = TextureCache.getTexture(this.icon.getImageLocation());
    }
    
    public void setShowLabel(final boolean showLabel) {
        this.showLabel = showLabel;
    }
    
    @Override
    public void draw(final Pass pass, final double xOffset, final double yOffset, final GridRenderer gridRenderer, final double fontScale, final double rotation) {
        final Point2D.Double pixel = this.getPosition(xOffset, yOffset, gridRenderer, true);
        if (gridRenderer.isOnScreen(pixel)) {
            if (this.showLabel && pass == Pass.Text) {
                final Point2D labelPoint = gridRenderer.shiftWindowPosition(pixel.getX(), pixel.getY(), 0, (rotation == 0.0) ? (-this.texture.getHeight()) : this.texture.getHeight());
                DrawUtil.drawLabel(this.waypoint.getName(), labelPoint.getX(), labelPoint.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, this.label.getBackgroundColor(), this.label.getBackgroundOpacity(), this.label.getColor(), this.label.getOpacity(), fontScale, this.label.hasFontShadow(), rotation);
            }
            else if (this.isEdit && pass == Pass.Object) {
                final TextureImpl editTex = TextureCache.getTexture(TextureCache.WaypointEdit);
                DrawUtil.drawColoredImage(editTex, this.icon.getColor(), 1.0f, pixel.getX() - editTex.getWidth() / 2, pixel.getY() - editTex.getHeight() / 2, -rotation);
            }
            if (pass == Pass.Object) {
                DrawUtil.drawColoredImage(this.texture, this.icon.getColor(), 1.0f, pixel.getX() - this.texture.getWidth() / 2, pixel.getY() - this.texture.getHeight() / 2, -rotation);
            }
        }
        else if (!this.isEdit && pass == Pass.Object) {
            gridRenderer.ensureOnScreen(pixel);
            DrawUtil.drawColoredImage(this.texture, this.icon.getColor(), 1.0f, pixel.getX() - this.texture.getWidth() / 2, pixel.getY() - this.texture.getHeight() / 2, -rotation);
        }
    }
    
    public static void drawIcon(final MapImage icon, final Point2D.Double pixel) {
        final TextureImpl texture = TextureCache.getTexture(icon.getImageLocation());
        final double width = icon.getDisplayWidth();
        final double height = icon.getDisplayHeight();
        DrawUtil.drawColoredSprite(texture, width, height, icon.getTextureX(), icon.getTextureY(), width, height, icon.getColor(), icon.getOpacity(), pixel.x - width / 2.0, pixel.y - height / 2.0, 1.0f, icon.getRotation());
    }
    
    public void drawOffscreen(final Pass pass, final Point2D pixel, final double rotation) {
        if (pass == Pass.Object) {
            DrawUtil.drawColoredImage(this.texture, this.icon.getColor(), 1.0f, pixel.getX() - this.texture.getWidth() / 2, pixel.getY() - this.texture.getHeight() / 2, -rotation);
        }
    }
    
    public Point2D.Double getPosition(final double xOffset, final double yOffset, final GridRenderer gridRenderer, final boolean forceUpdate) {
        if (!forceUpdate && this.lastPosition != null) {
            return this.lastPosition;
        }
        final BlockPos pos = this.waypoint.getPosition(gridRenderer.getMapView().dimension);
        final double x = pos.func_177958_n();
        final double z = pos.func_177952_p();
        final double halfBlock = Math.pow(2.0, gridRenderer.getZoom()) / 2.0;
        final Point2D.Double pixel = gridRenderer.getBlockPixelInGrid(x, z);
        pixel.setLocation(pixel.getX() + halfBlock + xOffset, pixel.getY() + halfBlock + yOffset);
        return this.lastPosition = pixel;
    }
    
    public boolean isOnScreen() {
        return this.lastOnScreen;
    }
    
    public void setOnScreen(final boolean lastOnScreen) {
        this.lastOnScreen = lastOnScreen;
    }
    
    @Override
    public int getDisplayOrder() {
        return 0;
    }
    
    @Override
    public String getModId() {
        return this.waypoint.getModId();
    }
    
    public static class SimpleCacheLoader extends CacheLoader<Waypoint, DrawWayPointStep>
    {
        public DrawWayPointStep load(final Waypoint waypoint) throws Exception {
            return new DrawWayPointStep(waypoint);
        }
    }
}
