package journeymap.common.properties;

import java.nio.charset.*;
import journeymap.common.version.*;
import java.io.*;
import journeymap.common.properties.config.*;
import journeymap.common.*;
import com.google.common.io.*;
import journeymap.common.log.*;
import java.lang.reflect.*;
import java.util.*;
import com.google.gson.*;
import com.google.common.base.*;

public abstract class PropertiesBase
{
    protected static final Charset UTF8;
    protected Version configVersion;
    protected CategorySet categories;
    protected transient File sourceFile;
    protected transient State currentState;
    private transient Map<String, ConfigField<?>> configFields;
    
    protected PropertiesBase() {
        this.configVersion = null;
        this.categories = new CategorySet();
        this.sourceFile = null;
        this.currentState = State.New;
    }
    
    public Gson getGson(final boolean verbose) {
        final GsonBuilder gb = verbose ? GsonHelper.BUILDER_VERBOSE() : GsonHelper.BUILDER_TERSE();
        final List<ExclusionStrategy> exclusionStrategies = this.getExclusionStrategies(verbose);
        if (exclusionStrategies != null && !exclusionStrategies.isEmpty()) {
            gb.setExclusionStrategies((ExclusionStrategy[])exclusionStrategies.toArray(new ExclusionStrategy[exclusionStrategies.size()]));
        }
        return gb.create();
    }
    
    public <T extends PropertiesBase> T fromJsonString(final String jsonString, final Class<T> propertiesClass, final boolean verbose) {
        return (T)this.getGson(verbose).fromJson(jsonString, (Class)propertiesClass);
    }
    
    public abstract String getName();
    
    public abstract File getFile();
    
    public abstract String[] getHeaders();
    
    public abstract String getFileName();
    
    public boolean isCurrent() {
        return Journeymap.JM_VERSION.equals(this.configVersion);
    }
    
    public <T extends PropertiesBase> T load() {
        return this.load(this.getFile(), false);
    }
    
    public <T extends PropertiesBase> T load(final File configFile, final boolean verbose) {
        this.ensureInit();
        boolean saveNeeded = false;
        if (!configFile.canRead() || configFile.length() == 0L) {
            this.postLoad(true);
            this.currentState = State.FirstLoaded;
            saveNeeded = true;
        }
        else {
            try {
                final String jsonString = Files.toString(configFile, PropertiesBase.UTF8);
                final T jsonInstance = this.fromJsonString(jsonString, this.getClass(), verbose);
                this.updateFrom(jsonInstance);
                this.postLoad(false);
                this.currentState = State.FileLoaded;
                saveNeeded = !this.isValid(false);
            }
            catch (Exception e) {
                this.error(String.format("Can't load config file %s", configFile), e);
                try {
                    final File badPropFile = new File(configFile.getParentFile(), configFile.getName() + ".bad");
                    configFile.renameTo(badPropFile);
                }
                catch (Exception e2) {
                    this.error(String.format("Can't rename config file %s: %s", configFile, e2.getMessage()));
                }
            }
        }
        if (saveNeeded) {
            this.save(configFile, verbose);
        }
        return (T)this;
    }
    
    protected void postLoad(final boolean isNew) {
        this.ensureInit();
    }
    
    public <T extends PropertiesBase> void updateFrom(final T otherInstance) {
        for (final Map.Entry<String, ConfigField<?>> otherEntry : otherInstance.getConfigFields().entrySet()) {
            final String fieldName = otherEntry.getKey();
            final ConfigField<?> otherField = otherEntry.getValue();
            if (Strings.isNullOrEmpty(fieldName) || otherField == null) {
                this.warn("Bad configField entry during updateFrom(): " + otherEntry);
            }
            else if (otherField.getAttributeMap() == null) {
                this.warn("Bad configField source (no attributes) during updateFrom(): " + fieldName);
            }
            else {
                final ConfigField<?> myField = this.getConfigField(fieldName);
                if (myField == null) {
                    this.warn("configField target doesn't exist during updateFrom(): " + fieldName);
                }
                else if (myField.getAttributeMap() == null) {
                    this.warn("Bad configField target (no attributes) during updateFrom(): " + fieldName);
                }
                else {
                    myField.getAttributeMap().putAll(otherField.getAttributeMap());
                }
            }
        }
        this.configVersion = otherInstance.configVersion;
    }
    
    protected void ensureInit() {
        if (this.configFields == null) {
            this.getConfigFields();
            this.currentState = State.Initialized;
        }
    }
    
    protected void preSave() {
        this.ensureInit();
    }
    
    public boolean save() {
        return this.save(this.getFile(), false);
    }
    
    public boolean save(final File configFile, final boolean verbose) {
        this.preSave();
        boolean saved = false;
        final boolean canSave = this.isValid(true);
        if (!canSave) {
            this.error(String.format("Can't save invalid config to file: %s", this.getFileName()));
        }
        else {
            try {
                if (!configFile.exists()) {
                    this.info(String.format("Creating config file: %s", configFile));
                    if (!configFile.getParentFile().exists()) {
                        configFile.getParentFile().mkdirs();
                    }
                }
                else if (!this.isCurrent()) {
                    if (this.configVersion != null) {
                        this.info(String.format("Updating config file from version \"%s\" to \"%s\": %s", this.configVersion, Journeymap.JM_VERSION, configFile));
                    }
                    this.configVersion = Journeymap.JM_VERSION;
                }
                final StringBuilder sb = new StringBuilder();
                final String lineEnding = System.getProperty("line.separator");
                for (final String line : this.getHeaders()) {
                    sb.append(line).append(lineEnding);
                }
                final String header = sb.toString();
                final String json = this.toJsonString(verbose);
                Files.write((CharSequence)(header + json), configFile, PropertiesBase.UTF8);
                saved = true;
            }
            catch (Exception e) {
                this.error(String.format("Can't save config file %s: %s", configFile, e), e);
            }
        }
        this.currentState = (saved ? State.SavedOk : State.SavedError);
        return saved;
    }
    
