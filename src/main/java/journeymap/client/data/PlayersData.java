package journeymap.client.data;

import com.google.common.cache.CacheLoader;
import journeymap.client.feature.Feature;
import journeymap.client.feature.FeatureManager;
import journeymap.client.model.EntityDTO;
import journeymap.client.model.EntityHelper;
import journeymap.common.Journeymap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayersData extends CacheLoader<Class, Map<String, EntityDTO>> {
    public Map<String, EntityDTO> load(final Class aClass) throws Exception {
        if (!FeatureManager.isAllowed(Feature.RadarPlayers)) {
            return new HashMap<>();
        }
        final List<EntityDTO> list = EntityHelper.getPlayersNearby();
        return EntityHelper.buildEntityIdMap(list, true);
    }

    public long getTTL() {
        return Journeymap.getClient().getCoreProperties().cachePlayersData.get();
    }
}
