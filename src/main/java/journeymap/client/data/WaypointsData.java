package journeymap.client.data;

import com.google.common.cache.CacheLoader;
import journeymap.client.api.display.Waypoint;
import journeymap.client.feature.ClientFeatures;
import journeymap.client.waypoint.WaypointStore;
import journeymap.common.api.feature.Feature;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class WaypointsData extends CacheLoader<Class, Collection<Waypoint>> {
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
