package journeymap.client.io;

import net.minecraft.client.multiplayer.*;
import net.minecraftforge.fml.client.*;
import net.minecraft.client.*;
import journeymap.common.*;
import org.apache.logging.log4j.*;
import journeymap.common.log.*;
import journeymap.client.data.*;
import journeymap.client.log.*;
import journeymap.client.*;
import com.google.gson.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;
import org.lwjgl.*;
import net.minecraft.util.*;
import net.minecraftforge.fml.common.*;
import java.util.*;
import java.net.*;
import java.util.zip.*;
import com.google.common.base.*;
import com.google.common.io.*;

public class FileHandler
{
    public static final String DEV_MINECRAFT_DIR = "run/";
    public static final String ASSETS_JOURNEYMAP = "/assets/journeymap";
    public static final String ASSETS_JOURNEYMAP_UI = "/assets/journeymap/ui";
    public static final File MinecraftDirectory;
    public static final File JourneyMapDirectory;
    public static final File StandardConfigDirectory;
    private static WorldClient theLastWorld;
    
    public static File getMinecraftDirectory() {
        final Minecraft minecraft = FMLClientHandler.instance().getClient();
        if (minecraft != null) {
            return minecraft.field_71412_D;
        }
        return new File("run/");
    }
    
    public static File getMCWorldDir(final Minecraft minecraft) {
        if (minecraft.func_71387_A()) {
            final String lastMCFolderName = minecraft.func_71401_C().func_71270_I();
            final File lastMCWorldDir = new File(getMinecraftDirectory(), "saves" + File.separator + lastMCFolderName);
            return lastMCWorldDir;
        }
        return null;
    }
    
