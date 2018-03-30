package journeymap.client.cartography.color;

import org.apache.logging.log4j.*;
import javax.annotation.*;
import net.minecraft.client.renderer.block.model.*;
import java.awt.image.*;
import net.minecraft.util.*;
import net.minecraft.client.renderer.texture.*;
import journeymap.common.log.*;
import journeymap.client.render.texture.*;
import journeymap.common.*;

@ParametersAreNonnullByDefault
public class ColoredSprite
{
    private static Logger logger;
    private final Integer color;
    private final TextureAtlasSprite sprite;
    
    public ColoredSprite(final TextureAtlasSprite sprite, @Nullable final Integer color) {
        this.sprite = sprite;
        this.color = null;
    }
    
    public ColoredSprite(final BakedQuad quad) {
        this.sprite = quad.func_187508_a();
        this.color = null;
    }
    
    public String getIconName() {
        return this.sprite.func_94215_i();
    }
    
    @Nullable
    public Integer getColor() {
        return this.color;
    }
    
    public boolean hasColor() {
        return this.color != null;
    }
    
    @Nullable
    public BufferedImage getColoredImage() {
        try {
            final ResourceLocation resourceLocation = new ResourceLocation(this.getIconName());
            if (resourceLocation.equals((Object)TextureMap.field_174945_f)) {
                return null;
            }
            BufferedImage image = this.getFrameTextureData(this.sprite);
            if (image == null || image.getWidth() == 0) {
                image = this.getImageResource(this.sprite);
            }
            if (image == null || image.getWidth() == 0) {
                return null;
            }
            return this.applyColor(image);
        }
        catch (Throwable e1) {
            if (ColoredSprite.logger.isDebugEnabled()) {
                ColoredSprite.logger.error("ColoredSprite: Error getting image for " + this.getIconName() + ": " + LogFormatter.toString(e1));
            }
            return null;
        }
    }
    
    private BufferedImage getFrameTextureData(final TextureAtlasSprite tas) {
        try {
            if (tas.func_110970_k() > 0) {
                final int[] rgb = tas.func_147965_a(0)[0];
                if (rgb.length > 0) {
                    final int width = tas.func_94211_a();
                    final int height = tas.func_94216_b();
                    final BufferedImage textureImg = new BufferedImage(width, height, 2);
                    textureImg.setRGB(0, 0, width, height, rgb, 0, width);
                    return textureImg;
                }
            }
        }
        catch (Throwable t) {
            ColoredSprite.logger.error(String.format("ColoredSprite: Unable to use frame data for %s: %s", tas.func_94215_i(), t.getMessage()));
        }
        return null;
    }
    
    private BufferedImage getImageResource(final TextureAtlasSprite tas) {
        try {
            final ResourceLocation iconNameLoc = new ResourceLocation(tas.func_94215_i());
            final ResourceLocation fileLoc = new ResourceLocation(iconNameLoc.func_110624_b(), "textures/" + iconNameLoc.func_110623_a() + ".png");
            return TextureCache.resolveImage(fileLoc);
        }
        catch (Throwable t) {
            ColoredSprite.logger.error(String.format("ColoredSprite: Unable to use texture file for %s: %s", tas.func_94215_i(), t.getMessage()));
            return null;
        }
    }
    
    private BufferedImage applyColor(final BufferedImage original) {
        return original;
    }
    
    static {
        ColoredSprite.logger = Journeymap.getLogger();
    }
}
