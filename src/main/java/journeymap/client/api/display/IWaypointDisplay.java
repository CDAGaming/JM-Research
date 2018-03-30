package journeymap.client.api.display;

import journeymap.client.api.model.MapImage;
import journeymap.client.api.model.MapText;

import java.util.Set;

public interface IWaypointDisplay {
    Set<Integer> getDisplayDimensions();

    MapImage getIcon();

    MapText getLabel();
}
