package journeymap.client.data;

import com.google.common.cache.CacheLoader;
import journeymap.client.feature.Feature;
import journeymap.client.feature.FeatureManager;
import journeymap.client.model.EntityDTO;
import journeymap.client.model.EntityHelper;
import journeymap.common.Journeymap;
import net.minecraft.entity.EntityLivingBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimalsData extends CacheLoader<Class, Map<String, EntityDTO>> {
    public Map<String, EntityDTO> load(final Class aClass) throws Exception {
        if (!FeatureManager.isAllowed(Feature.RadarAnimals)) {
            return new HashMap<>();
        }
        final List<EntityDTO> list = EntityHelper.getAnimalsNearby();
        final List<EntityDTO> finalList = new ArrayList<>(list);
        for (final EntityDTO entityDTO : list) {
            final EntityLivingBase entityLiving = entityDTO.entityLivingRef.get();
            if (entityLiving == null) {
                finalList.remove(entityDTO);
            } else {
                if (!entityLiving.isBeingRidden()) {
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
