package journeymap.client.data;

import com.google.common.cache.CacheLoader;
import journeymap.client.feature.ClientFeatures;
import journeymap.client.model.EntityDTO;
import journeymap.client.model.EntityHelper;
import journeymap.common.Journeymap;
import journeymap.common.api.feature.Feature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayersData extends CacheLoader<Class, Map<String, EntityDTO>> {
    public Map<String, EntityDTO> load(final Class aClass) throws Exception {
        if (!ClientFeatures.instance().isAllowed(Feature.Radar.Player, DataCache.getPlayer().dimension)) {
            return new HashMap<>();
        }
        final List<EntityDTO> list = EntityHelper.getPlayersNearby();
        return EntityHelper.buildEntityIdMap(list, true);
    }

    public long getTTL() {
        return Journeymap.getClient().getCoreProperties().cachePlayersData.get();
    }
}
