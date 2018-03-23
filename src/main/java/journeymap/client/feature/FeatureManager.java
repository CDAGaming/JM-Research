package journeymap.client.feature;

import journeymap.server.properties.*;
import journeymap.common.*;
import java.util.*;

public enum FeatureManager
{
    INSTANCE;
    
    private final HashMap<Feature, Policy> policyMap;
    
    private FeatureManager() {
        this.policyMap = new HashMap<Feature, Policy>();
        this.reset();
    }
    
    public static String getPolicyDetails() {
        final StringBuilder sb = new StringBuilder("Features: ");
        for (final Feature feature : Feature.values()) {
            boolean single = false;
            boolean multi = false;
            if (FeatureManager.INSTANCE.policyMap.containsKey(feature)) {
                single = FeatureManager.INSTANCE.policyMap.get(feature).allowInSingleplayer;
                multi = FeatureManager.INSTANCE.policyMap.get(feature).allowInMultiplayer;
            }
            sb.append(String.format("\n\t%s : singleplayer = %s , multiplayer = %s", feature.name(), single, multi));
        }
        return sb.toString();
    }
    
    public static boolean isAllowed(final Feature feature) {
        final Policy policy = FeatureManager.INSTANCE.policyMap.get(feature);
        return policy != null && policy.isCurrentlyAllowed();
    }
    
    public static Map<Feature, Boolean> getAllowedFeatures() {
        final Map<Feature, Boolean> map = new HashMap<Feature, Boolean>(Feature.values().length * 2);
        for (final Feature feature : Feature.values()) {
            map.put(feature, isAllowed(feature));
        }
        return map;
    }
    
    public void updateDimensionFeatures(final PermissionProperties properties) {
        this.reset();
        if (!properties.caveMappingEnabled.get()) {
            Journeymap.getLogger().info("Feature disabled in multiplayer: " + Feature.MapCaves);
            this.policyMap.put(Feature.MapCaves, new Policy(Feature.MapCaves, true, false));
        }
        if (properties.radarEnabled.get()) {
            this.setMultiplayerFeature(Feature.RadarAnimals, properties.animalRadarEnabled.get());
            this.setMultiplayerFeature(Feature.RadarMobs, properties.mobRadarEnabled.get());
            this.setMultiplayerFeature(Feature.RadarPlayers, properties.playerRadarEnabled.get());
            this.setMultiplayerFeature(Feature.RadarVillagers, properties.villagerRadarEnabled.get());
        }
        else {
            this.setMultiplayerFeature(Feature.RadarAnimals, false);
            this.setMultiplayerFeature(Feature.RadarMobs, false);
            this.setMultiplayerFeature(Feature.RadarPlayers, false);
            this.setMultiplayerFeature(Feature.RadarVillagers, false);
        }
    }
    
    private void setMultiplayerFeature(final Feature feature, final boolean enable) {
        if (!enable) {
            Journeymap.getLogger().info("Feature disabled in multiplayer: " + feature);
        }
        this.policyMap.put(feature, new Policy(feature, true, enable));
    }
    
    public void reset() {
        for (final Policy policy : Policy.bulkCreate(true, true)) {
            this.policyMap.put(policy.feature, policy);
        }
    }
}
