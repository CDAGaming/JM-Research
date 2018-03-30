package journeymap.client.render.texture;

import net.minecraft.util.*;
import net.minecraft.client.*;
import java.util.function.*;
import journeymap.client.task.main.*;
import net.minecraft.client.renderer.texture.*;
import javax.imageio.*;
import journeymap.common.*;
import net.minecraft.client.resources.*;
import java.io.*;
import journeymap.client.ui.theme.*;
import journeymap.client.io.*;
import java.awt.image.*;
import java.awt.*;
import java.util.*;
import journeymap.common.thread.*;
import java.util.concurrent.*;

public class TextureCache
{
    public static final ResourceLocation GridCheckers;
    public static final ResourceLocation GridDots;
    public static final ResourceLocation GridSquares;
    public static final ResourceLocation ColorPicker;
    public static final ResourceLocation ColorPicker2;
    public static final ResourceLocation TileSampleDay;
    public static final ResourceLocation TileSampleNight;
    public static final ResourceLocation TileSampleUnderground;
    public static final ResourceLocation UnknownEntity;
    public static final ResourceLocation Deathpoint;
    public static final ResourceLocation MobDot;
    public static final ResourceLocation MobDot_Large;
    public static final ResourceLocation MobDotArrow;
    public static final ResourceLocation MobDotArrow_Large;
    public static final ResourceLocation MobDotChevron;
    public static final ResourceLocation MobDotChevron_Large;
    public static final ResourceLocation MobIconArrow;
    public static final ResourceLocation MobIconArrow_Large;
    public static final ResourceLocation PlayerArrow;
    public static final ResourceLocation PlayerArrowBG;
    public static final ResourceLocation PlayerArrow_Large;
    public static final ResourceLocation PlayerArrowBG_Large;
    public static final ResourceLocation Logo;
    public static final ResourceLocation MinimapSquare128;
    public static final ResourceLocation MinimapSquare256;
    public static final ResourceLocation MinimapSquare512;
    public static final ResourceLocation Patreon;
    public static final ResourceLocation Discord;
    public static final ResourceLocation Waypoint;
    public static final ResourceLocation WaypointEdit;
    public static final ResourceLocation WaypointOffscreen;
    public static final Map<String, TextureImpl> playerSkins;
    public static final Map<String, TextureImpl> themeImages;
    private static ThreadPoolExecutor texExec;
    
    private static ResourceLocation uiImage(final String fileName) {
        return new ResourceLocation("journeymap", "ui/img/" + fileName);
    }
    
    public static TextureImpl getTexture(final ResourceLocation location) {
        if (location == null) {
            return null;
        }
        final TextureManager textureManager = Minecraft.func_71410_x().func_110434_K();
        ITextureObject textureObject = textureManager.func_110581_b(location);
        if (textureObject == null || !(textureObject instanceof TextureImpl)) {
            textureObject = (ITextureObject)new TextureImpl(location);
            final boolean loaded = textureManager.func_110579_a(location, textureObject);
            if (!loaded) {
                textureObject = null;
            }
        }
        return (TextureImpl)textureObject;
    }
    
    public static <T extends TextureImpl> Future<T> scheduleTextureTask(final Callable<T> textureTask) {
        return TextureCache.texExec.submit(textureTask);
    }
    
    public static void reset() {
        TextureCache.playerSkins.clear();
        Arrays.asList(TextureCache.ColorPicker, TextureCache.ColorPicker2, TextureCache.Deathpoint, TextureCache.GridCheckers, TextureCache.GridDots, TextureCache.GridSquares, TextureCache.Logo, TextureCache.MinimapSquare128, TextureCache.MinimapSquare256, TextureCache.MinimapSquare512, TextureCache.MobDot, TextureCache.MobDot_Large, TextureCache.MobDotArrow, TextureCache.MobDotArrow_Large, TextureCache.MobDotChevron, TextureCache.MobDotChevron_Large, TextureCache.MobIconArrow_Large, TextureCache.Patreon, TextureCache.PlayerArrow, TextureCache.PlayerArrow_Large, TextureCache.PlayerArrowBG, TextureCache.PlayerArrowBG, TextureCache.TileSampleDay, TextureCache.TileSampleNight, TextureCache.TileSampleUnderground, TextureCache.UnknownEntity, TextureCache.Waypoint, TextureCache.WaypointEdit, TextureCache.WaypointOffscreen).stream().map((Function<? super Object, ?>)TextureCache::getTexture);
        Arrays.asList(TextureCache.ColorPicker, TextureCache.ColorPicker2, TextureCache.GridCheckers, TextureCache.GridDots, TextureCache.GridSquares, TextureCache.TileSampleDay, TextureCache.TileSampleNight, TextureCache.TileSampleUnderground, TextureCache.UnknownEntity).stream().map((Function<? super Object, ?>)TextureCache::getTexture);
    }
    
