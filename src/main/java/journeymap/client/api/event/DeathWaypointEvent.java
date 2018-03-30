package journeymap.client.api.event;

import com.google.common.base.MoreObjects;
import net.minecraft.util.math.BlockPos;

public class DeathWaypointEvent extends ClientEvent {
    public final BlockPos location;

    public DeathWaypointEvent(final BlockPos location, final int dimension) {
        super(Type.DEATH_WAYPOINT, dimension);
        this.location = location;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("location", this.location).toString();
    }
}
