package journeymap.client.log;

import net.minecraft.util.text.*;
import net.minecraft.util.text.event.*;
import java.io.*;
import journeymap.common.*;
import journeymap.common.log.*;
import journeymap.client.*;
import net.minecraft.client.*;
import journeymap.common.version.*;
import org.apache.logging.log4j.*;
import net.minecraft.util.*;
import journeymap.client.forge.event.*;
import journeymap.client.service.*;
import java.util.*;

public class ChatLog
{
    static final List<TextComponentTranslation> announcements;
    public static boolean enableAnnounceMod;
    private static boolean initialized;
    
    public static void queueAnnouncement(final ITextComponent chat) {
        final TextComponentTranslation wrap = new TextComponentTranslation("jm.common.chat_announcement", new Object[] { chat });
        ChatLog.announcements.add(wrap);
    }
    
    public static void announceURL(final String message, final String url) {
        final TextComponentString chat = new TextComponentString(message);
        chat.func_150256_b().func_150241_a(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        chat.func_150256_b().func_150209_a(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (ITextComponent)new TextComponentString(url)));
        queueAnnouncement((ITextComponent)chat);
    }
    
    public static void announceFile(final String message, final File file) {
        final TextComponentString chat = new TextComponentString(message);
        try {
            chat.func_150256_b().func_150241_a(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getCanonicalPath()));
            chat.func_150256_b().func_150209_a(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (ITextComponent)new TextComponentString(file.getCanonicalPath())));
        }
        catch (Exception e) {
            Journeymap.getLogger().warn("Couldn't build ClickEvent for file: " + LogFormatter.toString(e));
        }
        queueAnnouncement((ITextComponent)chat);
    }
    
    public static void announceI18N(final String key, final Object... parms) {
        final String text = Constants.getString(key, parms);
        final TextComponentString chat = new TextComponentString(text);
        queueAnnouncement((ITextComponent)chat);
    }
    
    public static void announceError(final String text) {
        final ErrorChat chat = new ErrorChat(text);
        queueAnnouncement((ITextComponent)chat);
    }
    
    public static void showChatAnnouncements(final Minecraft mc) {
        if (!ChatLog.initialized) {
            ChatLog.enableAnnounceMod = Journeymap.getClient().getCoreProperties().announceMod.get();
            if (ChatLog.enableAnnounceMod) {
                announceMod();
            }
            VersionCheck.getVersionIsCurrent();
            ChatLog.initialized = true;
        }
        while (!ChatLog.announcements.isEmpty()) {
            final TextComponentTranslation message = ChatLog.announcements.remove(0);
            if (message != null) {
                try {
                    mc.field_71456_v.func_146158_b().func_146227_a((ITextComponent)message);
                }
                catch (Exception e) {
                    Journeymap.getLogger().error("Could not display announcement in chat: " + LogFormatter.toString(e));
                }
                finally {
                    final Level logLevel = (message.func_150271_j()[0] instanceof ErrorChat) ? Level.ERROR : Level.INFO;
                    Journeymap.getLogger().log(logLevel, StringUtils.func_76338_a(message.func_150261_e()));
                }
            }
        }
    }
    
    public static void announceMod() {
        if (ChatLog.enableAnnounceMod) {
            final String keyName = KeyEventHandler.INSTANCE.kbFullscreenToggle.getDisplayName();
            if (Journeymap.getClient().getWebMapProperties().enabled.get()) {
                try {
                    final WebServer webServer = Journeymap.getClient().getJmServer();
                    final String port = (webServer.getPort() == 80) ? "" : (":" + Integer.toString(webServer.getPort()));
                    final String message = Constants.getString("jm.common.webserver_and_mapgui_ready", keyName, port);
                    announceURL(message, "http://localhost" + port);
                }
                catch (Throwable t) {
                    Journeymap.getLogger().error("Couldn't check webserver: " + LogFormatter.toString(t));
                }
            }
            else {
                announceI18N("jm.common.mapgui_only_ready", keyName);
            }
            if (!Journeymap.getClient().getCoreProperties().mappingEnabled.get()) {
                announceI18N("jm.common.enable_mapping_false_text", new Object[0]);
            }
            ChatLog.enableAnnounceMod = false;
        }
    }
    
    static {
        announcements = Collections.synchronizedList(new LinkedList<TextComponentTranslation>());
        ChatLog.enableAnnounceMod = false;
        ChatLog.initialized = false;
    }
    
    private static class ErrorChat extends TextComponentString
    {
        public ErrorChat(final String text) {
            super(text);
        }
    }
}
