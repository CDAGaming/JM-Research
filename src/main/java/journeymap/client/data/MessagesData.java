package journeymap.client.data;

import com.google.common.cache.*;
import journeymap.client.*;
import net.minecraftforge.fml.client.*;
import journeymap.client.io.*;
import com.google.common.collect.*;
import java.util.concurrent.*;
import java.util.*;

public class MessagesData extends CacheLoader<Class, Map<String, Object>>
{
    private static final String KEY_PREFIX = "jm.webmap.";
    
    public Map<String, Object> load(final Class aClass) throws Exception {
        final HashMap<String, Object> props = new HashMap<String, Object>();
        props.put("locale", Constants.getLocale());
        props.put("lang", FMLClientHandler.instance().getClient().field_71474_y.field_74363_ab);
        final Properties properties = FileHandler.getLangFile("en_US.lang");
        if (properties != null) {
            final Enumeration<Object> allKeys = ((Hashtable<Object, V>)properties).keys();
            while (allKeys.hasMoreElements()) {
                final String key = allKeys.nextElement();
                if (key.startsWith("jm.webmap.")) {
                    final String name = key.split("jm.webmap.")[1];
                    final String value = Constants.getString(key);
                    props.put(name, value);
                }
            }
        }
        return (Map<String, Object>)ImmutableMap.copyOf((Map)props);
    }
    
    public long getTTL() {
        return TimeUnit.DAYS.toMillis(1L);
    }
}
