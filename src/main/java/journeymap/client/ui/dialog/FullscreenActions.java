package journeymap.client.ui.dialog;

import journeymap.client.ui.*;
import journeymap.common.*;
import java.awt.*;
import org.apache.logging.log4j.*;
import journeymap.common.log.*;
import java.io.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import journeymap.client.ui.fullscreen.*;
import java.net.*;
import journeymap.common.version.*;

public class FullscreenActions
{
    public static void open() {
        UIManager.INSTANCE.openFullscreenMap();
    }
    
    public static void showCaveLayers() {
        UIManager.INSTANCE.openFullscreenMap().showCaveLayers();
    }
    
    public static void launchLocalhost() {
        final String url = "http://localhost:" + Journeymap.getClient().getWebMapProperties().port.get();
        try {
            Desktop.getDesktop().browse(URI.create(url));
        }
        catch (IOException e) {
            Journeymap.getLogger().log(Level.ERROR, "Could not launch browser with URL: " + url + ": " + LogFormatter.toString(e));
        }
    }
    
    public static void launchPatreon() {
        final String url = "http://patreon.com/techbrew";
        try {
            Desktop.getDesktop().browse(URI.create(url));
        }
        catch (IOException e) {
            Journeymap.getLogger().log(Level.ERROR, "Could not launch browser with URL: " + url + ": " + LogFormatter.toString(e));
        }
    }
    
    public static void launchWebsite(final String path) {
        final String url = "http://journeymap.info/" + path;
        try {
            Desktop.getDesktop().browse(URI.create(url));
        }
        catch (Throwable e) {
            Journeymap.getLogger().error("Could not launch browser with URL: " + url, (Object)LogFormatter.toString(e));
        }
    }
    
    public static void openKeybindings() {
        UIManager.INSTANCE.closeAll();
        final Fullscreen fullscreen = UIManager.INSTANCE.openFullscreenMap();
        final Minecraft mc = Minecraft.func_71410_x();
        mc.func_147108_a((GuiScreen)new GuiControls((GuiScreen)fullscreen, mc.field_71474_y));
    }
    
    public static void tweet(final String message) {
        String path = null;
        try {
            path = "http://twitter.com/home/?status=@JourneyMapMod+" + URLEncoder.encode(message, "UTF-8");
            Desktop.getDesktop().browse(URI.create(path));
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }
    
    public static void discord() {
        String path = null;
        try {
            path = "https://discord.gg/eP8gE69";
            Desktop.getDesktop().browse(URI.create(path));
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }
    
    public static void launchDownloadWebsite() {
        final String url = VersionCheck.getDownloadUrl();
        try {
            Desktop.getDesktop().browse(URI.create(url));
        }
        catch (Throwable e) {
            Journeymap.getLogger().error("Could not launch browser with URL: " + url, (Object)LogFormatter.toString(e));
        }
    }
}
