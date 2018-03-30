package journeymap.client.data;

import com.google.common.cache.*;
import journeymap.client.feature.*;
import journeymap.common.api.feature.*;
import journeymap.client.model.*;
import java.util.*;
import journeymap.common.*;

public class HostileMobsData extends CacheLoader<Class, Map<String, EntityDTO>>
{
    public Map<String, EntityDTO> load(final Class aClass) throws Exception {
        if (!ClientFeatures.instance().isAllowed(Feature.Radar.HostileMob, DataCache.getPlayer().dimension)) {
            return new HashMap<String, EntityDTO>();
        }
        final List<EntityDTO> list = EntityHelper.getMobsNearby();
        return EntityHelper.buildEntityIdMap(list, true);
    }
    
    public long getTTL() {
        return Journeymap.getClient().getCoreProperties().cacheMobsData.get();
    }
}
