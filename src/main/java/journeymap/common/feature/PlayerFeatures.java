package journeymap.common.feature;

import javax.annotation.*;
import journeymap.common.api.feature.*;
import net.minecraft.world.*;
import com.google.gson.annotations.*;
import java.util.function.*;
import java.util.*;
import java.util.stream.*;
import com.google.common.collect.*;

@ParametersAreNonnullByDefault
public class PlayerFeatures
{
    public static List<Feature> ALL_FEATURES;
    public static Set<GameType> VALID_GAME_TYPES;
    @Since(2.0)
    protected final UUID id;
    @Since(2.0)
    protected final Map<Integer, DimensionPolicies> dimensionPolicies;
    
    public PlayerFeatures(final UUID playerID) {
        this.dimensionPolicies = (Map<Integer, DimensionPolicies>)Maps.synchronizedNavigableMap((NavigableMap)new TreeMap());
        this.id = playerID;
    }
    
    public UUID getID() {
        return this.id;
    }
    
    public String getJson(final int dimension) {
        return this.get(dimension).toJson();
    }
    
    public boolean isAllowed(final GameType gameType, final Feature feature, final int dimension) {
        return this.get(dimension).isAllowed(gameType, feature);
    }
    
    public final DimensionPolicies get(final int dimension) {
        synchronized (this.dimensionPolicies) {
            return this.dimensionPolicies.computeIfAbsent(dimension, this::initializePolicies);
        }
    }
    
    public void updatePolicies(final DimensionPolicies newPolicies) {
        this.get(newPolicies.getDimension()).update(newPolicies);
    }
    
    public Policy setAllowed(final int dimension, final GameType gameType, final Feature feature, final boolean allowed, final String origin) {
        return this.get(dimension).setAllowed(gameType, feature, allowed, origin);
    }
    
    public void reset(final int dimension, final boolean isOp) {
        this.get(dimension).reset(isOp);
    }
    
    public void resetAll(final boolean isOp) {
        for (final int dim : this.dimensionPolicies.keySet()) {
            this.reset(dim, isOp);
        }
    }
    
    protected DimensionPolicies initializePolicies(final int dimension) {
        return new DimensionPolicies(dimension);
    }
    
    static {
        PlayerFeatures.ALL_FEATURES = Stream.of((Stream[])new Stream[] { Arrays.stream(Feature.Action.values()), Arrays.stream(Feature.Display.values()), Arrays.stream(Feature.MapType.values()), Arrays.stream(Feature.Radar.values()) }).flatMap(v -> v).collect((Collector<? super Object, ?, List<Feature>>)Collectors.toList());
        PlayerFeatures.VALID_GAME_TYPES = (Set<GameType>)Sets.immutableEnumSet((Enum)GameType.SURVIVAL, (Enum[])new GameType[] { GameType.CREATIVE, GameType.ADVENTURE, GameType.SPECTATOR });
    }
}
