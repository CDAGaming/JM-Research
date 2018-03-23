package journeymap.client.task.main;

import org.apache.logging.log4j.*;
import journeymap.client.render.texture.*;
import journeymap.common.*;
import net.minecraft.client.*;
import journeymap.client.*;
import java.util.*;
import org.lwjgl.opengl.*;
import net.minecraft.client.renderer.*;
import org.lwjgl.*;

public class ExpireTextureTask implements IMainThreadTask
{
    private static final int MAX_FAILS = 5;
    private static String NAME;
    private static Logger LOGGER;
    private final List<TextureImpl> textures;
    private final int textureId;
    private volatile int fails;
    
    private ExpireTextureTask(final int textureId) {
        this.textures = null;
        this.textureId = textureId;
    }
    
    private ExpireTextureTask(final TextureImpl texture) {
        (this.textures = new ArrayList<TextureImpl>()).add(texture);
        this.textureId = -1;
    }
    
    private ExpireTextureTask(final Collection<TextureImpl> textureCollection) {
        this.textures = new ArrayList<TextureImpl>(textureCollection);
        this.textureId = -1;
    }
    
    public static void queue(final int textureId) {
        if (textureId != -1) {
            Journeymap.getClient().queueMainThreadTask(new ExpireTextureTask(textureId));
        }
    }
    
    public static void queue(final TextureImpl texture) {
        Journeymap.getClient().queueMainThreadTask(new ExpireTextureTask(texture));
    }
    
    public static void queue(final Collection<TextureImpl> textureCollection) {
        Journeymap.getClient().queueMainThreadTask(new ExpireTextureTask(textureCollection));
    }
    
    @Override
    public IMainThreadTask perform(final Minecraft mc, final JourneymapClient jm) {
        final boolean success = this.deleteTextures();
        if (!success && this.textures != null && !this.textures.isEmpty()) {
            ++this.fails;
            ExpireTextureTask.LOGGER.warn("ExpireTextureTask.perform() couldn't delete textures: " + this.textures + ", fails: " + this.fails);
            if (this.fails <= 5) {
                return this;
            }
        }
        return null;
    }
    
    private boolean deleteTextures() {
        if (this.textureId != -1) {
            return this.deleteTexture(this.textureId);
        }
        final Iterator<TextureImpl> iter = this.textures.listIterator();
        while (iter.hasNext()) {
            final TextureImpl texture = iter.next();
            if (texture == null) {
                iter.remove();
            }
            else {
                if (!this.deleteTexture(texture)) {
                    break;
                }
                iter.remove();
            }
        }
        return this.textures.isEmpty();
    }
    
    private boolean deleteTexture(final TextureImpl texture) {
        boolean success = false;
        if (texture.isBound()) {
            try {
                if (Display.isCurrent()) {
                    GlStateManager.func_179150_h(texture.func_110552_b());
                    texture.clear();
                    success = true;
                }
            }
            catch (LWJGLException t) {
                ExpireTextureTask.LOGGER.warn("Couldn't delete texture " + texture + ": " + t);
                success = false;
            }
        }
        else {
            texture.clear();
            success = true;
        }
        return success;
    }
    
    private boolean deleteTexture(final int textureId) {
        try {
            if (Display.isCurrent()) {
                GlStateManager.func_179150_h(textureId);
                return true;
            }
        }
        catch (LWJGLException t) {
            ExpireTextureTask.LOGGER.warn("Couldn't delete textureId " + textureId + ": " + t);
        }
        return false;
    }
    
    @Override
    public String getName() {
        return ExpireTextureTask.NAME;
    }
    
    static {
        ExpireTextureTask.NAME = "Tick." + MappingMonitorTask.class.getSimpleName();
        ExpireTextureTask.LOGGER = Journeymap.getLogger();
    }
}
