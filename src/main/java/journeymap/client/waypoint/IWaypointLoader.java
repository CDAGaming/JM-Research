package journeymap.client.waypoint;

import java.io.*;
import journeymap.client.api.display.*;
import java.util.*;

public interface IWaypointLoader
{
    Waypoint load(final File p0);
    
    Collection<Waypoint> loadAll();
    
    boolean save(final Waypoint p0);
}
