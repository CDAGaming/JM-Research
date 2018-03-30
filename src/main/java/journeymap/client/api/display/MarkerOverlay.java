package journeymap.client.api.display;

import javax.annotation.*;
import net.minecraft.util.math.*;
import journeymap.client.api.model.*;

@ParametersAreNonnullByDefault
public final class MarkerOverlay extends Overlay
{
    private BlockPos point;
    private MapImage icon;
    
    public MarkerOverlay(final String modId, final String markerId, final BlockPos point, final MapImage icon) {
        super(modId, markerId);
        this.setPoint(point);
        this.setIcon(icon);
    }
    
    public BlockPos getPoint() {
        return this.point;
    }
    
    public MarkerOverlay setPoint(final BlockPos point) {
        this.point = point;
        return this;
    }
    
    public MapImage getIcon() {
        return this.icon;
    }
    
    public MarkerOverlay setIcon(final MapImage icon) {
        this.icon = icon;
        return this;
    }
    
    @Override
    public String toString() {
        return this.toStringHelper(this).add("icon", (Object)this.icon).add("point", (Object)this.point).toString();
    }
}
