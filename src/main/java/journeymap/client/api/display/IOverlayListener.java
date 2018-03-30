package journeymap.client.api.display;

import journeymap.client.api.util.*;
import java.awt.geom.*;
import net.minecraft.util.math.*;

public interface IOverlayListener
{
    void onActivate(final UIState p0);
    
    void onDeactivate(final UIState p0);
    
    void onMouseMove(final UIState p0, final Point2D.Double p1, final BlockPos p2);
    
    void onMouseOut(final UIState p0, final Point2D.Double p1, final BlockPos p2);
    
    boolean onMouseClick(final UIState p0, final Point2D.Double p1, final BlockPos p2, final int p3, final boolean p4);
}
