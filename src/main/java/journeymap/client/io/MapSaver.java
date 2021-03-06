package journeymap.client.io;

import com.google.common.base.Joiner;
import journeymap.client.Constants;
import journeymap.client.data.WorldData;
import journeymap.client.log.ChatLog;
import journeymap.client.log.StatTimer;
import journeymap.client.model.MapType;
import journeymap.client.model.RegionCoord;
import journeymap.client.model.RegionImageCache;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapSaver {
    private static final DateFormat dateFormat;

    static {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    }

    final File worldDir;
    final MapType mapType;
    File saveFile;
    int outputColumns;
    int outputRows;
    ArrayList<File> files;

    public MapSaver(final File worldDir, final MapType mapType) {
        this.worldDir = worldDir;
        this.mapType = mapType;
        this.prepareFiles();
    }

    public File saveMap() {
        final StatTimer timer = StatTimer.get("MapSaver.saveMap");
        try {
            if (!this.isValid()) {
                Journeymap.getLogger().warn("No images found in " + this.getImageDir());
                return null;
            }
            RegionImageCache.INSTANCE.flushToDisk(false);
            timer.start();
            final File[] fileArray = this.files.toArray(new File[this.files.size()]);
            PngjHelper.mergeFiles(fileArray, this.saveFile, this.outputColumns, 512);
            timer.stop();
            Journeymap.getLogger().info("Map filesize:" + this.saveFile.length());
            final String message = Constants.getString("jm.common.map_saved", this.saveFile);
            ChatLog.announceFile(message, this.saveFile);
        } catch (OutOfMemoryError e) {
            final String error = "Out Of Memory: Increase Java Heap Size for Minecraft to save large maps.";
            Journeymap.getLogger().error(error);
            ChatLog.announceError(error);
            timer.cancel();
        } catch (Throwable t) {
            Journeymap.getLogger().error(LogFormatter.toString(t));
            timer.cancel();
            return null;
        }
        return this.saveFile;
    }

    public String getSaveFileName() {
        return this.saveFile.getName();
    }

    public boolean isValid() {
        return this.files != null && this.files.size() > 0;
    }

    private File getImageDir() {
        final RegionCoord fakeRc = new RegionCoord(this.worldDir, 0, 0, this.mapType.dimension);
        return RegionImageHandler.getImageDir(fakeRc, this.mapType);
    }

    private void prepareFiles() {
        try {
            final Minecraft mc = FMLClientHandler.instance().getClient();
            final String date = MapSaver.dateFormat.format(new Date());
            final String worldName = WorldData.getWorldName(mc, false);
            final String dimName = WorldData.getSafeDimensionName(new WorldData.WrappedProvider(mc.world.provider));
            final String fileName = Joiner.on("_").skipNulls().join(date, worldName, dimName, this.mapType.name, this.mapType.vSlice) + ".png";
            final File screenshotsDir = new File(FileHandler.getMinecraftDirectory(), "screenshots");
            if (!screenshotsDir.exists()) {
                screenshotsDir.mkdir();
            }
            this.saveFile = new File(screenshotsDir, fileName);
            RegionImageCache.INSTANCE.flushToDisk(false);
            final File imageDir = this.getImageDir();
            final File[] pngFiles = imageDir.listFiles();
            final Pattern tilePattern = Pattern.compile("([^.]+),([^.]+)\\.png");
            Integer minX = null;
            Integer minZ = null;
            Integer maxX = null;
            Integer maxZ = null;
            for (final File file : pngFiles) {
                final Matcher matcher = tilePattern.matcher(file.getName());
                if (matcher.matches()) {
                    final Integer x = Integer.parseInt(matcher.group(1));
                    final Integer z = Integer.parseInt(matcher.group(2));
                    if (minX == null || x < minX) {
                        minX = x;
                    }
                    if (minZ == null || z < minZ) {
                        minZ = z;
                    }
                    if (maxX == null || x > maxX) {
                        maxX = x;
                    }
                    if (maxZ == null || z > maxZ) {
                        maxZ = z;
                    }
                }
            }
            if (minX == null || maxX == null || minZ == null || maxZ == null) {
                Journeymap.getLogger().warn("No region files to save in " + imageDir);
                return;
            }
            final long blankSize = RegionImageHandler.getBlank512x512ImageFile().length();
            this.outputColumns = maxX - minX + 1;
            this.outputRows = maxZ - minZ + 1;
            this.files = new ArrayList<>(this.outputColumns * this.outputRows);
            for (int rz = minZ; rz <= maxZ; ++rz) {
                for (int rx = minX; rx <= maxX; ++rx) {
                    final RegionCoord rc = new RegionCoord(this.worldDir, rx, rz, this.mapType.dimension);
                    final File rfile = RegionImageHandler.getRegionImageFile(rc, this.mapType, true);
                    if (rfile.canRead()) {
                        this.files.add(rfile);
                    } else {
                        this.files.add(RegionImageHandler.getBlank512x512ImageFile());
                    }
                }
            }
        } catch (Throwable t) {
            Journeymap.getLogger().log(Level.ERROR, LogFormatter.toString(t));
        }
    }
}
