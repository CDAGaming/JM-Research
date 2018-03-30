package journeymap.client.api.display;

import journeymap.client.api.model.*;
import javax.annotation.*;
import com.google.gson.annotations.*;
import java.util.*;
import com.google.common.base.*;

@ParametersAreNonnullByDefault
public class WaypointGroup extends WaypointBase<WaypointGroup>
{
    public static final double VERSION = 1.4;
    @Since(1.4)
    protected final double version = 1.4;
    @Since(1.4)
    protected int order;
    protected transient IWaypointDisplay defaultDisplay;
    
    public WaypointGroup(final String modId, final String name) {
        this(modId, UUID.randomUUID().toString(), name);
    }
    
    public WaypointGroup(final String modId, final String id, final String name) {
        super(modId, id, name);
    }
    
    public WaypointGroup setDefaultDisplay(final IWaypointDisplay defaultDisplay) {
        if (defaultDisplay == this) {
            throw new IllegalArgumentException("WaypointGroup may not use itself as a defaultDisplay");
        }
        this.defaultDisplay = defaultDisplay;
        return this;
    }
    
    @Override
    protected IWaypointDisplay getDelegate() {
        return this.defaultDisplay;
    }
    
    @Override
    protected boolean hasDelegate() {
        return this.defaultDisplay != null;
    }
    
    @Override
    public int getDisplayOrder() {
        return this.order;
    }
    
    public WaypointGroup setDisplayOrder(final int order) {
        this.order = order;
        return this;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WaypointGroup)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final WaypointGroup that = (WaypointGroup)o;
        if (Objects.equal((Object)this.getGuid(), (Object)that.getGuid())) {
            that.getClass();
            if (Double.compare(1.4, 1.4) == 0) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(new Object[] { super.hashCode(), 1.4 });
    }
}
