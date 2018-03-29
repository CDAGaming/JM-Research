package journeymap.client.waypoint;

import com.google.common.io.Files;
import journeymap.client.model.Waypoint;
import journeymap.common.Journeymap;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

public class JmReader {
    public Collection<Waypoint> loadWaypoints(final File waypointDir) {
        final ArrayList<Waypoint> waypoints = new ArrayList<>();
        final File[] files = waypointDir.listFiles((dir, name) -> name.endsWith(".json") && !name.equals("waypoint_groups.json"));
        if (files == null || files.length == 0) {
            return waypoints;
        }
        final ArrayList<File> obsoleteFiles = new ArrayList<>();
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
        } catch (Exception e) {
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
        } catch (Throwable e) {
            Journeymap.getLogger().error(String.format("Can't load waypoint file %s with contents: %s because %s", waypointFile, waypointString, e.getMessage()));
            return waypoint;
        }
    }
}
