package modinfo;

import org.apache.logging.log4j.*;
import java.util.*;
import journeymap.client.io.*;
import com.google.gson.*;
import java.io.*;

public class Config implements Serializable
{
    private static final String[] HEADERS;
    private static final String PARENT_DIR = "config";
    private static final String FILE_PATTERN = "%s_ModInfo.cfg";
    private static final String ENABLED_STATUS_PATTERN = "Enabled (%s)";
    private static final String DISABLED_STATUS_PATTERN = "Disabled (%s)";
    private String modId;
    private Boolean enable;
    private String salt;
    private String status;
    private Boolean verbose;
    
    public static synchronized Config getInstance(final String modId) {
        Config config = null;
        final File configFile = getFile(modId);
        if (configFile.exists()) {
            try {
                final Gson gson = new Gson();
                config = (Config)gson.fromJson((Reader)new FileReader(configFile), (Class)Config.class);
            }
            catch (Exception e) {
                ModInfo.LOGGER.log(Level.ERROR, "Can't read file " + configFile, (Object)e.getMessage());
                if (configFile.exists()) {
                    configFile.delete();
                }
            }
        }
        if (config == null) {
            config = new Config();
        }
        config.validate(modId);
        return config;
    }
    
    static boolean isConfirmedDisabled(final Config config) {
        return !config.enable && generateStatusString(config).equals(config.status);
    }
    
    static String generateStatusString(final Config config) {
        return generateStatusString(config.modId, config.enable);
    }
    
    static String generateStatusString(final String modId, final Boolean enable) {
        final UUID uuid = ModInfo.createUUID(modId, enable.toString());
        final String pattern = enable ? "Enabled (%s)" : "Disabled (%s)";
        return String.format(pattern, uuid.toString());
    }
    
    private static File getFile(final String modId) {
        final File dir = new File(FileHandler.getMinecraftDirectory(), "config");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, String.format("%s_ModInfo.cfg", modId.replaceAll("%", "_")));
    }
    
    private void validate(final String modId) {
        boolean dirty = false;
        if (!modId.equals(this.modId)) {
            this.modId = modId;
            dirty = true;
        }
        if (this.enable == null) {
            this.enable = Boolean.TRUE;
            dirty = true;
        }
        if (this.salt == null) {
            this.salt = Long.toHexString(System.currentTimeMillis());
            dirty = true;
        }
        if (this.verbose == null) {
            this.verbose = Boolean.FALSE;
            dirty = true;
        }
        if (dirty) {
            this.save();
        }
    }
    
    public void save() {
        final File configFile = getFile(this.modId);
        try {
            final String lineEnding = System.getProperty("line.separator");
            final StringBuilder sb = new StringBuilder();
            for (final String line : Config.HEADERS) {
                sb.append(line).append(lineEnding);
            }
            final String header = String.format(sb.toString(), "0.2", this.modId);
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();
            final String json = gson.toJson((Object)this);
            final FileWriter fw = new FileWriter(configFile);
            fw.write(header);
            fw.write(json);
            fw.flush();
            fw.close();
        }
        catch (IOException e) {
            ModInfo.LOGGER.log(Level.ERROR, "Can't save file " + configFile, (Throwable)e);
        }
    }
    
    public String getSalt() {
        return this.salt;
    }
    
    public String getModId() {
        return this.modId;
    }
    
    public Boolean isEnabled() {
        return this.enable;
    }
    
    public Boolean isVerbose() {
        return this.verbose;
    }
    
    public String getStatus() {
        return this.status;
    }
    
    void disable() {
        this.enable = false;
        this.confirmStatus();
    }
    
    public void confirmStatus() {
        final String newStatus = generateStatusString(this);
        if (!newStatus.equals(this.status)) {
            this.status = newStatus;
            this.save();
        }
    }
    
    static {
        HEADERS = new String[] { "// ModInfo v%s - Configuration file for %s", "// ModInfo is a simple utility which helps the Mod developer support their mod.", "// For more information: https://github.com/MCModInfo/modinfo/blob/master/README.md" };
    }
}
