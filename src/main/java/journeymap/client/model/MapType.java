package journeymap.client.model;

import journeymap.client.api.display.*;
import journeymap.client.data.*;
import journeymap.client.feature.*;

public class MapType
{
    public final Integer vSlice;
    public final Name name;
    public final int dimension;
    public final Context.MapType apiMapType;
    private final int theHashCode;
    private final String theCacheKey;
    
    public MapType(final Name name, Integer vSlice, final int dimension) {
        vSlice = ((name != Name.underground) ? null : vSlice);
        this.name = name;
        this.vSlice = vSlice;
        this.dimension = dimension;
        this.apiMapType = this.toApiContextMapType(name);
        this.theCacheKey = toCacheKey(name, vSlice, dimension);
        this.theHashCode = this.theCacheKey.hashCode();
    }
    
    public static MapType from(final Name name, final Integer vSlice, final int dimension) {
        return DataCache.INSTANCE.getMapType(name, vSlice, dimension);
    }
    
    public static MapType from(final Integer vSlice, final int dimension) {
        return from((vSlice == null) ? Name.surface : Name.underground, vSlice, dimension);
    }
    
    public static MapType from(final Name name, final EntityDTO player) {
        return from(name, player.chunkCoordY, player.dimension);
    }
    
    public static MapType day(final int dimension) {
        return from(Name.day, null, dimension);
    }
    
    public static MapType day(final EntityDTO player) {
        return from(Name.day, null, player.dimension);
    }
    
    public static MapType night(final int dimension) {
        return from(Name.night, null, dimension);
    }
    
    public static MapType night(final EntityDTO player) {
        return from(Name.night, null, player.dimension);
    }
    
    public static MapType topo(final int dimension) {
        return from(Name.topo, null, dimension);
    }
    
    public static MapType topo(final EntityDTO player) {
        return from(Name.topo, null, player.dimension);
    }
    
    public static MapType underground(final EntityDTO player) {
        return from(Name.underground, player.chunkCoordY, player.dimension);
    }
    
    public static MapType underground(final Integer vSlice, final int dimension) {
        return from(Name.underground, vSlice, dimension);
    }
    
    public static MapType none() {
        return from(Name.none, 0, 0);
    }
    
    public static String toCacheKey(final Name name, final Integer vSlice, final int dimension) {
        return String.format("%s|%s|%s", dimension, name, (vSlice == null) ? "_" : vSlice);
    }
    
    private Context.MapType toApiContextMapType(final Name name) {
        switch (name) {
            case day: {
                return Context.MapType.Day;
            }
            case topo: {
                return Context.MapType.Topo;
            }
            case night: {
                return Context.MapType.Night;
            }
            case underground: {
                return Context.MapType.Underground;
            }
            default: {
                return Context.MapType.Any;
            }
        }
    }
    
    public static MapType fromApiContextMapType(final Context.MapType apiMapType, final Integer vSlice, final int dimension) {
        switch (apiMapType) {
            case Day: {
                return new MapType(Name.day, vSlice, dimension);
            }
            case Night: {
                return new MapType(Name.night, vSlice, dimension);
            }
            case Underground: {
                return new MapType(Name.underground, vSlice, dimension);
            }
            case Topo: {
                return new MapType(Name.topo, vSlice, dimension);
            }
            default: {
                return new MapType(Name.day, vSlice, dimension);
            }
        }
    }
    
    public String toCacheKey() {
        return this.theCacheKey;
    }
    
    @Override
    public String toString() {
        return this.theCacheKey;
    }
    
    public String name() {
        return this.name.name();
    }
    
    public boolean isUnderground() {
        return this.name == Name.underground;
    }
    
    public boolean isSurface() {
        return this.name == Name.surface;
    }
    
    public boolean isDay() {
        return this.name == Name.day;
    }
    
    public boolean isNight() {
        return this.name == Name.night;
    }
    
    public boolean isTopo() {
        return this.name == Name.topo;
    }
    
    public boolean isDayOrNight() {
        return this.name == Name.day || this.name == Name.night;
    }
    
    public boolean isAllowed() {
        if (this.isUnderground()) {
            return FeatureManager.isAllowed(Feature.MapCaves);
        }
        if (this.isTopo()) {
            return FeatureManager.isAllowed(Feature.MapTopo);
        }
        if (this.isDayOrNight() || this.isSurface()) {
            return FeatureManager.isAllowed(Feature.MapSurface);
        }
        return this.name == Name.none;
    }
    
    @Override
    public int hashCode() {
        return this.theHashCode;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final MapType mapType = (MapType)o;
        if (this.dimension != mapType.dimension) {
            return false;
        }
        if (this.name != mapType.name) {
            return false;
        }
        if (this.vSlice != null) {
            if (this.vSlice.equals(mapType.vSlice)) {
                return true;
            }
        }
        else if (mapType.vSlice == null) {
            return true;
        }
        return false;
    }
    
    public enum Name
    {
        day, 
        night, 
        underground, 
        surface, 
        topo, 
        none;
    }
}