    public String toJsonString(final boolean verbose) {
        this.ensureInit();
        return this.getGson(verbose).toJson((Object)this);
    }
    
    public boolean isValid(final boolean fix) {
        this.ensureInit();
        boolean valid = this.validateFields(fix);
        if (!this.isCurrent()) {
            if (fix) {
                this.configVersion = Journeymap.JM_VERSION;
                this.info(String.format("Setting config file to version \"%s\": %s", this.configVersion, this.getFileName()));
            }
            else {
                valid = false;
                this.info(String.format("Config file isn't current, has version \"%s\": %s", this.configVersion, this.getFileName()));
            }
        }
        this.currentState = (valid ? State.Valid : State.Invalid);
        return valid;
    }
    
    protected ConfigField<?> getConfigField(final String fieldName) {
        return this.getConfigFields().get(fieldName);
    }
    
    public Map<String, ConfigField<?>> getConfigFields() {
        if (this.configFields == null) {
            final HashMap<String, ConfigField<?>> map = new HashMap<String, ConfigField<?>>();
            try {
                for (final Field field : this.getClass().getFields()) {
                    final Class<?> fieldType = field.getType();
                    if (ConfigField.class.isAssignableFrom(fieldType)) {
                        final ConfigField configField = (ConfigField)field.get(this);
                        if (configField != null) {
                            configField.setOwner(field.getName(), this);
                            final Category category = configField.getCategory();
                            if (category != null) {
                                this.categories.add(category);
                            }
                        }
                        map.put(field.getName(), configField);
                    }
                }
            }
            catch (Throwable t) {
                this.error("Unexpected error getting fields: " + LogFormatter.toString(t));
            }
            this.configFields = Collections.unmodifiableMap((Map<? extends String, ? extends ConfigField<?>>)map);
        }
        return this.configFields;
    }
    
    public Category getCategoryByName(final String name) {
        for (final Category category : this.categories) {
            if (category.getName().equalsIgnoreCase(name)) {
                return category;
            }
        }
        return null;
    }
    
    protected boolean validateFields(final boolean fix) {
        try {
            boolean valid = true;
            for (final Map.Entry<String, ConfigField<?>> entry : this.getConfigFields().entrySet()) {
                final ConfigField<?> configField = entry.getValue();
                if (configField == null) {
                    this.warn(String.format("%s.%s is null", this.getClass().getSimpleName(), entry.getKey()));
                    valid = false;
                }
                else {
                    final boolean fieldValid = configField.validate(fix);
                    if (fieldValid) {
                        continue;
                    }
                    valid = false;
                }
            }
            return valid;
        }
        catch (Throwable t) {
            this.error("Unexpected error in validateFields: " + LogFormatter.toPartialString(t));
            return false;
        }
    }
    
    public List<ExclusionStrategy> getExclusionStrategies(final boolean verbose) {
        final ArrayList strategies = new ArrayList();
        if (!verbose) {
            strategies.add(new ExclusionStrategy() {
                public boolean shouldSkipField(final FieldAttributes f) {
                    return f.getDeclaringClass().equals(PropertiesBase.class) && f.getName().equals("categories");
                }
                
                public boolean shouldSkipClass(final Class<?> clazz) {
                    return false;
                }
            });
        }
        return (List<ExclusionStrategy>)strategies;
    }
    
    public long lastModified() {
        final File file = this.getFile();
        if (file.canRead()) {
            return file.lastModified();
        }
        return 0L;
    }
    
    protected MoreObjects.ToStringHelper toStringHelper() {
        final MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper((Object)this).add("state", (Object)this.currentState).add("file", (Object)this.getFileName()).add("configVersion", (Object)this.configVersion);
        return toStringHelper;
    }
    
    @Override
    public String toString() {
        final MoreObjects.ToStringHelper toStringHelper = this.toStringHelper();
        for (final Map.Entry<String, ConfigField<?>> entry : this.getConfigFields().entrySet()) {
            final ConfigField<?> configField = entry.getValue();
            toStringHelper.add((String)entry.getKey(), (Object)configField.get());
        }
        return toStringHelper.toString();
    }
    
    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PropertiesBase)) {
            return false;
        }
        final PropertiesBase that = (PropertiesBase)o;
        return Objects.equal((Object)this.getFileName(), (Object)that.getFileName());
    }
    
    @Override
    public final int hashCode() {
        return Objects.hashCode(new Object[] { this.getConfigFields() });
    }
    
    protected void info(final String message) {
        Journeymap.getLogger().info(String.format("%s (%s) %s", this.getName(), this.currentState, message));
    }
    
    protected void warn(final String message) {
        Journeymap.getLogger().warn(String.format("%s (%s) %s", this.getName(), this.currentState, message));
    }
    
    protected void error(final String message) {
        Journeymap.getLogger().error(String.format("%s (%s) %s", this.getName(), this.currentState, message));
    }
    
    protected void error(final String message, final Throwable throwable) {
        Journeymap.getLogger().error(String.format("%s (%s) %s : %s", this.getName(), this.currentState, message, LogFormatter.toString(throwable)));
    }
    
    static {
        UTF8 = Charset.forName("UTF-8");
    }
    
    protected enum State
    {
        New, 
        Initialized, 
        FirstLoaded, 
        FileLoaded, 
        Valid, 
        Invalid, 
        SavedOk, 
        SavedError;
    }
}
