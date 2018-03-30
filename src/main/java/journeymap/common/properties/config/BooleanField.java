package journeymap.common.properties.config;

import journeymap.common.properties.*;

public class BooleanField extends ConfigField<Boolean>
{
    public static final String ATTR_CATEGORY_MASTER = "isMaster";
    
    protected BooleanField() {
    }
    
    public BooleanField(final Category category, final boolean defaultValue) {
        this(category, null, defaultValue);
    }
    
    public BooleanField(final Category category, final String key, final boolean defaultValue) {
        this(category, key, defaultValue, false);
    }
    
    public BooleanField(final Category category, final String key, final boolean defaultValue, final boolean isMaster) {
        super(category, key);
        this.defaultValue(defaultValue);
        this.setToDefault();
        this.categoryMaster(isMaster);
    }
    
    @Override
    public Boolean getDefaultValue() {
        return this.getBooleanAttr("default");
    }
    
    @Override
    public BooleanField set(final Boolean value) {
        this.put("value", value);
        return this;
    }
    
    @Override
    public Boolean get() {
        return this.getBooleanAttr("value");
    }
    
    public boolean toggle() {
        this.set(Boolean.valueOf(!this.get()));
        return this.get();
    }
    
    public boolean toggleAndSave() {
        this.set(Boolean.valueOf(!this.get()));
        this.save();
        return this.get();
    }
    
    public boolean isCategoryMaster() {
        return this.getBooleanAttr("isMaster");
    }
    
    public BooleanField categoryMaster(final boolean isMaster) {
        this.put("isMaster", isMaster);
        return this;
    }
}
