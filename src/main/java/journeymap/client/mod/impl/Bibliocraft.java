package journeymap.client.mod.impl;

import com.google.common.base.Strings;
import journeymap.client.cartography.color.ColoredSprite;
import journeymap.client.mod.IBlockSpritesProxy;
import journeymap.client.mod.IModBlockHandler;
import journeymap.client.mod.ModBlockDelegate;
import journeymap.client.mod.ModPropertyEnum;
import journeymap.client.model.BlockMD;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Bibliocraft implements IModBlockHandler, IBlockSpritesProxy {
    List<ModPropertyEnum<String>> colorProperties;

    public Bibliocraft() {
        (this.colorProperties = new ArrayList<>(2)).add(new ModPropertyEnum<>("jds.bibliocraft.blocks.BiblioColorBlock", "COLOR", "getWoolTextureString", String.class));
        this.colorProperties.add(new ModPropertyEnum<>("jds.bibliocraft.blocks.BiblioWoodBlock", "WOOD_TYPE", "getTextureString", String.class));
    }

    @Override
    public void initialize(final BlockMD blockMD) {
        blockMD.setBlockSpritesProxy(this);
    }

    @Nullable
    @Override
    public Collection<ColoredSprite> getSprites(final BlockMD blockMD) {
        final IBlockState blockState = blockMD.getBlockState();
        final String textureString = ModPropertyEnum.getFirstValue(this.colorProperties, blockState);
        if (!Strings.isNullOrEmpty(textureString)) {
            try {
                final ResourceLocation loc = new ResourceLocation(textureString);
                final TextureAtlasSprite tas = FMLClientHandler.instance().getClient().getTextureMapBlocks().getAtlasSprite(loc.toString());
                return Collections.singletonList(new ColoredSprite(tas, null));
            } catch (Exception e) {
                Journeymap.getLogger().error(String.format("Error getting sprite from %s: %s", textureString, LogFormatter.toPartialString(e)));
            }
        }
        return ModBlockDelegate.INSTANCE.getDefaultBlockSpritesProxy().getSprites(blockMD);
    }
}
