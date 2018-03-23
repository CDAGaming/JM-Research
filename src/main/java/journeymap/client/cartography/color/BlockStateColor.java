package journeymap.client.cartography.color;

import com.google.gson.annotations.*;
import journeymap.client.model.*;
import journeymap.common.*;
import com.google.common.collect.*;
import java.util.*;

class BlockStateColor implements Comparable<BlockStateColor>
{
    @Since(5.45)
    String block;
    @Since(5.45)
    String state;
    @Since(5.2)
    String name;
    @Since(5.2)
    String color;
    @Since(5.2)
    Float alpha;
    
    BlockStateColor(final BlockMD blockMD) {
        this(blockMD, Integer.valueOf(blockMD.getTextureColor()));
    }
    
    BlockStateColor(final BlockMD blockMD, final Integer color) {
        if (Journeymap.getClient().getCoreProperties().verboseColorPalette.get()) {
            this.block = blockMD.getBlockId();
            this.state = blockMD.getBlockStateId();
            this.name = blockMD.getName();
        }
        this.color = RGB.toHexString(color);
        if (blockMD.getAlpha() != 1.0f) {
            this.alpha = blockMD.getAlpha();
        }
    }
    
    BlockStateColor(final String color, final Float alpha) {
        this.color = color;
        this.alpha = ((alpha == null) ? 1.0f : alpha);
    }
    
    @Override
    public int compareTo(final BlockStateColor that) {
        final Ordering ordering = Ordering.natural().nullsLast();
        return ComparisonChain.start().compare((Object)this.name, (Object)that.name, (Comparator)ordering).compare((Object)this.block, (Object)that.block, (Comparator)ordering).compare((Object)this.state, (Object)that.state, (Comparator)ordering).compare((Object)this.color, (Object)that.color, (Comparator)ordering).compare((Object)this.alpha, (Object)that.alpha, (Comparator)ordering).result();
    }
}
