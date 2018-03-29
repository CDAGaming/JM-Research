package journeymap.client.mod.vanilla;

import journeymap.client.cartography.color.ColoredSprite;
import journeymap.client.mod.IBlockSpritesProxy;
import journeymap.client.model.BlockMD;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

public class VanillaBlockSpriteProxy implements IBlockSpritesProxy {
    private static Logger logger;

    static {
        VanillaBlockSpriteProxy.logger = Journeymap.getLogger();
    }

    BlockModelShapes bms;

    public VanillaBlockSpriteProxy() {
        this.bms = FMLClientHandler.instance().getClient().getBlockRendererDispatcher().getBlockModelShapes();
    }

    @Nullable
    @Override
    public Collection<ColoredSprite> getSprites(final BlockMD blockMD) {
        IBlockState blockState = blockMD.getBlockState();
        final Block block = blockState.getBlock();
        if (block instanceof IFluidBlock) {
            final ResourceLocation loc = ((IFluidBlock) block).getFluid().getStill();
            final TextureAtlasSprite tas = FMLClientHandler.instance().getClient().getTextureMapBlocks().getAtlasSprite(loc.toString());
            return Collections.singletonList(new ColoredSprite(tas, null));
        }
        if (blockState.getPropertyKeys().contains(BlockDoublePlant.HALF)) {
            blockState = blockState.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER);
        }
        final HashMap<String, ColoredSprite> map = new HashMap<>();
        try {
            final IBakedModel model = this.bms.getModelForState(blockState);
            Label_0220:
            for (final IBlockState state : new IBlockState[]{blockState, null}) {
                for (final EnumFacing facing : new EnumFacing[]{EnumFacing.UP, null}) {
                    if (this.getSprites(blockMD, model, state, facing, map)) {
                        break Label_0220;
                    }
                }
            }
            if (map.isEmpty()) {
                final TextureAtlasSprite defaultSprite = this.bms.getTexture(blockState);
                if (defaultSprite != null) {
                    map.put(defaultSprite.getIconName(), new ColoredSprite(defaultSprite, null));
                    if (!blockMD.isVanillaBlock() && VanillaBlockSpriteProxy.logger.isDebugEnabled()) {
                        VanillaBlockSpriteProxy.logger.debug(String.format("Resorted to using BlockModelStates.getTexture() to use %s as color for %s", defaultSprite.getIconName(), blockState));
                    }
                } else {
                    VanillaBlockSpriteProxy.logger.warn(String.format("Unable to get any texture to use as color for %s", blockState));
                }
            }
        } catch (Exception e) {
            VanillaBlockSpriteProxy.logger.error("Unexpected error during getSprites(): " + LogFormatter.toPartialString(e));
        }
        return map.values();
    }

    private boolean getSprites(final BlockMD blockMD, final IBakedModel model, @Nullable final IBlockState blockState, @Nullable final EnumFacing facing, final HashMap<String, ColoredSprite> map) {
        final BlockRenderLayer originalLayer = MinecraftForgeClient.getRenderLayer();
        boolean success = false;
        try {
            for (final BlockRenderLayer layer : BlockRenderLayer.values()) {
                if (blockMD.getBlock().canRenderInLayer(blockState, layer)) {
                    ForgeHooksClient.setRenderLayer(layer);
                    final List<BakedQuad> quads = model.getQuads(blockState, facing, 0L);
                    if (this.addSprites(map, quads)) {
                        if (!blockMD.isVanillaBlock() && VanillaBlockSpriteProxy.logger.isDebugEnabled()) {
                            VanillaBlockSpriteProxy.logger.debug(String.format("Success during [%s] %s.getQuads(%s, %s, %s)", layer, model.getClass(), blockState, facing, 0));
                        }
                        success = true;
                    }
                }
            }
        } catch (Exception e) {
            if (VanillaBlockSpriteProxy.logger.isDebugEnabled()) {
                VanillaBlockSpriteProxy.logger.error(String.format("Error during [%s] %s.getQuads(%s, %s, %s): %s", MinecraftForgeClient.getRenderLayer(), model.getClass(), blockState, facing, 0, LogFormatter.toPartialString(e)));
            }
        } finally {
            ForgeHooksClient.setRenderLayer(originalLayer);
        }
        return success;
    }

    public boolean addSprites(final HashMap<String, ColoredSprite> sprites, List<BakedQuad> quads) {
        if (quads == null || quads.isEmpty()) {
            return false;
        }
        if (quads.size() > 1) {
            final HashSet<BakedQuad> culled = new HashSet<>(quads.size());
            culled.addAll(quads);
            quads = new ArrayList<>(culled);
        }
        boolean added = false;
        for (final BakedQuad quad : quads) {
            final TextureAtlasSprite sprite = quad.getSprite();
            if (sprite != null) {
                final String iconName = quad.getSprite().getIconName();
                if (sprites.containsKey(iconName)) {
                    continue;
                }
                final ResourceLocation resourceLocation = new ResourceLocation(iconName);
                if (resourceLocation.equals(TextureMap.LOCATION_MISSING_TEXTURE)) {
                    continue;
                }
                sprites.put(iconName, new ColoredSprite(quad));
                added = true;
            }
        }
        return added;
    }
}