    public static File getWorldSaveDir(final Minecraft minecraft) {
        if (minecraft.func_71356_B()) {
            try {
                final File savesDir = new File(getMinecraftDirectory(), "saves");
                final File worldSaveDir = new File(savesDir, minecraft.func_71401_C().func_71270_I());
                if (minecraft.field_71441_e.field_73011_w.getSaveFolder() != null) {
                    final File dir = new File(worldSaveDir, minecraft.field_71441_e.field_73011_w.getSaveFolder());
                    dir.mkdirs();
                    return dir;
                }
                return worldSaveDir;
            }
            catch (Throwable t) {
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
                Collections.sort(list, new Comparator<File>() {
                    @Override
                    public int compare(final File o1, final File o2) {
                        return new Integer(o1.getName().length()).compareTo(Integer.valueOf(o2.getName().length()));
                    }
                });
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
        if (minecraft.field_71441_e == null) {
            return null;
        }
        if (!minecraft.func_71356_B()) {
            return getJMWorldDir(minecraft, Journeymap.getClient().getCurrentWorldId());
        }
        return getJMWorldDir(minecraft, null);
    }
    
    public static synchronized File getJMWorldDir(final Minecraft minecraft, final String worldId) {
        if (minecraft.field_71441_e == null) {
            FileHandler.theLastWorld = null;
            return null;
        }
        File worldDirectory = null;
        try {
            worldDirectory = getJMWorldDirForWorldId(minecraft, worldId);
            if (worldDirectory != null && worldDirectory.exists()) {
                return worldDirectory;
            }
            final File defaultWorldDirectory = getJMWorldDirForWorldId(minecraft, null);
            if (worldId != null && defaultWorldDirectory.exists() && !worldDirectory.exists()) {
                Journeymap.getLogger().log(Level.INFO, "Moving default directory to " + worldDirectory);
                try {
                    migrateLegacyFolderName(defaultWorldDirectory, worldDirectory);
                    return worldDirectory;
                }
                catch (Exception e) {
                    Journeymap.getLogger().error(LogFormatter.toPartialString(e));
                }
            }
            if (!minecraft.func_71356_B()) {
                boolean migrated = false;
                String legacyWorldName = WorldData.getLegacyServerName() + "_0";
                File legacyWorldDir = new File(FileHandler.MinecraftDirectory, Constants.MP_DATA_DIR + legacyWorldName);
                if (legacyWorldDir.exists() && !legacyWorldDir.getName().equals(defaultWorldDirectory.getName()) && !legacyWorldDir.getName().equals(worldDirectory.getName())) {
                    migrateLegacyFolderName(legacyWorldDir, worldDirectory);
                    migrated = true;
                }
                if (worldId != null) {
                    legacyWorldName = WorldData.getWorldName(minecraft, true) + "_" + worldId;
                    legacyWorldDir = new File(FileHandler.MinecraftDirectory, Constants.MP_DATA_DIR + legacyWorldName);
                    if (legacyWorldDir.exists() && !legacyWorldDir.getName().equals(worldDirectory.getName())) {
                        migrateLegacyFolderName(legacyWorldDir, worldDirectory);
                        migrated = true;
                    }
                }
            }
            else {
                final File legacyWorldDir2 = new File(FileHandler.MinecraftDirectory, Constants.SP_DATA_DIR + WorldData.getWorldName(minecraft, true));
                if (!legacyWorldDir2.getName().equals(worldDirectory.getName()) && legacyWorldDir2.exists() && worldDirectory.exists()) {
                    JMLogger.logOnce(String.format("Found two directories that might be in conflict. Using:  %s , Ignoring: %s", worldDirectory, legacyWorldDir2), null);
                }
                if (legacyWorldDir2.exists() && !worldDirectory.exists() && !legacyWorldDir2.getName().equals(worldDirectory.getName())) {
                    migrateLegacyFolderName(legacyWorldDir2, worldDirectory);
                }
            }
            if (!worldDirectory.exists() && (worldId == null || !worldDirectory.getName().equals(defaultWorldDirectory.getName()))) {
                worldDirectory.mkdirs();
            }
        }
        catch (Exception e2) {
            Journeymap.getLogger().error(LogFormatter.toPartialString(e2));
            throw new RuntimeException(e2);
        }
        FileHandler.theLastWorld = minecraft.field_71441_e;
        return worldDirectory;
    }
    
    public static File getJMWorldDirForWorldId(final Minecraft minecraft, String worldId) {
        if (minecraft == null || minecraft.field_71441_e == null) {
            return null;
        }
        File testWorldDirectory = null;
        try {
            final String worldName = WorldData.getWorldName(minecraft, false).replaceAll("[^\\w\\s]+", "~");
            if (!minecraft.func_71356_B()) {
                if (worldId != null) {
                    worldId = worldId.replaceAll("\\W+", "~");
                }
                final String suffix = (worldId != null) ? ("_" + worldId) : "";
                testWorldDirectory = new File(FileHandler.MinecraftDirectory, Constants.MP_DATA_DIR + worldName + suffix);
            }
            else {
                testWorldDirectory = new File(FileHandler.MinecraftDirectory, Constants.SP_DATA_DIR + worldName);
            }
        }
        catch (Exception e) {
            Journeymap.getLogger().error(LogFormatter.toPartialString(e));
        }
        return testWorldDirectory;
    }
    
    private static void migrateLegacyFolderName(final File legacyWorldDir, final File worldDir) {
        if (legacyWorldDir.getPath().equals(worldDir.getPath())) {
            return;
        }
        boolean success = false;
        try {
            success = legacyWorldDir.renameTo(worldDir);
            if (!success) {
                throw new IllegalStateException("Need to rename legacy folder, but not able to");
            }
            Journeymap.getLogger().info(String.format("Migrated legacy folder from %s to %s", legacyWorldDir.getName(), worldDir.getName()));
        }
        catch (Exception e) {
            JMLogger.logOnce(String.format("Failed to migrate legacy folder from %s to %s", legacyWorldDir.getName(), worldDir.getName()), e);
            final String tempName = worldDir.getName() + "__OLD";
            try {
                success = legacyWorldDir.renameTo(new File(legacyWorldDir.getParentFile(), tempName));
            }
            catch (Exception e2) {
                success = false;
            }
            if (!success) {
                JMLogger.logOnce(String.format("Failed to even rename legacy folder from %s to %s", legacyWorldDir.getName(), tempName), e);
            }
        }
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
        }
        catch (IOException e) {
            final String error = "Could not get language file " + fileName + ": " + e.getMessage();
            Journeymap.getLogger().error(error);
            return null;
        }
    }
    
    public static <M> M getMessageModel(final Class<M> model, final String filePrefix) {
        try {
            final String lang = Minecraft.func_71410_x().func_135016_M().func_135041_c().func_135034_a();
            InputStream is = getMessageModelInputStream(filePrefix, lang);
            if (is == null && !lang.equals("en_US")) {
                is = getMessageModelInputStream(filePrefix, "en_US");
            }
            if (is == null) {
                Journeymap.getLogger().warn("Message file not found: " + filePrefix);
                return null;
            }
            return (M)new GsonBuilder().create().fromJson((Reader)new InputStreamReader(is), (Class)model);
        }
        catch (Throwable e) {
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
        }
        catch (IOException e) {
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
            if (file.exists() && (file.getName().endsWith(".jar") || file.getName().endsWith(".jar"))) {
                return true;
            }
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
        }
        catch (Throwable t) {
            Journeymap.getLogger().warn("Couldn't copy color palette html: " + t);
            return null;
        }
    }
    
    public static void open(final File file) {
        final String path = file.getAbsolutePath();
        Label_0156: {
            if (Util.func_110647_a() == Util.EnumOS.OSX) {
                try {
                    Runtime.getRuntime().exec(new String[] { "/usr/bin/open", path });
                    return;
                }
                catch (IOException e) {
                    Journeymap.getLogger().error("Could not open path with /usr/bin/open: " + path + " : " + LogFormatter.toString(e));
                    break Label_0156;
                }
            }
            if (Util.func_110647_a() == Util.EnumOS.WINDOWS) {
                final String cmd = String.format("cmd.exe /C start \"Open file\" \"%s\"", path);
                try {
                    Runtime.getRuntime().exec(cmd);
                    return;
                }
                catch (IOException e2) {
                    Journeymap.getLogger().error("Could not open path with cmd.exe: " + path + " : " + LogFormatter.toString(e2));
                }
            }
            try {
                final Class desktopClass = Class.forName("java.awt.Desktop");
                final Object method = desktopClass.getMethod("getDesktop", (Class[])new Class[0]).invoke(null, new Object[0]);
                desktopClass.getMethod("browse", URI.class).invoke(method, file.toURI());
            }
            catch (Throwable e3) {
                Journeymap.getLogger().error("Could not open path with Desktop: " + path + " : " + LogFormatter.toString(e3));
                Sys.openURL("file://" + path);
            }
        }
    }
    
    public static boolean copyResources(final File targetDirectory, final ResourceLocation location, final String setName, final boolean overwrite) {
        final String fromPath = null;
        final File toDir = null;
        try {
            final String domain = location.func_110624_b();
            URL fileLocation = null;
            if (domain.equals("minecraft")) {
                fileLocation = Minecraft.class.getProtectionDomain().getCodeSource().getLocation();
            }
            else {
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
                if (location.func_110623_a().startsWith("assets/")) {
                    assetsPath = location.func_110623_a();
                }
                else {
                    assetsPath = String.format("assets/%s/%s", domain, location.func_110623_a());
                }
                return copyResources(targetDirectory, fileLocation, assetsPath, setName, overwrite);
            }
        }
        catch (Throwable t) {
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
            }
            catch (MalformedURLException e) {
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
                }
                else {
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
        }
        catch (Throwable t) {
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
                        new ZipEntryByteSource(zipFile, entry).copyTo(Files.asByteSink(toFile, new FileWriteMode[0]));
                        success = true;
                    }
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
        finally {
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
            }
            else if (overWrite || !to.exists()) {
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
        final Util.EnumOS os = Util.func_110647_a();
        switch (os) {
            case WINDOWS: {
                cmd = new String[] { String.format("cmd.exe /C RD /S /Q \"%s\"", path) };
                break;
            }
            case OSX: {
                cmd = new String[] { "rm", "-rf", path };
                break;
            }
            default: {
                cmd = new String[] { "rm", "-rf", path };
                break;
            }
        }
        try {
            final ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            final Process p = pb.start();
            p.waitFor();
        }
        catch (Throwable e) {
            Journeymap.getLogger().error(String.format("Could not delete using: %s : %s", Joiner.on(" ").join((Object[])cmd), LogFormatter.toString(e)));
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
            final String filePath = Joiner.on(File.separatorChar).join((Object)setName, (Object)iconPath.replace('/', File.separatorChar), new Object[0]);
            iconFile = new File(parentdir, filePath);
            if (iconFile.exists()) {
                img = getImage(iconFile);
            }
        }
        catch (Exception e) {
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
        }
        catch (IOException e) {
            final String error = String.format("Could not get icon from resource: %s, %s, %s : %s", assetsPath, setName, iconPath, e.getMessage());
            Journeymap.getLogger().error(error);
            return null;
        }
    }
    
    public static InputStream getIconStream(final String assetsPath, final String setName, final String iconPath) {
        try {
            final String pngPath = Joiner.on('/').join((Object)assetsPath, (Object)setName, new Object[] { iconPath });
            final InputStream is = JourneymapClient.class.getResourceAsStream(pngPath);
            if (is == null) {
                Journeymap.getLogger().warn(String.format("Icon Set asset not found: " + pngPath, new Object[0]));
                return null;
            }
            return is;
        }
        catch (Throwable e) {
            final String error = String.format("Could not get icon stream: %s, %s, %s : %s", assetsPath, setName, iconPath, e.getMessage());
            Journeymap.getLogger().error(error);
            return null;
        }
    }
    
    static {
        MinecraftDirectory = getMinecraftDirectory();
        JourneyMapDirectory = new File(FileHandler.MinecraftDirectory, Constants.JOURNEYMAP_DIR);
        StandardConfigDirectory = new File(FileHandler.MinecraftDirectory, Constants.CONFIG_DIR);
    }
    
    private static class ZipEntryByteSource extends ByteSource
    {
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
