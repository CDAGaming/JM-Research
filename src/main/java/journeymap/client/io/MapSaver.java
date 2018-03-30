package journeymap.client.io;

import java.io.*;
import journeymap.common.*;
import journeymap.client.*;
import journeymap.client.log.*;
import journeymap.common.log.*;
import journeymap.client.model.*;
import net.minecraftforge.fml.client.*;
import java.util.*;
import journeymap.client.data.*;
import com.google.common.base.*;
import net.minecraft.client.*;
import java.util.regex.*;
import java.text.*;

public class MapSaver
{
    private static final DateFormat dateFormat;
    final File worldDir;
    final MapView mapView;
    File saveFile;
    int outputColumns;
    int outputRows;
    ArrayList<File> files;
    
    public MapSaver(final File worldDir, final MapView mapView) {
        this.worldDir = worldDir;
        this.mapView = mapView;
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
        }
        catch (OutOfMemoryError e) {
            final String error = "Out Of Memory: Increase Java Heap Size for Minecraft to save large maps.";
            Journeymap.getLogger().error(error);
            ChatLog.announceError(error);
            timer.cancel();
        }
        catch (Throwable t) {
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
        final RegionCoord fakeRc = new RegionCoord(this.worldDir, 0, 0, this.mapView.dimension);
        return RegionImageHandler.getImageDir(fakeRc, this.mapView);
    }
    
    private void prepareFiles() {
        try {
            final Minecraft mc = FMLClientHandler.instance().getClient();
            final String date = MapSaver.dateFormat.format(new Date());
            final String worldName = WorldData.getWorldName(mc, false);
            final String dimName = WorldData.getSafeDimensionName(new WorldData.WrappedProvider(Journeymap.clientWorld().field_73011_w));
            final String fileName = Joiner.on("_").skipNulls().join((Object)date, (Object)worldName, new Object[] { dimName, this.mapView.name().toLowerCase(), this.mapView.vSlice }) + ".png";
            final File screenshotsDir = new File(FileHandler.getMinecraftDirectory(), "screenshots");
            if (!screenshotsDir.exists()) {
                screenshotsDir.mkdir();
            }
            this.saveFile = new File(screenshotsDir, fileName);
            RegionImageCache.INSTANCE.flushToDisk(false);
            final File imageDir = this.getImageDir();
            final File[] pngFiles = imageDir.listFiles();
            final Pattern tilePattern = Pattern.compile("([^\\.]+)\\,([^\\.]+)\\.png");
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
            this.files = new ArrayList<File>(this.outputColumns * this.outputRows);
            for (int rz = minZ; rz <= maxZ; ++rz) {
                for (int rx = minX; rx <= maxX; ++rx) {
                    final RegionCoord rc = new RegionCoord(this.worldDir, rx, rz, this.mapView.dimension);
                    final File rfile = RegionImageHandler.getRegionImageFile(rc, this.mapView, true);
                    if (rfile.canRead()) {
                        this.files.add(rfile);
                    }
                    else {
                        this.files.add(RegionImageHandler.getBlank512x512ImageFile());
                    }
                }
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().error(LogFormatter.toPartialString(t));
        }
    }
    
    static {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    }
}
