package journeymap.client.mod;

import journeymap.client.model.*;
import java.util.*;
import journeymap.client.cartography.color.*;
import javax.annotation.*;

public interface IBlockSpritesProxy
{
    @Nullable
    Collection<ColoredSprite> getSprites(final BlockMD p0);
}