    public static void purgeThemeImages(final Map<String, TextureImpl> themeImages) {
        synchronized (themeImages) {
            ExpireTextureTask.queue(themeImages.values());
            themeImages.clear();
        }
    }
    
    public static BufferedImage resolveImage(final ResourceLocation location) {
        if (location.func_110624_b().equals("fake")) {
            return null;
        }
        final IResourceManager resourceManager = Minecraft.func_71410_x().func_110442_L();
        try {
            final IResource resource = resourceManager.func_110536_a(location);
            final InputStream is = resource.func_110527_b();
            return TextureUtil.func_177053_a(is);
        }
        catch (FileNotFoundException e) {
            if ("journeymap".equals(location.func_110624_b())) {
                final File imgFile = new File("../src/main/resources/assets/journeymap/" + location.func_110623_a());
                if (imgFile.exists()) {
                    try {
                        return ImageIO.read(imgFile);
                    }
                    catch (IOException ex) {}
                }
            }
            Journeymap.getLogger().warn("Image not found: " + e.getMessage());
            return null;
        }
        catch (Exception e2) {
            Journeymap.getLogger().warn("Resource not readable with TextureUtil.readBufferedImage(): " + location);
            return null;
        }
    }
    
    public static TextureImpl getThemeTexture(final Theme theme, final String iconPath) {
        return getSizedThemeTexture(theme, iconPath, 0, 0, false, 1.0f, false);
    }
    
    public static TextureImpl getSizedThemeTexture(final Theme theme, final String iconPath, final int width, final int height, final boolean resize, final float alpha, final boolean retainImage) {
        final String texName = String.format("%s/%s", theme.directory, iconPath);
        synchronized (TextureCache.themeImages) {
            TextureImpl tex = TextureCache.themeImages.get(texName);
            if (tex == null || tex.retainImage != retainImage || (!tex.hasImage() && tex.retainImage) || (resize && (width != tex.width || height != tex.height)) || tex.alpha != alpha) {
                final File parentDir = ThemeLoader.getThemeIconDir();
                BufferedImage img = FileHandler.getIconFromFile(parentDir, theme.directory, iconPath);
                if (img == null) {
                    final String resourcePath = String.format("theme/%s/%s", theme.directory, iconPath);
                    img = resolveImage(new ResourceLocation("journeymap", resourcePath));
                }
                if (img == null) {
                    Journeymap.getLogger().error("Unknown theme image: " + texName);
                    return getTexture(TextureCache.UnknownEntity);
                }
                if ((resize || alpha < 1.0f) && (alpha < 1.0f || img.getWidth() != width || img.getHeight() != height)) {
                    final BufferedImage tmp = new BufferedImage(width, height, img.getType());
                    final Graphics2D g = tmp.createGraphics();
                    g.setComposite(AlphaComposite.getInstance(3, alpha));
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.drawImage(img, 0, 0, width, height, null);
                    g.dispose();
                    img = tmp;
                }
                if (tex != null) {
                    tex.queueForDeletion();
                }
                tex = new TextureImpl(img, retainImage);
                tex.alpha = alpha;
                TextureCache.themeImages.put(texName, tex);
            }
            return tex;
        }
    }
    
