package journeymap.common.network.model;

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class InitLogin {
    public static final Gson GSON;

    static {
        GSON = new GsonBuilder().create();
    }

    private boolean teleportEnabled;

    public boolean isTeleportEnabled() {
        return this.teleportEnabled;
    }

    public void setTeleportEnabled(final boolean teleportEnabled) {
        this.teleportEnabled = teleportEnabled;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper((Object) this).add("teleportEnabled", this.teleportEnabled).toString();
    }
}
