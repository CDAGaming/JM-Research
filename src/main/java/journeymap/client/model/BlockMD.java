package journeymap.client.model;

import org.apache.logging.log4j.*;
import net.minecraft.block.state.*;
import javax.annotation.*;
import journeymap.client.mod.*;
import java.util.function.*;
import net.minecraft.init.*;
import net.minecraft.block.*;
import journeymap.client.data.*;
import net.minecraftforge.registries.*;
import java.util.stream.*;
import net.minecraft.util.math.*;
import journeymap.client.world.*;
import journeymap.common.log.*;
import net.minecraft.util.*;
import net.minecraft.item.*;
import net.minecraft.util.text.translation.*;
import journeymap.common.*;
import com.google.common.base.*;
import com.google.common.collect.*;
import java.util.*;
import com.google.common.cache.*;
import net.minecraftforge.common.property.*;

public class BlockMD implements Comparable<BlockMD>
{
    public static final EnumSet<BlockFlag> FlagsPlantAndCrop;
    public static final EnumSet<BlockFlag> FlagsNormal;
    public static final BlockMD AIRBLOCK;
    public static final BlockMD VOIDBLOCK;
    private static Logger LOGGER;
    private final IBlockState blockState;
    private final String blockId;
    private final String blockStateId;
    private final String name;
    private EnumSet<BlockFlag> flags;
    private Integer color;
    private float alpha;
    private IBlockSpritesProxy blockSpritesProxy;
    private IBlockColorProxy blockColorProxy;
    private boolean noShadow;
    private boolean isIgnore;
    private boolean isWater;
    private boolean isLava;
    private boolean isFluid;
    private boolean isFire;
    private boolean isIce;
    private boolean isFoliage;
    private boolean isGrass;
    private boolean isPlantOrCrop;
    private boolean isError;
    
    private BlockMD(@Nonnull final IBlockState blockState) {
        this(blockState, getBlockId(blockState), getBlockStateId(blockState), getBlockName(blockState));
    }
    
    private BlockMD(@Nonnull final IBlockState blockState, final String blockId, final String blockStateId, final String name) {
        this(blockState, blockId, blockStateId, name, 1.0f, EnumSet.noneOf(BlockFlag.class), true);
    }
    
    private BlockMD(@Nonnull final IBlockState blockState, final String blockId, final String blockStateId, final String name, final Float alpha, final EnumSet<BlockFlag> flags, final boolean initDelegates) {
        this.blockState = blockState;
        this.blockId = blockId;
        this.blockStateId = blockStateId;
        this.name = name;
        this.alpha = alpha;
        this.flags = flags;
        if (initDelegates) {
            ModBlockDelegate.INSTANCE.initialize(this);
        }
        this.updateProperties();
    }
    
    public Set<BlockMD> getValidStateMDs() {
        return this.getBlock().func_176194_O().func_177619_a().stream().map(BlockMD::get).collect((Collector<? super Object, ?, Set<BlockMD>>)Collectors.toSet());
    }
    
    private void updateProperties() {
        this.isIgnore = (this.blockState == null || this.hasFlag(BlockFlag.Ignore) || this.blockState.func_177230_c() instanceof BlockAir || this.blockState.func_185901_i() == EnumBlockRenderType.INVISIBLE);
        if (this.isIgnore) {
            this.color = -1;
            this.setAlpha(0.0f);
            this.flags.add(BlockFlag.Ignore);
            this.flags.add(BlockFlag.OpenToSky);
            this.flags.add(BlockFlag.NoShadow);
        }
        if (this.blockState != null) {
            final Block block = this.blockState.func_177230_c();
            this.isLava = (block == Blocks.field_150353_l || block == Blocks.field_150356_k);
            this.isIce = (block == Blocks.field_150432_aD);
            this.isFire = (block == Blocks.field_150480_ab);
        }
        this.isFluid = this.hasFlag(BlockFlag.Fluid);
        this.isWater = this.hasFlag(BlockFlag.Water);
        this.noShadow = this.hasFlag(BlockFlag.NoShadow);
        this.isFoliage = this.hasFlag(BlockFlag.Foliage);
        this.isGrass = this.hasFlag(BlockFlag.Grass);
        this.isPlantOrCrop = this.hasAnyFlag(BlockMD.FlagsPlantAndCrop);
        this.isError = this.hasFlag(BlockFlag.Error);
    }
    
    public Block getBlock() {
        return this.blockState.func_177230_c();
    }
    
    public static void reset() {
        DataCache.INSTANCE.resetBlockMetadata();
    }
    
    public static Set<BlockMD> getAll() {
        return StreamSupport.stream((Spliterator<Object>)GameData.getBlockStateIDMap().spliterator(), false).map((Function<? super Object, ?>)BlockMD::get).collect((Collector<? super Object, ?, Set<BlockMD>>)Collectors.toSet());
    }
    
    public static Set<BlockMD> getAllValid() {
        return getAll().stream().filter(blockMD -> !blockMD.isIgnore() && !blockMD.hasFlag(BlockFlag.Error)).collect((Collector<? super Object, ?, Set<BlockMD>>)Collectors.toSet());
    }
    
