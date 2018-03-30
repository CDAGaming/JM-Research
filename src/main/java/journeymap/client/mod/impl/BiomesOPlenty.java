package journeymap.client.mod.impl;

import journeymap.client.mod.*;
import journeymap.client.model.*;
import java.util.*;

public class BiomesOPlenty implements IModBlockHandler
{
    private List<String> plants;
    private List<String> crops;
    
    public BiomesOPlenty() {
        this.plants = Arrays.asList("flower", "mushroom", "sapling", "plant", "ivy", "waterlily", "moss");
        this.crops = Collections.singletonList("turnip");
    }
    
    @Override
    public void initialize(final BlockMD blockMD) {
        final String name = blockMD.getBlockId().toLowerCase();
        for (final String plant : this.plants) {
            if (name.contains(plant)) {
                blockMD.addFlags(BlockFlag.Plant);
                break;
            }
        }
        for (final String crop : this.crops) {
            if (name.contains(crop)) {
                blockMD.addFlags(BlockFlag.Crop);
                break;
            }
        }
    }
}
