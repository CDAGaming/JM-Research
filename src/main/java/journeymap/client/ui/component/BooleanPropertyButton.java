package journeymap.client.ui.component;

import journeymap.common.properties.config.*;

public class BooleanPropertyButton extends OnOffButton implements IConfigFieldHolder<BooleanField>
{
    final BooleanField booleanField;
    
    public BooleanPropertyButton(final String labelOn, final String labelOff, final BooleanField field) {
        super(labelOn, labelOff, field != null && field.get());
        this.booleanField = field;
    }
    
    public BooleanField getField() {
        return this.booleanField;
    }
    
    @Override
    public void toggle() {
        if (this.isEnabled()) {
            if (this.booleanField != null) {
                this.setToggled(this.booleanField.toggleAndSave());
            }
            else {
                this.setToggled(!this.toggled);
            }
        }
    }
    
    @Override
    public void refresh() {
        if (this.booleanField != null) {
            this.setToggled(this.booleanField.get());
        }
    }
    
    public void setValue(final Boolean value) {
        if (this.booleanField == null) {
            this.toggled = value;
        }
        else {
            this.booleanField.set(value);
            this.booleanField.save();
        }
    }
    
    @Override
    public BooleanField getConfigField() {
        return this.booleanField;
    }
}
