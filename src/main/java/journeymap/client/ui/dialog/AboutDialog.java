package journeymap.client.ui.dialog;

import journeymap.client.ui.component.*;
import journeymap.client.model.*;
import journeymap.common.*;
import journeymap.client.*;
import journeymap.client.render.texture.*;
import journeymap.client.io.*;
import journeymap.client.ui.*;
import journeymap.common.properties.*;
import java.util.*;
import org.lwjgl.input.*;
import java.awt.geom.*;
import journeymap.client.render.draw.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.*;

public class AboutDialog extends JmUI
{
    protected TextureImpl patreonLogo;
    protected TextureImpl discordLogo;
    Button buttonClose;
    Button buttonOptions;
    Button buttonPatreon;
    Button buttonDiscord;
    Button buttonWebsite;
    Button buttonDownload;
    ButtonList peopleButtons;
    ButtonList devButtons;
    ButtonList logoButtons;
    ButtonList linkButtons;
    ButtonList bottomButtons;
    ButtonList infoButtons;
    private long lastPeopleMove;
    private List<SplashPerson> people;
    private List<SplashPerson> devs;
    private SplashInfo info;
    
    public AboutDialog(final JmUI returnDisplay) {
        super(Constants.getString("jm.common.splash_title", Journeymap.JM_VERSION), returnDisplay);
        this.patreonLogo = TextureCache.getTexture(TextureCache.Patreon);
        this.discordLogo = TextureCache.getTexture(TextureCache.Discord);
        this.people = Arrays.asList(new SplashPerson("AlexDurrani", "Sikandar Durrani", "jm.common.splash_patreon"), new SplashPerson("Davkas", "Davkas", "jm.common.splash_patreon"), new SplashPerson("TECH_GEEK10", "TECH_GEEK10", "jm.common.splash_patreon"), new SplashPerson("_TheEndless_", "The Endless", "jm.common.splash_patreon"), new SplashPerson("eladjenkins", "eladjenkins", "jm.common.splash_patreon"));
        this.devs = Arrays.asList(new SplashPerson("mysticdrew", "mysticdrew", "jm.common.splash_developer"), new SplashPerson("techbrew", "techbrew", "jm.common.splash_developer"));
    }
    
