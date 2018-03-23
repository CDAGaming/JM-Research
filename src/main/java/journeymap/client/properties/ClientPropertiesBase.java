package journeymap.client.properties;

import journeymap.client.io.*;
import net.minecraftforge.fml.client.*;
import com.google.common.io.*;
import java.io.*;
import journeymap.common.properties.*;
import journeymap.common.log.*;
import journeymap.client.*;

public abstract class ClientPropertiesBase extends PropertiesBase
{
    private static final String[] HEADERS;
    
    @Override
    public String getFileName() {
        return String.format("journeymap.%s.config", this.getName());
    }
    
    @Override
    public File getFile() {
        if (this.sourceFile == null) {
            this.sourceFile = new File(FileHandler.getWorldConfigDir(false), this.getFileName());
            if (!this.sourceFile.canRead()) {
                this.sourceFile = new File(FileHandler.StandardConfigDirectory, this.getFileName());
            }
        }
        return this.sourceFile;
    }
    
    public boolean isWorldConfig() {
        if (FMLClientHandler.instance().getClient() != null) {
            final File worldConfigDir = FileHandler.getWorldConfigDir(false);
            return worldConfigDir != null && worldConfigDir.equals(this.getFile().getParentFile());
        }
        return false;
    }
    
    @Override
    public <T extends PropertiesBase> void updateFrom(final T otherInstance) {
        super.updateFrom(otherInstance);
    }
    
    public boolean copyToWorldConfig(final boolean overwrite) {
        if (!this.isWorldConfig()) {
            try {
                final File worldConfig = this.getFile();
                if (overwrite || !worldConfig.exists()) {
                    this.save();
                    Files.copy(this.sourceFile, worldConfig);
                    return worldConfig.canRead();
                }
            }
            catch (IOException e) {
                this.error("Couldn't copy config to world config: " + e, e);
            }
            return false;
        }
        throw new IllegalStateException("Can't create World config from itself.");
    }
    
    @Override
    public boolean isValid(final boolean fix) {
        final boolean valid = super.isValid(fix);
        return valid;
    }
    
    @Override
    public String[] getHeaders() {
        return ClientPropertiesBase.HEADERS;
    }
    
    @Override
    public Category getCategoryByName(final String name) {
        Category category = super.getCategoryByName(name);
        if (category == null) {
            category = ClientCategory.valueOf(name);
        }
        return category;
    }
    
    public boolean copyToStandardConfig() {
        if (this.isWorldConfig()) {
            try {
                this.save();
                final File standardConfig = new File(FileHandler.StandardConfigDirectory, this.getFileName());
                Files.copy(this.sourceFile, standardConfig);
                return standardConfig.canRead();
            }
            catch (IOException e) {
                this.error("Couldn't copy config to world config: " + LogFormatter.toString(e));
                return false;
            }
        }
        throw new IllegalStateException("Can't replace standard config with itself.");
    }
    
    static {
        HEADERS = new String[] { "// " + Constants.getString("jm.config.file_header_1"), "// " + Constants.getString("jm.config.file_header_2", Constants.CONFIG_DIR), "// " + Constants.getString("jm.config.file_header_5", "http://journeymap.info/Options_Manager") };
    }
}
