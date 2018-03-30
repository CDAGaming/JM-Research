package journeymap.client.data;

import com.google.common.cache.CacheLoader;
import journeymap.client.feature.ClientFeatures;
import journeymap.client.model.EntityDTO;
import journeymap.client.model.EntityHelper;
import journeymap.common.Journeymap;
import journeymap.common.api.feature.Feature;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehiclesData extends CacheLoader<Class, Map<String, EntityDTO>> {
    public Map<String, EntityDTO> load(final Class aClass) throws Exception {
        if (!ClientFeatures.instance().isAllowed(Feature.Radar.Vehicle, DataCache.getPlayer().dimension)) {
            return new HashMap<>();
        }
        final List<EntityDTO> list = EntityHelper.getVehiclesNearby();
        final List<EntityDTO> finalList = new ArrayList<>(list);
        for (final EntityDTO entityDTO : list) {
            final Entity entity = entityDTO.entityRef.get();
            if (entity == null) {
                finalList.remove(entityDTO);
            } else {
                if (!entity.isBeingRidden()) {
                    continue;
                }
                finalList.remove(entityDTO);
            }
        }
        return EntityHelper.buildEntityIdMap(finalList, true);
    }

    public long getTTL() {
        return Math.max(1000, Journeymap.getClient().getCoreProperties().cacheAnimalsData.get());
    }
}
