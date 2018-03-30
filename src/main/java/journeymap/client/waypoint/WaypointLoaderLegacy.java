package journeymap.client.waypoint;

import journeymap.client.api.display.*;
import java.util.*;
import java.nio.charset.*;
import com.google.common.io.*;
import journeymap.common.*;
import java.io.*;
import com.google.gson.*;

public class WaypointLoaderLegacy implements IWaypointLoader
{
    public static final Gson GSON;
    final File backup55Dir;
    
    public WaypointLoaderLegacy(final File waypointDir) {
        (this.backup55Dir = new File(waypointDir, "backup_5.5")).mkdirs();
    }
    
    @Override
    public Collection<Waypoint> loadAll() {
        final ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
        final File[] files = this.backup55Dir.listFiles((dir, name) -> name.endsWith(".json") && !name.equals("waypoint_groups.json"));
        if (files == null || files.length == 0) {
            return waypoints;
        }
        for (final File waypointFile : files) {
            final Waypoint wp = this.load(waypointFile);
            if (wp != null) {
                waypoints.add(wp);
            }
        }
        return waypoints;
    }
    
    @Override
    public boolean save(final Waypoint waypoint) {
        if (waypoint.isPersistent()) {
            File legacyFile = null;
            try {
                final WaypointLegacy legacy = WaypointLegacy.toLegacy(waypoint);
                final String legacyId = String.format("%s_%s,%s,%s", legacy.name, legacy.x, legacy.y, legacy.z);
                final String fileName = legacyId.toLowerCase().replaceAll("[\\\\/:\"*?<>|]", "_").concat(".json");
                legacyFile = new File(this.backup55Dir, fileName);
                Files.write((CharSequence)WaypointLoaderLegacy.GSON.toJson((Object)legacy), legacyFile, Charset.forName("UTF-8"));
                return true;
            }
            catch (Exception e) {
                Journeymap.getLogger().warn(String.format("Can't write legacy waypoint file to %s: %s", legacyFile, e.getMessage()));
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
            waypoint = WaypointLegacy.fromString(waypointString).toWaypoint();
        }
        catch (Throwable e) {
            Journeymap.getLogger().error(String.format("Can't load legacy waypoint file %s with contents: %s because %s", waypointFile, waypointString, e.getMessage()));
        }
        if (!waypointFile.getParentFile().equals(this.backup55Dir)) {
            final File backupLocation = new File(this.backup55Dir, waypointFile.getName());
            try {
                Files.move(waypointFile, backupLocation);
            }
            catch (IOException e2) {
                Journeymap.getLogger().warn(String.format("Can't move legacy waypoint file to %s: %s", backupLocation, e2.getMessage()));
            }
        }
        return waypoint;
    }
    
    static {
        GSON = new GsonBuilder().setPrettyPrinting().create();
    }
}
