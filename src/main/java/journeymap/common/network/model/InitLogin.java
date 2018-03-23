package journeymap.common.network.model;

import com.google.common.base.*;
import com.google.gson.*;

public class InitLogin
{
    public static final Gson GSON;
    private boolean teleportEnabled;
    
    public boolean isTeleportEnabled() {
        return this.teleportEnabled;
    }
    
    public void setTeleportEnabled(final boolean teleportEnabled) {
        this.teleportEnabled = teleportEnabled;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("teleportEnabled", this.teleportEnabled).toString();
    }
    
    static {
        GSON = new GsonBuilder().create();
    }
}
