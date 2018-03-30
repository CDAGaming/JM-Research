package journeymap.client.waypoint;

import javax.annotation.*;
import com.google.gson.*;
import journeymap.client.io.*;
import java.io.*;
import java.nio.charset.*;
import com.google.common.io.*;
import journeymap.common.*;
import journeymap.common.log.*;
import java.util.*;
import journeymap.client.api.display.*;
import com.google.common.cache.*;
import journeymap.client.*;

@ParametersAreNonnullByDefault
public enum WaypointGroupStore
{
    INSTANCE;
    
    public static final WaypointGroup DEFAULT;
    public final Gson GSON;
    public static final String KEY_PATTERN = "%s:%s";
    public static final String FILENAME = "waypoint_groups.json";
    public final LoadingCache<String, WaypointGroup> cache;
    
    private WaypointGroupStore() {
        this.GSON = new GsonBuilder().setVersion(1.4).create();
        this.cache = this.createCache();
    }
    
    public WaypointGroup get(final String name) {
        return this.get("journeymap", name);
    }
    
    public WaypointGroup get(final String origin, final String name) {
        this.ensureLoaded();
        return (WaypointGroup)this.cache.getUnchecked((Object)String.format("%s:%s", origin, name));
    }
    
    public boolean exists(final WaypointGroup waypointGroup) {
        this.ensureLoaded();
        return this.cache.getIfPresent((Object)waypointGroup.getGuid()) != null;
    }
    
    public void put(final WaypointGroup waypointGroup) {
        this.ensureLoaded();
        this.cache.put((Object)waypointGroup.getGuid(), (Object)waypointGroup);
        this.save(true);
    }
    
    public boolean putIfNew(final WaypointGroup waypointGroup) {
        if (this.exists(waypointGroup)) {
            return false;
        }
        this.put(waypointGroup);
        return true;
    }
    
    public void remove(final WaypointGroup waypointGroup) {
        this.ensureLoaded();
        this.cache.invalidate((Object)waypointGroup.getGuid());
        waypointGroup.setDirty(false);
        this.save();
    }
    
    private void ensureLoaded() {
        if (this.cache.size() == 0L) {
            this.load();
        }
    }
    
    private void load() {
        final File groupFile = new File(FileHandler.getWaypointDir(), "waypoint_groups.json");
        if (groupFile.exists()) {
            HashMap<String, WaypointGroup> map = new HashMap<String, WaypointGroup>(0);
            try {
                final String groupsString = Files.toString(groupFile, Charset.forName("UTF-8"));
                map = (HashMap<String, WaypointGroup>)this.GSON.fromJson(groupsString, (Class)map.getClass());
            }
            catch (Exception e) {
                Journeymap.getLogger().error(String.format("Error reading WaypointGroups file %s: %s", groupFile, LogFormatter.toPartialString(e)));
                try {
                    groupFile.renameTo(new File(groupFile.getParentFile(), groupFile.getName() + ".bad"));
                }
                catch (Exception e2) {
                    Journeymap.getLogger().error(String.format("Error renaming bad WaypointGroups file %s: %s", groupFile, LogFormatter.toPartialString(e)));
                }
            }
            if (!map.isEmpty()) {
                this.cache.invalidateAll();
                this.cache.putAll((Map)map);
                Journeymap.getLogger().info(String.format("Loaded WaypointGroups file %s", groupFile));
                this.cache.put((Object)WaypointGroupStore.DEFAULT.getGuid(), (Object)WaypointGroupStore.DEFAULT);
                return;
            }
        }
        this.cache.put((Object)WaypointGroupStore.DEFAULT.getGuid(), (Object)WaypointGroupStore.DEFAULT);
        this.save(true);
    }
    
    public void save() {
        this.save(true);
    }
    
    public void save(final boolean force) {
        boolean doWrite = force;
        if (!force) {
            for (final WaypointGroup group : this.cache.asMap().values()) {
                if (group.isDirty()) {
                    doWrite = true;
                    break;
                }
            }
        }
        if (doWrite) {
            TreeMap<String, WaypointGroup> map = null;
            try {
                map = new TreeMap<String, WaypointGroup>(new Comparator<String>() {
                    final String defaultKey = WaypointGroupStore.DEFAULT.getGuid();
                    
                    @Override
                    public int compare(final String o1, final String o2) {
                        if (o1.equals(this.defaultKey)) {
                            return -1;
                        }
                        if (o2.equals(this.defaultKey)) {
                            return 1;
                        }
                        return o1.compareTo(o2);
                    }
                });
                map.putAll(this.cache.asMap());
            }
            catch (Exception e) {
                Journeymap.getLogger().error(String.format("Error preparing WaypointGroups: %s", LogFormatter.toPartialString(e)));
                return;
            }
            File groupFile = null;
            try {
                final File waypointDir = FileHandler.getWaypointDir();
                if (!waypointDir.exists()) {
                    waypointDir.mkdirs();
                }
                groupFile = new File(waypointDir, "waypoint_groups.json");
                final boolean isNew = groupFile.exists();
                Files.write((CharSequence)this.GSON.toJson((Object)map), groupFile, Charset.forName("UTF-8"));
                for (final WaypointGroup group2 : this.cache.asMap().values()) {
                    group2.setDirty(false);
                }
                if (isNew) {
                    Journeymap.getLogger().info("Created WaypointGroups file: " + groupFile);
                }
            }
            catch (Exception e2) {
                Journeymap.getLogger().error(String.format("Error writing WaypointGroups file %s: %s", groupFile, LogFormatter.toPartialString(e2)));
            }
        }
    }
    
    private LoadingCache<String, WaypointGroup> createCache() {
        final LoadingCache<String, WaypointGroup> cache = (LoadingCache<String, WaypointGroup>)CacheBuilder.newBuilder().concurrencyLevel(1).removalListener((RemovalListener)new RemovalListener<String, WaypointGroup>() {
            @ParametersAreNonnullByDefault
            public void onRemoval(final RemovalNotification<String, WaypointGroup> notification) {
                for (final Waypoint orphan : WaypointStore.INSTANCE.getAll((WaypointGroup)notification.getValue())) {
                    orphan.setName(WaypointGroupStore.DEFAULT.getName());
                    orphan.setGroup(WaypointGroupStore.DEFAULT);
                }
                WaypointGroupStore.this.save();
            }
        }).build((CacheLoader)new CacheLoader<String, WaypointGroup>() {
            @ParametersAreNonnullByDefault
            public WaypointGroup load(final String key) throws Exception {
                final int index = key.indexOf(":");
                String origin;
                String name;
                if (index < 1) {
                    origin = "Unknown";
                    name = key;
                    Journeymap.getLogger().warn("Problematic waypoint group key: " + key);
                }
                else {
                    origin = key.substring(0, index);
                    name = key.substring(index, key.length());
                }
                return new WaypointGroup(origin, name);
            }
        });
        return cache;
    }
    
    static {
        DEFAULT = new WaypointGroup("journeymap", Constants.getString("jm.config.category.waypoint"));
    }
}
