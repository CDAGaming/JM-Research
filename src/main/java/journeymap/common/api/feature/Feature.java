package journeymap.common.api.feature;

import net.minecraft.world.*;

public interface Feature
{
    String name();
    
    default boolean getDefaultAllowed(final boolean isOp, final GameType gameType) {
        return true;
    }
    
    default String getFeatureType() {
        return this.getClass().getSimpleName();
    }
    
    default String getFeatureCategoryKey() {
        return String.format("jm.common.feature.%s", this.getFeatureType().toLowerCase());
    }
    
    default String getFeatureKey() {
        return String.format("%s.%s", this.getFeatureCategoryKey(), this.name().toLowerCase());
    }
    
    default String getFeatureTooltipKey() {
        return String.format("%s.tooltip", this.getFeatureKey());
    }
    
    public enum Action implements Feature
    {
        Teleport;
        
        @Override
        public boolean getDefaultAllowed(final boolean isOp, final GameType gameType) {
            switch (this) {
                case Teleport: {
                    return isOp || gameType.func_77145_d();
                }
                default: {
                    return false;
                }
            }
        }
    }
    
    public enum Display implements Feature
    {
        Compass(false, true), 
        Fullscreen(true, false), 
        Minimap(true, false), 
        WaypointBeacon(false, true), 
        WaypointManager(false, false), 
        Webmap(true, false);
        
        private boolean map;
        private boolean inGame;
        
        private Display(final boolean isMap, final boolean isInGame) {
            this.map = isMap;
            this.inGame = isInGame;
        }
        
        public boolean isMap() {
            return this.map;
        }
        
        public boolean isInGame() {
            return this.inGame;
        }
    }
    
    public enum MapType implements Feature
    {
        Day, 
        Night, 
        Underground, 
        Topo, 
        Biome;
    }
    
    public enum Radar implements Feature
    {
        HostileMob, 
        NPC, 
        PassiveMob, 
        Player, 
        Vehicle, 
        Waypoint;
    }
}
