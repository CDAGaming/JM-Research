package journeymap.client.mod.vanilla;

import net.minecraft.client.renderer.color.*;
import net.minecraftforge.fml.client.*;
import journeymap.client.mod.*;
import journeymap.client.model.*;
import net.minecraft.util.math.*;
import journeymap.common.*;
import net.minecraft.block.*;
import journeymap.common.log.*;
import net.minecraft.block.state.*;
import net.minecraft.item.*;
import net.minecraft.block.properties.*;

public enum FlowerBlockProxy implements IBlockColorProxy
{
    INSTANCE;
    
    boolean enabled;
    private final BlockColors blockColors;
    
    private FlowerBlockProxy() {
        this.enabled = true;
        this.blockColors = FMLClientHandler.instance().getClient().func_184125_al();
    }
    
    @Override
    public int deriveBlockColor(final BlockMD blockMD) {
        if (blockMD.getBlock() instanceof BlockFlower) {
            final Integer color = this.getFlowerColor(blockMD.getBlockState());
            if (color != null) {
                return color;
            }
        }
        return ModBlockDelegate.INSTANCE.getDefaultBlockColorProxy().deriveBlockColor(blockMD);
    }
    
    @Override
    public int getBlockColor(final ChunkMD chunkMD, final BlockMD blockMD, final BlockPos blockPos) {
        if (blockMD.getBlock() instanceof BlockFlower) {
            return blockMD.getTextureColor();
        }
        if (blockMD.getBlock() instanceof BlockFlowerPot && Journeymap.getClient().getCoreProperties().mapPlants.get()) {
            try {
                final IBlockState blockState = blockMD.getBlockState();
                final ItemStack stack = ((BlockFlowerPot)blockState.func_177230_c()).func_185473_a(chunkMD.getWorld(), blockPos, blockState);
                if (stack != null) {
                    final IBlockState contentBlockState = Block.func_149634_a(stack.func_77973_b()).func_176203_a(stack.func_77973_b().getDamage(stack));
                    return BlockMD.get(contentBlockState).getTextureColor();
                }
            }
            catch (Exception e) {
                Journeymap.getLogger().error("Error checking FlowerPot: " + LogFormatter.toPartialString(e));
                this.enabled = false;
            }
        }
        return ModBlockDelegate.INSTANCE.getDefaultBlockColorProxy().getBlockColor(chunkMD, blockMD, blockPos);
    }
    
    private Integer getFlowerColor(final IBlockState blockState) {
        if (blockState.func_177230_c() instanceof BlockFlower) {
            final IProperty<BlockFlower.EnumFlowerType> typeProperty = (IProperty<BlockFlower.EnumFlowerType>)((BlockFlower)blockState.func_177230_c()).func_176494_l();
            final BlockFlower.EnumFlowerType flowerType = (BlockFlower.EnumFlowerType)blockState.func_177228_b().get((Object)typeProperty);
            if (flowerType != null) {
                switch (flowerType) {
                    case POPPY: {
                        return 9962502;
                    }
                    case BLUE_ORCHID: {
                        return 1998518;
                    }
                    case ALLIUM: {
                        return 8735158;
                    }
                    case HOUSTONIA: {
                        return 10330535;
                    }
                    case RED_TULIP: {
                        return 9962502;
                    }
                    case ORANGE_TULIP: {
                        return 10704922;
                    }
                    case WHITE_TULIP: {
                        return 11579568;
                    }
                    case PINK_TULIP: {
                        return 11573936;
                    }
                    case OXEYE_DAISY: {
                        return 11776947;
                    }
                    case DANDELION: {
                        return 11514881;
                    }
                    default: {
                        return 65280;
                    }
                }
            }
        }
        return null;
    }
}
