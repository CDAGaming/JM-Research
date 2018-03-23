package journeymap.client.render.draw;

import journeymap.client.api.display.*;
import java.awt.geom.*;
import journeymap.client.render.map.*;
import net.minecraft.util.math.*;
import java.util.*;
import journeymap.client.api.model.*;

public class DrawPolygonStep extends BaseOverlayDrawStep<PolygonOverlay>
{
    protected List<Point2D.Double> screenPoints;
    boolean onScreen;
    
    public DrawPolygonStep(final PolygonOverlay polygon) {
        super(polygon);
        this.screenPoints = new ArrayList<Point2D.Double>();
    }
    
    @Override
    public void draw(final DrawStep.Pass pass, final double xOffset, final double yOffset, final GridRenderer gridRenderer, final double fontScale, final double rotation) {
        if (pass == DrawStep.Pass.Object) {
            if (((PolygonOverlay)this.overlay).getOuterArea().getPoints().isEmpty()) {
                this.onScreen = false;
                return;
            }
            this.onScreen = this.isOnScreen(xOffset, yOffset, gridRenderer, rotation);
            if (this.onScreen) {
                DrawUtil.drawPolygon(xOffset, yOffset, this.screenPoints, ((PolygonOverlay)this.overlay).getShapeProperties());
            }
        }
        else if (this.onScreen) {
            super.drawText(pass, xOffset, yOffset, gridRenderer, fontScale, rotation);
        }
    }
    
    @Override
    protected void updatePositions(final GridRenderer gridRenderer, final double rotation) {
        if (((PolygonOverlay)this.overlay).getOuterArea().getPoints().isEmpty()) {
            this.onScreen = false;
            return;
        }
        final List<BlockPos> points = ((PolygonOverlay)this.overlay).getOuterArea().getPoints();
        this.screenPoints.clear();
        for (final BlockPos pos : points) {
            final Point2D.Double pixel = gridRenderer.getBlockPixelInGrid(pos);
            pixel.setLocation(pixel.getX(), pixel.getY());
            if (this.screenPoints.isEmpty()) {
                this.screenBounds.setRect(pixel.x, pixel.y, 1.0, 1.0);
            }
            else {
                this.screenBounds.add(pixel);
            }
            this.screenPoints.add(pixel);
        }
        final TextProperties textProperties = ((PolygonOverlay)this.overlay).getTextProperties();
        this.labelPosition.setLocation(this.screenBounds.getCenterX() + textProperties.getOffsetX(), this.screenBounds.getCenterY() + textProperties.getOffsetY());
    }
}
