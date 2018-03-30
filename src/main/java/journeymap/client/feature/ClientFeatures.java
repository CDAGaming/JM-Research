package journeymap.client.feature;

import javax.annotation.*;
import net.minecraft.client.*;
import journeymap.common.api.feature.*;
import net.minecraft.world.*;
import journeymap.client.*;
import journeymap.common.feature.*;
import journeymap.common.*;
import net.minecraftforge.fml.common.*;
import java.text.*;
import com.google.common.base.*;
import java.util.*;

@ParametersAreNonnullByDefault
public class ClientFeatures extends PlayerFeatures
{
    private static DateFormat DATEFORMAT;
    private static Supplier<ClientFeatures> instance;
    private static final Map<String, String> modIdMap;
    
    private ClientFeatures() {
        super(Minecraft.func_71410_x().func_110432_I().func_148256_e().getId());
    }
    
    public boolean isAllowed(final Feature feature, final int dimension) {
        return this.get(dimension).isAllowed(JourneymapClient.getGameType(), feature);
    }
    
    @Override
    public boolean isAllowed(final GameType gameType, final Feature feature, final int dimension) {
        return this.get(dimension).isAllowed(gameType, feature);
    }
    
    public static ClientFeatures instance() {
        return (ClientFeatures)ClientFeatures.instance.get();
    }
    
    public static String getServerOriginName() {
        return Constants.getString("jm.common.server_config");
    }
    
    public static String getFeatureCategoryName(final Feature feature) {
        return Constants.getString(feature.getFeatureCategoryKey());
    }
    
    public static String getFeatureName(final Feature feature) {
        return Constants.getString(feature.getFeatureKey());
    }
    
    public static String getFeatureTooltip(final Feature feature) {
        return Constants.getString(feature.getFeatureTooltipKey());
    }
    
    public static String getOriginString(final Policy policy) {
        final String origin = policy.getOrigin();
        if (origin == null) {
            return null;
        }
        if (origin.startsWith("jm.")) {
            return Constants.getString(origin);
        }
        return getModName(origin);
    }
    
    public static String getAuditString(final Policy policy) {
        final String origin = getOriginString(policy);
        final String time = ClientFeatures.DATEFORMAT.format(new Date(policy.getTimestamp()));
        switch (policy.getEvent()) {
            case Initialize: {
                return Constants.getString("jm.common.features.audit_feature_init", origin, time);
            }
            case Reset: {
                return Constants.getString("jm.common.features.audit_feature_reset", origin, time);
            }
            case Update: {
                if (policy.isAllowed()) {
                    return Constants.getString("jm.common.features.audit_feature_enable", origin, time);
                }
                return Constants.getString("jm.common.features.audit_feature_disable", origin, time);
            }
            default: {
                return policy.toString();
            }
        }
    }
    
    public void logDeltas(final String header, final DimensionPolicies oldPolicies, final DimensionPolicies newPolicies) {
        boolean unchanged = true;
        final StringBuilder sb = new StringBuilder(header);
        for (final GameType gameType : ClientFeatures.VALID_GAME_TYPES) {
            boolean showType = true;
            for (final Feature feature : PlayerFeatures.ALL_FEATURES) {
                final boolean oldAllowed = oldPolicies.isAllowed(gameType, feature);
                final boolean newAllowed = newPolicies.isAllowed(gameType, feature);
                if (oldAllowed != newAllowed) {
                    unchanged = false;
                    if (showType) {
                        sb.append("\n\t").append(gameType.name());
                        showType = false;
                    }
                    sb.append("\n\t\t").append(Constants.getString(feature.getFeatureCategoryKey()));
                    sb.append(" ").append(Constants.getString(feature.getFeatureKey()));
                    sb.append(" = ").append(newAllowed);
                }
            }
        }
        if (unchanged) {
            sb.append(": No changes");
        }
        Journeymap.getLogger().info(sb.toString());
    }
    
    private static String getModName(final String modId) {
        ModContainer mod;
        final Iterator<Map.Entry<String, ModContainer>> iterator;
        Map.Entry<String, ModContainer> modEntry;
        return ClientFeatures.modIdMap.computeIfAbsent(modId, id -> {
            try {
                mod = Loader.instance().getIndexedModList().get(modId);
                if (mod == null) {
                    Loader.instance().getIndexedModList().entrySet().iterator();
                    while (iterator.hasNext()) {
                        modEntry = iterator.next();
                        if (modEntry.getValue().getModId().toLowerCase().equals(modId)) {
                            mod = modEntry.getValue();
                            break;
                        }
                    }
                }
                if (mod != null) {
                    return mod.getName();
                }
            }
            catch (Exception e) {
                Journeymap.getLogger().error("Error looking up mod " + id, (Throwable)e);
            }
            return id;
        });
    }
    
    static {
        ClientFeatures.DATEFORMAT = new SimpleDateFormat("d MMM yyyy HH:mm:ss");
        ClientFeatures.instance = (Supplier<ClientFeatures>)Suppliers.memoize(ClientFeatures::new);
        modIdMap = new HashMap<String, String>();
    }
}
