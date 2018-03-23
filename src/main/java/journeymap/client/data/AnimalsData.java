package journeymap.client.data;

import com.google.common.cache.*;
import journeymap.client.feature.*;
import journeymap.client.model.*;
import net.minecraft.entity.*;
import java.util.*;
import journeymap.common.*;

public class AnimalsData extends CacheLoader<Class, Map<String, EntityDTO>>
{
    public Map<String, EntityDTO> load(final Class aClass) throws Exception {
        if (!FeatureManager.isAllowed(Feature.RadarAnimals)) {
            return new HashMap<String, EntityDTO>();
        }
        final List<EntityDTO> list = EntityHelper.getAnimalsNearby();
        final List<EntityDTO> finalList = new ArrayList<EntityDTO>(list);
        for (final EntityDTO entityDTO : list) {
            final EntityLivingBase entityLiving = entityDTO.entityLivingRef.get();
            if (entityLiving == null) {
                finalList.remove(entityDTO);
            }
            else {
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
