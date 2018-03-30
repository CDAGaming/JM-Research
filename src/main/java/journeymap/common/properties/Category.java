package journeymap.common.properties;

import com.google.common.base.*;

public class Category implements Comparable<Category>
{
    public static final Category Inherit;
    public static final Category Hidden;
    String name;
    String label;
    String tooltip;
    int order;
    
    public Category(final String name, final int order, final String label, final String tooltip) {
        this.name = name;
        this.order = order;
        this.label = label;
        this.tooltip = tooltip;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getLabel() {
        return (this.label == null) ? this.getName() : this.label;
    }
    
    public String getTooltip() {
        return (this.tooltip == null) ? this.getLabel() : this.tooltip;
    }
    
    public int getOrder() {
        return this.order;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Category)) {
            return false;
        }
        final Category category = (Category)o;
        return Objects.equal((Object)this.getName(), (Object)category.getName());
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(new Object[] { this.getName() });
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
    @Override
    public int compareTo(final Category o) {
        int result = Integer.compare(this.order, o.order);
        if (result == 0) {
            result = this.name.compareTo(o.name);
        }
        return result;
    }
    
    static {
        Inherit = new Category("Inherit", 0, "", "");
        Hidden = new Category("Hidden", 0, "", "");
    }
}
