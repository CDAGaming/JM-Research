package journeymap.server.properties;

import journeymap.common.properties.*;
import java.util.*;
import com.google.gson.*;
import java.io.*;
import journeymap.server.*;

public abstract class ServerPropertiesBase extends PropertiesBase implements Cloneable
{
    protected final String displayName;
    protected final String description;
    
    protected ServerPropertiesBase(final String displayName, final String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    @Override
    public String[] getHeaders() {
        return new String[] { "// JourneyMap server configuration file. Modify at your own risk!", "// To restore the default settings, simply delete this file before starting Minecraft server", "// For more information, go to: http://journeymap.info/JourneyMapServer", "//", String.format("// %s : %s ", this.displayName, this.description) };
    }
    
    @Override
    public <T extends PropertiesBase> void updateFrom(final T otherInstance) {
        super.updateFrom(otherInstance);
    }
    
    public <T extends PropertiesBase> T load(final String jsonString, final boolean verbose) {
        this.ensureInit();
        try {
            final T jsonInstance = this.fromJsonString(jsonString, (Class<T>)this.getClass(), verbose);
            this.updateFrom(jsonInstance);
            this.postLoad(false);
            this.currentState = State.FileLoaded;
            if (!this.isValid(true)) {
                return null;
            }
            return (T)this;
        }
        catch (Exception e) {
            this.error(String.format("Can't load JSON string: %s", jsonString), e);
            return null;
        }
    }
    
    @Override
    public Category getCategoryByName(final String name) {
        Category category = super.getCategoryByName(name);
        if (category == null) {
            category = ServerCategory.valueOf(name);
        }
        return category;
    }
    
    @Override
    public List<ExclusionStrategy> getExclusionStrategies(final boolean verbose) {
        final List<ExclusionStrategy> strategies = super.getExclusionStrategies(verbose);
        if (!verbose) {
            strategies.add((ExclusionStrategy)new ExclusionStrategy() {
                public boolean shouldSkipField(final FieldAttributes f) {
                    return f.getDeclaringClass().equals(ServerPropertiesBase.class) && (f.getName().equals("displayName") || f.getName().equals("description"));
                }
                
                public boolean shouldSkipClass(final Class<?> clazz) {
                    return false;
                }
            });
        }
        return strategies;
    }
    
    @Override
    public boolean isValid(final boolean fix) {
        final boolean valid = super.isValid(fix);
        return valid;
    }
    
    @Override
    public String getFileName() {
        return String.format("journeymap.server.%s.config", this.getName());
    }
    
    @Override
    public File getFile() {
        if (this.sourceFile == null) {
            this.sourceFile = new File(Constants.CONFIG_DIR, this.getFileName());
        }
        return this.sourceFile;
    }
    
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
