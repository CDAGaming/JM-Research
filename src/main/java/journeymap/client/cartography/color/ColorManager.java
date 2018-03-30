package journeymap.client.cartography.color;

import org.apache.logging.log4j.*;
import journeymap.common.*;
import journeymap.client.*;
import net.minecraft.client.resources.*;
import com.google.common.base.*;
import org.lwjgl.opengl.*;
import org.lwjgl.*;
import journeymap.client.model.*;
import journeymap.client.task.multi.*;
import journeymap.common.log.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import javax.annotation.*;
import java.awt.image.*;

@ParametersAreNonnullByDefault
public enum ColorManager
{
    INSTANCE;
    
    private Logger logger;
    private volatile ColorPalette currentPalette;
    private String lastResourcePackNames;
    private String lastModNames;
    private double lastPaletteVersion;
    private HashMap<String, float[]> iconColorCache;
    
    private ColorManager() {
        this.logger = Journeymap.getLogger();
        this.iconColorCache = new HashMap<String, float[]>();
    }
    
    public void reset() {
        this.lastResourcePackNames = null;
        this.lastModNames = null;
        this.lastPaletteVersion = 0.0;
        this.currentPalette = null;
        this.iconColorCache.clear();
    }
    
    public static String getResourcePackNames() {
        final List<ResourcePackRepository.Entry> entries = Constants.getResourcePacks();
        String packs;
        if (entries.isEmpty()) {
            packs = Constants.RESOURCE_PACKS_DEFAULT;
        }
        else {
            final ArrayList<String> entryStrings = new ArrayList<String>(entries.size());
            for (final ResourcePackRepository.Entry entry : entries) {
                entryStrings.add(entry.toString());
            }
            Collections.sort(entryStrings);
            packs = Joiner.on(", ").join((Iterable)entryStrings);
        }
        return packs;
    }
    
    public void ensureCurrent(boolean forceReset) {
        try {
            if (!Display.isCurrent()) {
                this.logger.error("ColorManager.ensureCurrent() must be called on main thread!");
            }
        }
        catch (LWJGLException e) {
            e.printStackTrace();
            return;
        }
        final String currentResourcePackNames = getResourcePackNames();
        final String currentModNames = Constants.getModNames();
        final double currentPaletteVersion = (this.currentPalette == null) ? 0.0 : this.currentPalette.getVersion();
        if (this.currentPalette != null && !forceReset) {
            if (!currentResourcePackNames.equals(this.lastResourcePackNames) && !this.iconColorCache.isEmpty()) {
                this.logger.debug("Resource Pack(s) changed: " + currentResourcePackNames);
                forceReset = true;
            }
            if (!currentModNames.equals(this.lastModNames)) {
                this.logger.debug("Mod Pack(s) changed: " + currentModNames);
                forceReset = true;
            }
            if (currentPaletteVersion != this.lastPaletteVersion) {
                this.logger.debug("Color Palette version changed: " + currentPaletteVersion);
                forceReset = true;
            }
        }
        if (forceReset || this.iconColorCache.isEmpty()) {
            this.logger.debug("Building color palette...");
            this.initBlockColors(forceReset);
        }
        this.lastModNames = currentModNames;
        this.lastResourcePackNames = currentResourcePackNames;
        this.lastPaletteVersion = ((this.currentPalette == null) ? 0.0 : this.currentPalette.getVersion());
    }
    
    public ColorPalette getCurrentPalette() {
        return this.currentPalette;
    }
    
