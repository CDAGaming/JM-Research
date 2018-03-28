package journeymap.client.mod.vanilla;

import journeymap.client.mod.IBlockColorProxy;
import journeymap.client.mod.ModBlockDelegate;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;

public enum FlowerBlockProxy implements IBlockColorProxy {
    INSTANCE;

    private final BlockColors blockColors;
    boolean enabled;

    private FlowerBlockProxy() {
        this.enabled = true;
        this.blockColors = FMLClientHandler.instance().getClient().getBlockColors();
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
                final ItemStack stack = ((BlockFlowerPot) blockState.getBlock()).getItem(chunkMD.getWorld(), blockPos, blockState);
                if (stack != null) {
                    final IBlockState contentBlockState = Block.getBlockFromItem(stack.getItem()).getStateFromMeta(stack.getItem().getDamage(stack));
                    return BlockMD.get(contentBlockState).getTextureColor();
                }
            } catch (Exception e) {
                Journeymap.getLogger().error("Error checking FlowerPot: " + e, (Object) LogFormatter.toPartialString(e));
                this.enabled = false;
            }
        }
        return ModBlockDelegate.INSTANCE.getDefaultBlockColorProxy().getBlockColor(chunkMD, blockMD, blockPos);
    }

    private Integer getFlowerColor(final IBlockState blockState) {
        if (blockState.getBlock() instanceof BlockFlower) {
            final IProperty<BlockFlower.EnumFlowerType> typeProperty = (IProperty<BlockFlower.EnumFlowerType>) ((BlockFlower) blockState.getBlock()).getTypeProperty();
            final BlockFlower.EnumFlowerType flowerType = (BlockFlower.EnumFlowerType) blockState.getProperties().get((Object) typeProperty);
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
