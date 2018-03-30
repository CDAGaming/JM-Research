package journeymap.client.mod.vanilla;

import net.minecraft.block.material.*;
import com.google.common.collect.*;
import journeymap.common.*;
import net.minecraft.init.*;
import net.minecraftforge.common.*;
import journeymap.client.properties.*;
import journeymap.client.model.*;
import net.minecraft.util.*;
import net.minecraftforge.fluids.*;
import net.minecraft.block.*;
import journeymap.client.mod.*;
import net.minecraft.block.state.*;
import java.util.*;

public final class VanillaBlockHandler implements IModBlockHandler
{
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
        this.materialFlags = (ListMultimap<Material, BlockFlag>)MultimapBuilder.ListMultimapBuilder.linkedHashKeys().arrayListValues().build();
        this.blockClassFlags = (ListMultimap<Class<?>, BlockFlag>)MultimapBuilder.ListMultimapBuilder.linkedHashKeys().arrayListValues().build();
        this.blockFlags = (ListMultimap<Block, BlockFlag>)MultimapBuilder.ListMultimapBuilder.linkedHashKeys().arrayListValues().build();
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
        this.setFlags(Material.field_175972_I, BlockFlag.Ignore);
        this.setFlags(Material.field_151579_a, BlockFlag.Ignore);
        this.setFlags(Material.field_151592_s, Float.valueOf(0.4f), BlockFlag.Transparency);
        this.setFlags(Material.field_151577_b, BlockFlag.Grass);
        if (coreProperties.caveIgnoreGlass.get()) {
            this.setFlags(Material.field_151592_s, BlockFlag.OpenToSky);
        }
        this.setFlags(Material.field_151587_i, Float.valueOf(1.0f), BlockFlag.NoShadow);
        this.setFlags(Material.field_151586_h, Float.valueOf(0.25f), BlockFlag.Water, BlockFlag.NoShadow);
        this.materialAlphas.put(Material.field_151588_w, 0.8f);
        this.materialAlphas.put(Material.field_151598_x, 0.8f);
        this.setFlags(Blocks.field_150411_aY, Float.valueOf(0.4f), BlockFlag.Transparency);
        this.setFlags((Block)Blocks.field_150480_ab, BlockFlag.NoShadow);
        this.setFlags(Blocks.field_150468_ap, BlockFlag.OpenToSky);
        this.setFlags(Blocks.field_150431_aC, BlockFlag.NoTopo, BlockFlag.NoShadow);
        this.setFlags(Blocks.field_150473_bD, BlockFlag.Ignore);
        this.setFlags((Block)Blocks.field_150479_bC, BlockFlag.Ignore);
        this.setFlags(Blocks.field_150321_G, BlockFlag.OpenToSky, BlockFlag.NoShadow);
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
        final Block block = blockMD.getBlockState().func_177230_c();
        final Material material = blockMD.getBlockState().func_185904_a();
        final IBlockState blockState = blockMD.getBlockState();
        if (blockState.func_185901_i() == EnumBlockRenderType.INVISIBLE) {
            blockMD.addFlags(BlockFlag.Ignore);
            return;
        }
        blockMD.addFlags(this.materialFlags.get((Object)material));
        Float alpha = this.materialAlphas.get(material);
        if (alpha != null) {
            blockMD.setAlpha(alpha);
        }
        if (this.blockFlags.containsKey((Object)block)) {
            blockMD.addFlags(this.blockFlags.get((Object)block));
        }
        alpha = this.blockAlphas.get(block);
        if (alpha != null) {
            blockMD.setAlpha(alpha);
        }
        for (final Class<?> parentClass : this.blockClassFlags.keys()) {
            if (parentClass.isAssignableFrom(block.getClass())) {
                blockMD.addFlags(this.blockClassFlags.get((Object)parentClass));
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
        if (material == Material.field_151592_s && (block instanceof BlockGlowstone || block instanceof BlockSeaLantern || block instanceof BlockBeacon)) {
            blockMD.removeFlags(BlockFlag.OpenToSky, BlockFlag.Transparency);
            blockMD.setAlpha(1.0f);
        }
        if (block instanceof BlockBush && blockMD.getBlockState().func_177228_b().get((Object)BlockDoublePlant.field_176492_b) == BlockDoublePlant.EnumBlockHalf.UPPER) {
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
            }
            else if (!this.mapPlantShadows) {
                blockMD.addFlags(BlockFlag.NoShadow);
            }
        }
        if (blockMD.isIgnore()) {
            blockMD.removeFlags(BlockMD.FlagsNormal);
        }
    }
    
    private void setFlags(final Material material, final BlockFlag... flags) {
        this.materialFlags.putAll((Object)material, (Iterable)new ArrayList(Arrays.asList(flags)));
    }
    
    private void setFlags(final Material material, final Float alpha, final BlockFlag... flags) {
        this.materialAlphas.put(material, alpha);
        this.setFlags(material, flags);
    }
    
    private void setFlags(final Class parentClass, final BlockFlag... flags) {
        this.blockClassFlags.putAll((Object)parentClass, (Iterable)new ArrayList(Arrays.asList(flags)));
    }
    
    private void setFlags(final Class parentClass, final Float alpha, final BlockFlag... flags) {
        this.blockClassAlphas.put(parentClass, alpha);
        this.setFlags(parentClass, flags);
    }
    
    private void setFlags(final Block block, final BlockFlag... flags) {
        this.blockFlags.putAll((Object)block, (Iterable)new ArrayList(Arrays.asList(flags)));
    }
    
    private void setFlags(final Block block, final Float alpha, final BlockFlag... flags) {
        this.blockAlphas.put(block, alpha);
        this.setFlags(block, flags);
    }
}
