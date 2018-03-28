package journeymap.client.log;

import journeymap.client.Constants;
import journeymap.client.forge.event.KeyEventHandler;
import journeymap.client.service.WebServer;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.version.VersionCheck;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ChatLog {
    static final List<TextComponentTranslation> announcements;
    public static boolean enableAnnounceMod;
    private static boolean initialized;

    static {
        announcements = Collections.synchronizedList(new LinkedList<TextComponentTranslation>());
        ChatLog.enableAnnounceMod = false;
        ChatLog.initialized = false;
    }

    public static void queueAnnouncement(final ITextComponent chat) {
        final TextComponentTranslation wrap = new TextComponentTranslation("jm.common.chat_announcement", new Object[]{chat});
        ChatLog.announcements.add(wrap);
    }

    public static void announceURL(final String message, final String url) {
        final TextComponentString chat = new TextComponentString(message);
        chat.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        chat.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (ITextComponent) new TextComponentString(url)));
        queueAnnouncement((ITextComponent) chat);
    }

    public static void announceFile(final String message, final File file) {
        final TextComponentString chat = new TextComponentString(message);
        try {
            chat.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getCanonicalPath()));
            chat.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (ITextComponent) new TextComponentString(file.getCanonicalPath())));
        } catch (Exception e) {
            Journeymap.getLogger().warn("Couldn't build ClickEvent for file: " + LogFormatter.toString(e));
        }
        queueAnnouncement((ITextComponent) chat);
    }

    public static void announceI18N(final String key, final Object... parms) {
        final String text = Constants.getString(key, parms);
        final TextComponentString chat = new TextComponentString(text);
        queueAnnouncement((ITextComponent) chat);
    }

    public static void announceError(final String text) {
        final ErrorChat chat = new ErrorChat(text);
        queueAnnouncement((ITextComponent) chat);
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
                    mc.ingameGUI.getChatGUI().printChatMessage((ITextComponent) message);
                } catch (Exception e) {
                    Journeymap.getLogger().error("Could not display announcement in chat: " + LogFormatter.toString(e));
                } finally {
                    final Level logLevel = (message.getFormatArgs()[0] instanceof ErrorChat) ? Level.ERROR : Level.INFO;
                    Journeymap.getLogger().log(logLevel, StringUtils.stripControlCodes(message.getUnformattedComponentText()));
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
                } catch (Throwable t) {
                    Journeymap.getLogger().error("Couldn't check webserver: " + LogFormatter.toString(t));
                }
            } else {
                announceI18N("jm.common.mapgui_only_ready", keyName);
            }
            if (!Journeymap.getClient().getCoreProperties().mappingEnabled.get()) {
                announceI18N("jm.common.enable_mapping_false_text", new Object[0]);
            }
            ChatLog.enableAnnounceMod = false;
        }
    }

    private static class ErrorChat extends TextComponentString {
        public ErrorChat(final String text) {
            super(text);
        }
    }
}
