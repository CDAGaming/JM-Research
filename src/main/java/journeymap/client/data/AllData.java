package journeymap.client.data;

import com.google.common.cache.*;
import journeymap.common.*;
import journeymap.client.model.*;
import com.google.common.collect.*;
import java.util.*;

public class AllData extends CacheLoader<Long, Map>
{
    public Map load(final Long since) throws Exception {
        final DataCache cache = DataCache.INSTANCE;
        final LinkedHashMap<Key, Object> props = new LinkedHashMap<Key, Object>();
        props.put(Key.world, cache.getWorld(false));
        props.put(Key.player, cache.getPlayer(false));
        props.put(Key.images, new ImagesData(since));
        if (Journeymap.getClient().getWebMapProperties().showWaypoints.get()) {
            final int currentDimension = cache.getPlayer(false).dimension;
            final Collection<Waypoint> waypoints = cache.getWaypoints(false);
            final Map<String, Waypoint> wpMap = new HashMap<String, Waypoint>();
            for (final Waypoint waypoint : waypoints) {
                if (waypoint.getDimensions().contains(currentDimension)) {
                    wpMap.put(waypoint.getId(), waypoint);
                }
            }
            props.put(Key.waypoints, wpMap);
        }
        else {
            props.put(Key.waypoints, Collections.emptyMap());
        }
        if (!WorldData.isHardcoreAndMultiplayer()) {
            props.put(Key.animals, Collections.emptyMap());
            props.put(Key.mobs, Collections.emptyMap());
            props.put(Key.players, Collections.emptyMap());
            props.put(Key.villagers, Collections.emptyMap());
        }
        return (Map)ImmutableMap.copyOf((Map)props);
    }
    
    public long getTTL() {
        return Journeymap.getClient().getCoreProperties().renderDelay.get() * 2000;
    }
    
    public enum Key
    {
        animals, 
        images, 
        mobs, 
        player, 
        players, 
        villagers, 
        waypoints, 
        world;
    }
}
