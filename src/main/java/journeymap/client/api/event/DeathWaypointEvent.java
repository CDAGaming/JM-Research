package journeymap.client.api.event;

import net.minecraft.util.math.*;
import com.google.common.base.*;

public class DeathWaypointEvent extends ClientEvent
{
    public final BlockPos location;
    
    public DeathWaypointEvent(final BlockPos location, final int dimension) {
        super(Type.DEATH_WAYPOINT, dimension);
        this.location = location;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("location", (Object)this.location).toString();
    }
}
