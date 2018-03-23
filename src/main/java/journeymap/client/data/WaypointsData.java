package journeymap.client.data;

import com.google.common.cache.*;
import journeymap.client.model.*;
import journeymap.common.*;
import java.util.*;
import journeymap.client.waypoint.*;

public class WaypointsData extends CacheLoader<Class, Collection<Waypoint>>
{
    public static boolean isManagerEnabled() {
        return Journeymap.getClient().getWaypointProperties().managerEnabled.get();
    }
    
    protected static List<Waypoint> getWaypoints() {
        final ArrayList<Waypoint> list = new ArrayList<Waypoint>(0);
        if (isManagerEnabled()) {
            list.addAll(WaypointStore.INSTANCE.getAll());
        }
        return list;
    }
    
    private static boolean waypointClassesFound(final String... names) throws Exception {
        boolean loaded = true;
        for (final String name : names) {
            if (!loaded) {
                break;
            }
            try {
                loaded = false;
                Class.forName(name);
                loaded = true;
                Journeymap.getLogger().debug("Class found: " + name);
            }
            catch (NoClassDefFoundError e) {
                throw new Exception("Class detected, but is obsolete: " + e.getMessage());
            }
            catch (ClassNotFoundException e2) {
                Journeymap.getLogger().debug("Class not found: " + name);
            }
            catch (VerifyError v) {
                throw new Exception("Class detected, but is obsolete: " + v.getMessage());
            }
            catch (Throwable t) {
                throw new Exception("Class detected, but produced errors.", t);
            }
        }
        return loaded;
    }
    
    public Collection<Waypoint> load(final Class aClass) throws Exception {
        return getWaypoints();
    }
    
    public long getTTL() {
        return 5000L;
    }
}
