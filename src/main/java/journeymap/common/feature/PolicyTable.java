package journeymap.common.feature;

import javax.annotation.*;
import com.google.common.collect.*;
import journeymap.common.api.feature.*;
import net.minecraft.world.*;
import java.util.*;

@ParametersAreNonnullByDefault
public class PolicyTable
{
    protected final HashBasedTable<Feature, GameType, Policy> policies;
    
    public static HashBasedTable<Feature, GameType, Policy> createTable() {
        return (HashBasedTable<Feature, GameType, Policy>)HashBasedTable.create(PlayerFeatures.ALL_FEATURES.size(), GameType.values().length);
    }
    
    public PolicyTable() {
        this.policies = createTable();
    }
    
    public HashBasedTable<Feature, GameType, Policy> getTable() {
        return this.policies;
    }
    
    public String getOrigin(final GameType gameType, final Feature feature) {
        return this.getPolicy(gameType, feature).getOrigin();
    }
    
    public boolean isAllowed(final GameType gameType, final Feature feature) {
        return this.getPolicy(gameType, feature).isAllowed();
    }
    
    public void setPolicies(final GameType gameType, final Map<Feature, Policy> policyMap) {
        policyMap.forEach((feature, policy) -> this.setPolicy(gameType, policy));
    }
    
    public Policy setAllowed(final GameType gameType, final Feature feature, final boolean allowed, final String origin) {
        return this.setPolicy(gameType, Policy.update(origin, gameType, feature, allowed));
    }
    
    public void setAllowed(final Set<GameType> gameTypes, final Feature feature, final boolean allowed, final String origin) {
        for (final GameType gameType : gameTypes) {
            this.setPolicy(gameType, Policy.update(origin, gameType, feature, allowed));
        }
    }
    
    protected Policy getPolicy(final GameType gameType, final Feature feature) {
        synchronized (this.policies) {
            return this.policies.row((Object)feature).computeIfAbsent(gameType, gt -> this.initPolicy(false, gameType, feature));
        }
    }
    
    protected Policy initPolicy(final boolean isOp, final GameType gameType, final Feature feature) {
        return Policy.initialize(isOp, gameType, feature);
    }
    
    protected Policy setPolicy(final GameType gameType, final Policy policy) {
        synchronized (this.policies) {
            this.policies.put((Object)policy.getFeature(), (Object)gameType, (Object)policy);
        }
        return policy;
    }
    
    public void reset(final boolean isOp) {
        PlayerFeatures.ALL_FEATURES.forEach(feature -> PlayerFeatures.VALID_GAME_TYPES.forEach(type -> this.setPolicy(type, Policy.reset(isOp, type, feature))));
    }
    
    public void update(final PolicyTable other) {
        final Policy change;
        PlayerFeatures.ALL_FEATURES.forEach(feature -> PlayerFeatures.VALID_GAME_TYPES.forEach(type -> {
            if (other.policies.contains((Object)feature, (Object)type)) {
                change = other.getPolicy(type, feature);
                this.setPolicy(type, Policy.update(change));
            }
        }));
    }
    
    public void update(final GameType gameType, final Map<Feature, Boolean> featureMap, final String origin) {
        featureMap.forEach((feature, allowed) -> this.setAllowed(gameType, feature, allowed, origin));
    }
    
    public Map<Feature, Boolean> getPermissionMap(final GameType gameType) {
        final TreeMap<Feature, Boolean> map = new TreeMap<Feature, Boolean>();
        PlayerFeatures.ALL_FEATURES.forEach(feature -> map.put(feature, this.isAllowed(gameType, feature)));
        return map;
    }
    
    public Map<Feature, Policy> getPolicyMap(final GameType gameType) {
        final IdentityHashMap<Feature, Policy> map = new IdentityHashMap<Feature, Policy>();
        final IdentityHashMap<Feature, Policy> identityHashMap;
        PlayerFeatures.ALL_FEATURES.forEach(feature -> {
            if (this.policies.contains((Object)feature, (Object)gameType)) {
                identityHashMap.put(feature, this.getPolicy(gameType, feature));
            }
            return;
        });
        return map;
    }
    
    public void inflate() {
        PlayerFeatures.ALL_FEATURES.forEach(feature -> PlayerFeatures.VALID_GAME_TYPES.forEach(type -> this.isAllowed(type, feature)));
    }
    
    @Override
    public String toString() {
        return this.policies.toString();
    }
}
