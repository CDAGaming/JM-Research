package journeymap.client.ui.minimap;

import journeymap.client.ui.option.*;
import journeymap.client.render.texture.*;
import net.minecraft.util.*;
import com.google.common.base.*;
import journeymap.client.*;

public enum EntityDisplay implements KeyedEnum
{
    SmallDots("jm.common.entity_display.small_dots"), 
    LargeDots("jm.common.entity_display.large_dots"), 
    SmallIcons("jm.common.entity_display.small_icons"), 
    LargeIcons("jm.common.entity_display.large_icons");
    
    public final String key;
    
    private EntityDisplay(final String key) {
        this.key = key;
    }
    
    public static TextureImpl getLocatorTexture(final EntityDisplay entityDisplay, final boolean showHeading) {
        ResourceLocation texLocation = null;
        switch (entityDisplay) {
            case LargeDots: {
                texLocation = (showHeading ? TextureCache.MobDotArrow_Large : TextureCache.MobDot_Large);
                break;
            }
            case SmallDots: {
                texLocation = (showHeading ? TextureCache.MobDotArrow : TextureCache.MobDot);
                break;
            }
            case LargeIcons: {
                texLocation = (showHeading ? TextureCache.MobIconArrow_Large : null);
                break;
            }
            case SmallIcons: {
                texLocation = (showHeading ? TextureCache.MobIconArrow : null);
                break;
            }
        }
        return TextureCache.getTexture(texLocation);
    }
    
    public static TextureImpl getEntityTexture(final EntityDisplay entityDisplay) {
        return getEntityTexture(entityDisplay, (String)null);
    }
    
    public static TextureImpl getEntityTexture(final EntityDisplay entityDisplay, final String playerName) {
        switch (entityDisplay) {
            case LargeDots: {
                return TextureCache.getTexture(TextureCache.MobDotChevron_Large);
            }
            case SmallDots: {
                return TextureCache.getTexture(TextureCache.MobDotChevron);
            }
            default: {
                if (!Strings.isNullOrEmpty(playerName)) {
                    return TextureCache.getPlayerSkin(playerName);
                }
                return null;
            }
        }
    }
    
    public static TextureImpl getEntityTexture(final EntityDisplay entityDisplay, final ResourceLocation iconLocation) {
        switch (entityDisplay) {
            case LargeDots: {
                return TextureCache.getTexture(TextureCache.MobDotChevron_Large);
            }
            case SmallDots: {
                return TextureCache.getTexture(TextureCache.MobDotChevron);
            }
            default: {
                return TextureCache.getTexture(iconLocation);
            }
        }
    }
    
    @Override
    public String getKey() {
        return this.key;
    }
    
    @Override
    public String toString() {
        return Constants.getString(this.key);
    }
    
    public boolean isDots() {
        return this == EntityDisplay.LargeDots || this == EntityDisplay.SmallDots;
    }
    
    public boolean isLarge() {
        return this == EntityDisplay.LargeDots || this == EntityDisplay.LargeIcons;
    }
}
