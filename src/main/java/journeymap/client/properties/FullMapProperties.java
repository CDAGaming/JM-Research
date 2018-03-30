package journeymap.client.properties;

import journeymap.common.properties.config.*;
import journeymap.common.properties.*;
import net.minecraftforge.fml.client.*;
import journeymap.client.ui.component.*;

public class FullMapProperties extends InGameMapProperties
{
    public final BooleanField showKeys;
    
    public FullMapProperties() {
        this.showKeys = new BooleanField(Category.Inherit, "jm.common.show_keys", true);
    }
    
    public void postLoad(final boolean isNew) {
        super.postLoad(isNew);
        if (isNew && FMLClientHandler.instance().getClient() != null && JmUI.fontRenderer().func_82883_a()) {
            super.fontScale.set(2);
        }
    }
    
    @Override
    public String getName() {
        return "fullmap";
    }
}