    public static Set<BlockMD> getAllMinecraft() {
        return StreamSupport.stream((Spliterator<Object>)GameData.getBlockStateIDMap().spliterator(), false).filter(blockState1 -> blockState1.func_177230_c().getRegistryName().func_110624_b().equals("minecraft")).map((Function<? super Object, ?>)BlockMD::get).collect((Collector<? super Object, ?, Set<BlockMD>>)Collectors.toSet());
    }
    
    public static BlockMD getBlockMDFromChunkLocal(final ChunkMD chunkMd, final int localX, final int y, final int localZ) {
        return getBlockMD(chunkMd, chunkMd.getBlockPos(localX, y, localZ));
    }
    
    public static BlockMD getBlockMD(final ChunkMD chunkMd, final BlockPos blockPos) {
        try {
            if (blockPos.func_177956_o() >= 0) {
                IBlockState blockState;
                if (chunkMd != null && chunkMd.hasChunk()) {
                    blockState = chunkMd.getChunk().func_177435_g(blockPos);
                }
                else {
                    blockState = JmBlockAccess.INSTANCE.func_180495_p(blockPos);
                }
                return get(blockState);
            }
            return BlockMD.VOIDBLOCK;
        }
        catch (Exception e) {
            BlockMD.LOGGER.error(String.format("Can't get blockId/meta for chunk %s,%s at %s : %s", chunkMd.getChunk().field_76635_g, chunkMd.getChunk().field_76647_h, blockPos, LogFormatter.toString(e)));
            return BlockMD.AIRBLOCK;
        }
    }
    
    public static BlockMD get(final IBlockState blockState) {
        return DataCache.INSTANCE.getBlockMD(blockState);
    }
    
    public static String getBlockId(final BlockMD blockMD) {
        return getBlockId(blockMD.getBlockState());
    }
    
    public static String getBlockId(final IBlockState blockState) {
        return ((ResourceLocation)Block.field_149771_c.func_177774_c((Object)blockState.func_177230_c())).toString();
    }
    
    public static String getBlockStateId(final BlockMD blockMD) {
        return getBlockStateId(blockMD.getBlockState());
    }
    
    public static String getBlockStateId(final IBlockState blockState) {
        final Collection properties = (Collection)blockState.func_177228_b().values();
        if (properties.isEmpty()) {
            return Integer.toString(blockState.func_177230_c().func_176201_c(blockState));
        }
        return Joiner.on(",").join((Iterable)properties);
    }
    
    private static String getBlockName(final IBlockState blockState) {
        String displayName = null;
        try {
            final Block block = blockState.func_177230_c();
            final Item item = Item.func_150898_a(block);
            if (item != null) {
                final ItemStack idPicked = new ItemStack(item, 1, block.func_176201_c(blockState));
                displayName = I18n.func_74838_a(item.func_77667_c(idPicked) + ".name");
            }
            if (Strings.isNullOrEmpty(displayName)) {
                displayName = block.func_149732_F();
            }
        }
        catch (Exception e) {
            BlockMD.LOGGER.debug(String.format("Couldn't get display name for %s: %s ", blockState, e));
        }
        if (Strings.isNullOrEmpty(displayName) || displayName.contains("tile")) {
            displayName = blockState.func_177230_c().getClass().getSimpleName().replaceAll("Block", "");
        }
        return displayName;
    }
    
    public static void setAllFlags(final Block block, final BlockFlag... flags) {
        final BlockMD defaultBlockMD = get(block.func_176223_P());
        for (final BlockMD blockMD : defaultBlockMD.getValidStateMDs()) {
            blockMD.addFlags(flags);
        }
        BlockMD.LOGGER.debug(block.func_149739_a() + " flags set: " + flags);
    }
    
    public boolean hasFlag(final BlockFlag checkFlag) {
        return this.flags.contains(checkFlag);
    }
    
    public boolean hasAnyFlag(final EnumSet<BlockFlag> checkFlags) {
        for (final BlockFlag flag : checkFlags) {
            if (this.flags.contains(flag)) {
                return true;
            }
        }
        return false;
    }
    
    public void addFlags(final BlockFlag... addFlags) {
        Collections.addAll(this.flags, addFlags);
        this.updateProperties();
    }
    
    public void removeFlags(final BlockFlag... removeFlags) {
        for (final BlockFlag flag : removeFlags) {
            this.flags.remove(flag);
        }
        this.updateProperties();
    }
    
    public void removeFlags(final Collection<BlockFlag> removeFlags) {
        this.flags.removeAll(removeFlags);
        this.updateProperties();
    }
    
    public void addFlags(final Collection<BlockFlag> addFlags) {
        this.flags.addAll((Collection<?>)addFlags);
        this.updateProperties();
    }
    
    public int getBlockColor(final ChunkMD chunkMD, final BlockPos blockPos) {
        return this.blockColorProxy.getBlockColor(chunkMD, this, blockPos);
    }
    
    public int getTextureColor() {
        if (this.color == null && !this.isError && this.blockColorProxy != null) {
            this.color = this.blockColorProxy.deriveBlockColor(this);
        }
        if (this.color == null) {
            this.color = 0;
        }
        return this.color;
    }
    
