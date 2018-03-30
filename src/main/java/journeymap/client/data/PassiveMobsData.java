package journeymap.client.data;

import com.google.common.cache.*;
import journeymap.client.feature.*;
import journeymap.common.api.feature.*;
import journeymap.client.model.*;
import net.minecraft.entity.*;
import java.util.*;
import journeymap.common.*;

public class PassiveMobsData extends CacheLoader<Class, Map<String, EntityDTO>>
{
    public Map<String, EntityDTO> load(final Class aClass) throws Exception {
        if (!ClientFeatures.instance().isAllowed(Feature.Radar.PassiveMob, DataCache.getPlayer().dimension)) {
            return new HashMap<String, EntityDTO>();
        }
        final List<EntityDTO> list = EntityHelper.getPassiveMobsNearby();
        final List<EntityDTO> finalList = new ArrayList<EntityDTO>(list);
        for (final EntityDTO entityDTO : list) {
            final Entity entity = entityDTO.entityRef.get();
            if (entity == null) {
                finalList.remove(entityDTO);
            }
            else {
                if (!entity.func_184207_aI()) {
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
