package journeymap.client.properties;

import journeymap.common.properties.config.*;
import journeymap.common.properties.*;
import net.minecraftforge.fml.client.*;

public class FullMapProperties extends InGameMapProperties
{
    public final BooleanField showKeys;
    
    public FullMapProperties() {
        this.showKeys = new BooleanField(Category.Inherit, "jm.common.show_keys", true);
    }
    
    public void postLoad(final boolean isNew) {
        super.postLoad(isNew);
        if (isNew && FMLClientHandler.instance().getClient() != null && FMLClientHandler.instance().getClient().field_71466_p.func_82883_a()) {
            super.fontScale.set(2);
        }
    }
    
    @Override
    public String getName() {
        return "fullmap";
    }
}
