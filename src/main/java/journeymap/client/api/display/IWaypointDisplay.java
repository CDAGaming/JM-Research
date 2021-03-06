package journeymap.client.api.display;

import journeymap.client.api.model.MapImage;

public interface IWaypointDisplay {
    Integer getColor();

    Integer getBackgroundColor();

    int[] getDisplayDimensions();

    MapImage getIcon();
}
