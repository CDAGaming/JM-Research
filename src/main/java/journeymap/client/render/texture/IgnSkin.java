package journeymap.client.render.texture;

import net.minecraft.util.*;
import journeymap.common.*;
import java.net.*;
import net.minecraft.client.*;
import javax.imageio.*;
import journeymap.client.io.*;
import java.awt.image.*;
import java.awt.*;

public class IgnSkin
{
    private static String SKINS;
    private static String DEFAULT;
    
    public static BufferedImage downloadSkin(final String username) {
        BufferedImage img = null;
        final HttpURLConnection conn = null;
        try {
            String skinPath = String.format(IgnSkin.SKINS, StringUtils.func_76338_a(username));
            img = downloadImage(new URL(skinPath));
            if (img == null) {
                skinPath = String.format(IgnSkin.SKINS, IgnSkin.DEFAULT);
                img = downloadImage(new URL(skinPath));
            }
        }
        catch (Throwable e) {
            Journeymap.getLogger().warn("Error getting skin image for " + username + ": " + e.getMessage());
        }
        return img;
    }
    
    private static BufferedImage downloadImage(final URL imageURL) {
        BufferedImage img = null;
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection)imageURL.openConnection(Minecraft.func_71410_x().func_110437_J());
            HttpURLConnection.setFollowRedirects(true);
            conn.setInstanceFollowRedirects(true);
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.connect();
            if (conn.getResponseCode() / 100 == 2) {
                final BufferedImage fullImage = ImageIO.read(conn.getInputStream());
                final BufferedImage face = fullImage.getSubimage(8, 8, 8, 8);
                if (fullImage.getColorModel().hasAlpha()) {
                    final Graphics2D g = RegionImageHandler.initRenderingHints(face.createGraphics());
                    final BufferedImage hat = fullImage.getSubimage(40, 8, 8, 8);
                    g.drawImage(hat, 0, 0, 8, 8, null);
                    g.dispose();
                }
                img = new BufferedImage(24, 24, face.getType());
                final Graphics2D g = RegionImageHandler.initRenderingHints(img.createGraphics());
                g.drawImage(face, 0, 0, 24, 24, null);
                g.dispose();
            }
            else {
                Journeymap.getLogger().debug("Bad Response getting image: " + imageURL + " : " + conn.getResponseCode());
            }
        }
        catch (Throwable e) {
            Journeymap.getLogger().error("Error getting skin image: " + imageURL + " : " + e.getMessage());
        }
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return img;
    }
    
    static {
        IgnSkin.SKINS = "http://skins.minecraft.net/MinecraftSkins/%s.png";
        IgnSkin.DEFAULT = "Herobrine";
    }
}
