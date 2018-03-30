package journeymap.client.data;

import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableMap;
import journeymap.client.api.display.Waypoint;
import journeymap.client.feature.ClientFeatures;
import journeymap.client.waypoint.WaypointStore;
import journeymap.common.Journeymap;
import journeymap.common.api.feature.Feature;

import java.util.*;

public class AllData extends CacheLoader<Long, Map> {
    public Map load(final Long since) throws Exception {
        final DataCache cache = DataCache.INSTANCE;
        final LinkedHashMap<Key, Object> props = new LinkedHashMap<>();
        props.put(Key.world, cache.getWorld(false));
        props.put(Key.player, cache.getPlayer(false));
        props.put(Key.images, new ImagesData(since));
        final int dim = cache.getPlayer(false).dimension;
        if (ClientFeatures.instance().isAllowed(Feature.Radar.Waypoint, dim) && Journeymap.getClient().getWebMapProperties().showWaypoints.get()) {
            final Collection<Waypoint> waypoints = WaypointStore.INSTANCE.getAll(dim);
            final Map<String, Waypoint> wpMap = new HashMap<>();
            for (final Waypoint waypoint : waypoints) {
                wpMap.put(waypoint.getId(), waypoint);
            }
            props.put(Key.waypoints, wpMap);
        } else {
            props.put(Key.waypoints, Collections.emptyMap());
        }
        if (ClientFeatures.instance().isAllowed(Feature.Radar.PassiveMob, dim)) {
            props.put(Key.passiveMobs, cache.getPassiveMobs(false));
        } else {
            props.put(Key.passiveMobs, Collections.emptyMap());
        }
        if (ClientFeatures.instance().isAllowed(Feature.Radar.HostileMob, dim)) {
            props.put(Key.hostileMobs, cache.getHostileMobs(false));
        } else {
            props.put(Key.hostileMobs, Collections.emptyMap());
        }
        if (ClientFeatures.instance().isAllowed(Feature.Radar.Player, dim)) {
            props.put(Key.players, cache.getPlayers(false));
        } else {
            props.put(Key.players, Collections.emptyMap());
        }
        if (ClientFeatures.instance().isAllowed(Feature.Radar.NPC, dim)) {
            props.put(Key.npcs, cache.getNpcs(false));
        } else {
            props.put(Key.npcs, Collections.emptyMap());
        }
        return ImmutableMap.copyOf((Map) props);
    }

    public long getTTL() {
        return Journeymap.getClient().getCoreProperties().renderDelay.get() * 2000;
    }

    public enum Key {
        passiveMobs,
        images,
        hostileMobs,
        player,
        players,
        npcs,
        waypoints,
        world
    }
}
