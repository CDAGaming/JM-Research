package journeymap.common.properties.config;

import journeymap.common.properties.*;

public class IntegerField extends ConfigField<Integer>
{
    public static final String ATTR_MIN = "min";
    public static final String ATTR_MAX = "max";
    
    protected IntegerField() {
    }
    
    public IntegerField(final Category category, final String key, final int minValue, final int maxValue, final int defaultValue) {
        this(category, key, minValue, maxValue, defaultValue, 100);
    }
    
    public IntegerField(final Category category, final String key, final int minValue, final int maxValue, final int defaultValue, final int sortOrder) {
        super(category, key);
        this.range(minValue, maxValue);
        this.defaultValue(defaultValue);
        this.setToDefault();
        this.sortOrder(sortOrder);
    }
    
    @Override
    public Integer getDefaultValue() {
        return this.getIntegerAttr("default");
    }
    
    @Override
    public Integer get() {
        return this.getIntegerAttr("value");
    }
    
    @Override
    public boolean validate(final boolean fix) {
        boolean valid = super.validate(fix);
        valid = (this.require("min", "max") && valid);
        final Integer value = this.get();
        if (value == null || value < this.getMinValue() || value > this.getMaxValue()) {
            if (fix) {
                this.setToDefault();
            }
            else {
                valid = false;
            }
        }
        return valid;
    }
    
    public IntegerField range(final int min, final int max) {
        this.put("min", min);
        this.put("max", max);
        return this;
    }
    
    public int getMinValue() {
        return this.getIntegerAttr("min");
    }
    
    public int getMaxValue() {
        return this.getIntegerAttr("max");
    }
    
    public Integer incrementAndGet() {
        final Integer value = Math.min(this.getMaxValue(), this.get() + 1);
        this.set(value);
        return value;
    }
    
    public Integer decrementAndGet() {
        final Integer value = Math.max(this.getMinValue(), this.get() - 1);
        this.set(value);
        return value;
    }
}
