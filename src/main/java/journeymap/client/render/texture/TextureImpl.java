package journeymap.client.render.texture;

import com.google.common.base.MoreObjects;
import journeymap.client.task.main.ExpireTextureTask;
import journeymap.client.task.multi.MapPlayerTask;
import journeymap.common.Journeymap;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class TextureImpl extends AbstractTexture {
    protected final ReentrantLock bufferLock;
    protected BufferedImage image;
    protected boolean retainImage;
    protected int width;
    protected int height;
    protected float alpha;
    protected long lastImageUpdate;
    protected long lastBound;
    protected String description;
    protected ResourceLocation resourceLocation;
    protected List<WeakReference<Listener>> listeners;
    protected ByteBuffer buffer;
    protected boolean bindNeeded;

    public TextureImpl(final ResourceLocation resourceLocation) {
        this(null, TextureCache.resolveImage(resourceLocation), false, false);
        this.resourceLocation = resourceLocation;
        this.setDescription(resourceLocation.getResourcePath());
    }

    public TextureImpl(final BufferedImage image) {
        this(null, image, false, true);
    }

    public TextureImpl(final BufferedImage image, final boolean retainImage) {
        this(null, image, retainImage, true);
    }

    public TextureImpl(final Integer glId, final BufferedImage image, final boolean retainImage) {
        this(glId, image, retainImage, true);
    }

    public TextureImpl(final Integer glId, final BufferedImage image, final boolean retainImage, final boolean bindImmediately) {
        this.bufferLock = new ReentrantLock();
        this.listeners = new ArrayList<>(0);
        if (glId != null) {
            this.glTextureId = glId;
        }
        this.retainImage = retainImage;
        if (image != null) {
            this.setImage(image, retainImage);
        }
        if (bindImmediately) {
            this.bindTexture();
            this.buffer = null;
        }
    }

    public static void loadByteBuffer(final BufferedImage bufferedImage, final ByteBuffer buffer) {
        final int width = bufferedImage.getWidth();
        final int height = bufferedImage.getHeight();
        buffer.clear();
        final int[] pixels = new int[width * height];
        bufferedImage.getRGB(0, 0, width, height, pixels, 0, width);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                final int pixel = pixels[y * width + x];
                buffer.put((byte) (pixel >> 16 & 0xFF));
                buffer.put((byte) (pixel >> 8 & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) (pixel >> 24 & 0xFF));
            }
        }
        buffer.flip();
        buffer.rewind();
    }

    public void setImage(final BufferedImage bufferedImage, final boolean retainImage) {
        if (bufferedImage == null) {
            return;
        }
        try {
            this.bufferLock.lock();
            if (this.retainImage = retainImage) {
                this.image = bufferedImage;
            }
            this.width = bufferedImage.getWidth();
            this.height = bufferedImage.getHeight();
            final int bufferSize = this.width * this.height * 4;
            if (this.buffer == null || this.buffer.capacity() != bufferSize) {
                this.buffer = ByteBuffer.allocateDirect(bufferSize);
            }
            loadByteBuffer(bufferedImage, this.buffer);
            this.bindNeeded = true;
        } finally {
            this.bufferLock.unlock();
        }
        this.lastImageUpdate = System.currentTimeMillis();
        this.notifyListeners();
    }

    public void bindTexture() {
        if (!this.bindNeeded) {
            return;
        }
        if (this.bufferLock.tryLock()) {
            if (this.glTextureId > -1) {
                MapPlayerTask.addTempDebugMessage("tex" + this.glTextureId, "Updating: " + this.getDescription());
            }
            try {
                GlStateManager.bindTexture(super.getGlTextureId());
                GL11.glTexParameteri(3553, 10242, 10497);
                GL11.glTexParameteri(3553, 10243, 10497);
                GL11.glTexParameteri(3553, 10241, 9729);
                GL11.glTexParameteri(3553, 10240, 9729);
                GL11.glTexImage2D(3553, 0, 32856, this.width, this.height, 0, 6408, 5121, this.buffer);
                this.bindNeeded = false;
                int glErr;
                while ((glErr = GL11.glGetError()) != 0) {
                    Journeymap.getLogger().warn("GL Error in TextureImpl after glTexImage2D: " + glErr + " in " + this);
                    this.bindNeeded = true;
                }
                if (!this.bindNeeded) {
                    this.lastBound = System.currentTimeMillis();
                }
            } catch (Throwable t) {
                Journeymap.getLogger().warn("Can't bind texture: " + t);
                this.buffer = null;
            } finally {
                this.bufferLock.unlock();
            }
        }
    }

    public boolean isBindNeeded() {
        return this.bindNeeded;
    }

    public boolean isBound() {
        return this.glTextureId != -1;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void updateAndBind(final BufferedImage image) {
        this.updateAndBind(image, this.retainImage);
    }

    public void updateAndBind(final BufferedImage image, final boolean retainImage) {
        this.setImage(image, retainImage);
        this.bindTexture();
    }

    public boolean hasImage() {
        return this.image != null;
    }

    public BufferedImage getImage() {
        if (this.image != null) {
            return this.image;
        }
        if (this.resourceLocation != null) {
            return TextureCache.resolveImage(this.resourceLocation);
        }
        return null;
    }

    public boolean isDefunct() {
        return this.glTextureId == -1 && this.image == null && this.buffer == null;
    }

    public int getGlTextureId() {
        if (this.bindNeeded) {
            this.bindTexture();
        }
        return super.getGlTextureId();
    }

    public int getGlTextureId(final boolean forceBind) {
        if (forceBind || this.glTextureId == -1) {
            return this.getGlTextureId();
        }
        return this.glTextureId;
    }

    public void clear() {
        this.bufferLock.lock();
        this.buffer = null;
        this.bufferLock.unlock();
        this.image = null;
        this.bindNeeded = false;
        this.lastImageUpdate = 0L;
        this.lastBound = 0L;
        this.glTextureId = -1;
    }

    public void queueForDeletion() {
        ExpireTextureTask.queue(this);
    }

    public long getLastImageUpdate() {
        return this.lastImageUpdate;
    }

    public long getLastBound() {
        return this.lastBound;
    }

    public void loadTexture(final IResourceManager par1ResourceManager) {
        if (this.resourceLocation != null) {
        }
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("glid", this.glTextureId).add("description", this.description).add("lastImageUpdate", this.lastImageUpdate).add("lastBound", this.lastBound).toString();
    }

    public void finalize() {
        if (this.isBound()) {
            Journeymap.getLogger().warn("TextureImpl disposed without deleting texture glID: " + this);
            ExpireTextureTask.queue(this.glTextureId);
        }
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(final int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(final int height) {
        this.height = height;
    }

    public float getAlpha() {
        return this.alpha;
    }

    public void setAlpha(final float alpha) {
        this.alpha = alpha;
    }

    public void addListener(final Listener addedListener) {
        final Iterator<WeakReference<Listener>> iter = this.listeners.iterator();
        while (iter.hasNext()) {
            final WeakReference<Listener> ref = iter.next();
            final Listener listener = ref.get();
            if (listener == null) {
                iter.remove();
            } else {
                if (addedListener == listener) {
                    return;
                }
            }
        }
        this.listeners.add(new WeakReference<>(addedListener));
    }

    protected void notifyListeners() {
        final Iterator<WeakReference<Listener>> iter = this.listeners.iterator();
        while (iter.hasNext()) {
            final WeakReference<Listener> ref = iter.next();
            final Listener listener = ref.get();
            if (listener == null) {
                iter.remove();
            } else {
                listener.textureImageUpdated(this);
            }
        }
    }

    public interface Listener<T extends TextureImpl> {
        void textureImageUpdated(final T p0);
    }
}
