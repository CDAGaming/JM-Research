package journeymap.client.properties;

import journeymap.common.properties.Category;
import journeymap.common.properties.config.BooleanField;
import net.minecraftforge.fml.client.FMLClientHandler;

public class FullMapProperties extends InGameMapProperties {
    public final BooleanField showKeys;

    public FullMapProperties() {
        this.showKeys = new BooleanField(Category.Inherit, "jm.common.show_keys", true);
    }

    public void postLoad(final boolean isNew) {
        super.postLoad(isNew);
        if (isNew && FMLClientHandler.instance().getClient() != null && FMLClientHandler.instance().getClient().fontRenderer.getUnicodeFlag()) {
            super.fontScale.set(2);
        }
    }

    @Override
    public String getName() {
        return "fullmap";
    }
}
