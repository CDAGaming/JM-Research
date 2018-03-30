package journeymap.client.api.display;

import javax.annotation.*;
import com.google.gson.annotations.*;
import java.util.*;
import com.google.common.base.*;

@ParametersAreNonnullByDefault
public abstract class Displayable implements Comparable<Displayable>
{
    @Since(1.1)
    protected final String modId;
    @Since(1.1)
    protected final String id;
    @Since(1.1)
    protected final DisplayType displayType;
    private transient String guid;
    
    protected Displayable() {
        this.modId = null;
        this.id = null;
        this.displayType = null;
    }
    
    protected Displayable(final String modId) {
        this(modId, UUID.randomUUID().toString());
    }
    
    protected Displayable(final String modId, final String displayId) {
        if (Strings.isNullOrEmpty(modId)) {
            throw new IllegalArgumentException("modId may not be blank");
        }
        if (Strings.isNullOrEmpty(displayId)) {
            throw new IllegalArgumentException("displayId may not be blank");
        }
        this.modId = modId;
        this.id = displayId;
        this.displayType = DisplayType.of(this.getClass());
    }
    
    public static int clampRGB(final int rgb) {
        return 0xFF000000 | rgb;
    }
    
    public static float clampOpacity(final float opacity) {
        return Math.max(0.0f, Math.min(opacity, 1.0f));
    }
    
    public abstract int getDisplayOrder();
    
    public final String getModId() {
        return this.modId;
    }
    
    public final String getId() {
        return this.id;
    }
    
    public final DisplayType getDisplayType() {
        return this.displayType;
    }
    
    public final String getGuid() {
        if (this.guid == null) {
            this.guid = Joiner.on(":").join((Object)this.modId, (Object)this.displayType, new Object[] { this.id });
        }
        return this.guid;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Displayable)) {
            return false;
        }
        final Displayable that = (Displayable)o;
        return Objects.equal((Object)this.modId, (Object)that.modId) && Objects.equal((Object)this.displayType, (Object)that.displayType) && Objects.equal((Object)this.id, (Object)that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(new Object[] { this.modId, this.displayType, this.id });
    }
    
    @Override
    public int compareTo(final Displayable o) {
        return Integer.compare(this.getDisplayOrder(), o.getDisplayOrder());
    }
}
