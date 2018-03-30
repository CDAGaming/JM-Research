package journeymap.client.api.display;

import java.util.*;
import journeymap.client.api.model.*;

public interface IWaypointDisplay
{
    Set<Integer> getDisplayDimensions();
    
    MapImage getIcon();
    
    MapText getLabel();
}
