package journeymap.common.properties.config;

import journeymap.common.properties.*;
import journeymap.common.*;
import java.util.*;

public class EnumField<E extends Enum> extends ConfigField<E>
{
    public static final String ATTR_ENUM_TYPE = "enumType";
    
    protected EnumField() {
    }
    
    public EnumField(final Category category, final String key, final E defaultValue) {
        super(category, key);
        this.put("enumType", defaultValue.getClass().getName());
        this.defaultValue(defaultValue);
        this.setToDefault();
    }
    
    @Override
    public E getDefaultValue() {
        return this.getEnumAttr("default", this.getEnumClass());
    }
    
    @Override
    public EnumField<E> set(final E value) {
        this.put("value", value.name());
        return this;
    }
    
    @Override
    public E get() {
        return this.getEnumAttr("value", this.getEnumClass());
    }
    
    public Class<E> getEnumClass() {
        Object value = this.get("enumType");
        if (value instanceof Class) {
            return (Class<E>)value;
        }
        if (value instanceof String) {
            try {
                value = Class.forName((String)value);
                this.attributes.put("enumType", value);
                return (Class<E>)value;
            }
            catch (Exception e) {
                Journeymap.getLogger().warn(String.format("Couldn't get Enum Class %s : %s", "enumType", e.getMessage()));
            }
        }
        return null;
    }
    
    public Set<E> getValidValues() {
        final Class<? extends Enum> enumClass = this.getEnumClass();
        return (Set<E>)EnumSet.allOf(enumClass);
    }
    
    @Override
    public boolean validate(final boolean fix) {
        return this.require("enumType") && super.validate(fix);
    }
}
