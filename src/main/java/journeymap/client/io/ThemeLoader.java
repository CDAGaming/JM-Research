package journeymap.client.io;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import journeymap.client.Constants;
import journeymap.client.log.JMLogger;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.theme.Theme;
import journeymap.client.ui.theme.ThemePresets;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.properties.config.StringField;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ThemeLoader {
    public static final String THEME_FILE_SUFFIX = ".theme2.json";
    public static final String DEFAULT_THEME_FILE = "default.theme.config";
    public static final Gson GSON;
    private static transient Theme currentTheme;

    static {
        GSON = new GsonBuilder().setPrettyPrinting().setVersion(2.0).create();
        ThemeLoader.currentTheme = null;
    }

    public static void initialize(final boolean preLoadCurrentTheme) {
        Journeymap.getLogger().trace("Initializing themes ...");
        final Set<String> themeDirNames = new HashSet<>(ThemePresets.getPresetDirs());
        for (final String dirName : themeDirNames) {
            FileHandler.copyResources(getThemeIconDir(), new ResourceLocation("journeymap", "theme/" + dirName), dirName, true);
        }
        ThemePresets.getPresets().forEach(ThemeLoader::save);
        ensureDefaultThemeFile();
        if (preLoadCurrentTheme) {
            preloadCurrentTheme();
        }
    }

    public static File getThemeIconDir() {
        final File dir = new File(FileHandler.getMinecraftDirectory(), Constants.THEME_ICON_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File[] getThemeDirectories() {
        final File parentDir = getThemeIconDir();
        return parentDir.listFiles(File::isDirectory);
    }

    public static List<Theme> getThemes() {
        File[] themeDirs = getThemeDirectories();
        if (themeDirs == null || themeDirs.length == 0) {
            initialize(false);
            themeDirs = getThemeDirectories();
            if (themeDirs == null || themeDirs.length == 0) {
                Journeymap.getLogger().error("Couldn't find theme directories.");
                return Collections.emptyList();
            }
        }
        final ArrayList<Theme> themes = new ArrayList<>();
        for (final File themeDir : themeDirs) {
            final File[] themeFiles = themeDir.listFiles((dir, name) -> name.endsWith(".theme2.json"));
            if (themeFiles != null && themeFiles.length > 0) {
                for (final File themeFile : themeFiles) {
                    final Theme theme2 = loadThemeFromFile(themeFile, false);
                    if (theme2 != null) {
                        themes.add(theme2);
                    }
                }
            }
        }
        if (themes.isEmpty()) {
            themes.addAll(ThemePresets.getPresets());
        }
        themes.sort(Comparator.comparing(theme -> theme.name));
        return themes;
    }

    public static List<String> getThemeNames() {
        List<Theme> themes = null;
        try {
            themes = getThemes();
        } catch (Exception e) {
            themes = ThemePresets.getPresets();
        }
        return themes.stream().map(theme -> theme.name).collect(Collectors.toList());
    }

    public static Theme getCurrentTheme() {
        return getCurrentTheme(false);
    }

    public static synchronized void setCurrentTheme(final Theme theme) {
        if (ThemeLoader.currentTheme == theme) {
            return;
        }
        Journeymap.getClient().getCoreProperties().themeName.set(theme.name);
        getCurrentTheme(true);
        UIManager.INSTANCE.getMiniMap().reset();
    }

    public static synchronized Theme getCurrentTheme(final boolean forceReload) {
        if (forceReload) {
            TextureCache.purgeThemeImages(TextureCache.themeImages);
        }
        final String themeName = Journeymap.getClient().getCoreProperties().themeName.get();
        if (forceReload || ThemeLoader.currentTheme == null || !themeName.equals(ThemeLoader.currentTheme.name)) {
            ThemeLoader.currentTheme = getThemeByName(themeName);
            Journeymap.getClient().getCoreProperties().themeName.set(ThemeLoader.currentTheme.name);
        }
        return ThemeLoader.currentTheme;
    }

    public static Theme getThemeByName(final String themeName) {
        for (final Theme theme : getThemes()) {
            if (theme.name.equals(themeName)) {
                return theme;
            }
        }
        Journeymap.getLogger().warn(String.format("Theme '%s' not found, reverting to default", themeName));
        return ThemePresets.getDefault();
    }

    public static Theme loadThemeFromFile(final File themeFile, final boolean createIfMissing) {
        try {
            if (themeFile != null && themeFile.exists()) {
                final Charset UTF8 = Charset.forName("UTF-8");
                final String json = Files.toString(themeFile, UTF8);
                final Theme theme = ThemeLoader.GSON.fromJson(json, Theme.class);
                if (theme.schema < 2.0) {
                    Journeymap.getLogger().error("Theme file schema is obsolete, cannot be used: " + themeFile);
                    return null;
                }
            } else if (createIfMissing) {
                Journeymap.getLogger().info("Generating Theme json file: " + themeFile);
                final Theme theme2 = new Theme();
                theme2.name = themeFile.getName();
                save(theme2);
                return theme2;
            }
        } catch (Throwable t) {
            Journeymap.getLogger().error("Could not load Theme json file: " + LogFormatter.toString(t));
        }
        return null;
    }

    private static File getThemeFile(final String themeDirName, final String themeFileName) {
        final File themeDir = new File(getThemeIconDir(), themeDirName);
        final String fileName = String.format("%s%s", themeFileName.replaceAll("[\\\\/:\"*?<>|]", "_"), ".theme2.json");
        return new File(themeDir, fileName);
    }

    public static void save(final Theme theme) {
        try {
            final File themeFile = getThemeFile(theme.directory, theme.name);
            Files.createParentDirs(themeFile);
            final Charset UTF8 = Charset.forName("UTF-8");
            Files.write(ThemeLoader.GSON.toJson(theme), themeFile, UTF8);
        } catch (Throwable t) {
            Journeymap.getLogger().error("Could not save Theme json file: " + t);
        }
    }

    private static void ensureDefaultThemeFile() {
        final File defaultThemeFile = new File(getThemeIconDir(), "default.theme.config");
        if (!defaultThemeFile.exists()) {
            try {
                final Theme.DefaultPointer defaultPointer = new Theme.DefaultPointer(ThemePresets.getDefault());
                final Charset UTF8 = Charset.forName("UTF-8");
                Files.write(ThemeLoader.GSON.toJson(defaultPointer), defaultThemeFile, UTF8);
            } catch (Throwable t) {
                Journeymap.getLogger().error("Could not save DefaultTheme json file: " + t);
            }
        }
    }

    public static Theme getDefaultTheme() {
        if (FMLClientHandler.instance().getClient() == null) {
            return ThemePresets.getDefault();
        }
        Theme theme = null;
        File themeFile = null;
        Theme.DefaultPointer pointer = null;
        try {
            pointer = loadDefaultPointer();
            pointer.filename = pointer.filename.replace(".theme2.json", "");
            themeFile = getThemeFile(pointer.directory, pointer.filename);
            theme = loadThemeFromFile(themeFile, false);
        } catch (Exception e) {
            JMLogger.logOnce("Default theme not found in files", e);
        }
        if (theme == null) {
            if (pointer != null) {
                JMLogger.logOnce(String.format("Default theme not found in %s: %s", themeFile, pointer.name), null);
            }
            theme = ThemePresets.getDefault();
        }
        return theme;
    }

    public static synchronized void loadNextTheme() {
        final List<String> themeNames = getThemeNames();
        int index = themeNames.indexOf(getCurrentTheme().name);
        if (index < 0 || index >= themeNames.size() - 1) {
            index = 0;
        } else {
            ++index;
        }
        setCurrentTheme(getThemes().get(index));
    }

    private static Theme.DefaultPointer loadDefaultPointer() {
        try {
            ensureDefaultThemeFile();
            final File defaultThemeFile = new File(getThemeIconDir(), "default.theme.config");
            if (defaultThemeFile.exists()) {
                final Charset UTF8 = Charset.forName("UTF-8");
                final String json = Files.toString(defaultThemeFile, UTF8);
                return ThemeLoader.GSON.fromJson(json, Theme.DefaultPointer.class);
            }
            return new Theme.DefaultPointer(ThemePresets.getDefault());
        } catch (Throwable t) {
            Journeymap.getLogger().error("Could not load Theme.DefaultTheme json file: " + LogFormatter.toString(t));
            return null;
        }
    }

    public static void preloadCurrentTheme() {
        int count = 0;
        try {
            final Theme theme = getCurrentTheme();
            final File themeDir = new File(getThemeIconDir(), theme.directory).getCanonicalFile();
            final Path themePath = themeDir.toPath();
            for (final File file : Files.fileTreeTraverser().breadthFirstTraversal(themeDir)) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".png")) {
                    final String relativePath = themePath.relativize(file.toPath()).toString().replaceAll("\\\\", "/");
                    TextureCache.getThemeTexture(theme, relativePath);
                    ++count;
                }
            }
        } catch (Throwable t) {
            Journeymap.getLogger().error("Error preloading theme textures: " + LogFormatter.toString(t));
        }
        Journeymap.getLogger().info("Preloaded theme textures: " + count);
    }

    public static class ThemeValuesProvider implements StringField.ValuesProvider {
        @Override
        public List<String> getStrings() {
            return ThemeLoader.getThemeNames();
        }

        @Override
        public String getDefaultString() {
            return ThemeLoader.getDefaultTheme().name;
        }
    }
}