    @Override
    public void func_73866_w_() {
        Journeymap.getClient().getCoreProperties().splashViewed.set(Journeymap.JM_VERSION.toString());
        if (this.info == null) {
            this.info = FileHandler.getMessageModel(SplashInfo.class, "splash");
            if (this.info == null) {
                this.info = new SplashInfo();
            }
            final String bday = Constants.birthdayMessage();
            if (bday != null) {
                this.info.lines.add(0, new SplashInfo.Line(bday, "dialog.FullscreenActions#tweet#" + bday));
                (this.devs = new ArrayList<SplashPerson>(this.devs)).add(new SplashPerson.Fake("", "", TextureCache.getTexture(TextureCache.ColorPicker2)));
            }
            return;
        }
        this.field_146292_n.clear();
        final FontRenderer fr = this.getFontRenderer();
        this.devButtons = new ButtonList();
        for (final SplashPerson dev : this.devs) {
            final Button button = new Button(dev.name);
            this.devButtons.add(button);
            dev.setButton(button);
        }
        this.devButtons.setWidths(20);
        this.devButtons.setHeights(20);
        this.devButtons.layoutDistributedHorizontal(0, 35, this.field_146294_l, true);
        this.peopleButtons = new ButtonList();
        for (final SplashPerson peep : this.people) {
            final Button button = new Button(peep.name);
            this.peopleButtons.add(button);
            peep.setButton(button);
        }
        this.peopleButtons.setWidths(20);
        this.peopleButtons.setHeights(20);
        this.peopleButtons.layoutDistributedHorizontal(0, this.field_146295_m - 65, this.field_146294_l, true);
        this.infoButtons = new ButtonList();
        for (final SplashInfo.Line line : this.info.lines) {
            final SplashInfoButton button2 = new SplashInfoButton(line);
            button2.setDrawBackground(false);
            button2.setDefaultStyle(false);
            button2.setDrawFrame(false);
            button2.setHeight(fr.field_78288_b + 5);
            if (line.hasAction()) {
                button2.setTooltip(Constants.getString("jm.common.splash_action"));
            }
            ((ArrayList<SplashInfoButton>)this.infoButtons).add(button2);
        }
        this.infoButtons.equalizeWidths(fr);
        this.field_146292_n.addAll(this.infoButtons);
        (this.buttonClose = new Button(Constants.getString("jm.common.close"))).addClickListener(button -> {
            this.closeAndReturn();
            return true;
        });
        (this.buttonOptions = new Button(Constants.getString("jm.common.options_button"))).addClickListener(button -> {
            if (this.returnDisplay != null && this.returnDisplay instanceof OptionsManager) {
                this.closeAndReturn();
            }
            else {
                UIManager.INSTANCE.openOptionsManager(this, new Category[0]);
            }
            return true;
        });
        this.bottomButtons = new ButtonList(new Button[] { this.buttonOptions });
        if (this.field_146297_k.field_71441_e != null) {
            this.bottomButtons.add(this.buttonClose);
        }
        this.bottomButtons.equalizeWidths(fr);
        this.bottomButtons.setWidths(Math.max(100, this.buttonOptions.getWidth()));
        this.field_146292_n.addAll(this.bottomButtons);
        (this.buttonWebsite = new Button("http://journeymap.info")).setTooltip(Constants.getString("jm.common.website"));
        this.buttonWebsite.addClickListener(button -> {
            FullscreenActions.launchWebsite("");
            return true;
        });
        (this.buttonDownload = new Button(Constants.getString("jm.common.download"))).setTooltip(Constants.getString("jm.common.download.tooltip"));
        this.buttonDownload.addClickListener(button -> {
            FullscreenActions.launchDownloadWebsite();
            return true;
        });
        (this.linkButtons = new ButtonList(new Button[] { this.buttonWebsite, this.buttonDownload })).equalizeWidths(fr);
        this.field_146292_n.addAll(this.linkButtons);
        final int commonWidth = Math.max(this.bottomButtons.getWidth(0) / this.bottomButtons.size(), this.linkButtons.getWidth(0) / this.linkButtons.size());
        this.bottomButtons.setWidths(commonWidth);
        this.linkButtons.setWidths(commonWidth);
        (this.buttonPatreon = new Button("")).setDefaultStyle(false);
        this.buttonPatreon.setDrawBackground(false);
        this.buttonPatreon.setDrawFrame(false);
        this.buttonPatreon.setTooltip(Constants.getString("jm.common.patreon"), Constants.getString("jm.common.patreon.tooltip"));
        this.buttonPatreon.func_175211_a(this.patreonLogo.getWidth() / this.scaleFactor);
        this.buttonPatreon.setHeight(this.patreonLogo.getHeight() / this.scaleFactor);
        this.buttonPatreon.addClickListener(button -> {
            FullscreenActions.launchPatreon();
            return true;
        });
        (this.buttonDiscord = new Button("")).setDefaultStyle(false);
        this.buttonDiscord.setDrawBackground(false);
        this.buttonDiscord.setDrawFrame(false);
        this.buttonDiscord.setTooltip(Constants.getString("jm.common.discord"), Constants.getString("jm.common.discord.tooltip"));
        this.buttonDiscord.func_175211_a(this.discordLogo.getWidth() / this.scaleFactor);
        this.buttonDiscord.setHeight(this.discordLogo.getHeight() / this.scaleFactor);
        this.buttonDiscord.addClickListener(button -> {
            FullscreenActions.discord();
            return true;
        });
        (this.logoButtons = new ButtonList(new Button[] { this.buttonDiscord, this.buttonPatreon })).setLayout(ButtonList.Layout.Horizontal, ButtonList.Direction.LeftToRight);
        this.logoButtons.setHeights(Math.max(this.discordLogo.getHeight(), this.patreonLogo.getHeight()) / this.scaleFactor);
        this.logoButtons.setWidths(Math.max(this.discordLogo.getWidth(), this.patreonLogo.getWidth()) / this.scaleFactor);
        this.field_146292_n.addAll(this.logoButtons);
    }
    
