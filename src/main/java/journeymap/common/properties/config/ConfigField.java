package journeymap.common.properties.config;

import journeymap.common.properties.*;
import journeymap.common.*;
import journeymap.common.log.*;
import java.util.*;
import com.google.common.base.*;

public abstract class ConfigField<T>
{
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_CATEGORY = "category";
    public static final String ATTR_KEY = "key";
    public static final String ATTR_LABEL = "label";
    public static final String ATTR_TOOLTIP = "tooltip";
    public static final String ATTR_ORDER = "order";
    public static final String ATTR_VALUE = "value";
    public static final String ATTR_DEFAULT = "default";
    public static final String ATTR_VALID_VALUES = "validValues";
    protected final transient Map<String, Object> attributes;
    protected transient PropertiesBase owner;
    protected transient String fieldName;
    
    public ConfigField() {
        this.attributes = new TreeMap<String, Object>();
        this.put("type", this.getClass().getSimpleName());
    }
    
    protected ConfigField(final Category category) {
        this.attributes = new TreeMap<String, Object>();
        this.put("type", this.getClass().getSimpleName());
        this.put("category", category);
    }
    
    protected ConfigField(final Category category, final String key) {
        this.attributes = new TreeMap<String, Object>();
        this.put("type", this.getClass().getSimpleName());
        this.put("category", category);
        this.put("key", key);
    }
    
    public String getStringAttr(final String attrName) {
        final Object value = this.attributes.get(attrName);
        if (value == null) {
            return null;
        }
        if (value instanceof Enum) {
            return ((Enum)value).name();
        }
        if (value instanceof Class) {
            return ((Class)value).getCanonicalName();
        }
        return value.toString();
    }
    
    public ConfigField<T> put(final String attrName, final Object value) {
        this.attributes.put(attrName, value);
        return this;
    }
    
    public abstract T getDefaultValue();
    
    public abstract T get();
    
    public ConfigField<T> set(final T value) {
        this.put("value", value);
        return this;
    }
    
    public boolean validate(final boolean fix) {
        final boolean hasRequired = this.require("type", "value", "default");
        final boolean hasCategory = this.getCategory() != null;
        return hasRequired && hasCategory;
    }
    
    public ConfigField<T> sortOrder(final int order) {
        this.put("order", order);
        return this;
    }
    
    public String getKey() {
        return this.getStringAttr("key");
    }
    
    public ConfigField<T> category(final Category category) {
        this.attributes.put("category", category);
        return this;
    }
    
    public Category getCategory() {
        final Object val = this.get("category");
        if (val instanceof Category) {
            return (Category)val;
        }
        if (val instanceof String && this.owner != null) {
            final Category category = this.owner.getCategoryByName((String)val);
            this.category(category);
            return category;
        }
        return null;
    }
    
    public String getLabel() {
        return this.getStringAttr("label");
    }
    
    public ConfigField<T> label(final String label) {
        this.attributes.put("label", label);
        return this;
    }
    
    public String getTooltip() {
        return this.getStringAttr("tooltip");
    }
    
    public String getType() {
        return this.getStringAttr("type");
    }
    
    public int getSortOrder() {
        Integer order = this.getIntegerAttr("order");
        if (order == null) {
            order = 100;
        }
        return order;
    }
    
    public Object get(final String attrName) {
        return this.attributes.get(attrName);
    }
    
    public Integer getIntegerAttr(final String attrName) {
        Object value = this.attributes.get(attrName);
        if (value instanceof Integer) {
            return (Integer)value;
        }
        if (value instanceof String) {
            try {
                value = Integer.parseInt((String)value);
                this.attributes.put(attrName, value);
                return (Integer)value;
            }
            catch (NumberFormatException e) {
                Journeymap.getLogger().warn(String.format("Couldn't get Integer %s from %s: %s", attrName, value, e.getMessage()));
            }
        }
        return null;
    }
    
    public Boolean getBooleanAttr(final String attrName) {
        Object value = this.attributes.get(attrName);
        if (value instanceof Boolean) {
            return (Boolean)value;
        }
        if (value instanceof String) {
            try {
                value = Boolean.valueOf((String)value);
                this.attributes.put(attrName, value);
                return (Boolean)value;
            }
            catch (NumberFormatException e) {
                Journeymap.getLogger().warn(String.format("Couldn't get Boolean %s from %s: %s", attrName, value, e.getMessage()));
            }
        }
        return null;
    }
    
    public <E extends Enum> E getEnumAttr(final String attrName, final Class<E> enumType) {
        if (!Strings.isNullOrEmpty(attrName) && enumType != null) {
            final Object value = this.attributes.get(attrName);
            if (value instanceof Enum) {
                return (E)value;
            }
            if (value instanceof String) {
                try {
                    for (final Object enumValue : EnumSet.allOf(enumType)) {
                        if (((Enum)enumValue).name().equalsIgnoreCase((String)value)) {
                            return (E)enumValue;
                        }
                    }
                }
                catch (Exception e) {
                    Journeymap.getLogger().warn(String.format("Couldn't get %s as Enum %s with value %s: %s", attrName, enumType, value, LogFormatter.toString(e)));
                }
            }
        }
        this.setToDefault();
        return this.getDefaultValue();
    }
    
    public void setToDefault() {
        this.set(this.getDefaultValue());
    }
    
    public ConfigField<T> defaultValue(final T defaultValue) {
        if (defaultValue == null) {
            Journeymap.getLogger().warn("defaultValue shouldn't be null");
        }
        this.put("default", defaultValue);
        return this;
    }
    
    protected boolean require(final String... attrNames) {
        boolean pass = true;
        for (final String attrName : attrNames) {
            final Object attr = this.get(attrName);
            if (attr == null) {
                Journeymap.getLogger().warn(String.format("Missing required attribute '%s' in %s", attrName, this.getDeclaredField()));
                pass = false;
            }
        }
        return pass;
    }
    
    public Map<String, Object> getAttributeMap() {
        return this.attributes;
    }
    
    public Set<String> getAttributeNames() {
        return this.attributes.keySet();
    }
    
    public PropertiesBase getOwner() {
        return this.owner;
    }
    
    public void setOwner(final String fieldName, final PropertiesBase properties) {
        this.fieldName = fieldName;
        this.owner = properties;
    }
    
    public boolean save() {
        return this.owner != null && this.owner.save();
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConfigField)) {
            return false;
        }
        final ConfigField<?> that = (ConfigField<?>)o;
        return Objects.equal((Object)this.getKey(), (Object)that.getKey()) && this.getCategory() == that.getCategory() && Objects.equal(this.get(), (Object)that.get());
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(new Object[] { this.getKey(), this.getCategory(), this.get() });
    }
    
    public String getDeclaredField() {
        if (this.owner == null) {
            return null;
        }
        return String.format("%s.%s", this.owner.getClass().getSimpleName(), this.fieldName);
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("on", (Object)this.getDeclaredField()).add("attributes", (Object)this.attributes).toString();
    }
}
