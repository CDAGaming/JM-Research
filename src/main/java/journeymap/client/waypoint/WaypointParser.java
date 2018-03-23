package journeymap.client.waypoint;

import java.util.List;
import java.util.regex.*;
import org.apache.commons.lang3.*;
import journeymap.client.model.*;
import journeymap.common.*;
import net.minecraft.util.math.*;
import java.awt.*;
import net.minecraftforge.client.event.*;
import net.minecraft.util.text.event.*;
import java.util.*;
import net.minecraft.util.text.*;

public class WaypointParser
{
    public static String[] QUOTES;
    public static Pattern PATTERN;
    
    public static List<String> getWaypointStrings(final String line) {
        List<String> list = null;
        final String[] candidates = StringUtils.substringsBetween(line, "[", "]");
        if (candidates != null) {
            for (final String candidate : candidates) {
                if (WaypointParser.PATTERN.matcher(candidate).find() && parse(candidate) != null) {
                    if (list == null) {
                        list = new ArrayList<String>(1);
                    }
                    list.add("[" + candidate + "]");
                }
            }
        }
        return list;
    }
    
    public static List<Waypoint> getWaypoints(final String line) {
        List<Waypoint> list = null;
        final String[] candidates = StringUtils.substringsBetween(line, "[", "]");
        if (candidates != null) {
            for (final String candidate : candidates) {
                if (WaypointParser.PATTERN.matcher(candidate).find()) {
                    final Waypoint waypoint = parse(candidate);
                    if (waypoint != null) {
                        if (list == null) {
                            list = new ArrayList<Waypoint>(1);
                        }
                        list.add(waypoint);
                    }
                }
            }
        }
        return list;
    }
    
