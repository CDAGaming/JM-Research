package journeymap.client.render.texture;

import net.minecraft.util.math.*;
import java.awt.image.*;
import net.minecraft.client.renderer.texture.*;
import journeymap.client.task.multi.*;
import net.minecraft.client.renderer.*;
import org.lwjgl.opengl.*;
import java.nio.*;
import journeymap.common.*;
import java.util.*;

public class RegionTextureImpl extends TextureImpl
{
    protected HashSet<ChunkPos> dirtyChunks;
    
    public RegionTextureImpl(final BufferedImage image) {
        super(null, image, true, false);
        this.dirtyChunks = new HashSet<ChunkPos>();
    }
    
    public void setImage(final BufferedImage bufferedImage, final boolean retainImage, final HashSet<ChunkPos> updatedChunks) {
        if (updatedChunks.size() > 15) {
            super.setImage(bufferedImage, retainImage);
        }
        else {
            this.dirtyChunks.addAll((Collection<?>)updatedChunks);
            this.bindNeeded = true;
            try {
                this.bufferLock.lock();
                this.retainImage = retainImage;
                if (retainImage) {
                    this.image = bufferedImage;
                }
                this.width = bufferedImage.getWidth();
                this.height = bufferedImage.getHeight();
            }
            finally {
                this.bufferLock.unlock();
            }
        }
        this.lastImageUpdate = System.currentTimeMillis();
        this.notifyListeners();
    }
    
    @Override
    public void bindTexture() {
        if (!this.bindNeeded) {
            return;
        }
        if (this.field_110553_a == -1) {
            this.field_110553_a = TextureUtil.func_110996_a();
        }
        if (this.lastBound == 0L || this.dirtyChunks.isEmpty()) {
            super.bindTexture();
            return;
        }
        if (this.bufferLock.tryLock()) {
            MapPlayerTask.addTempDebugMessage("tex" + this.field_110553_a, "Updating " + this.dirtyChunks.size() + " chunks within: " + this.getDescription());
            GlStateManager.func_179144_i(this.field_110553_a);
            GL11.glTexParameteri(3553, 10242, 10497);
            GL11.glTexParameteri(3553, 10243, 10497);
            GL11.glTexParameteri(3553, 10241, 9729);
            GL11.glTexParameteri(3553, 10240, 9729);
            try {
                boolean glErrors = false;
                final ByteBuffer chunkBuffer = ByteBuffer.allocateDirect(1024);
                for (final ChunkPos pos : this.dirtyChunks) {
                    final BufferedImage chunkImage = this.getImage().getSubimage(pos.field_77276_a, pos.field_77275_b, 16, 16);
                    TextureImpl.loadByteBuffer(chunkImage, chunkBuffer);
                    GL11.glTexSubImage2D(3553, 0, pos.field_77276_a, pos.field_77275_b, 16, 16, 6408, 5121, chunkBuffer);
                    int err;
                    while ((err = GL11.glGetError()) != 0) {
                        glErrors = true;
                        Journeymap.getLogger().warn("GL Error in RegionTextureImpl after glTexSubImage2D: " + err + " for " + pos + " in " + this);
                    }
                    if (glErrors) {
                        break;
                    }
                }
                this.dirtyChunks.clear();
                if (glErrors) {
                    this.bindNeeded = true;
                }
                else {
                    this.bindNeeded = false;
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
    
    public Set<ChunkPos> getDirtyAreas() {
        return this.dirtyChunks;
    }
}
