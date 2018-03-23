package journeymap.client.waypoint;

import journeymap.client.model.*;
import java.util.*;
import java.io.*;
import journeymap.common.*;
import java.nio.charset.*;
import com.google.common.io.*;

public class JmReader
{
    public Collection<Waypoint> loadWaypoints(final File waypointDir) {
        final ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
        final File[] files = waypointDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.endsWith(".json") && !name.equals("waypoint_groups.json");
            }
        });
        if (files == null || files.length == 0) {
            return waypoints;
        }
        final ArrayList<File> obsoleteFiles = new ArrayList<File>();
        for (final File waypointFile : files) {
            final Waypoint wp = this.load(waypointFile);
            if (wp != null) {
                if (!wp.getFileName().endsWith(waypointFile.getName())) {
                    wp.setDirty(true);
                    obsoleteFiles.add(waypointFile);
                }
                waypoints.add(wp);
            }
        }
        while (!obsoleteFiles.isEmpty()) {
            this.remove(obsoleteFiles.remove(0));
        }
        return waypoints;
    }
    
    private void remove(final File waypointFile) {
        try {
            waypointFile.delete();
        }
        catch (Exception e) {
            Journeymap.getLogger().warn(String.format("Can't delete waypoint file %s: %s", waypointFile, e.getMessage()));
            waypointFile.deleteOnExit();
        }
    }
    
    private Waypoint load(final File waypointFile) {
        String waypointString = null;
        Waypoint waypoint = null;
        try {
            waypointString = Files.toString(waypointFile, Charset.forName("UTF-8"));
            waypoint = Waypoint.fromString(waypointString);
            return waypoint;
        }
        catch (Throwable e) {
            Journeymap.getLogger().error(String.format("Can't load waypoint file %s with contents: %s because %s", waypointFile, waypointString, e.getMessage()));
            return waypoint;
        }
    }
}
