package journeymap.client.ui.minimap;

import journeymap.client.render.draw.*;
import journeymap.client.ui.theme.*;
import net.minecraft.client.renderer.*;

class LabelVars
{
    final double x;
    final double y;
    final double fontScale;
    final DrawUtil.HAlign hAlign;
    final DrawUtil.VAlign vAlign;
    final DisplayVars displayVars;
    final Theme.LabelSpec labelSpec;
    
    LabelVars(final DisplayVars displayVars, final double x, final double y, final DrawUtil.HAlign hAlign, final DrawUtil.VAlign vAlign, final double fontScale, final Theme.LabelSpec labelSpec) {
        this.displayVars = displayVars;
        this.x = x;
        this.y = y;
        this.hAlign = hAlign;
        this.vAlign = vAlign;
        this.fontScale = fontScale;
        this.labelSpec = labelSpec;
    }
    
    void draw(final String text) {
        GlStateManager.func_179147_l();
        DrawUtil.drawLabel(text, this.labelSpec, (int)this.x, (int)this.y, this.hAlign, this.vAlign, this.fontScale, 0.0);
        GlStateManager.func_179084_k();
    }
}
