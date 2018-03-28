package journeymap.client.mod;

import journeymap.client.cartography.color.ColoredSprite;
import journeymap.client.model.BlockMD;

import javax.annotation.Nullable;
import java.util.Collection;

public interface IBlockSpritesProxy {
    @Nullable
    Collection<ColoredSprite> getSprites(final BlockMD p0);
}
