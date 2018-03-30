package journeymap.common.feature;

import javax.annotation.*;
import journeymap.common.properties.config.*;

@ParametersAreNonnullByDefault
public class DimensionPolicies extends PolicyTable
{
    private final int dimension;
    
    public static DimensionPolicies fromJson(final String json) {
        return (DimensionPolicies)GsonHelper.BUILDER_VERBOSE().create().fromJson(json, (Class)DimensionPolicies.class);
    }
    
    private DimensionPolicies() {
        this.dimension = 0;
    }
    
    public DimensionPolicies(final int dimension) {
        this.dimension = dimension;
    }
    
    public int getDimension() {
        return this.dimension;
    }
    
    public String toJson() {
        return GsonHelper.BUILDER_VERBOSE().create().toJson((Object)this);
    }
    
    public String toCompactJson() {
        return GsonHelper.BUILDER_COMPACT().create().toJson((Object)this);
    }
    
    @Override
    public String toString() {
        return "DimensionPolicies{dimension=" + this.dimension + ", policies=" + this.policies + '}';
    }
}
