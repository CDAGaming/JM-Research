package journeymap.client.render.texture;

import net.minecraft.client.renderer.texture.*;
import java.util.concurrent.locks.*;
import java.awt.image.*;
import net.minecraft.util.*;
import java.lang.ref.*;
import java.nio.*;
import journeymap.client.task.multi.*;
import net.minecraft.client.renderer.*;
import org.lwjgl.opengl.*;
import journeymap.common.*;
import journeymap.client.task.main.*;
import net.minecraft.client.resources.*;
import com.google.common.base.*;
import java.util.*;

public class TextureImpl extends AbstractTexture
{
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
        this.setDescription(resourceLocation.func_110623_a());
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
        this.listeners = new ArrayList<WeakReference<Listener>>(0);
        if (glId != null) {
            this.field_110553_a = glId;
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
        }
        finally {
            this.bufferLock.unlock();
        }
        this.lastImageUpdate = System.currentTimeMillis();
        this.notifyListeners();
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
                buffer.put((byte)(pixel >> 16 & 0xFF));
                buffer.put((byte)(pixel >> 8 & 0xFF));
                buffer.put((byte)(pixel & 0xFF));
                buffer.put((byte)(pixel >> 24 & 0xFF));
            }
        }
        buffer.flip();
        buffer.rewind();
    }
    
    public void bindTexture() {
        if (!this.bindNeeded) {
            return;
        }
        if (this.bufferLock.tryLock()) {
            if (this.field_110553_a > -1) {
                MapPlayerTask.addTempDebugMessage("tex" + this.field_110553_a, "Updating: " + this.getDescription());
            }
            try {
                GlStateManager.func_179144_i(super.func_110552_b());
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
            }
            catch (Throwable t) {
                Journeymap.getLogger().warn("Can't bind texture: " + t);
                this.buffer = null;
            }
            finally {
                this.bufferLock.unlock();
            }
        }
    }
    
    public boolean isBindNeeded() {
        return this.bindNeeded;
    }
    
    public boolean isBound() {
        return this.field_110553_a != -1;
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
        return this.field_110553_a == -1 && this.image == null && this.buffer == null;
    }
    
    public int func_110552_b() {
        if (this.bindNeeded) {
            this.bindTexture();
        }
        return super.func_110552_b();
    }
    
    public int getGlTextureId(final boolean forceBind) {
        if (forceBind || this.field_110553_a == -1) {
            return this.func_110552_b();
        }
        return this.field_110553_a;
    }
    
    public void clear() {
        this.bufferLock.lock();
        this.buffer = null;
        this.bufferLock.unlock();
        this.image = null;
        this.bindNeeded = false;
        this.lastImageUpdate = 0L;
        this.lastBound = 0L;
        this.field_110553_a = -1;
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
    
    public void func_110551_a(final IResourceManager par1ResourceManager) {
        if (this.resourceLocation != null) {}
    }
    
    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("glid", this.field_110553_a).add("description", (Object)this.description).add("lastImageUpdate", this.lastImageUpdate).add("lastBound", this.lastBound).toString();
    }
    
    public void finalize() {
        if (this.isBound()) {
            Journeymap.getLogger().warn("TextureImpl disposed without deleting texture glID: " + this);
            ExpireTextureTask.queue(this.field_110553_a);
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
            }
            else {
                if (addedListener == listener) {
                    return;
                }
                continue;
            }
        }
        this.listeners.add(new WeakReference<Listener>(addedListener));
    }
    
    protected void notifyListeners() {
        final Iterator<WeakReference<Listener>> iter = this.listeners.iterator();
        while (iter.hasNext()) {
            final WeakReference<Listener> ref = iter.next();
            final Listener listener = ref.get();
            if (listener == null) {
                iter.remove();
            }
            else {
                listener.textureImageUpdated(this);
            }
        }
    }
    
    public interface Listener<T extends TextureImpl>
    {
        void textureImageUpdated(final T p0);
    }
}