    public void clearColor() {
        this.color = null;
    }
    
    public int setColor(final int baseColor) {
        this.color = baseColor;
        return baseColor;
    }
    
    public boolean hasColor() {
        return this.color != null;
    }
    
    public void setBlockSpritesProxy(final IBlockSpritesProxy blockSpritesProxy) {
        this.blockSpritesProxy = blockSpritesProxy;
    }
    
    public IBlockSpritesProxy getBlockSpritesProxy() {
        return this.blockSpritesProxy;
    }
    
    public void setBlockColorProxy(final IBlockColorProxy blockColorProxy) {
        this.blockColorProxy = blockColorProxy;
    }
    
    public IBlockColorProxy getBlockColorProxy() {
        return this.blockColorProxy;
    }
    
    public float getAlpha() {
        return this.alpha;
    }
    
    public void setAlpha(final float alpha) {
        this.alpha = alpha;
        if (alpha < 1.0f) {
            this.flags.add(BlockFlag.Transparency);
        }
        else {
            this.flags.remove(BlockFlag.Transparency);
        }
    }
    
    public boolean hasNoShadow() {
        return this.noShadow || (this.isPlantOrCrop && !Journeymap.getClient().getCoreProperties().mapPlantShadows.get());
    }
    
    public IBlockState getBlockState() {
        return this.blockState;
    }
    
    public boolean hasTransparency() {
        return this.alpha < 1.0f;
    }
    
    public boolean isIgnore() {
        return this.isIgnore;
    }
    
    public boolean isIce() {
        return this.isIce;
    }
    
    public boolean isWater() {
        return this.isWater;
    }
    
    public boolean isFluid() {
        return this.isFluid;
    }
    
    public boolean isLava() {
        return this.isLava;
    }
    
    public boolean isFire() {
        return this.isFire;
    }
    
    public boolean isFoliage() {
        return this.isFoliage;
    }
    
    public boolean isGrass() {
        return this.isGrass;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getBlockId() {
        return this.blockId;
    }
    
    public String getBlockStateId() {
        return this.blockStateId;
    }
    
    public String getBlockDomain() {
        return this.getBlock().getRegistryName().func_110624_b();
    }
    
    public EnumSet<BlockFlag> getFlags() {
        return this.flags;
    }
    
    public boolean isVanillaBlock() {
        return this.getBlockDomain().equals("minecraft");
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BlockMD)) {
            return false;
        }
        final BlockMD blockMD = (BlockMD)o;
        return Objects.equal((Object)this.getBlockId(), (Object)blockMD.getBlockId()) && Objects.equal((Object)this.getBlockStateId(), (Object)blockMD.getBlockStateId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(new Object[] { this.getBlockId(), this.getBlockStateId() });
    }
    
    @Override
    public String toString() {
        return String.format("BlockMD [%s] (%s)", this.blockState, Joiner.on(",").join((Iterable)this.flags));
    }
    
    @Override
    public int compareTo(final BlockMD that) {
        final Ordering ordering = Ordering.natural().nullsLast();
        return ComparisonChain.start().compare((Object)this.blockId, (Object)that.blockId, (Comparator)ordering).compare((Object)this.blockStateId, (Object)that.blockStateId, (Comparator)ordering).result();
    }
    
    static {
        FlagsPlantAndCrop = EnumSet.of(BlockFlag.Plant, BlockFlag.Crop);
        FlagsNormal = EnumSet.complementOf((EnumSet<BlockFlag>)EnumSet.of((E)BlockFlag.Error, (E)BlockFlag.Ignore));
        AIRBLOCK = new BlockMD(Blocks.field_150350_a.func_176223_P(), "minecraft:air", "0", "Air", 0.0f, EnumSet.of(BlockFlag.Ignore), false);
        VOIDBLOCK = new BlockMD(Blocks.field_150350_a.func_176223_P(), "journeymap:void", "0", "Void", 0.0f, EnumSet.of(BlockFlag.Ignore), false);
        BlockMD.LOGGER = Journeymap.getLogger();
    }
    
    public static class CacheLoader extends com.google.common.cache.CacheLoader<IBlockState, BlockMD>
    {
        public BlockMD load(@Nonnull IBlockState blockState) throws Exception {
            try {
                if (blockState instanceof IExtendedBlockState) {
                    final IBlockState clean = ((IExtendedBlockState)blockState).getClean();
                    if (clean != null) {
                        blockState = clean;
                    }
                }
                if (blockState == null || blockState.func_185901_i() == EnumBlockRenderType.INVISIBLE) {
                    return BlockMD.AIRBLOCK;
                }
                if (blockState.func_177230_c().getRegistryName() == null) {
                    BlockMD.LOGGER.warn("Unregistered block will be treated like air: " + blockState);
                    return BlockMD.AIRBLOCK;
                }
                return new BlockMD(blockState, null);
            }
            catch (Exception e) {
                BlockMD.LOGGER.error(String.format("Can't get BlockMD for %s : %s", blockState, LogFormatter.toPartialString(e)));
                return BlockMD.AIRBLOCK;
            }
        }
    }
}
