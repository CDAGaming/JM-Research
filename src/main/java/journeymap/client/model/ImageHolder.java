package journeymap.client.model;

import com.google.common.base.MoreObjects;
import journeymap.client.io.RegionImageHandler;
import journeymap.client.log.StatTimer;
import journeymap.client.render.texture.RegionTextureImpl;
import journeymap.client.task.main.ExpireTextureTask;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.IThreadedFileIO;
import net.minecraft.world.storage.ThreadedFileIOBase;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ImageHolder implements IThreadedFileIO {
    static final Logger logger;

    static {
        logger = Journeymap.getLogger();
    }

    final MapType mapType;
    final Path imagePath;
    final int imageSize;
    boolean blank;
    boolean dirty;
    boolean partialUpdate;
    private volatile ReentrantLock writeLock;
    private volatile RegionTextureImpl texture;
    private boolean debug;
    private HashSet<ChunkPos> updatedChunks;

    ImageHolder(final MapType mapType, final File imageFile, final int imageSize) {
        this.blank = true;
        this.dirty = true;
        this.writeLock = new ReentrantLock();
        this.updatedChunks = new HashSet<ChunkPos>();
        this.mapType = mapType;
        this.imagePath = imageFile.toPath();
        this.imageSize = imageSize;
        this.debug = ImageHolder.logger.isEnabled(Level.DEBUG);
        this.getTexture();
    }

    File getFile() {
        return this.imagePath.toFile();
    }

    MapType getMapType() {
        return this.mapType;
    }

    BufferedImage getImage() {
        return this.texture.getImage();
    }

    void setImage(final BufferedImage image) {
        this.texture.setImage(image, true);
        this.setDirty();
    }

    void partialImageUpdate(final BufferedImage imagePart, final int x, final int y) {
        this.writeLock.lock();
        final StatTimer timer = StatTimer.get("ImageHolder.partialImageUpdate", 5, 500);
        timer.start();
        try {
            if (this.texture != null) {
                this.blank = false;
                final int width = imagePart.getWidth();
                final int height = imagePart.getHeight();
                final int[] updatedPixels = new int[width * height];
                imagePart.getRGB(0, 0, width, height, updatedPixels, 0, width);
                this.texture.getImage().setRGB(x, y, width, height, updatedPixels, 0, width);
                this.partialUpdate = true;
                this.updatedChunks.add(new ChunkPos(x, y));
            } else {
                ImageHolder.logger.warn(this + " can't partialImageUpdate without a texture.");
            }
        } finally {
            timer.stop();
            this.writeLock.unlock();
        }
    }

    void finishPartialImageUpdates() {
        this.writeLock.lock();
        try {
            if (this.partialUpdate && !this.updatedChunks.isEmpty()) {
                final BufferedImage textureImage = this.texture.getImage();
                this.texture.setImage(textureImage, true, this.updatedChunks);
                this.setDirty();
                this.partialUpdate = false;
                this.updatedChunks.clear();
            }
        } finally {
            this.writeLock.unlock();
        }
    }

    public boolean hasTexture() {
        return this.texture != null && !this.texture.isDefunct();
    }

    public RegionTextureImpl getTexture() {
        if (!this.hasTexture()) {
            if (!this.imagePath.toFile().exists()) {
                final File temp = new File(this.imagePath.toString() + ".new");
                if (temp.exists()) {
                    Journeymap.getLogger().warn("Recovered image file: " + temp);
                    temp.renameTo(this.imagePath.toFile());
                }
            }
            BufferedImage image = RegionImageHandler.readRegionImage(this.imagePath.toFile(), false);
            if (image == null || image.getWidth() != this.imageSize || image.getHeight() != this.imageSize) {
                image = new BufferedImage(this.imageSize, this.imageSize, 2);
                this.blank = true;
                this.dirty = false;
            } else {
                this.blank = false;
            }
            (this.texture = new RegionTextureImpl(image)).setDescription(this.imagePath.toString());
        }
        return this.texture;
    }

    private void setDirty() {
        this.dirty = true;
    }

    boolean isDirty() {
        return this.dirty;
    }

    protected boolean writeToDisk(final boolean async) {
        if (this.blank || this.texture == null || !this.texture.hasImage()) {
            return false;
        }
        if (async) {
            ThreadedFileIOBase.getThreadedIOInstance().queueIO((IThreadedFileIO) this);
            return true;
        }
        int tries = 0;
        boolean success = false;
        while (tries < 5) {
            if (!this.writeNextIO()) {
                success = true;
                break;
            }
            ++tries;
        }
        if (!success) {
            Journeymap.getLogger().warn("Couldn't write file after 5 tries: " + this);
        }
        return success;
    }

    public boolean writeNextIO() {
        if (this.texture == null || !this.texture.hasImage()) {
            return false;
        }
        try {
            if (this.writeLock.tryLock(250L, TimeUnit.MILLISECONDS)) {
                this.writeImageToFile();
                this.writeLock.unlock();
                return false;
            }
            ImageHolder.logger.warn("Couldn't get write lock for file: " + this.writeLock + " for " + this);
            return false;
        } catch (InterruptedException e) {
            ImageHolder.logger.warn("Timeout waiting for write lock  " + this.writeLock + " for " + this);
            return false;
        }
    }

    private void writeImageToFile() {
        final File imageFile = this.imagePath.toFile();
        try {
            final BufferedImage image = this.texture.getImage();
            if (image != null) {
                if (!imageFile.exists()) {
                    imageFile.getParentFile().mkdirs();
                }
                final File temp = new File(imageFile.getParentFile(), imageFile.getName() + ".new");
                ImageIO.write(image, "PNG", temp);
                if (imageFile.exists() && !imageFile.delete()) {
                    ImageHolder.logger.warn("Couldn't delete old file " + imageFile.getName());
                }
                if (temp.renameTo(imageFile)) {
                    this.dirty = false;
                } else {
                    ImageHolder.logger.warn("Couldn't rename temp file to " + imageFile.getName());
                }
                if (this.debug) {
                    ImageHolder.logger.debug("Wrote to disk: " + imageFile);
                }
            }
        } catch (IOException e) {
            if (imageFile.exists()) {
                try {
                    ImageHolder.logger.error("IOException updating file, will delete and retry: " + this + ": " + LogFormatter.toPartialString(e));
                    imageFile.delete();
                    this.writeImageToFile();
                } catch (Throwable e3) {
                    ImageHolder.logger.error("Exception after delete/retry: " + this + ": " + LogFormatter.toPartialString(e));
                }
            } else {
                ImageHolder.logger.error("IOException creating file: " + this + ": " + LogFormatter.toPartialString(e));
            }
        } catch (Throwable e2) {
            ImageHolder.logger.error("Exception writing to disk: " + this + ": " + LogFormatter.toPartialString(e2));
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper((Object) this).add("mapType", (Object) this.mapType).add("textureId", (Object) ((this.texture == null) ? null : (this.texture.isBound() ? this.texture.getGlTextureId(false) : -1))).add("dirty", this.dirty).add("imagePath", (Object) this.imagePath).toString();
    }

    public void clear() {
        this.writeLock.lock();
        ExpireTextureTask.queue(this.texture);
        this.texture = null;
        this.writeLock.unlock();
    }

    public void finalize() {
        if (this.texture != null) {
            this.clear();
        }
    }

    public long getImageTimestamp() {
        if (this.texture != null) {
            return this.texture.getLastImageUpdate();
        }
        return 0L;
    }
}
