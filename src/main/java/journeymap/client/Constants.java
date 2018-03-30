package journeymap.client;

import com.google.common.collect.*;
import com.google.common.base.*;
import net.minecraftforge.fml.client.*;
import journeymap.common.*;
import net.minecraft.client.resources.*;
import journeymap.common.log.*;
import net.minecraftforge.fml.common.*;
import java.util.*;
import java.io.*;

public class Constants
{
    public static final Ordering<String> CASE_INSENSITIVE_NULL_SAFE_ORDER;
    public static final TimeZone GMT;
    private static final Joiner path;
    private static final String END;
    public static String JOURNEYMAP_DIR;
    public static String CONFIG_DIR_LEGACY;
    public static String CONFIG_DIR;
    public static String DATA_DIR;
    public static String SP_DATA_DIR;
    public static String MP_DATA_DIR;
    public static String RESOURCE_PACKS_DEFAULT;
    private static String ICON_DIR;
    public static String ENTITY_ICON_DIR;
    public static String WAYPOINT_ICON_DIR;
    public static String THEME_ICON_DIR;
    
    public static Locale getLocale() {
        Locale locale = Locale.getDefault();
        try {
            final String lang = FMLClientHandler.instance().getClient().func_135016_M().func_135041_c().func_135034_a();
            locale = new Locale(lang);
        }
        catch (Exception e) {
            Journeymap.getLogger().warn("Couldn't determine locale from game settings, defaulting to " + locale);
        }
        return locale;
    }
    
    public static String getString(final String key) {
        if (FMLClientHandler.instance().getClient() == null) {
            return key;
        }
        try {
            final String result = I18n.func_135052_a(key, new Object[0]);
            if (result.equals(key)) {
                Journeymap.getLogger().warn("Message key not found: " + key);
            }
            return result;
        }
        catch (Throwable t) {
            Journeymap.getLogger().warn(String.format("Message key '%s' threw exception: %s", key, t.getMessage()));
            return key;
        }
    }
    
    public static String getString(final String key, final Object... params) {
        if (FMLClientHandler.instance().getClient() == null) {
            return String.format("%s (%s)", key, Joiner.on(",").join(params));
        }
        try {
            final String result = I18n.func_135052_a(key, params);
            if (result.equals(key)) {
                Journeymap.getLogger().warn("Message key not found: " + key);
            }
            return result;
        }
        catch (Throwable t) {
            Journeymap.getLogger().warn(String.format("Message key '%s' threw exception: %s", key, t.getMessage()));
            return key;
        }
    }
    
    public static boolean safeEqual(final String first, final String second) {
        final int result = Constants.CASE_INSENSITIVE_NULL_SAFE_ORDER.compare((Object)first, (Object)second);
        return result == 0 && Constants.CASE_INSENSITIVE_NULL_SAFE_ORDER.compare((Object)first, (Object)second) == 0;
    }
    
    public static List<ResourcePackRepository.Entry> getResourcePacks() {
        final ArrayList<ResourcePackRepository.Entry> entries = new ArrayList<ResourcePackRepository.Entry>();
        try {
            final ResourcePackRepository resourcepackrepository = FMLClientHandler.instance().getClient().func_110438_M();
            entries.addAll(resourcepackrepository.func_110613_c());
        }
        catch (Throwable t) {
            Journeymap.getLogger().error(String.format("Can't get resource pack names: %s", LogFormatter.toString(t)));
        }
        return entries;
    }
    
    public static String getModNames() {
        final ArrayList<String> list = new ArrayList<String>();
        for (final ModContainer mod : Loader.instance().getActiveModList()) {
            if (Loader.isModLoaded(mod.getModId())) {
                list.add(String.format("%s:%s", mod.getName(), mod.getVersion()));
            }
        }
        Collections.sort(list);
        return Joiner.on(", ").join((Iterable)list);
    }
    
    public static String birthdayMessage() {
        final Calendar today = Calendar.getInstance();
        final int month = today.get(2);
        final int date = today.get(5);
        if (month == 6 && date == 2) {
            return getString("jm.common.birthday", "techbrew");
        }
        if (month == 8 && date == 21) {
            return getString("jm.common.birthday", "mysticdrew");
        }
        return null;
    }
    
    static {
        CASE_INSENSITIVE_NULL_SAFE_ORDER = Ordering.from((Comparator)String.CASE_INSENSITIVE_ORDER).nullsLast();
        GMT = TimeZone.getTimeZone("GMT");
        path = Joiner.on(File.separator).useForNull("");
        END = null;
        Constants.JOURNEYMAP_DIR = "journeymap";
        Constants.CONFIG_DIR_LEGACY = Constants.path.join((Object)Constants.JOURNEYMAP_DIR, (Object)"config", new Object[0]);
        Constants.CONFIG_DIR = Constants.path.join((Object)Constants.JOURNEYMAP_DIR, (Object)"config", new Object[] { Journeymap.JM_VERSION.toMajorMinorString(), Constants.END });
        Constants.DATA_DIR = Constants.path.join((Object)Constants.JOURNEYMAP_DIR, (Object)"data", new Object[0]);
        Constants.SP_DATA_DIR = Constants.path.join((Object)Constants.DATA_DIR, (Object)WorldType.sp, new Object[] { Constants.END });
        Constants.MP_DATA_DIR = Constants.path.join((Object)Constants.DATA_DIR, (Object)WorldType.mp, new Object[] { Constants.END });
        Constants.RESOURCE_PACKS_DEFAULT = "Default";
        Constants.ICON_DIR = Constants.path.join((Object)Constants.JOURNEYMAP_DIR, (Object)"icon", new Object[0]);
        Constants.ENTITY_ICON_DIR = Constants.path.join((Object)Constants.ICON_DIR, (Object)"entity", new Object[] { Constants.END });
        Constants.WAYPOINT_ICON_DIR = Constants.path.join((Object)Constants.ICON_DIR, (Object)"waypoint", new Object[] { Constants.END });
        Constants.THEME_ICON_DIR = Constants.path.join((Object)Constants.ICON_DIR, (Object)"theme", new Object[] { Constants.END });
    }
    
    public enum WorldType
    {
        mp, 
        sp;
    }
}
