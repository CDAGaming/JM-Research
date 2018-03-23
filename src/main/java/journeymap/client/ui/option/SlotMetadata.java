package journeymap.client.ui.option;

import net.minecraftforge.fml.client.*;
import net.minecraft.util.text.*;
import journeymap.client.*;
import net.minecraft.client.gui.*;
import java.util.*;
import journeymap.client.ui.component.*;
import journeymap.common.properties.config.*;
import journeymap.common.properties.*;

public class SlotMetadata<T> implements Comparable<SlotMetadata>
{
    protected final Button button;
    protected final String range;
    protected final T defaultValue;
    protected final ValueType valueType;
    protected String name;
    protected String tooltip;
    protected boolean advanced;
    protected String[] tooltipLines;
    protected List valueList;
    protected boolean master;
    protected int order;
    
    public SlotMetadata(final Button button) {
        this(button, false);
    }
    
    public SlotMetadata(final Button button, final int order) {
        this(button, false);
        this.order = order;
    }
    
    public SlotMetadata(final Button button, final boolean advanced) {
        this(button, button.field_146126_j, button.getUnformattedTooltip(), null, null, advanced);
    }
    
    public SlotMetadata(final Button button, final String name, final String tooltip, final boolean advanced) {
        this(button, name, tooltip, null, null, advanced);
    }
    
    public SlotMetadata(final Button button, final String name, final String tooltip) {
        this(button, name, tooltip, null, null, false);
    }
    
    public SlotMetadata(final Button button, final String name, final String tooltip, final int order) {
        this(button, name, tooltip, null, null, false);
        this.order = order;
    }
    
    public SlotMetadata(final Button button, final String name, final String tooltip, final String range, final T defaultValue, final boolean advanced) {
        this.button = button;
        this.name = name;
        this.tooltip = tooltip;
        this.range = range;
        this.defaultValue = defaultValue;
        this.advanced = advanced;
        if (defaultValue == null && range == null && !advanced) {
            this.valueType = ValueType.Toolbar;
        }
        else if (defaultValue instanceof Boolean) {
            this.valueType = ValueType.Boolean;
        }
        else if (defaultValue instanceof Integer) {
            this.valueType = ValueType.Integer;
        }
        else {
            this.valueType = ValueType.Set;
        }
    }
    
    public boolean isMasterPropertyForCategory() {
        return this.master;
    }
    
    public void setMasterPropertyForCategory(final boolean master) {
        this.master = master;
    }
    
    public Button getButton() {
        return this.button;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getRange() {
        return this.range;
    }
    
    public boolean isAdvanced() {
        return this.advanced;
    }
    
    public void setAdvanced(final boolean advanced) {
        this.advanced = advanced;
    }
    
    public ValueType getValueType() {
        return this.valueType;
    }
    
    public String[] getTooltipLines() {
        return this.tooltipLines;
    }
    
    public boolean isMaster() {
        return this.master;
    }
    
    public T getDefaultValue() {
        return this.defaultValue;
    }
    
    public boolean isToolbar() {
        return this.valueType == ValueType.Toolbar;
    }
    
    public int getOrder() {
        return this.order;
    }
    
    public void setOrder(final int order) {
        this.order = order;
    }
    
    public List getValueList() {
        return this.valueList;
    }
    
    public void setValueList(final List valueList) {
        this.valueList = valueList;
    }
    
    public void updateFromButton() {
        if (this.button != null) {
            this.name = this.button.field_146126_j;
            this.tooltip = this.button.getUnformattedTooltip();
            this.tooltipLines = null;
        }
    }
    
    public String[] getTooltip() {
        final FontRenderer fontRenderer = FMLClientHandler.instance().getClient().field_71466_p;
        final String bidiColor = fontRenderer.func_78260_a() ? "%2$s%1$s" : "%1$s%2$s";
        if (this.tooltipLines == null) {
            final ArrayList<TextComponentTranslation> lines = new ArrayList<TextComponentTranslation>(4);
            if (this.tooltip != null || this.range != null || this.defaultValue != null || this.advanced) {
                final TextFormatting nameColor = this.isToolbar() ? TextFormatting.GREEN : (this.advanced ? TextFormatting.RED : TextFormatting.AQUA);
                lines.add(new TextComponentTranslation("jm.config.tooltip_format", new Object[] { nameColor, this.name }));
                if (this.tooltip != null) {
                    lines.addAll(this.getWordWrappedLines(TextFormatting.YELLOW.toString(), this.tooltip));
                }
                if (this.button != null && this.button instanceof IntSliderButton) {
                    lines.addAll(this.getWordWrappedLines(TextFormatting.GRAY.toString() + TextFormatting.ITALIC.toString(), Constants.getString("jm.config.control_arrowkeys")));
                }
                if (this.range != null) {
                    lines.add(new TextComponentTranslation("jm.config.tooltip_format", new Object[] { TextFormatting.WHITE, this.range }));
                }
            }
            if (!lines.isEmpty()) {
                final ArrayList<String> stringLines = new ArrayList<String>();
                for (final TextComponentTranslation line : lines) {
                    stringLines.add(line.func_150260_c().trim());
                }
                this.tooltipLines = stringLines.toArray(new String[stringLines.size()]);
            }
        }
        return this.tooltipLines;
    }
    
    protected List<TextComponentTranslation> getWordWrappedLines(final String color, final String original) {
        final FontRenderer fontRenderer = FMLClientHandler.instance().getClient().field_71466_p;
        final List<TextComponentTranslation> list = new ArrayList<TextComponentTranslation>();
        final int max = fontRenderer.func_78260_a() ? 170 : 250;
        for (final Object line : fontRenderer.func_78271_c(original, max)) {
            list.add(new TextComponentTranslation("jm.config.tooltip_format", new Object[] { color, line }));
        }
        return list;
    }
    
    public void resetToDefaultValue() {
        if (this.button != null) {
            if (this.button instanceof IConfigFieldHolder) {
                try {
                    final ConfigField configField = ((IConfigFieldHolder)this.button).getConfigField();
                    if (configField != null) {
                        configField.setToDefault();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.button.refresh();
        }
    }
    
    public boolean hasConfigField() {
        return this.button != null && this.button instanceof IConfigFieldHolder && ((IConfigFieldHolder)this.button).getConfigField() != null;
    }
    
    public PropertiesBase getProperties() {
        if (this.hasConfigField()) {
            return ((IConfigFieldHolder)this.button).getConfigField().getOwner();
        }
        return null;
    }
    
    @Override
    public int compareTo(final SlotMetadata other) {
        int result = Boolean.compare(this.isToolbar(), other.isToolbar());
        if (result == 0) {
            result = Integer.compare(this.order, other.order);
        }
        if (result == 0) {
            result = Boolean.compare(other.master, this.master);
        }
        if (result == 0) {
            result = this.valueType.compareTo(other.valueType);
        }
        if (result == 0) {
            result = this.name.compareTo(other.name);
        }
        return result;
    }
    
    public enum ValueType
    {
        Boolean, 
        Set, 
        Integer, 
        Toolbar;
    }
}
