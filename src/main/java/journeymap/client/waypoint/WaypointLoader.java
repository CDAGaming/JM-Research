package journeymap.client.waypoint;

import java.io.*;
import journeymap.client.api.display.*;
import java.util.*;
import journeymap.client.io.*;
import java.nio.charset.*;
import com.google.common.io.*;
import journeymap.common.*;
import journeymap.common.log.*;
import com.google.gson.*;

public class WaypointLoader implements IWaypointLoader
{
    public static final Gson GSON;
    final File waypointDir;
    final IWaypointLoader legacyLoader;
    
    WaypointLoader(final File waypointDir) {
        this.waypointDir = waypointDir;
        this.legacyLoader = new WaypointLoaderLegacy(waypointDir);
    }
    
    @Override
    public Collection<Waypoint> loadAll() {
        final ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
        final File[] files = this.waypointDir.listFiles((dir, name) -> name.endsWith(".json") && !name.equals("waypoint_groups.json"));
        if (files == null || files.length == 0) {
            return waypoints;
        }
        for (final File waypointFile : files) {
            Waypoint wp = null;
            Label_0125: {
                if (waypointFile.getName().contains("_waypoint_")) {
                    wp = this.load(waypointFile);
                    if (wp != null) {
                        waypoints.add(wp);
                        break Label_0125;
                    }
                }
                wp = this.legacyLoader.load(waypointFile);
                if (wp != null) {
                    waypoints.add(wp);
                    wp.setDirty(true);
                }
            }
        }
        return waypoints;
    }
    
    @Override
    public boolean save(final Waypoint waypoint) {
        if (waypoint.isPersistent()) {
            File waypointFile = null;
            try {
                waypointFile = new File(FileHandler.getWaypointDir(), WaypointStore.getFileName(waypoint));
                Files.write((CharSequence)WaypointLoader.GSON.toJson((Object)waypoint), waypointFile, Charset.forName("UTF-8"));
                this.legacyLoader.save(waypoint);
                return true;
            }
            catch (Exception e) {
                Journeymap.getLogger().error(String.format("Can't save waypoint file %s: %s", waypointFile, LogFormatter.toString(e)));
            }
        }
        return false;
    }
    
    @Override
    public Waypoint load(final File waypointFile) {
        String waypointString = null;
        Waypoint waypoint = null;
        try {
            waypointString = Files.toString(waypointFile, Charset.forName("UTF-8"));
            waypoint = (Waypoint)WaypointLoader.GSON.fromJson(waypointString, (Class)Waypoint.class);
            this.legacyLoader.save(waypoint);
        }
        catch (Throwable e) {
            Journeymap.getLogger().error(String.format("Can't load waypoint file %s with contents: %s because %s", waypointFile, waypointString, e.getMessage()));
        }
        return waypoint;
    }
    
    static {
        GSON = new GsonBuilder().setPrettyPrinting().create();
    }
}
