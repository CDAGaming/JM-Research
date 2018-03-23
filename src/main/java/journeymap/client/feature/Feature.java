package journeymap.client.feature;

import java.util.*;

public enum Feature
{
    RadarPlayers, 
    RadarAnimals, 
    RadarMobs, 
    RadarVillagers, 
    MapTopo, 
    MapSurface, 
    MapCaves;
    
    public static EnumSet<Feature> radar() {
        return EnumSet.of(Feature.RadarPlayers, Feature.RadarAnimals, Feature.RadarMobs, Feature.RadarVillagers);
    }
    
    public static EnumSet<Feature> all() {
        return EnumSet.allOf(Feature.class);
    }
}
