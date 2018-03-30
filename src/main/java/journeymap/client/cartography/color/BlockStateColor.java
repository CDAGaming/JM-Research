package journeymap.client.cartography.color;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.google.gson.annotations.Since;
import journeymap.client.model.BlockMD;
import journeymap.common.Journeymap;

class BlockStateColor implements Comparable<BlockStateColor> {
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
        this(blockMD, blockMD.getTextureColor());
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
        return ComparisonChain.start().compare(this.name, that.name, ordering).compare(this.block, that.block, ordering).compare(this.state, that.state, ordering).compare(this.color, that.color, ordering).compare(this.alpha, that.alpha, ordering).result();
    }
}
