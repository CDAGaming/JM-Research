package journeymap.client.ui.waypoint;

import journeymap.client.data.*;
import journeymap.client.waypoint.*;
import journeymap.common.*;
import journeymap.client.ui.component.*;
import journeymap.client.*;
import net.minecraft.client.gui.*;
import java.util.*;

public class DimensionsButton extends Button
{
    static boolean needInit;
    public static WorldData.DimensionProvider currentWorldProvider;
    final List<WorldData.DimensionProvider> dimensionProviders;
    
    public DimensionsButton() {
        super(0, 0, "");
        this.dimensionProviders = WorldData.getDimensionProviders(WaypointStore.INSTANCE.getLoadedDimensions());
        if (DimensionsButton.needInit || DimensionsButton.currentWorldProvider != null) {
            DimensionsButton.currentWorldProvider = new WorldData.WrappedProvider(Journeymap.clientWorld().field_73011_w);
            DimensionsButton.needInit = false;
        }
        this.updateLabel();
        this.fitWidth(JmUI.fontRenderer());
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
        this.field_146126_j = Constants.getString("jm.waypoint.dimension", dimName);
    }
    
    @Override
    public int getFitWidth(final FontRenderer fr) {
        int maxWidth = 0;
        for (final WorldData.DimensionProvider dimensionProvider : this.dimensionProviders) {
            final String name = Constants.getString("jm.waypoint.dimension", WorldData.getSafeDimensionName(dimensionProvider));
            maxWidth = Math.max(maxWidth, JmUI.fontRenderer().func_78256_a(name));
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
