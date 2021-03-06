package journeymap.client.api.display;

import journeymap.client.api.model.MapImage;
import net.minecraft.util.math.BlockPos;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class ImageOverlay extends Overlay {
    private BlockPos northWestPoint;
    private BlockPos southEastPoint;
    private MapImage image;

    public ImageOverlay(final String modId, final String imageId, final BlockPos northWestPoint, final BlockPos southEastPoint, final MapImage image) {
        super(modId, imageId);
        this.setNorthWestPoint(northWestPoint);
        this.setSouthEastPoint(southEastPoint);
        this.setImage(image);
    }

    public BlockPos getNorthWestPoint() {
        return this.northWestPoint;
    }

    public ImageOverlay setNorthWestPoint(final BlockPos northWestPoint) {
        this.northWestPoint = northWestPoint;
        return this;
    }

    public BlockPos getSouthEastPoint() {
        return this.southEastPoint;
    }

    public ImageOverlay setSouthEastPoint(final BlockPos southEastPoint) {
        this.southEastPoint = southEastPoint;
        return this;
    }

    public MapImage getImage() {
        return this.image;
    }

    public ImageOverlay setImage(final MapImage image) {
        this.image = image;
        return this;
    }

    @Override
    public String toString() {
        return this.toStringHelper(this).add("image", this.image).add("northWestPoint", this.northWestPoint).add("southEastPoint", this.southEastPoint).toString();
    }
}
