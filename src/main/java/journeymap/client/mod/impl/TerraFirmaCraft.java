package journeymap.client.mod.impl;

import journeymap.client.mod.*;
import journeymap.client.model.*;

public class TerraFirmaCraft implements IModBlockHandler
{
    private static final int WATER_COLOR = 727360;
    
    @Override
    public void initialize(final BlockMD blockMD) {
        final String name = blockMD.getBlockId().toLowerCase();
        if (name.contains("looserock") || name.contains("loose_rock") || name.contains("rubble") || name.contains("vegetation")) {
            blockMD.addFlags(BlockFlag.Ignore, BlockFlag.NoShadow, BlockFlag.NoTopo);
        }
        else if (name.contains("seagrass")) {
            blockMD.addFlags(BlockFlag.Plant);
        }
        else if (name.contains("grass")) {
            blockMD.addFlags(BlockFlag.Grass);
        }
        else if (name.contains("water")) {
            blockMD.setAlpha(0.3f);
            blockMD.addFlags(BlockFlag.Water, BlockFlag.NoShadow);
            blockMD.setColor(727360);
        }
        else if (name.contains("leaves")) {
            blockMD.addFlags(BlockFlag.NoTopo, BlockFlag.Foliage);
        }
    }
}
