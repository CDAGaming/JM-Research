package journeymap.client.mod.vanilla;

import journeymap.client.mod.*;
import org.apache.logging.log4j.*;
import net.minecraft.client.renderer.*;
import net.minecraftforge.fml.client.*;
import journeymap.client.model.*;
import journeymap.client.cartography.color.*;
import net.minecraftforge.fluids.*;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import journeymap.common.log.*;
import net.minecraft.block.*;
import javax.annotation.*;
import net.minecraft.util.*;
import net.minecraftforge.client.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.*;
import java.util.*;
import journeymap.common.*;

public class VanillaBlockSpriteProxy implements IBlockSpritesProxy
{
    private static Logger logger;
    BlockModelShapes bms;
    
    public VanillaBlockSpriteProxy() {
        this.bms = FMLClientHandler.instance().getClient().func_175602_ab().func_175023_a();
    }
    
    @Nullable
    @Override
    public Collection<ColoredSprite> getSprites(final BlockMD blockMD) {
        IBlockState blockState = blockMD.getBlockState();
        final Block block = blockState.func_177230_c();
        if (block instanceof IFluidBlock) {
            final ResourceLocation loc = ((IFluidBlock)block).getFluid().getStill();
            final TextureAtlasSprite tas = FMLClientHandler.instance().getClient().func_147117_R().func_110572_b(loc.toString());
            return Collections.singletonList(new ColoredSprite(tas, null));
        }
        if (blockState.func_177227_a().contains(BlockDoublePlant.field_176492_b)) {
            blockState = blockState.func_177226_a((IProperty)BlockDoublePlant.field_176492_b, (Comparable)BlockDoublePlant.EnumBlockHalf.UPPER);
        }
        final HashMap<String, ColoredSprite> map = new HashMap<String, ColoredSprite>();
        try {
            final IBakedModel model = this.bms.func_178125_b(blockState);
        Label_0220:
            for (final IBlockState state : new IBlockState[] { blockState, null }) {
                for (final EnumFacing facing : new EnumFacing[] { EnumFacing.UP, null }) {
                    if (this.getSprites(blockMD, model, state, facing, map)) {
                        break Label_0220;
                    }
                }
            }
            if (map.isEmpty()) {
                final TextureAtlasSprite defaultSprite = this.bms.func_178122_a(blockState);
                if (defaultSprite != null) {
                    map.put(defaultSprite.func_94215_i(), new ColoredSprite(defaultSprite, null));
                    if (!blockMD.isVanillaBlock() && VanillaBlockSpriteProxy.logger.isDebugEnabled()) {
                        VanillaBlockSpriteProxy.logger.debug(String.format("Resorted to using BlockModelStates.getTexture() to use %s as color for %s", defaultSprite.func_94215_i(), blockState));
                    }
                }
                else {
                    VanillaBlockSpriteProxy.logger.warn(String.format("Unable to get any texture to use as color for %s", blockState));
                }
            }
        }
        catch (Exception e) {
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
                    final List<BakedQuad> quads = (List<BakedQuad>)model.func_188616_a(blockState, facing, 0L);
                    if (this.addSprites(map, quads)) {
                        if (!blockMD.isVanillaBlock() && VanillaBlockSpriteProxy.logger.isDebugEnabled()) {
                            VanillaBlockSpriteProxy.logger.debug(String.format("Success during [%s] %s.getQuads(%s, %s, %s)", layer, model.getClass(), blockState, facing, 0));
                        }
                        success = true;
                    }
                }
            }
        }
        catch (Exception e) {
            if (VanillaBlockSpriteProxy.logger.isDebugEnabled()) {
                VanillaBlockSpriteProxy.logger.error(String.format("Error during [%s] %s.getQuads(%s, %s, %s): %s", MinecraftForgeClient.getRenderLayer(), model.getClass(), blockState, facing, 0, LogFormatter.toPartialString(e)));
            }
        }
        finally {
            ForgeHooksClient.setRenderLayer(originalLayer);
        }
        return success;
    }
    
    public boolean addSprites(final HashMap<String, ColoredSprite> sprites, List<BakedQuad> quads) {
        if (quads == null || quads.isEmpty()) {
            return false;
        }
        if (quads.size() > 1) {
            final HashSet<BakedQuad> culled = new HashSet<BakedQuad>(quads.size());
            culled.addAll((Collection<?>)quads);
            quads = new ArrayList<BakedQuad>(culled);
        }
        boolean added = false;
        for (final BakedQuad quad : quads) {
            final TextureAtlasSprite sprite = quad.func_187508_a();
            if (sprite != null) {
                final String iconName = quad.func_187508_a().func_94215_i();
                if (sprites.containsKey(iconName)) {
                    continue;
                }
                final ResourceLocation resourceLocation = new ResourceLocation(iconName);
                if (resourceLocation.equals((Object)TextureMap.field_174945_f)) {
                    continue;
                }
                sprites.put(iconName, new ColoredSprite(quad));
                added = true;
            }
        }
        return added;
    }
    
    static {
        VanillaBlockSpriteProxy.logger = Journeymap.getLogger();
    }
}