    private void initBlockColors(final boolean forceReset) {
        try {
            final long start = System.currentTimeMillis();
            ColorPalette palette = ColorPalette.getActiveColorPalette();
            Collection<BlockMD> blockMDs;
            if (Journeymap.getClient().isMapping()) {
                blockMDs = BlockMD.getAllValid();
            }
            else {
                blockMDs = BlockMD.getAllMinecraft();
            }
            if (forceReset || palette == null) {
                this.logger.debug("Color palette update required.");
                this.iconColorCache.clear();
                blockMDs.forEach(BlockMD::clearColor);
            }
            boolean standard = true;
            boolean permanent = false;
            Label_0211: {
                if (palette != null) {
                    standard = palette.isStandard();
                    permanent = palette.isPermanent();
                    if (permanent && forceReset) {
                        this.logger.debug("Applying permanent palette colors before updating");
                    }
                    if (!permanent) {
                        if (forceReset) {
                            break Label_0211;
                        }
                    }
                    try {
                        final int count = palette.applyColors(blockMDs, true);
                        this.logger.debug(String.format("Loaded %d block colors from %s", count, palette.getOrigin()));
                    }
                    catch (Exception e) {
                        this.logger.warn(String.format("Could not load block colors from %s: %s", palette.getOrigin(), e));
                    }
                }
            }
            if (forceReset || palette == null) {
                palette = ColorPalette.create(standard, permanent);
            }
            this.currentPalette = palette;
            for (final BlockMD blockMD : blockMDs) {
                if (!blockMD.hasColor()) {
                    blockMD.getTextureColor();
                    this.currentPalette.applyColor(blockMD, true);
                }
                if (!blockMD.hasColor()) {
                    this.logger.warn("Could not derive color for " + blockMD.getBlockState());
                }
            }
            if (this.currentPalette.isDirty()) {
                final long elapsed = System.currentTimeMillis() - start;
                this.currentPalette.writeToFile();
                this.logger.info(String.format("Update color palette for %s blockstates in %sms: %s", this.currentPalette.size(), elapsed, this.currentPalette.getOrigin()));
            }
            else {
                final long elapsed = System.currentTimeMillis() - start;
                this.logger.info(String.format("Loaded color palette for %s blockstates in %sms", this.currentPalette.size(), elapsed));
            }
            MapPlayerTask.forceNearbyRemap();
        }
        catch (Throwable t) {
            this.logger.error("ColorManager.initBlockColors() encountered an unexpected error: " + LogFormatter.toPartialString(t));
        }
    }
    
    @Nullable
    public float[] getAverageColor(final Collection<ColoredSprite> sprites) {
        if (sprites == null || sprites.isEmpty()) {
            return null;
        }
        final List<String> names = sprites.stream().map((Function<? super ColoredSprite, ?>)ColoredSprite::getIconName).collect((Collector<? super Object, ?, List<String>>)Collectors.toList());
        Collections.sort(names);
        final String name = Joiner.on(",").join((Iterable)names);
        float[] rgba;
        if (this.iconColorCache.containsKey(name)) {
            rgba = this.iconColorCache.get(name);
        }
        else {
            rgba = this.calculateAverageColor(sprites);
            if (rgba != null) {
                this.iconColorCache.put(name, rgba);
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug(String.format("Cached color %s for %s", RGB.toHexString(RGB.toInteger(rgba)), name));
                }
            }
        }
        return rgba;
    }
    
    private float[] calculateAverageColor(final Collection<ColoredSprite> sprites) {
        final List<BufferedImage> images = new ArrayList<BufferedImage>(sprites.size());
        for (final ColoredSprite coloredSprite : sprites) {
            final BufferedImage img = coloredSprite.getColoredImage();
            if (img != null) {
                images.add(img);
            }
        }
        if (images.isEmpty()) {
            return null;
        }
        int count;
        int b;
        int g;
        int a;
        int r = a = (g = (b = (count = 0)));
        for (final BufferedImage image : images) {
            try {
                final int[] rgb2;
                final int[] argbInts = rgb2 = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
                for (final int argb : rgb2) {
                    final int alpha = argb >> 24 & 0xFF;
                    if (alpha > 0) {
                        ++count;
                        a += alpha;
                        r += (argb >> 16 & 0xFF);
                        g += (argb >> 8 & 0xFF);
                        b += (argb & 0xFF);
                    }
                }
            }
            catch (Exception e) {}
        }
        if (count > 0) {
            final int rgb = RGB.toInteger(r / count, g / count, b / count);
            return RGB.floats(rgb, a / count);
        }
        return null;
    }
}
