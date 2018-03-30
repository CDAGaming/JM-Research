package journeymap.client.ui.dialog;

import journeymap.client.ui.component.*;
import net.minecraft.client.gui.*;
import journeymap.client.ui.*;
import journeymap.common.properties.*;
import journeymap.client.properties.*;
import journeymap.client.ui.fullscreen.*;
import journeymap.common.*;
import journeymap.client.task.main.*;
import net.minecraft.client.*;
import journeymap.client.*;
import journeymap.client.task.multi.*;

public class AutoMapConfirmation extends JmUI
{
    Button buttonOptions;
    Button buttonAll;
    Button buttonMissing;
    Button buttonClose;
    
    public AutoMapConfirmation() {
        this((JmUI)null);
    }
    
    public AutoMapConfirmation(final JmUI returnDisplay) {
        super(Constants.getString("jm.common.automap_dialog"), returnDisplay);
    }
    
    @Override
    public void func_73866_w_() {
        this.field_146292_n.clear();
        this.buttonOptions = new Button(Constants.getString("jm.common.options_button"));
        this.buttonAll = new Button(Constants.getString("jm.common.automap_dialog_all"));
        this.buttonMissing = new Button(Constants.getString("jm.common.automap_dialog_missing"));
        this.buttonClose = new Button(Constants.getString("jm.common.close"));
        this.field_146292_n.add(this.buttonOptions);
        this.field_146292_n.add(this.buttonAll);
        this.field_146292_n.add(this.buttonMissing);
        this.field_146292_n.add(this.buttonClose);
    }
    
    @Override
    protected void layoutButtons() {
        if (this.field_146292_n.isEmpty()) {
            this.func_73866_w_();
        }
        final FontRenderer fr = this.getFontRenderer();
        final int x = this.field_146294_l / 2;
        final int lineHeight = fr.field_78288_b + 3;
        int y = 35 + lineHeight * 2;
        this.func_73732_a(this.getFontRenderer(), Constants.getString("jm.common.automap_dialog_summary_1"), x, y, 16777215);
        y += lineHeight;
        this.func_73732_a(this.getFontRenderer(), Constants.getString("jm.common.automap_dialog_summary_2"), x, y, 16777215);
        y += lineHeight * 2;
        this.buttonOptions.centerHorizontalOn(x).centerVerticalOn(y);
        y += lineHeight * 3;
        this.func_73732_a(this.getFontRenderer(), Constants.getString("jm.common.automap_dialog_text"), x, y, 16776960);
        y += lineHeight * 2;
        final ButtonList buttons = new ButtonList(new Button[] { this.buttonAll, this.buttonMissing });
        buttons.equalizeWidths(this.getFontRenderer(), 4, 200);
        buttons.layoutCenteredHorizontal(x, y, true, 4);
        this.buttonClose.centerHorizontalOn(x).below(this.buttonMissing, lineHeight);
    }
    
    protected void func_146284_a(final GuiButton guibutton) {
        if (guibutton == this.buttonOptions) {
            UIManager.INSTANCE.openOptionsManager(this, ClientCategory.Cartography);
            return;
        }
        if (guibutton != this.buttonClose) {
            boolean enable;
            Object arg;
            if (guibutton == this.buttonAll) {
                enable = true;
                arg = Boolean.TRUE;
            }
            else if (guibutton == this.buttonMissing) {
                enable = true;
                arg = Boolean.FALSE;
            }
            else {
                enable = false;
                arg = null;
            }
            MapRegionTask.MAP_VIEW = Fullscreen.state().getMapView();
            Journeymap.getClient().queueMainThreadTask(new IMainThreadTask() {
                @Override
                public IMainThreadTask perform(final Minecraft mc, final JourneymapClient jm) {
                    Journeymap.getClient().toggleTask(MapRegionTask.Manager.class, enable, arg);
                    return null;
                }
                
                @Override
                public String getName() {
                    return "Automap";
                }
            });
        }
        this.closeAndReturn();
    }
    
    @Override
    protected void func_73869_a(final char c, final int i) {
        switch (i) {
            case 1: {
                this.closeAndReturn();
                break;
            }
        }
    }
}
