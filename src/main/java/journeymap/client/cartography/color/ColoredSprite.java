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
        this.sprite = quad.getSprite();
        this.color = null;
    }
    
    public String getIconName() {
        return this.sprite.getIconName();
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
            if (resourceLocation.equals((Object)TextureMap.LOCATION_MISSING_TEXTURE)) {
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
            if (tas.getFrameCount() > 0) {
                final int[] rgb = tas.getFrameTextureData(0)[0];
                if (rgb.length > 0) {
                    final int width = tas.getIconWidth();
                    final int height = tas.getIconHeight();
                    final BufferedImage textureImg = new BufferedImage(width, height, 2);
                    textureImg.setRGB(0, 0, width, height, rgb, 0, width);
                    return textureImg;
                }
            }
        }
        catch (Throwable t) {
            ColoredSprite.logger.error(String.format("ColoredSprite: Unable to use frame data for %s: %s", tas.getIconName(), t.getMessage()));
        }
        return null;
    }
    
    private BufferedImage getImageResource(final TextureAtlasSprite tas) {
        try {
            final ResourceLocation iconNameLoc = new ResourceLocation(tas.getIconName());
            final ResourceLocation fileLoc = new ResourceLocation(iconNameLoc.getResourceDomain(), "textures/" + iconNameLoc.getResourcePath() + ".png");
            return TextureCache.resolveImage(fileLoc);
        }
        catch (Throwable t) {
            ColoredSprite.logger.error(String.format("ColoredSprite: Unable to use texture file for %s: %s", tas.getIconName(), t.getMessage()));
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
