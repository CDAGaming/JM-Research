package journeymap.client.io;

import com.google.common.base.Joiner;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.gson.GsonBuilder;
import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.data.WorldData;
import journeymap.client.log.JMLogger;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.Level;
import org.lwjgl.Sys;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class FileHandler {
    public static final String DEV_MINECRAFT_DIR = "run/";
    public static final String ASSETS_JOURNEYMAP = "/assets/journeymap";
    public static final String ASSETS_JOURNEYMAP_UI = "/assets/journeymap/ui";
    public static final File MinecraftDirectory;
    public static final File JourneyMapDirectory;
    public static final File StandardConfigDirectory;
    private static WorldClient theLastWorld;

    static {
        MinecraftDirectory = getMinecraftDirectory();
        JourneyMapDirectory = new File(FileHandler.MinecraftDirectory, Constants.JOURNEYMAP_DIR);
        StandardConfigDirectory = new File(FileHandler.MinecraftDirectory, Constants.CONFIG_DIR);
    }

    public static File getMinecraftDirectory() {
        final Minecraft minecraft = FMLClientHandler.instance().getClient();
        if (minecraft != null) {
            return minecraft.mcDataDir;
        }
        return new File("run/");
    }

    public static File getMCWorldDir(final Minecraft minecraft) {
        if (minecraft.isIntegratedServerRunning()) {
            final String lastMCFolderName = minecraft.getIntegratedServer().getFolderName();
            final File lastMCWorldDir = new File(getMinecraftDirectory(), "saves" + File.separator + lastMCFolderName);
            return lastMCWorldDir;
        }
        return null;
    }

    public static File getWorldSaveDir(final Minecraft minecraft) {
        if (minecraft.isSingleplayer()) {
            try {
                final File savesDir = new File(getMinecraftDirectory(), "saves");
                final File worldSaveDir = new File(savesDir, minecraft.getIntegratedServer().getFolderName());
                if (minecraft.world.provider.getSaveFolder() != null) {
                    final File dir = new File(worldSaveDir, minecraft.world.provider.getSaveFolder());
                    dir.mkdirs();
                    return dir;
                }
                return worldSaveDir;
            } catch (Throwable t) {
                Journeymap.getLogger().error("Error getting world save dir: %s", t);
            }
        }
        return null;
    }

    public static File getMCWorldDir(final Minecraft minecraft, final int dimension) {
        final File worldDir = getMCWorldDir(minecraft);
        if (worldDir == null) {
            return null;
        }
        if (dimension == 0) {
            return worldDir;
        }
        final String dimString = Integer.toString(dimension);
        File dimDir = null;
        if (dimension == -1 || dimension == 1) {
            dimDir = new File(worldDir, "DIM" + dimString);
        }
        if (dimDir == null || !dimDir.exists()) {
            final File[] dims = worldDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String name) {
                    return name.startsWith("DIM") && name.endsWith(dimString) && !name.endsWith("-" + dimString);
                }
            });
            if (dims.length == 0) {
                return new File(worldDir, "DIM" + dimString);
            }
            if (dims.length != 1) {
                final List<File> list = Arrays.asList(dims);
                list.sort(Comparator.comparingInt(o -> o.getName().length()));
                return list.get(0);
            }
            dimDir = dims[0];
        }
        return dimDir;
    }

    public static File getJourneyMapDir() {
        return FileHandler.JourneyMapDirectory;
    }

    public static File getJMWorldDir(final Minecraft minecraft) {
        if (minecraft.world == null) {
            return null;
        }
        if (!minecraft.isSingleplayer()) {
            return getJMWorldDir(minecraft, Journeymap.getClient().getCurrentWorldId());
        }
        return getJMWorldDir(minecraft, null);
    }

    public static synchronized File getJMWorldDir(final Minecraft minecraft, final String worldId) {
        if (minecraft.world == null) {
            FileHandler.theLastWorld = null;
            return null;
        }
        File worldDirectory = null;
        try {
            worldDirectory = getJMWorldDirForWorldId(minecraft, worldId);
            if (worldDirectory == null) {
                worldDirectory = getJMWorldDirForWorldId(minecraft, null);
            }
            if (worldDirectory != null && !worldDirectory.exists()) {
                worldDirectory.mkdirs();
            }
        } catch (Exception e) {
            Journeymap.getLogger().log(Level.ERROR, LogFormatter.toString(e));
            throw new RuntimeException(e);
        }
        FileHandler.theLastWorld = minecraft.world;
        return worldDirectory;
    }

    public static File getJMWorldDirForWorldId(final Minecraft minecraft, String worldId) {
        if (minecraft == null || minecraft.world == null) {
            return null;
        }
        File testWorldDirectory = null;
        try {
            final String worldName = WorldData.getWorldName(minecraft, false).replaceAll("[^\\w\\s]+", "~");
            if (!minecraft.isSingleplayer()) {
                if (worldId != null) {
                    worldId = worldId.replaceAll("\\W+", "~");
                }
                final String suffix = (worldId != null) ? ("_" + worldId) : "";
                testWorldDirectory = new File(FileHandler.MinecraftDirectory, Constants.MP_DATA_DIR + worldName + suffix);
            } else {
                testWorldDirectory = new File(FileHandler.MinecraftDirectory, Constants.SP_DATA_DIR + worldName);
            }
        } catch (Exception e) {
            Journeymap.getLogger().log(Level.ERROR, LogFormatter.toString(e));
        }
        return testWorldDirectory;
    }

    public static File getWaypointDir() {
        return getWaypointDir(getJMWorldDir(FMLClientHandler.instance().getClient()));
    }

    public static File getWaypointDir(final File jmWorldDir) {
        final File waypointDir = new File(jmWorldDir, "waypoints");
        if (!waypointDir.isDirectory()) {
            waypointDir.delete();
        }
        if (!waypointDir.exists()) {
            waypointDir.mkdirs();
        }
        return waypointDir;
    }

    public static Properties getLangFile(final String fileName) {
        try {
            InputStream is = JourneymapClient.class.getResourceAsStream("/assets/journeymap/lang/" + fileName);
            if (is == null) {
                final File file = new File("../src/main/resources/assets/journeymap/lang/" + fileName);
                if (!file.exists()) {
                    Journeymap.getLogger().warn("Language file not found: " + fileName);
                    return null;
                }
                is = new FileInputStream(file);
            }
            final Properties properties = new Properties();
            properties.load(is);
            is.close();
            return properties;
        } catch (IOException e) {
            final String error = "Could not get language file " + fileName + ": " + e.getMessage();
            Journeymap.getLogger().error(error);
            return null;
        }
    }

    public static <M> M getMessageModel(final Class<M> model, final String filePrefix) {
        try {
            final String lang = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
            InputStream is = getMessageModelInputStream(filePrefix, lang);
            if (is == null && !lang.equals("en_US")) {
                is = getMessageModelInputStream(filePrefix, "en_US");
            }
            if (is == null) {
                Journeymap.getLogger().warn("Message file not found: " + filePrefix);
                return null;
            }
            return new GsonBuilder().create().fromJson(new InputStreamReader(is), model);
        } catch (Throwable e) {
            final String error = "Could not get Message model " + filePrefix + ": " + e.getMessage();
            Journeymap.getLogger().error(error);
            return null;
        }
    }

    public static InputStream getMessageModelInputStream(final String filePrefix, final String lang) {
        final String file = String.format("/assets/journeymap/lang/message/%s-%s.json", filePrefix, lang);
        return JourneymapClient.class.getResourceAsStream(file);
    }

    public static File getWorldConfigDir(final boolean fallbackToStandardConfigDir) {
        final File worldDir = getJMWorldDirForWorldId(FMLClientHandler.instance().getClient(), null);
        if (worldDir != null && worldDir.exists()) {
            final File worldConfigDir = new File(worldDir, "config");
            if (worldConfigDir.exists()) {
                return worldConfigDir;
            }
        }
        return fallbackToStandardConfigDir ? FileHandler.StandardConfigDirectory : null;
    }

    public static BufferedImage getImage(final File imageFile) {
        try {
            if (!imageFile.canRead()) {
                return null;
            }
            return ImageIO.read(imageFile);
        } catch (IOException e) {
            final String error = "Could not get imageFile " + imageFile + ": " + e.getMessage();
            Journeymap.getLogger().error(error);
            return null;
        }
    }

    public static boolean isInJar() {
        return isInJar(JourneymapClient.class.getProtectionDomain().getCodeSource().getLocation());
    }

    public static boolean isInJar(final URL location) {
        if ("jar".equals(location.getProtocol())) {
            return true;
        }
        if ("file".equals(location.getProtocol())) {
            final File file = new File(location.getFile());
            return file.exists() && (file.getName().endsWith(".jar") || file.getName().endsWith(".jar"));
        }
        return false;
    }

    public static File copyColorPaletteHtmlFile(final File toDir, final String fileName) {
        try {
            final File outFile = new File(toDir, fileName);
            final String htmlPath = "/assets/journeymap/ui/" + fileName;
            final InputStream inputStream = JourneymapClient.class.getResource(htmlPath).openStream();
            final ByteSink out = new ByteSink() {
                public OutputStream openStream() throws IOException {
                    return new FileOutputStream(outFile);
                }
            };
            out.writeFrom(inputStream);
            return outFile;
        } catch (Throwable t) {
            Journeymap.getLogger().warn("Couldn't copy color palette html: " + t);
            return null;
        }
    }

    public static void open(final File file) {
        final String path = file.getAbsolutePath();
        Label_0156:
        {
            if (Util.getOSType() == Util.EnumOS.OSX) {
                try {
                    Runtime.getRuntime().exec(new String[]{"/usr/bin/open", path});
                    return;
                } catch (IOException e) {
                    Journeymap.getLogger().error("Could not open path with /usr/bin/open: " + path + " : " + LogFormatter.toString(e));
                    break Label_0156;
                }
            }
            if (Util.getOSType() == Util.EnumOS.WINDOWS) {
                final String cmd = String.format("cmd.exe /C start \"Open file\" \"%s\"", path);
                try {
                    Runtime.getRuntime().exec(cmd);
                    return;
                } catch (IOException e2) {
                    Journeymap.getLogger().error("Could not open path with cmd.exe: " + path + " : " + LogFormatter.toString(e2));
                }
            }
            try {
                final Class desktopClass = Class.forName("java.awt.Desktop");
                final Object method = desktopClass.getMethod("getDesktop", (Class[]) new Class[0]).invoke(null);
                desktopClass.getMethod("browse", URI.class).invoke(method, file.toURI());
            } catch (Throwable e3) {
                Journeymap.getLogger().error("Could not open path with Desktop: " + path + " : " + LogFormatter.toString(e3));
                Sys.openURL("file://" + path);
            }
        }
    }

    public static boolean copyResources(final File targetDirectory, final ResourceLocation location, final String setName, final boolean overwrite) {
        final String fromPath = null;
        final File toDir = null;
        try {
            final String domain = location.getResourceDomain();
            URL fileLocation = null;
            if (domain.equals("minecraft")) {
                fileLocation = Minecraft.class.getProtectionDomain().getCodeSource().getLocation();
            } else {
                ModContainer mod = Loader.instance().getIndexedModList().get(domain);
                if (mod == null) {
                    for (final Map.Entry<String, ModContainer> modEntry : Loader.instance().getIndexedModList().entrySet()) {
                        if (modEntry.getValue().getModId().toLowerCase().equals(domain)) {
                            mod = modEntry.getValue();
                            break;
                        }
                    }
                }
                if (mod != null) {
                    fileLocation = mod.getSource().toURI().toURL();
                }
            }
            if (fileLocation != null) {
                String assetsPath;
                if (location.getResourcePath().startsWith("assets/")) {
                    assetsPath = location.getResourcePath();
                } else {
                    assetsPath = String.format("assets/%s/%s", domain, location.getResourcePath());
                }
                return copyResources(targetDirectory, fileLocation, assetsPath, setName, overwrite);
            }
        } catch (Throwable t) {
            Journeymap.getLogger().error(String.format("Couldn't get resource set from %s to %s: %s", fromPath, toDir, t));
        }
        return false;
    }

    public static boolean copyResources(final File targetDirectory, final String assetsPath, final String setName, final boolean overwrite) {
        final ModContainer modContainer = Loader.instance().getIndexedModList().get("journeymap");
        if (modContainer != null) {
            try {
                final URL resourceDir = modContainer.getSource().toURI().toURL();
                return copyResources(targetDirectory, resourceDir, assetsPath, setName, overwrite);
            } catch (MalformedURLException e) {
                Journeymap.getLogger().error(String.format("Couldn't find resource directory %s ", targetDirectory));
            }
        }
        return false;
    }

    public static boolean copyResources(final File targetDirectory, final URL resourceDir, final String assetsPath, final String setName, final boolean overwrite) {
        String fromPath = null;
        File toDir = null;
        try {
            toDir = new File(targetDirectory, setName);
            final boolean inJar = isInJar(resourceDir);
            if (inJar) {
                if ("jar".equals(resourceDir.getProtocol())) {
                    fromPath = URLDecoder.decode(resourceDir.getPath(), "utf-8").split("file:")[1].split("!/")[0];
                } else {
                    fromPath = new File(resourceDir.getPath()).getPath();
                }
                return copyFromZip(fromPath, assetsPath, toDir, overwrite);
            }
            final File fromDir = new File(resourceDir.getFile(), assetsPath);
            if (fromDir.exists()) {
                fromPath = fromDir.getPath();
                return copyFromDirectory(fromDir, toDir, overwrite);
            }
            Journeymap.getLogger().error(String.format("Couldn't locate icons for %s: %s", setName, fromDir));
        } catch (Throwable t) {
            Journeymap.getLogger().error(String.format("Couldn't unzip resource set from %s to %s: %s", fromPath, toDir, t));
        }
        return false;
    }

    static boolean copyFromZip(final String zipFilePath, String zipEntryName, final File destDir, final boolean overWrite) throws Throwable {
        if (zipEntryName.startsWith("/")) {
            zipEntryName = zipEntryName.substring(1);
        }
        final ZipFile zipFile = new ZipFile(zipFilePath);
        final ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        boolean success = false;
        try {
            while (entry != null) {
                if (entry.getName().startsWith(zipEntryName)) {
                    final File toFile = new File(destDir, entry.getName().split(zipEntryName)[1]);
                    if ((overWrite || !toFile.exists()) && !entry.isDirectory()) {
                        Files.createParentDirs(toFile);
                        new ZipEntryByteSource(zipFile, entry).copyTo(Files.asByteSink(toFile));
                        success = true;
                    }
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        } finally {
            zipIn.close();
        }
        return success;
    }

    static boolean copyFromDirectory(final File fromDir, final File toDir, final boolean overWrite) throws IOException {
        if (!toDir.exists() && !toDir.mkdirs()) {
            throw new IOException("Couldn't create directory: " + toDir);
        }
        final File[] files = fromDir.listFiles();
        if (files == null) {
            throw new IOException(fromDir + " nas no files");
        }
        boolean success = true;
        for (final File from : files) {
            final File to = new File(toDir, from.getName());
            if (from.isDirectory()) {
                if (!copyFromDirectory(from, to, overWrite)) {
                    success = false;
                }
            } else if (overWrite || !to.exists()) {
                Files.copy(from, to);
                if (!to.exists()) {
                    success = false;
                }
            }
        }
        return success;
    }

    public static boolean delete(final File file) {
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        String[] cmd = null;
        final String path = file.getAbsolutePath();
        final Util.EnumOS os = Util.getOSType();
        switch (os) {
            case WINDOWS: {
                cmd = new String[]{String.format("cmd.exe /C RD /S /Q \"%s\"", path)};
                break;
            }
            case OSX: {
                cmd = new String[]{"rm", "-rf", path};
                break;
            }
            default: {
                cmd = new String[]{"rm", "-rf", path};
                break;
            }
        }
        try {
            final ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            final Process p = pb.start();
            p.waitFor();
        } catch (Throwable e) {
            Journeymap.getLogger().error(String.format("Could not delete using: %s : %s", Joiner.on(" ").join((Object[]) cmd), LogFormatter.toString(e)));
        }
        return file.exists();
    }

    public static BufferedImage getIconFromFile(final File parentdir, final String setName, String iconPath) {
        BufferedImage img = null;
        if (iconPath == null) {
            iconPath = "null";
        }
        File iconFile = null;
        try {
            final String filePath = Joiner.on(File.separatorChar).join(setName, iconPath.replace('/', File.separatorChar));
            iconFile = new File(parentdir, filePath);
            if (iconFile.exists()) {
                img = getImage(iconFile);
            }
        } catch (Exception e) {
            JMLogger.logOnce("Couldn't load iconset file: " + iconFile, e);
        }
        return img;
    }

    public static BufferedImage getIconFromResource(final String assetsPath, final String setName, final String iconPath) {
        try {
            final InputStream is = getIconStream(assetsPath, setName, iconPath);
            if (is == null) {
                return null;
            }
            final BufferedImage img = ImageIO.read(is);
            is.close();
            return img;
        } catch (IOException e) {
            final String error = String.format("Could not get icon from resource: %s, %s, %s : %s", assetsPath, setName, iconPath, e.getMessage());
            Journeymap.getLogger().error(error);
            return null;
        }
    }

    public static InputStream getIconStream(final String assetsPath, final String setName, final String iconPath) {
        try {
            final String pngPath = Joiner.on('/').join(assetsPath, setName, iconPath);
            final InputStream is = JourneymapClient.class.getResourceAsStream(pngPath);
            if (is == null) {
                Journeymap.getLogger().warn("Icon Set asset not found: " + pngPath);
                return null;
            }
            return is;
        } catch (Throwable e) {
            final String error = String.format("Could not get icon stream: %s, %s, %s : %s", assetsPath, setName, iconPath, e.getMessage());
            Journeymap.getLogger().error(error);
            return null;
        }
    }

    private static class ZipEntryByteSource extends ByteSource {
        final ZipFile file;
        final ZipEntry entry;

        ZipEntryByteSource(final ZipFile file, final ZipEntry entry) {
            this.file = file;
            this.entry = entry;
        }

        public InputStream openStream() throws IOException {
            return this.file.getInputStream(this.entry);
        }

        public String toString() {
            return String.format("ZipEntryByteSource( %s / %s )", this.file, this.entry);
        }
    }
}
