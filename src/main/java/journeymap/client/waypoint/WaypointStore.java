package journeymap.client.waypoint;

import javax.annotation.*;
import journeymap.client.api.model.*;
import com.google.common.cache.*;
import net.minecraft.util.math.*;
import journeymap.client.cartography.color.*;
import journeymap.common.*;
import journeymap.client.io.*;
import java.util.stream.*;
import journeymap.client.api.display.*;
import java.util.*;
import java.io.*;
import journeymap.common.log.*;
import journeymap.client.render.texture.*;

@ParametersAreNonnullByDefault
public enum WaypointStore
{
    INSTANCE;
    
    public static final MapImage DEFAULT_WAYPOINT_ICON;
    public static final MapText DEFAULT_WAYPOINT_LABEL;
    private final Cache<String, Waypoint> cache;
    private final Set<Integer> dimensions;
    private boolean loaded;
    private IWaypointLoader waypointLoader;
    
    private WaypointStore() {
        this.cache = (Cache<String, Waypoint>)CacheBuilder.newBuilder().build();
        this.dimensions = new HashSet<Integer>();
        this.loaded = false;
    }
    
    public static MapImage getWaypointIcon(final Waypoint waypoint) {
        MapImage icon = waypoint.getIcon();
        if (icon == null || icon.getImageLocation() == null) {
            icon = WaypointStore.DEFAULT_WAYPOINT_ICON;
        }
        return icon;
    }
    
    public static MapText getWaypointLabel(final Waypoint waypoint) {
        final MapText label = waypoint.getLabel();
        return (label == null) ? WaypointStore.DEFAULT_WAYPOINT_LABEL : label;
    }
    
    public static Waypoint create(final int dimension, final BlockPos position) {
        final int color = RGB.randomColor();
        final String name = String.format("%s, %s", position.func_177958_n(), position.func_177952_p());
        return new Waypoint("journeymap", name, dimension, position).setIconColor(color).setLabelColor(RGB.labelSafe(color));
    }
    
    static String getFileName(final Waypoint waypoint) {
        String fileName = waypoint.getGuid().toLowerCase().replaceAll("[\\\\/:\"*?<>|]", "_").concat(".json");
        if (fileName.equals("waypoint_groups.json")) {
            fileName = "bad" + fileName;
            Journeymap.getLogger().error("Waypoint file can't be waypoint_groups.json");
        }
        return fileName;
    }
    
    private IWaypointLoader getWaypointLoader() {
        if (this.waypointLoader == null) {
            this.waypointLoader = new WaypointLoader(FileHandler.getWaypointDir());
        }
        return this.waypointLoader;
    }
    
    public void setWaypointLoader(final WaypointLoader loader) {
        this.waypointLoader = loader;
    }
    
    public List<Waypoint> getAll() {
        return new ArrayList<Waypoint>(this.cache.asMap().values());
    }
    
    public List<Waypoint> getAll(final int dimension) {
        return (List<Waypoint>)this.cache.asMap().values().stream().filter(wp -> wp.isDisplayed(dimension)).collect(Collectors.toList());
    }
    
    public List<Waypoint> getAll(final WaypointGroup group) {
        return (List<Waypoint>)this.cache.asMap().values().stream().filter(waypoint -> Objects.equals(group, waypoint.getGroup())).collect(Collectors.toList());
    }
    
    public void add(final Waypoint waypoint) {
        if (this.cache.getIfPresent((Object)waypoint.getId()) == null) {
            this.cache.put((Object)waypoint.getId(), (Object)waypoint);
        }
    }
    
    public void save(final Waypoint waypoint) {
        this.cache.put((Object)waypoint.getId(), (Object)waypoint);
        final boolean saved = this.getWaypointLoader().save(waypoint);
        if (saved) {
            waypoint.setDirty(false);
        }
    }
    
    public void bulkSave() {
        for (final Waypoint waypoint : this.cache.asMap().values()) {
            if (waypoint.isDirty()) {
                final boolean saved = this.getWaypointLoader().save(waypoint);
                if (!saved) {
                    continue;
                }
                waypoint.setDirty(false);
            }
        }
    }
    
    public void remove(final Waypoint waypoint) {
        this.cache.invalidate((Object)waypoint.getId());
        if (waypoint.isPersistent()) {
            final File waypointFile = new File(FileHandler.getWaypointDir(), getFileName(waypoint));
            if (waypointFile.exists()) {
                this.remove(waypointFile);
            }
        }
    }
    
    private void remove(final File waypointFile) {
        try {
            waypointFile.delete();
        }
        catch (Exception e) {
            Journeymap.getLogger().warn(String.format("Can't delete waypoint file %s: %s", waypointFile, e.getMessage()));
            waypointFile.deleteOnExit();
        }
    }
    
    public void reset() {
        this.cache.invalidateAll();
        this.dimensions.clear();
        this.loaded = false;
        this.load();
    }
    
    private void load() {
        synchronized (this.cache) {
            final File waypointDir = FileHandler.getWaypointDir();
            try {
                this.cache.invalidateAll();
                this.waypointLoader = null;
                this.loadCache(this.getWaypointLoader().loadAll());
                Journeymap.getLogger().info(String.format("Loaded %s waypoints from %s", this.cache.size(), waypointDir));
            }
            catch (Exception e) {
                Journeymap.getLogger().error(String.format("Error loading waypoints from %s: %s", waypointDir, LogFormatter.toString(e)));
            }
        }
    }
    
    protected void loadCache(final Collection<Waypoint> waypoints) {
        for (final Waypoint waypoint : waypoints) {
            if (waypoint.isPersistent() && waypoint.isDirty()) {
                this.save(waypoint);
            }
            else {
                this.cache.put((Object)waypoint.getId(), (Object)waypoint);
            }
            this.dimensions.addAll(waypoint.getDisplayDimensions());
        }
        this.loaded = true;
    }
    
    public boolean hasLoaded() {
        return this.loaded;
    }
    
    public List<Integer> getLoadedDimensions() {
        return new ArrayList<Integer>(this.dimensions);
    }
    
    static {
        DEFAULT_WAYPOINT_ICON = new MapImage(TextureCache.Waypoint, 16, 16);
        DEFAULT_WAYPOINT_LABEL = new MapText();
    }
}
