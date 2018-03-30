package journeymap.client.mod.vanilla;

import journeymap.client.mod.*;
import org.apache.logging.log4j.*;
import net.minecraft.client.renderer.color.*;
import net.minecraftforge.fml.client.*;
import journeymap.common.*;
import journeymap.client.properties.*;
import net.minecraftforge.fluids.*;
import journeymap.common.log.*;
import net.minecraft.block.state.*;
import journeymap.client.model.*;
import net.minecraft.util.math.*;
import journeymap.client.world.*;
import net.minecraft.world.*;
import javax.annotation.*;
import java.util.*;
import journeymap.client.cartography.color.*;

public class VanillaBlockColorProxy implements IBlockColorProxy
{
    static Logger logger;
    private final BlockColors blockColors;
    private boolean blendFoliage;
    private boolean blendGrass;
    private boolean blendWater;
    
    public VanillaBlockColorProxy() {
        this.blockColors = FMLClientHandler.instance().getClient().func_184125_al();
        final CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
        this.blendFoliage = coreProperties.mapBlendFoliage.get();
        this.blendGrass = coreProperties.mapBlendGrass.get();
        this.blendWater = coreProperties.mapBlendWater.get();
    }
    
    @Override
    public int deriveBlockColor(final BlockMD blockMD) {
        final IBlockState blockState = blockMD.getBlockState();
        try {
            if (blockState.func_177230_c() instanceof IFluidBlock) {
                return getSpriteColor(blockMD, 12369084);
            }
            Integer color = getSpriteColor(blockMD, null);
            if (color == null) {
                color = setBlockColorToMaterial(blockMD);
            }
            return color;
        }
        catch (Throwable e) {
            VanillaBlockColorProxy.logger.error("Error deriving color for " + blockMD + ": " + LogFormatter.toPartialString(e));
            blockMD.addFlags(BlockFlag.Error);
            return setBlockColorToMaterial(blockMD);
        }
    }
    
    @Override
    public int getBlockColor(final ChunkMD chunkMD, final BlockMD blockMD, final BlockPos blockPos) {
        int result = blockMD.getTextureColor();
        if (blockMD.isFoliage()) {
            result = RGB.adjustBrightness(result, 0.8f);
        }
        else if (blockMD.isFluid()) {
            return RGB.multiply(result, ((IFluidBlock)blockMD.getBlock()).getFluid().getColor());
        }
        return RGB.multiply(result, this.getColorMultiplier(chunkMD, blockMD, blockPos, blockMD.getBlock().func_180664_k().ordinal()));
    }
    
    public int getColorMultiplier(final ChunkMD chunkMD, final BlockMD blockMD, final BlockPos blockPos, final int tintIndex) {
        if (!this.blendGrass && blockMD.isGrass()) {
            return chunkMD.getBiome(blockPos).func_180627_b(blockPos);
        }
        if (!this.blendFoliage && blockMD.isFoliage()) {
            return chunkMD.getBiome(blockPos).func_180625_c(blockPos);
        }
        if (!this.blendWater && blockMD.isWater()) {
            return chunkMD.getBiome(blockPos).getWaterColorMultiplier();
        }
        return this.blockColors.func_186724_a(blockMD.getBlockState(), (IBlockAccess)JmBlockAccess.INSTANCE, blockPos, tintIndex);
    }
    
    public static Integer getSpriteColor(@Nonnull final BlockMD blockMD, @Nullable final Integer defaultColor) {
        final Collection<ColoredSprite> sprites = blockMD.getBlockSpritesProxy().getSprites(blockMD);
        final float[] rgba = ColorManager.INSTANCE.getAverageColor(sprites);
        if (rgba != null) {
            return RGB.toInteger(rgba);
        }
        return defaultColor;
    }
    
    public static int setBlockColorToError(final BlockMD blockMD) {
        blockMD.setAlpha(0.0f);
        blockMD.addFlags(BlockFlag.Ignore, BlockFlag.Error);
        blockMD.setColor(-1);
        return -1;
    }
    
    public static int setBlockColorToMaterial(final BlockMD blockMD) {
        try {
            blockMD.setAlpha(1.0f);
            blockMD.addFlags(BlockFlag.Ignore);
            return blockMD.setColor(blockMD.getBlockState().func_185904_a().func_151565_r().field_76291_p);
        }
        catch (Exception e) {
            VanillaBlockColorProxy.logger.warn(String.format("Failed to use MaterialMapColor, marking as error: %s", blockMD));
            return setBlockColorToError(blockMD);
        }
    }
    
    static {
        VanillaBlockColorProxy.logger = Journeymap.getLogger();
    }
}
