package journeymap.client.ui.minimap;

import journeymap.client.Constants;
import journeymap.client.ui.option.KeyedEnum;

public enum ReticleOrientation implements KeyedEnum {
    Compass("jm.minimap.orientation.compass"),
    PlayerHeading("jm.minimap.orientation.playerheading");

    public final String key;

    private ReticleOrientation(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String toString() {
        return Constants.getString(this.key);
    }
}
