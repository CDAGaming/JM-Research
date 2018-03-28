package journeymap.client.data;

import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableMap;
import journeymap.client.Constants;
import journeymap.client.io.FileHandler;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class MessagesData extends CacheLoader<Class, Map<String, Object>> {
    private static final String KEY_PREFIX = "jm.webmap.";

    public Map<String, Object> load(final Class aClass) throws Exception {
        final HashMap<String, Object> props = new HashMap<String, Object>();
        props.put("locale", Constants.getLocale());
        props.put("lang", FMLClientHandler.instance().getClient().gameSettings.language);
        final Properties properties = FileHandler.getLangFile("en_US.lang");
        if (properties != null) {
            final Enumeration<Object> allKeys = (properties).keys();
            while (allKeys.hasMoreElements()) {
                final String key = (String) allKeys.nextElement();
                if (key.startsWith("jm.webmap.")) {
                    final String name = key.split("jm.webmap.")[1];
                    final String value = Constants.getString(key);
                    props.put(name, value);
                }
            }
        }
        return (Map<String, Object>) ImmutableMap.copyOf((Map) props);
    }

    public long getTTL() {
        return TimeUnit.DAYS.toMillis(1L);
    }
}
