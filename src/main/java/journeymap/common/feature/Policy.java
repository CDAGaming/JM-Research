package journeymap.common.feature;

import javax.annotation.*;
import journeymap.common.api.feature.*;
import net.minecraft.world.*;
import com.google.common.base.*;

@ParametersAreNonnullByDefault
public class Policy
{
    private final String origin;
    private final Event event;
    private final Feature feature;
    private final GameType gameType;
    private final boolean allowed;
    private final long timestamp;
    
    public static Policy update(final Policy original) {
        return new Policy(original.origin, Event.Update, original.gameType, original.feature, original.allowed, original.timestamp);
    }
    
    public static Policy update(final String origin, final GameType gameType, final Feature feature, final boolean allowed) {
        return new Policy(origin, Event.Update, gameType, feature, allowed, System.currentTimeMillis());
    }
    
    static Policy initialize(final boolean isOp, final GameType gameType, final Feature feature) {
        return new Policy("journeymap", Event.Initialize, gameType, feature, feature.getDefaultAllowed(isOp, gameType), System.currentTimeMillis());
    }
    
    static Policy reset(final boolean isOp, final GameType gameType, final Feature feature) {
        return new Policy("journeymap", Event.Reset, gameType, feature, feature.getDefaultAllowed(isOp, gameType), System.currentTimeMillis());
    }
    
    private Policy(final String origin, final Event event, final GameType gameType, final Feature feature, final boolean allowed, final long timestamp) {
        if (!feature.getClass().getDeclaringClass().equals(Feature.class)) {
            throw new IllegalArgumentException("Unknown feature class: " + feature.getClass());
        }
        this.origin = origin;
        this.event = event;
        this.gameType = gameType;
        this.feature = feature;
        this.allowed = allowed;
        this.timestamp = timestamp;
    }
    
    public String getOrigin() {
        return this.origin;
    }
    
    public Event getEvent() {
        return this.event;
    }
    
    public boolean isAllowed() {
        return this.allowed;
    }
    
    public Feature getFeature() {
        return this.feature;
    }
    
    public GameType getGameType() {
        return this.gameType;
    }
    
    public long getTimestamp() {
        return this.timestamp;
    }
    
    @Override
    public String toString() {
        return "Policy{origin='" + this.origin + '\'' + ", event=" + this.event + ", feature=" + this.feature + ", timestamp=" + this.timestamp + '}';
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Policy policy = (Policy)o;
        return this.allowed == policy.allowed && Objects.equal((Object)this.origin, (Object)policy.origin) && this.event == policy.event && Objects.equal((Object)this.feature, (Object)policy.feature);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(new Object[] { this.feature, this.allowed });
    }
    
    public enum Event
    {
        Initialize, 
        Reset, 
        Update;
    }
}
