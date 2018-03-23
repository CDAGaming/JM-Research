package journeymap.client.ui.waypoint;

import journeymap.client.ui.component.*;
import journeymap.client.data.*;
import journeymap.client.waypoint.*;
import net.minecraftforge.fml.client.*;
import journeymap.client.*;
import net.minecraft.client.gui.*;
import java.util.*;

class DimensionsButton extends Button
{
    static boolean needInit;
    static WorldData.DimensionProvider currentWorldProvider;
    final List<WorldData.DimensionProvider> dimensionProviders;
    
    public DimensionsButton() {
        super(0, 0, "");
        this.dimensionProviders = WorldData.getDimensionProviders(WaypointStore.INSTANCE.getLoadedDimensions());
        if (DimensionsButton.needInit || DimensionsButton.currentWorldProvider != null) {
            DimensionsButton.currentWorldProvider = new WorldData.WrappedProvider(FMLClientHandler.instance().getClient().player.world.provider);
            DimensionsButton.needInit = false;
        }
        this.updateLabel();
        this.fitWidth(FMLClientHandler.instance().getClient().fontRenderer);
    }
    
    @Override
    protected void updateLabel() {
        String dimName;
        if (DimensionsButton.currentWorldProvider != null) {
            dimName = WorldData.getSafeDimensionName(DimensionsButton.currentWorldProvider);
        }
        else {
            dimName = Constants.getString("jm.waypoint.dimension_all");
        }
        this.displayString = Constants.getString("jm.waypoint.dimension", dimName);
    }
    
    @Override
    public int getFitWidth(final FontRenderer fr) {
        int maxWidth = 0;
        for (final WorldData.DimensionProvider dimensionProvider : this.dimensionProviders) {
            final String name = Constants.getString("jm.waypoint.dimension", WorldData.getSafeDimensionName(dimensionProvider));
            maxWidth = Math.max(maxWidth, FMLClientHandler.instance().getClient().fontRenderer.getStringWidth(name));
        }
        return maxWidth + 12;
    }
    
    public void nextValue() {
        int index;
        if (DimensionsButton.currentWorldProvider == null) {
            index = 0;
        }
        else {
            index = -1;
            final int currentDimension = DimensionsButton.currentWorldProvider.getDimension();
            for (final WorldData.DimensionProvider dimensionProvider : this.dimensionProviders) {
                if (currentDimension == dimensionProvider.getDimension()) {
                    index = this.dimensionProviders.indexOf(dimensionProvider) + 1;
                    break;
                }
            }
        }
        if (index >= this.dimensionProviders.size() || index < 0) {
            DimensionsButton.currentWorldProvider = null;
        }
        else {
            DimensionsButton.currentWorldProvider = this.dimensionProviders.get(index);
        }
        this.updateLabel();
    }
    
    static {
        DimensionsButton.needInit = true;
    }
}
