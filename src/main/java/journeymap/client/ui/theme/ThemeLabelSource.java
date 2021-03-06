package journeymap.client.ui.theme;

import journeymap.client.Constants;
import journeymap.client.data.WorldData;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.option.KeyedEnum;
import net.minecraft.client.Minecraft;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;

public enum ThemeLabelSource implements KeyedEnum {
    FPS("jm.theme.labelsource.fps", 100L, 1L, ThemeLabelSource::getFps),
    GameTime("jm.theme.labelsource.gametime", 0L, 1000L, ThemeLabelSource::getGameTime),
    RealTime("jm.theme.labelsource.realtime", 0L, 1000L, ThemeLabelSource::getRealTime),
    Location("jm.theme.labelsource.location", 1000L, 1L, ThemeLabelSource::getLocation),
    Biome("jm.theme.labelsource.biome", 1000L, 1L, ThemeLabelSource::getBiome),
    Blank("jm.theme.labelsource.blank", 0L, 1L, () -> "");

    private static DateFormat timeFormat;

    static {
        ThemeLabelSource.timeFormat = new SimpleDateFormat("h:mm:ss a");
    }

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

    private static String getFps() {
        return Minecraft.getDebugFPS() + " fps";
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

    public String getLabelText(final long currentTimeMillis) {
        try {
            final long now = this.granularityMillis * (currentTimeMillis / this.granularityMillis);
            if (now - this.lastCallTime <= this.cacheMillis) {
                return this.lastValue;
            }
            this.lastCallTime = now;
            return this.lastValue = this.supplier.get();
        } catch (Exception e) {
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
}
