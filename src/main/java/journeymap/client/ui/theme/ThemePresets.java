package journeymap.client.ui.theme;

import journeymap.client.ui.theme.impl.FlatTheme;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ThemePresets {
    public static String DEFAULT_DIRECTORY;

    static {
        ThemePresets.DEFAULT_DIRECTORY = "flat";
    }

    public static Theme getDefault() {
        return FlatTheme.createOceanMonument();
    }

    public static List<String> getPresetDirs() {
        return Collections.singletonList(getDefault().directory);
    }

    public static List<Theme> getPresets() {
        return Arrays.asList(FlatTheme.createDesertTemple(), FlatTheme.EndCity(), FlatTheme.createForestMansion(), FlatTheme.createNetherFortress(), FlatTheme.createOceanMonument(), FlatTheme.createPurist(), FlatTheme.createStronghold());
    }
}