    public static TextureImpl getScaledCopy(final String texName, final TextureImpl original, final int width, final int height, final float alpha) {
        synchronized (TextureCache.themeImages) {
            TextureImpl tex = TextureCache.themeImages.get(texName);
            if (tex == null || (!tex.hasImage() && tex.retainImage) || width != tex.width || height != tex.height || tex.alpha != alpha) {
                BufferedImage img = original.getImage();
                if (img == null) {
                    Journeymap.getLogger().error("Unable to get scaled image: " + texName);
                    return getTexture(TextureCache.UnknownEntity);
                }
                if (alpha < 1.0f || img.getWidth() != width || img.getHeight() != height) {
                    final BufferedImage tmp = new BufferedImage(width, height, img.getType());
                    final Graphics2D g = tmp.createGraphics();
                    g.setComposite(AlphaComposite.getInstance(3, alpha));
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.drawImage(img, 0, 0, width, height, null);
                    g.dispose();
                    img = tmp;
                }
                if (tex != null) {
                    tex.queueForDeletion();
                }
                tex = new TextureImpl(img);
                tex.alpha = alpha;
                TextureCache.themeImages.put(texName, tex);
            }
            return tex;
        }
    }
    
    public static TextureImpl getPlayerSkin(final String username) {
        TextureImpl tex = null;
        synchronized (TextureCache.playerSkins) {
            tex = TextureCache.playerSkins.get(username);
            if (tex != null) {
                return tex;
            }
            final BufferedImage blank = new BufferedImage(24, 24, 2);
            tex = new TextureImpl(null, blank, true, false);
            TextureCache.playerSkins.put(username, tex);
        }
        final TextureImpl playerSkinTex = tex;
        final BufferedImage img;
        final TextureImpl textureImpl;
        TextureCache.texExec.submit(() -> {
            img = IgnSkin.downloadSkin(username);
            if (img != null) {
                textureImpl.setImage(img, true);
            }
            else {
                Journeymap.getLogger().warn("Couldn't get a skin at all for " + username);
            }
            return null;
        });
        return playerSkinTex;
    }
    
    static {
        GridCheckers = uiImage("grid-checkers.png");
        GridDots = uiImage("grid-dots.png");
        GridSquares = uiImage("grid.png");
        ColorPicker = uiImage("colorpick.png");
        ColorPicker2 = uiImage("colorpick2.png");
        TileSampleDay = uiImage("tile-sample-day.png");
        TileSampleNight = uiImage("tile-sample-night.png");
        TileSampleUnderground = uiImage("tile-sample-underground.png");
        UnknownEntity = uiImage("unknown.png");
        Deathpoint = uiImage("waypoint-death.png");
        MobDot = uiImage("marker-dot-16.png");
        MobDot_Large = uiImage("marker-dot-32.png");
        MobDotArrow = uiImage("marker-dot-arrow-16.png");
        MobDotArrow_Large = uiImage("marker-dot-arrow-32.png");
        MobDotChevron = uiImage("marker-chevron-16.png");
        MobDotChevron_Large = uiImage("marker-chevron-32.png");
        MobIconArrow = uiImage("marker-icon-arrow-16.png");
        MobIconArrow_Large = uiImage("marker-icon-arrow-32.png");
        PlayerArrow = uiImage("marker-player-16.png");
        PlayerArrowBG = uiImage("marker-player-bg-16.png");
        PlayerArrow_Large = uiImage("marker-player-32.png");
        PlayerArrowBG_Large = uiImage("marker-player-bg-32.png");
        Logo = uiImage("ico/journeymap.png");
        MinimapSquare128 = uiImage("minimap/minimap-square-128.png");
        MinimapSquare256 = uiImage("minimap/minimap-square-256.png");
        MinimapSquare512 = uiImage("minimap/minimap-square-512.png");
        Patreon = uiImage("patreon.png");
        Discord = uiImage("discord.png");
        Waypoint = uiImage("waypoint.png");
        WaypointEdit = uiImage("waypoint-edit.png");
        WaypointOffscreen = uiImage("waypoint-offscreen.png");
        playerSkins = Collections.synchronizedMap(new HashMap<String, TextureImpl>());
        themeImages = Collections.synchronizedMap(new HashMap<String, TextureImpl>());
        TextureCache.texExec = new ThreadPoolExecutor(2, 4, 15L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(8), new JMThreadFactory("texture"), new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
