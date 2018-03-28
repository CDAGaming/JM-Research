package journeymap.client.mod.vanilla;

import journeymap.client.cartography.color.ColorManager;
import journeymap.client.cartography.color.ColoredSprite;
import journeymap.client.cartography.color.RGB;
import journeymap.client.mod.IBlockColorProxy;
import journeymap.client.model.BlockFlag;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.properties.CoreProperties;
import journeymap.client.world.JmBlockAccess;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class VanillaBlockColorProxy implements IBlockColorProxy {
    static Logger logger;

    static {
        VanillaBlockColorProxy.logger = Journeymap.getLogger();
    }

    private final BlockColors blockColors;
    private boolean blendFoliage;
    private boolean blendGrass;
    private boolean blendWater;

    public VanillaBlockColorProxy() {
        this.blockColors = FMLClientHandler.instance().getClient().getBlockColors();
        final CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
        this.blendFoliage = coreProperties.mapBlendFoliage.get();
        this.blendGrass = coreProperties.mapBlendGrass.get();
        this.blendWater = coreProperties.mapBlendWater.get();
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
            return blockMD.setColor(blockMD.getBlockState().getMaterial().getMaterialMapColor().colorValue);
        } catch (Exception e) {
            VanillaBlockColorProxy.logger.warn(String.format("Failed to use MaterialMapColor, marking as error: %s", blockMD));
            return setBlockColorToError(blockMD);
        }
    }

    @Override
    public int deriveBlockColor(final BlockMD blockMD) {
        final IBlockState blockState = blockMD.getBlockState();
        try {
            if (blockState.getBlock() instanceof IFluidBlock) {
                return getSpriteColor(blockMD, 12369084);
            }
            Integer color = getSpriteColor(blockMD, null);
            if (color == null) {
                color = setBlockColorToMaterial(blockMD);
            }
            return color;
        } catch (Throwable e) {
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
        } else if (blockMD.isFluid()) {
            return RGB.multiply(result, ((IFluidBlock) blockMD.getBlock()).getFluid().getColor());
        }
        return RGB.multiply(result, this.getColorMultiplier(chunkMD, blockMD, blockPos, blockMD.getBlock().getBlockLayer().ordinal()));
    }

    public int getColorMultiplier(final ChunkMD chunkMD, final BlockMD blockMD, final BlockPos blockPos, final int tintIndex) {
        if (!this.blendGrass && blockMD.isGrass()) {
            return chunkMD.getBiome(blockPos).getGrassColorAtPos(blockPos);
        }
        if (!this.blendFoliage && blockMD.isFoliage()) {
            return chunkMD.getBiome(blockPos).getFoliageColorAtPos(blockPos);
        }
        if (!this.blendWater && blockMD.isWater()) {
            return chunkMD.getBiome(blockPos).getWaterColorMultiplier();
        }
        return this.blockColors.colorMultiplier(blockMD.getBlockState(), (IBlockAccess) JmBlockAccess.INSTANCE, blockPos, tintIndex);
    }
}
