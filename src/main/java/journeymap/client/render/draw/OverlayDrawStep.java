package journeymap.client.render.draw;

import journeymap.client.api.display.Overlay;
import journeymap.client.render.map.GridRenderer;

import javax.annotation.Nullable;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public interface OverlayDrawStep extends DrawStep {
    Overlay getOverlay();

    Rectangle2D.Double getBounds();

    boolean isOnScreen(final double p0, final double p1, final GridRenderer p2, final double p3);

    void setTitlePosition(@Nullable final Point2D.Double p0);

    void setEnabled(final boolean p0);
}
