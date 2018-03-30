package journeymap.client.ui.theme;

import journeymap.client.ui.option.*;
import java.util.function.*;
import journeymap.client.*;
import net.minecraft.client.*;
import journeymap.client.data.*;
import java.util.*;
import journeymap.client.ui.*;
import java.text.*;

public enum ThemeLabelSource implements KeyedEnum
{
    FPS("jm.theme.labelsource.fps", 100L, 1L, ThemeLabelSource::getFps), 
    GameTime("jm.theme.labelsource.gametime", 0L, 1000L, ThemeLabelSource::getGameTime), 
    RealTime("jm.theme.labelsource.realtime", 0L, 1000L, ThemeLabelSource::getRealTime), 
    Location("jm.theme.labelsource.location", 1000L, 1L, ThemeLabelSource::getLocation), 
    Biome("jm.theme.labelsource.biome", 1000L, 1L, ThemeLabelSource::getBiome), 
    Blank("jm.theme.labelsource.blank", 0L, 1L, () -> "");
    
    private static DateFormat timeFormat;
    private final String key;
    private final Supplier<String> supplier;
    private final long cacheMillis;
    private final long granularityMillis;
    private long lastCallTime;
    private String lastValue;
    
    private ThemeLabelSource(final String key, final long cacheMillis, final long granularityMillis, final Supplier<String> supplier) {
        this.lastValue = "";
        this.key = key;
        this.cacheMillis = cacheMillis;
        this.granularityMillis = granularityMillis;
        this.supplier = supplier;
    }
    
    public static void resetCaches() {
        for (final ThemeLabelSource source : values()) {
            source.lastCallTime = 0L;
            source.lastValue = "";
        }
    }
    
    public String getLabelText(final long currentTimeMillis) {
        try {
            final long now = this.granularityMillis * (currentTimeMillis / this.granularityMillis);
            if (now - this.lastCallTime <= this.cacheMillis) {
                return this.lastValue;
            }
            this.lastCallTime = now;
            return this.lastValue = this.supplier.get();
        }
        catch (Exception e) {
            return "?";
        }
    }
    
    public boolean isShown() {
        return this != ThemeLabelSource.Blank;
    }
    
    @Override
    public String getKey() {
        return this.key;
    }
    
    @Override
    public String toString() {
        return Constants.getString(this.key);
    }
    
    private static String getFps() {
        return Minecraft.func_175610_ah() + " fps";
    }
    
    private static String getGameTime() {
        return WorldData.getGameTime();
    }
    
    private static String getRealTime() {
        return ThemeLabelSource.timeFormat.format(new Date());
    }
    
    private static String getLocation() {
        return UIManager.INSTANCE.getMiniMap().getLocation();
    }
    
    private static String getBiome() {
        return UIManager.INSTANCE.getMiniMap().getBiome();
    }
    
    static {
        ThemeLabelSource.timeFormat = new SimpleDateFormat("h:mm:ss a");
    }
}
