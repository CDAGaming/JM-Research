package journeymap.client.mod.impl;

import journeymap.client.model.*;
import journeymap.client.cartography.color.*;
import com.google.common.base.*;
import net.minecraft.util.*;
import net.minecraftforge.fml.client.*;
import java.util.*;
import journeymap.common.*;
import journeymap.common.log.*;
import journeymap.client.mod.*;
import net.minecraft.block.state.*;
import net.minecraft.client.renderer.texture.*;
import javax.annotation.*;

public class Bibliocraft implements IModBlockHandler, IBlockSpritesProxy
{
    List<ModPropertyEnum<String>> colorProperties;
    
    public Bibliocraft() {
        (this.colorProperties = new ArrayList<ModPropertyEnum<String>>(2)).add(new ModPropertyEnum<String>("jds.bibliocraft.blocks.BiblioColorBlock", "COLOR", "getWoolTextureString", String.class));
        this.colorProperties.add(new ModPropertyEnum<String>("jds.bibliocraft.blocks.BiblioWoodBlock", "WOOD_TYPE", "getTextureString", String.class));
    }
    
    @Override
    public void initialize(final BlockMD blockMD) {
        blockMD.setBlockSpritesProxy(this);
    }
    
    @Nullable
    @Override
    public Collection<ColoredSprite> getSprites(final BlockMD blockMD) {
        final IBlockState blockState = blockMD.getBlockState();
        final String textureString = ModPropertyEnum.getFirstValue(this.colorProperties, blockState, new Object[0]);
        if (!Strings.isNullOrEmpty(textureString)) {
            try {
                final ResourceLocation loc = new ResourceLocation(textureString);
                final TextureAtlasSprite tas = FMLClientHandler.instance().getClient().func_147117_R().func_110572_b(loc.toString());
                return Collections.singletonList(new ColoredSprite(tas, null));
            }
            catch (Exception e) {
                Journeymap.getLogger().error(String.format("Error getting sprite from %s: %s", textureString, LogFormatter.toPartialString(e)));
            }
        }
        return ModBlockDelegate.INSTANCE.getDefaultBlockSpritesProxy().getSprites(blockMD);
    }
}
