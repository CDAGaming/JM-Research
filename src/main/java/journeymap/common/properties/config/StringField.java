package journeymap.common.properties.config;

import journeymap.common.properties.*;
import com.google.common.base.*;
import journeymap.common.*;
import journeymap.common.log.*;
import java.util.*;

public class StringField extends ConfigField<String>
{
    public static final String ATTR_VALUE_PROVIDER = "valueProvider";
    public static final String ATTR_VALUE_PATTERN = "pattern";
    public static final String ATTR_MULTILINE = "multiline";
    
    protected StringField() {
    }
    
    public StringField(final Category category, final String key) {
        this(category, key, null, null);
    }
    
    public StringField(final Category category, final String key, final String[] validValues, final String defaultValue) {
        super(category, key);
        if (validValues != null) {
            this.put("validValues", Joiner.on(",").join((Object[])validValues));
        }
        if (!Strings.isNullOrEmpty(defaultValue)) {
            this.defaultValue(defaultValue);
            this.setToDefault();
        }
    }
    
    public StringField(final Category category, final String key, final Class<? extends ValuesProvider> valueProviderClass) {
        super(category, key);
        if (valueProviderClass != null) {
            this.put("valueProvider", valueProviderClass);
            try {
                final ValuesProvider valuesProvider = (ValuesProvider)valueProviderClass.newInstance();
                this.validValues(valuesProvider.getStrings());
                this.defaultValue(valuesProvider.getDefaultString());
                this.setToDefault();
                if (!this.getValidValues().contains(this.getDefaultValue())) {
                    Journeymap.getLogger().error(String.format("Default value '%s' isn't in one of the valid values '%s' for %s", this.getDefaultValue(), this.getStringAttr("validValues"), this));
                }
            }
            catch (Throwable t) {
                Journeymap.getLogger().error(String.format("Couldn't use ValuesProvider %s: %s", valueProviderClass, LogFormatter.toString(t)));
            }
        }
    }
    
    @Override
    public String getDefaultValue() {
        return this.getStringAttr("default");
    }
    
    @Override
    public String get() {
        return this.getStringAttr("value");
    }
    
    @Override
    public StringField set(final String value) {
        super.set(value);
        return this;
    }
    
    public StringField pattern(final String regexPattern) {
        this.put("pattern", regexPattern);
        return this;
    }
    
    public String getPattern() {
        return this.getStringAttr("pattern");
    }
    
    public Class<? extends ValuesProvider> getValuesProviderClass() {
        Object value = this.get("valueProvider");
        if (value == null) {
            return null;
        }
        if (value instanceof Class) {
            return (Class<? extends ValuesProvider>)value;
        }
        if (value instanceof String) {
            try {
                value = Class.forName((String)value);
                this.put("valueProvider", value);
                return (Class<? extends ValuesProvider>)value;
            }
            catch (Exception e) {
                Journeymap.getLogger().warn(String.format("Couldn't get ValuesProvider Class %s : %s", value, e.getMessage()));
            }
        }
        return null;
    }
    
    @Override
    public boolean validate(final boolean fix) {
        final boolean hasRequired = this.require("type");
        final boolean hasCategory = this.getCategory() != null;
        boolean valid = hasRequired && hasCategory;
        final String value = this.get();
        if (!Strings.isNullOrEmpty(value)) {
            final String pattern = this.getPattern();
            if (!Strings.isNullOrEmpty(pattern)) {
                final boolean patternValid = value.matches(pattern);
                if (!patternValid) {
                    Journeymap.getLogger().warn(String.format("Value '%s' doesn't match pattern '%s' for %s", value, pattern, this));
                    if (fix && !Strings.isNullOrEmpty(this.getDefaultValue())) {
                        this.setToDefault();
                        Journeymap.getLogger().warn(String.format("Value set to default '%s' for %s", this.getDefaultValue(), this));
                    }
                    else {
                        valid = false;
                    }
                }
            }
        }
        final List<String> validValues = this.getValidValues();
        if (validValues != null && !validValues.contains(value)) {
            Journeymap.getLogger().warn(String.format("Value '%s' isn't in one of the valid values '%s' for %s", value, this.getStringAttr("validValues"), this));
            final String defaultValue = this.getDefaultValue();
            if (fix && !Strings.isNullOrEmpty(defaultValue)) {
                this.setToDefault();
                Journeymap.getLogger().warn(String.format("Value set to default '%s' for %s", defaultValue, this));
            }
            else {
                valid = false;
            }
        }
        return valid;
    }
    
    public List<String> getValidValues() {
        final String validValuesString = this.getStringAttr("validValues");
        if (!Strings.isNullOrEmpty(validValuesString)) {
            return Arrays.asList(validValuesString.split(","));
        }
        return null;
    }
    
    public StringField validValues(final Iterable<String> values) {
        this.put("validValues", Joiner.on(",").join((Iterable)values));
        return this;
    }
    
    public boolean isMultiline() {
        final Boolean val = this.getBooleanAttr("multiline");
        return val != null && val;
    }
    
    public StringField multiline(final boolean isMultiline) {
        this.put("multiline", isMultiline);
        return this;
    }
    
    public interface ValuesProvider
    {
        List<String> getStrings();
        
        String getDefaultString();
    }
}