    public static Waypoint parse(final String original) {
        String[] quotedVals = null;
        String raw = original.replaceAll("[\\[\\]]", "");
        for (final String quoteChar : WaypointParser.QUOTES) {
            if (raw.contains(quoteChar)) {
                quotedVals = StringUtils.substringsBetween(raw, quoteChar, quoteChar);
                if (quotedVals != null) {
                    for (int i = 0; i < quotedVals.length; ++i) {
                        final String val = quotedVals[i];
                        raw = raw.replaceAll(quoteChar + val + quoteChar, "__TEMP_" + i);
                    }
                }
            }
        }
        Integer x = null;
        Integer y = 63;
        Integer z = null;
        Integer dim = 0;
        String name = null;
        for (final String part : raw.split(",")) {
            if (part.contains(":")) {
                final String[] prop = part.split(":");
                if (prop.length == 2) {
                    final String key = prop[0].trim().toLowerCase();
                    final String val2 = prop[1].trim();
                    try {
                        if ("x".equals(key)) {
                            x = Integer.parseInt(val2);
                        }
                        else if ("y".equals(key)) {
                            y = Math.max(0, Math.min(255, Integer.parseInt(val2)));
                        }
                        else if ("z".equals(key)) {
                            z = Integer.parseInt(val2);
                        }
                        else if ("dim".equals(key)) {
                            dim = Integer.parseInt(val2);
                        }
                        else if ("name".equals(key)) {
                            name = val2;
                        }
                    }
                    catch (Exception e) {
                        Journeymap.getLogger().warn("Bad format in waypoint text part: " + part + ": " + e);
                    }
                }
            }
        }
        if (x != null && z != null) {
            if (name != null && quotedVals != null) {
                for (int j = 0; j < quotedVals.length; ++j) {
                    final String val3 = quotedVals[j];
                    name = name.replaceAll("__TEMP_" + j, val3);
                }
            }
            if (name == null) {
                name = String.format("%s,%s", x, z);
            }
            final Random r = new Random();
            final Waypoint waypoint = new Waypoint(name, new BlockPos((int)x, (int)y, (int)z), new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)), Waypoint.Type.Normal, dim);
            return waypoint;
        }
        return null;
    }
    
    public static void parseChatForWaypoints(final ClientChatReceivedEvent event, final String unformattedText) {
        final List<String> matches = getWaypointStrings(unformattedText);
        if (matches != null) {
            boolean changed = false;
            if (event.getMessage() instanceof TextComponentTranslation) {
                final Object[] formatArgs = ((TextComponentTranslation)event.getMessage()).getFormatArgs();
                for (int i = 0; i < formatArgs.length && !matches.isEmpty(); ++i) {
                    if (formatArgs[i] instanceof ITextComponent) {
                        final ITextComponent arg = (ITextComponent)formatArgs[i];
                        final ITextComponent result = addWaypointMarkup(arg.getUnformattedText(), matches);
                        if (result != null) {
                            formatArgs[i] = result;
                            changed = true;
                        }
                    }
                    else if (formatArgs[i] instanceof String) {
                        final String arg2 = (String)formatArgs[i];
                        final ITextComponent result = addWaypointMarkup(arg2, matches);
                        if (result != null) {
                            formatArgs[i] = result;
                            changed = true;
                        }
                    }
                }
                if (changed) {
                    event.setMessage((ITextComponent)new TextComponentTranslation(((TextComponentTranslation)event.getMessage()).getKey(), formatArgs));
                }
            }
            else if (event.getMessage() instanceof TextComponentString) {
                final ITextComponent result2 = addWaypointMarkup(event.getMessage().getUnformattedText(), matches);
                if (result2 != null) {
                    event.setMessage(result2);
                    changed = true;
                }
            }
            else {
                Journeymap.getLogger().warn("No implementation for handling waypoints in ITextComponent " + event.getMessage().getClass());
            }
            if (!changed) {
                Journeymap.getLogger().warn(String.format("Matched waypoint in chat but failed to update message for %s : %s\n%s", event.getMessage().getClass(), event.getMessage().getFormattedText(), ITextComponent.Serializer.componentToJson(event.getMessage())));
            }
        }
    }
    
    private static ITextComponent addWaypointMarkup(final String text, final List<String> matches) {
        final List<ITextComponent> newParts = new ArrayList<ITextComponent>();
        int index = 0;
        boolean matched = false;
        final Iterator<String> iterator = matches.iterator();
        while (iterator.hasNext()) {
            final String match = iterator.next();
            if (text.contains(match)) {
                final int start = text.indexOf(match);
                if (start > index) {
                    newParts.add((ITextComponent)new TextComponentString(text.substring(index, start)));
                }
                matched = true;
                final TextComponentString clickable = new TextComponentString(match);
                final Style chatStyle = clickable.getStyle();
                chatStyle.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/jm wpedit " + match));
                final TextComponentString hover = new TextComponentString("JourneyMap: ");
                hover.getStyle().setColor(TextFormatting.YELLOW);
                final TextComponentString hover2 = new TextComponentString("Click to create Waypoint.\nCtrl+Click to view on map.");
                hover2.getStyle().setColor(TextFormatting.AQUA);
                hover.appendSibling((ITextComponent)hover2);
                chatStyle.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (ITextComponent)hover));
                chatStyle.setColor(TextFormatting.AQUA);
                newParts.add((ITextComponent)clickable);
                index = start + match.length();
                iterator.remove();
            }
        }
        if (!matched) {
            return null;
        }
        if (index < text.length() - 1) {
            newParts.add((ITextComponent)new TextComponentString(text.substring(index, text.length())));
        }
        if (!newParts.isEmpty()) {
            final TextComponentString replacement = new TextComponentString("");
            for (final ITextComponent sib : newParts) {
                replacement.appendSibling(sib);
            }
            return (ITextComponent)replacement;
        }
        return null;
    }
    
    static {
        WaypointParser.QUOTES = new String[] { "'", "\"" };
        WaypointParser.PATTERN = Pattern.compile("(\\w+\\s*:\\s*-?[\\w\\d\\s'\"]+,\\s*)+(\\w+\\s*:\\s*-?[\\w\\d\\s'\"]+)", 2);
    }
}
