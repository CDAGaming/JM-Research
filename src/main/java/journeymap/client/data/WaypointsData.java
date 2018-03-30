package journeymap.client.data;

import com.google.common.cache.*;
import journeymap.client.api.display.*;
import journeymap.client.feature.*;
import journeymap.common.api.feature.*;
import java.util.*;
import journeymap.client.waypoint.*;

public class WaypointsData extends CacheLoader<Class, Collection<Waypoint>>
{
    protected static List<Waypoint> getWaypoints() {
        if (!ClientFeatures.instance().isAllowed(Feature.Radar.Waypoint, DataCache.getPlayer().dimension)) {
            return Collections.emptyList();
        }
        return WaypointStore.INSTANCE.getAll();
    }
    
    public Collection<Waypoint> load(final Class aClass) throws Exception {
        return getWaypoints();
    }
    
    public long getTTL() {
        return 5000L;
    }
}
