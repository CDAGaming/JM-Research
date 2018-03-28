package journeymap.client.model;

import journeymap.client.io.RegionImageHandler;
import journeymap.client.render.ComparableBufferedImage;

import java.awt.image.BufferedImage;
import java.io.File;

public class RegionImageSet extends ImageSet {
    protected final Key key;

    public RegionImageSet(final Key key) {
        this.key = key;
    }

    public ImageHolder getHolder(final MapType mapType) {
        synchronized (this.imageHolders) {
            ImageHolder imageHolder = this.imageHolders.get(mapType);
            if (imageHolder == null) {
                final File imageFile = RegionImageHandler.getRegionImageFile(this.getRegionCoord(), mapType, false);
                imageHolder = this.addHolder(mapType, imageFile);
            }
            return imageHolder;
        }
    }

    public ComparableBufferedImage getChunkImage(final ChunkMD chunkMd, final MapType mapType) {
        final RegionCoord regionCoord = this.getRegionCoord();
        final BufferedImage regionImage = this.getHolder(mapType).getImage();
        final BufferedImage sub = regionImage.getSubimage(regionCoord.getXOffset(chunkMd.getCoord().x), regionCoord.getZOffset(chunkMd.getCoord().z), 16, 16);
        final ComparableBufferedImage chunk = new ComparableBufferedImage(16, 16, regionImage.getType());
        chunk.setData(sub.getData());
        return chunk;
    }

    public void setChunkImage(final ChunkMD chunkMd, final MapType mapType, final ComparableBufferedImage chunkImage) {
        final ImageHolder holder = this.getHolder(mapType);
        final boolean wasBlank = holder.blank;
        if (chunkImage.isChanged() || wasBlank) {
            final RegionCoord regionCoord = this.getRegionCoord();
            holder.partialImageUpdate(chunkImage, regionCoord.getXOffset(chunkMd.getCoord().x), regionCoord.getZOffset(chunkMd.getCoord().z));
        }
        if (wasBlank) {
            holder.getTexture();
            holder.finishPartialImageUpdates();
            RegionImageCache.INSTANCE.getRegionImageSet(this.getRegionCoord());
        }
        chunkMd.setRendered(mapType);
    }

    public boolean hasChunkUpdates() {
        synchronized (this.imageHolders) {
            for (final ImageHolder holder : this.imageHolders.values()) {
                if (holder.partialUpdate) {
                    return true;
                }
            }
        }
        return false;
    }

    public void finishChunkUpdates() {
        synchronized (this.imageHolders) {
            for (final ImageHolder holder : this.imageHolders.values()) {
                holder.finishPartialImageUpdates();
            }
        }
    }

    public RegionCoord getRegionCoord() {
        return RegionCoord.fromRegionPos(this.key.worldDir, this.key.regionX, this.key.regionZ, this.key.dimension);
    }

    public long getOldestTimestamp() {
        long time = System.currentTimeMillis();
        synchronized (this.imageHolders) {
            for (final ImageHolder holder : this.imageHolders.values()) {
                if (holder != null) {
                    time = Math.min(time, holder.getImageTimestamp());
                }
            }
        }
        return time;
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || (obj != null && this.getClass() == obj.getClass() && this.key.equals(((RegionImageSet) obj).key));
    }

    @Override
    protected int getImageSize() {
        return 512;
    }

    public static class Key {
        private final File worldDir;
        private final int regionX;
        private final int regionZ;
        private final int dimension;

        private Key(final File worldDir, final int regionX, final int regionZ, final int dimension) {
            this.worldDir = worldDir;
            this.regionX = regionX;
            this.regionZ = regionZ;
            this.dimension = dimension;
        }

        public static Key from(final RegionCoord rCoord) {
            return new Key(rCoord.worldDir, rCoord.regionX, rCoord.regionZ, rCoord.dimension);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final Key key = (Key) o;
            return this.dimension == key.dimension && this.regionX == key.regionX && this.regionZ == key.regionZ && this.worldDir.equals(key.worldDir);
        }

        @Override
        public int hashCode() {
            int result = this.worldDir.hashCode();
            result = 31 * result + this.regionX;
            result = 31 * result + this.regionZ;
            result = 31 * result + this.dimension;
            return result;
        }
    }
}
