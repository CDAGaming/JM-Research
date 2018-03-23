package journeymap.client.data;

import journeymap.client.model.*;
import java.util.*;

public class ImagesData
{
    public static final String PARAM_SINCE = "images.since";
    final long since;
    final List<Object[]> regions;
    final long queryTime;
    
    public ImagesData(final Long since) {
        final long now = new Date().getTime();
        this.queryTime = now;
        this.since = ((since == null) ? now : since);
        final List<RegionCoord> dirtyRegions = RegionImageCache.INSTANCE.getChangedSince(null, this.since);
        if (dirtyRegions.isEmpty()) {
            this.regions = (List<Object[]>)Collections.EMPTY_LIST;
        }
        else {
            this.regions = new ArrayList<Object[]>(dirtyRegions.size());
            for (final RegionCoord rc : dirtyRegions) {
                this.regions.add(new Integer[] { rc.regionX, rc.regionZ });
            }
        }
    }
}
