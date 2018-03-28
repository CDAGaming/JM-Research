package journeymap.client.mod.vanilla;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import journeymap.client.mod.IModBlockHandler;
import journeymap.client.model.BlockFlag;
import journeymap.client.model.BlockMD;
import journeymap.client.properties.CoreProperties;
import journeymap.common.Journeymap;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public final class VanillaBlockHandler implements IModBlockHandler {
    ListMultimap<Material, BlockFlag> materialFlags;
    ListMultimap<Class<?>, BlockFlag> blockClassFlags;
    ListMultimap<Block, BlockFlag> blockFlags;
    HashMap<Material, Float> materialAlphas;
    HashMap<Block, Float> blockAlphas;
    HashMap<Class<?>, Float> blockClassAlphas;
    private boolean mapPlants;
    private boolean mapPlantShadows;
    private boolean mapCrops;

    public VanillaBlockHandler() {
        this.materialFlags = MultimapBuilder.ListMultimapBuilder.linkedHashKeys().arrayListValues().build();
        this.blockClassFlags = MultimapBuilder.ListMultimapBuilder.linkedHashKeys().arrayListValues().build();
        this.blockFlags = MultimapBuilder.ListMultimapBuilder.linkedHashKeys().arrayListValues().build();
        this.materialAlphas = new HashMap<Material, Float>();
        this.blockAlphas = new HashMap<Block, Float>();
        this.blockClassAlphas = new HashMap<Class<?>, Float>();
        this.preInitialize();
    }

    private void preInitialize() {
        final CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
        this.mapPlants = coreProperties.mapPlants.get();
        this.mapCrops = coreProperties.mapCrops.get();
        this.mapPlantShadows = coreProperties.mapPlantShadows.get();
        this.setFlags(Material.BARRIER, BlockFlag.Ignore);
        this.setFlags(Material.AIR, BlockFlag.Ignore);
        this.setFlags(Material.GLASS, Float.valueOf(0.4f), BlockFlag.Transparency);
        this.setFlags(Material.GRASS, BlockFlag.Grass);
        if (coreProperties.caveIgnoreGlass.get()) {
            this.setFlags(Material.GLASS, BlockFlag.OpenToSky);
        }
        this.setFlags(Material.LAVA, Float.valueOf(1.0f), BlockFlag.NoShadow);
        this.setFlags(Material.WATER, Float.valueOf(0.25f), BlockFlag.Water, BlockFlag.NoShadow);
        this.materialAlphas.put(Material.ICE, 0.8f);
        this.materialAlphas.put(Material.PACKED_ICE, 0.8f);
        this.setFlags(Blocks.IRON_BARS, Float.valueOf(0.4f), BlockFlag.Transparency);
        this.setFlags((Block) Blocks.FIRE, BlockFlag.NoShadow);
        this.setFlags(Blocks.LADDER, BlockFlag.OpenToSky);
        this.setFlags(Blocks.SNOW_LAYER, BlockFlag.NoTopo, BlockFlag.NoShadow);
        this.setFlags(Blocks.TRIPWIRE, BlockFlag.Ignore);
        this.setFlags((Block) Blocks.TRIPWIRE_HOOK, BlockFlag.Ignore);
        this.setFlags(Blocks.WEB, BlockFlag.OpenToSky, BlockFlag.NoShadow);
        this.setFlags(BlockBush.class, BlockFlag.Plant);
        this.setFlags(BlockFence.class, Float.valueOf(0.4f), BlockFlag.Transparency);
        this.setFlags(BlockFenceGate.class, Float.valueOf(0.4f), BlockFlag.Transparency);
        this.setFlags(BlockGrass.class, BlockFlag.Grass);
        this.setFlags(BlockLeaves.class, BlockFlag.OpenToSky, BlockFlag.Foliage, BlockFlag.NoTopo);
        this.setFlags(BlockLog.class, BlockFlag.OpenToSky, BlockFlag.NoTopo);
        this.setFlags(BlockRailBase.class, BlockFlag.NoShadow, BlockFlag.NoTopo);
        this.setFlags(BlockRedstoneWire.class, BlockFlag.Ignore);
        this.setFlags(BlockTorch.class, BlockFlag.Ignore);
        this.setFlags(BlockVine.class, Float.valueOf(0.2f), BlockFlag.OpenToSky, BlockFlag.Foliage, BlockFlag.NoShadow);
        this.setFlags(IPlantable.class, BlockFlag.Plant, BlockFlag.NoTopo);
    }

    @Override
    public void initialize(final BlockMD blockMD) {
        final Block block = blockMD.getBlockState().getBlock();
        final Material material = blockMD.getBlockState().getMaterial();
        final IBlockState blockState = blockMD.getBlockState();
        if (blockState.getRenderType() == EnumBlockRenderType.INVISIBLE) {
            blockMD.addFlags(BlockFlag.Ignore);
            return;
        }
        blockMD.addFlags(this.materialFlags.get(material));
        Float alpha = this.materialAlphas.get(material);
        if (alpha != null) {
            blockMD.setAlpha(alpha);
        }
        if (this.blockFlags.containsKey((Object) block)) {
            blockMD.addFlags(this.blockFlags.get(block));
        }
        alpha = this.blockAlphas.get(block);
        if (alpha != null) {
            blockMD.setAlpha(alpha);
        }
        for (final Class<?> parentClass : this.blockClassFlags.keys()) {
            if (parentClass.isAssignableFrom(block.getClass())) {
                blockMD.addFlags(this.blockClassFlags.get(parentClass));
                alpha = this.blockClassAlphas.get(parentClass);
                if (alpha != null) {
                    blockMD.setAlpha(alpha);
                    break;
                }
                break;
            }
        }
        if (block instanceof IFluidBlock) {
            blockMD.addFlags(BlockFlag.Fluid, BlockFlag.NoShadow);
            blockMD.setAlpha(0.7f);
        }
        if (material == Material.GLASS && (block instanceof BlockGlowstone || block instanceof BlockSeaLantern || block instanceof BlockBeacon)) {
            blockMD.removeFlags(BlockFlag.OpenToSky, BlockFlag.Transparency);
            blockMD.setAlpha(1.0f);
        }
        if (block instanceof BlockBush && blockMD.getBlockState().getProperties().get((Object) BlockDoublePlant.HALF) == BlockDoublePlant.EnumBlockHalf.UPPER) {
            blockMD.addFlags(BlockFlag.Ignore);
        }
        if (block instanceof BlockCrops) {
            blockMD.addFlags(BlockFlag.Crop);
        }
        if (block instanceof BlockFlower || block instanceof BlockFlowerPot) {
            blockMD.setBlockColorProxy(FlowerBlockProxy.INSTANCE);
        }
        if (blockMD.isVanillaBlock()) {
            return;
        }
        final String uid = blockMD.getBlockId();
        if (uid.toLowerCase().contains("torch")) {
            blockMD.addFlags(BlockFlag.Ignore);
        }
    }

    public void postInitialize(final BlockMD blockMD) {
        if (blockMD.hasFlag(BlockFlag.Crop)) {
            blockMD.removeFlags(BlockFlag.Plant);
        }
        if (blockMD.hasAnyFlag(BlockMD.FlagsPlantAndCrop)) {
            if ((!this.mapPlants && blockMD.hasFlag(BlockFlag.Plant)) || (!this.mapCrops && blockMD.hasFlag(BlockFlag.Crop))) {
                blockMD.addFlags(BlockFlag.Ignore);
            } else if (!this.mapPlantShadows) {
                blockMD.addFlags(BlockFlag.NoShadow);
            }
        }
        if (blockMD.isIgnore()) {
            blockMD.removeFlags(BlockMD.FlagsNormal);
        }
    }

    private void setFlags(final Material material, final BlockFlag... flags) {
        this.materialFlags.putAll(material, (Iterable) new ArrayList(Arrays.asList(flags)));
    }

    private void setFlags(final Material material, final Float alpha, final BlockFlag... flags) {
        this.materialAlphas.put(material, alpha);
        this.setFlags(material, flags);
    }

    private void setFlags(final Class parentClass, final BlockFlag... flags) {
        this.blockClassFlags.putAll(parentClass, (Iterable) new ArrayList(Arrays.asList(flags)));
    }

    private void setFlags(final Class parentClass, final Float alpha, final BlockFlag... flags) {
        this.blockClassAlphas.put(parentClass, alpha);
        this.setFlags(parentClass, flags);
    }

    private void setFlags(final Block block, final BlockFlag... flags) {
        this.blockFlags.putAll(block, (Iterable) new ArrayList(Arrays.asList(flags)));
    }

    private void setFlags(final Block block, final Float alpha, final BlockFlag... flags) {
        this.blockAlphas.put(block, alpha);
        this.setFlags(block, flags);
    }
}
