package journeymap.client.render.draw;

import journeymap.client.model.*;
import java.awt.geom.*;
import journeymap.client.render.map.*;
import journeymap.client.render.texture.*;
import com.google.common.cache.*;

public class DrawWayPointStep implements DrawStep
{
    public final Waypoint waypoint;
    final Integer color;
    final Integer fontColor;
    final TextureImpl texture;
    final boolean isEdit;
    Point2D.Double lastPosition;
    boolean lastOnScreen;
    boolean showLabel;
    
    public DrawWayPointStep(final Waypoint waypoint) {
        this(waypoint, waypoint.getColor(), waypoint.isDeathPoint() ? 16711680 : waypoint.getSafeColor(), false);
    }
    
    public DrawWayPointStep(final Waypoint waypoint, final Integer color, final Integer fontColor, final boolean isEdit) {
        this.waypoint = waypoint;
        this.color = color;
        this.fontColor = fontColor;
        this.isEdit = isEdit;
        this.texture = waypoint.getTexture();
    }
    
    public void setShowLabel(final boolean showLabel) {
        this.showLabel = showLabel;
    }
    
    @Override
    public void draw(final Pass pass, final double xOffset, final double yOffset, final GridRenderer gridRenderer, final double fontScale, final double rotation) {
        if (!this.waypoint.isInPlayerDimension()) {
            return;
        }
        final Point2D.Double pixel = this.getPosition(xOffset, yOffset, gridRenderer, true);
        if (gridRenderer.isOnScreen(pixel)) {
            if (this.showLabel && pass == Pass.Text) {
                final Point2D labelPoint = gridRenderer.shiftWindowPosition(pixel.getX(), pixel.getY(), 0, (rotation == 0.0) ? (-this.texture.getHeight()) : this.texture.getHeight());
                DrawUtil.drawLabel(this.waypoint.getName(), labelPoint.getX(), labelPoint.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, 0, 0.7f, this.fontColor, 1.0f, fontScale, false, rotation);
            }
            else if (this.isEdit && pass == Pass.Object) {
                final TextureImpl editTex = TextureCache.getTexture(TextureCache.WaypointEdit);
                DrawUtil.drawColoredImage(editTex, this.color, 1.0f, pixel.getX() - editTex.getWidth() / 2, pixel.getY() - editTex.getHeight() / 2, -rotation);
            }
            if (pass == Pass.Object) {
                DrawUtil.drawColoredImage(this.texture, this.color, 1.0f, pixel.getX() - this.texture.getWidth() / 2, pixel.getY() - this.texture.getHeight() / 2, -rotation);
            }
        }
        else if (!this.isEdit && pass == Pass.Object) {
            gridRenderer.ensureOnScreen(pixel);
            DrawUtil.drawColoredImage(this.texture, this.color, 1.0f, pixel.getX() - this.texture.getWidth() / 2, pixel.getY() - this.texture.getHeight() / 2, -rotation);
        }
    }
    
    public void drawOffscreen(final Pass pass, final Point2D pixel, final double rotation) {
        if (pass == Pass.Object) {
            DrawUtil.drawColoredImage(this.texture, this.color, 1.0f, pixel.getX() - this.texture.getWidth() / 2, pixel.getY() - this.texture.getHeight() / 2, -rotation);
        }
    }
    
    public Point2D.Double getPosition(final double xOffset, final double yOffset, final GridRenderer gridRenderer, final boolean forceUpdate) {
        if (!forceUpdate && this.lastPosition != null) {
            return this.lastPosition;
        }
        final double x = this.waypoint.getX();
        final double z = this.waypoint.getZ();
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
        return this.waypoint.getOrigin();
    }
    
    public static class SimpleCacheLoader extends CacheLoader<Waypoint, DrawWayPointStep>
    {
        public DrawWayPointStep load(final Waypoint waypoint) throws Exception {
            return new DrawWayPointStep(waypoint);
        }
    }
}