    @Override
    protected void layoutButtons() {
        if (this.field_146292_n.isEmpty()) {
            this.func_73866_w_();
        }
        final int mx = Mouse.getEventX() * this.field_146294_l / this.field_146297_k.field_71443_c;
        final int my = this.field_146295_m - Mouse.getEventY() * this.field_146295_m / this.field_146297_k.field_71440_d - 1;
        final int hgap = 4;
        final int vgap = 4;
        final FontRenderer fr = this.getFontRenderer();
        final int estimatedInfoHeight = this.infoButtons.getHeight(4);
        final int estimatedButtonsHeight = (this.buttonClose.getHeight() + 4) * 3 + 4;
        final int field_146295_m = this.field_146295_m;
        this.getClass();
        final int centerHeight = field_146295_m - 35 - estimatedButtonsHeight;
        final int lineHeight = (int)(fr.field_78288_b * 1.4);
        final int bx = this.field_146294_l / 2;
        int by = 0;
        final boolean movePeople = System.currentTimeMillis() - this.lastPeopleMove > 20L;
        if (movePeople) {
            this.lastPeopleMove = System.currentTimeMillis();
        }
        final Rectangle2D.Double screenBounds = new Rectangle2D.Double(0.0, 0.0, this.field_146294_l, this.field_146295_m);
        if (!this.devButtons.isEmpty()) {
            for (final SplashPerson dev : this.devs) {
                if (dev.getButton().mouseOver(mx, my)) {
                    dev.randomizeVector();
                }
                this.drawPerson(by, lineHeight, dev);
                if (movePeople) {
                    dev.avoid(this.devs);
                    dev.adjustVector(screenBounds);
                }
            }
        }
        if (!this.peopleButtons.isEmpty()) {
            for (final SplashPerson peep : this.people) {
                if (peep.getButton().mouseOver(mx, my)) {
                    peep.randomizeVector();
                }
                this.drawPerson(by, lineHeight, peep);
                if (movePeople) {
                    peep.avoid(this.devs);
                    peep.adjustVector(screenBounds);
                }
            }
        }
        if (!this.infoButtons.isEmpty()) {
            this.getClass();
            final int topY;
            by = (topY = 35 + (centerHeight - estimatedInfoHeight) / 2);
            by += (int)(lineHeight * 1.5);
            this.infoButtons.layoutCenteredVertical(bx - this.infoButtons.get(0).getWidth() / 2, by + this.infoButtons.getHeight(0) / 2, true, 0);
            final int listX = this.infoButtons.getLeftX() - 10;
            final int listY = topY - 5;
            final int listWidth = this.infoButtons.getRightX() + 10 - listX;
            final int listHeight = this.infoButtons.getBottomY() + 5 - listY;
            DrawUtil.drawGradientRect(listX - 1, listY - 1, listWidth + 2, listHeight + 2, 12632256, 0.8f, 12632256, 0.8f);
            DrawUtil.drawGradientRect(listX, listY, listWidth, listHeight, 4210752, 1.0f, 0, 1.0f);
            DrawUtil.drawLabel(Constants.getString("jm.common.splash_whatisnew"), bx, topY, DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, 0, 0.0f, 65535, 1.0f, 1.0, true);
        }
        final int rowHeight = this.buttonOptions.field_146121_g + 4;
        by = this.field_146295_m - rowHeight - 4;
        this.bottomButtons.layoutCenteredHorizontal(bx, by, true, 4);
        by -= rowHeight;
        this.linkButtons.layoutCenteredHorizontal(bx, by, true, 4);
        by -= 4 + this.logoButtons.getHeight();
        this.logoButtons.layoutCenteredHorizontal(bx, by, true, 6);
        DrawUtil.drawImage(this.patreonLogo, this.buttonPatreon.getX(), this.buttonPatreon.getY(), false, 1.0f / this.scaleFactor, 0.0);
        DrawUtil.drawImage(this.discordLogo, this.buttonDiscord.getX(), this.buttonDiscord.getY(), false, 1.0f / this.scaleFactor, 0.0);
    }
    
    protected int drawPerson(int by, final int lineHeight, final SplashPerson person) {
        final float scale = 1.0f;
        final Button button = person.getButton();
        final int imgSize = (int)(person.getSkin().getWidth() * scale);
        final int imgY = button.getY() - 2;
        final int imgX = button.getCenterX() - imgSize / 2;
        GlStateManager.func_179141_d();
        if (!(person instanceof SplashPerson.Fake)) {
            DrawUtil.drawGradientRect(imgX - 1, imgY - 1, imgSize + 2, imgSize + 2, 0, 0.4f, 0, 0.8f);
            DrawUtil.drawImage(person.getSkin(), 1.0f, imgX, imgY, false, scale, 0.0);
        }
        else {
            final float size = Math.min(person.getSkin().getWidth() * scale, 24.0f * scale);
            DrawUtil.drawQuad(person.getSkin(), 16777215, 1.0f, imgX, imgY, size, size, false, 0.0);
        }
        by = imgY + imgSize + 4;
        String name = person.name.trim();
        String name2 = null;
        final boolean twoLineName = name.contains(" ");
        if (twoLineName) {
            final String[] parts = person.name.split(" ");
            name = parts[0];
            name2 = parts[1];
        }
        DrawUtil.drawLabel(name, button.getCenterX(), by, DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, 0, 0.0f, 16777215, 1.0f, scale, true);
        by += lineHeight;
        if (name2 != null) {
            DrawUtil.drawLabel(name2, button.getCenterX(), by, DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, 0, 0.0f, 16777215, 1.0f, scale, true);
            by += lineHeight;
        }
        DrawUtil.drawLabel(person.title, button.getCenterX(), by, DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, 0, 0.0f, 65280, 1.0f, scale, true);
        by += lineHeight;
        return by;
    }
    
    protected void func_146284_a(final GuiButton guibutton) {
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
    
    class SplashInfoButton extends Button
    {
        final SplashInfo.Line infoLine;
        
        public SplashInfoButton(final SplashInfo.Line infoLine) {
            super(infoLine.label);
            this.infoLine = infoLine;
        }
        
        @Override
        public boolean func_146116_c(final Minecraft minecraft, final int mouseX, final int mouseY) {
            final boolean pressed = super.mousePressed(minecraft, mouseX, mouseY, false);
            if (pressed) {
                this.infoLine.invokeAction(AboutDialog.this);
            }
            return this.checkClickListeners();
        }
    }
}
