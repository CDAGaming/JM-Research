package journeymap.client.ui.theme;

import journeymap.client.ui.theme.impl.*;
import java.util.*;

public class ThemePresets
{
    public static String DEFAULT_DIRECTORY;
    
    public static Theme getDefault() {
        return FlatTheme.createOceanMonument();
    }
    
    public static List<String> getPresetDirs() {
        return Collections.singletonList(getDefault().directory);
    }
    
    public static List<Theme> getPresets() {
        return Arrays.asList(FlatTheme.createDesertTemple(), FlatTheme.EndCity(), FlatTheme.createForestMansion(), FlatTheme.createNetherFortress(), FlatTheme.createOceanMonument(), FlatTheme.createPurist(), FlatTheme.createStronghold());
    }
    
    static {
        ThemePresets.DEFAULT_DIRECTORY = "flat";
    }
}
