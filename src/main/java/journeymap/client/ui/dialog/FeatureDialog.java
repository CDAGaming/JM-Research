package journeymap.client.ui.dialog;

import journeymap.common.api.feature.*;
import journeymap.client.ui.waypoint.*;
import journeymap.client.ui.component.*;
import net.minecraft.client.*;
import journeymap.client.data.*;
import journeymap.client.feature.*;
import journeymap.common.*;
import journeymap.common.log.*;
import java.io.*;
import net.minecraft.client.gui.*;
import net.minecraft.util.text.*;
import com.google.common.base.*;
import journeymap.client.render.draw.*;
import journeymap.server.api.impl.*;
import net.minecraft.world.*;
import net.minecraft.entity.player.*;
import java.util.*;
import journeymap.client.*;
import net.minecraft.client.resources.*;
import journeymap.common.feature.*;

public class FeatureDialog extends JmUI
{
    private static List<Feature> HIDDEN;
    private ScrollPane scrollPane;
    private GameTypeButton buttonGameType;
    private DimensionsButton buttonDimensions;
    private Button buttonClose;
    private CheckBox buttonOverride;
    private Button buttonReport;
    private ButtonList topButtons;
    private int dimension;
    private int lastWidth;
    private boolean isCreative;
    private List<String> noticeLines;
    private int[] columnWidths;
    private int listHeight;
    private final int lineHeight;
    private final int vpad = 5;
    
    public FeatureDialog() {
        this((JmUI)null);
    }
    
    public FeatureDialog(final JmUI returnDisplay) {
        super(Constants.getString("jm.common.features"), returnDisplay);
        this.lineHeight = JmUI.fontRenderer().field_78288_b + 4;
    }
    
    @Override
    public void func_146280_a(final Minecraft minecraft, final int width, final int height) {
        if (height != this.field_146295_m) {
            this.listHeight = 0;
        }
        super.func_146280_a(minecraft, width, height);
    }
    
    @Override
    public void func_73866_w_() {
        this.field_146292_n.clear();
        this.isCreative = this.field_146297_k.field_71442_b.func_178889_l().func_77145_d();
        final FontRenderer fr = this.getFontRenderer();
        if (this.buttonDimensions == null) {
            (this.buttonDimensions = new DimensionsButton()).setDefaultStyle(false);
            this.buttonDimensions.setDrawBackground(false);
            this.buttonDimensions.setDrawFrame(false);
            this.buttonDimensions.resetLabelColors();
        }
        this.field_146292_n.add(this.buttonDimensions);
        if (this.buttonGameType == null) {
            (this.buttonGameType = new GameTypeButton()).setDefaultStyle(false);
            this.buttonGameType.setDrawBackground(false);
            this.buttonGameType.setDrawFrame(false);
            this.buttonGameType.resetLabelColors();
        }
        this.field_146292_n.add(this.buttonGameType);
        this.topButtons = new ButtonList(new Button[] { this.buttonDimensions, this.buttonGameType });
        if (this.buttonReport == null) {
            (this.buttonReport = new Button(Constants.getString("jm.common.features.notice.button"))).fitWidth(fr);
        }
        this.field_146292_n.add(this.buttonReport);
        if (this.buttonOverride == null) {
            (this.buttonOverride = new CheckBox(Constants.getString("jm.common.features.override"), false)).setEnabled(this.isCreative);
            if (!this.buttonOverride.field_146124_l) {
                this.buttonOverride.setTooltip(Constants.getString("jm.common.features.override.tooltip"));
            }
            this.buttonOverride.setLabelColors(14737632, 16777120, 6710886);
        }
        this.field_146292_n.add(this.buttonOverride);
        if (this.buttonClose == null) {
            (this.buttonClose = new Button(Constants.getString("jm.common.close"))).fitWidth(fr);
            this.buttonClose.func_175211_a(Math.max(this.buttonClose.getWidth(), 150));
        }
        this.field_146292_n.add(this.buttonClose);
        final ButtonList buttonsRow = new ButtonList(new Button[] { this.buttonReport, this.buttonClose });
        buttonsRow.layoutDistributedHorizontal(10, this.field_146295_m - this.buttonClose.getHeight() - 5, this.field_146294_l - 10, true);
        this.dimension = ((DimensionsButton.currentWorldProvider == null) ? DataCache.getPlayer().dimension : DimensionsButton.currentWorldProvider.getDimension());
        final DimensionPolicies dimPolicies = ClientFeatures.instance().get(this.dimension);
        final List<Policy> policies = new ArrayList<Policy>(dimPolicies.getPolicyMap(GameTypeButton.currentGameType).values());
        final TreeSet<FeatureLine> lineSet = new TreeSet<FeatureLine>();
        for (final Policy policyChange : policies) {
            if (!FeatureDialog.HIDDEN.contains(policyChange.getFeature())) {
                lineSet.add(new FeatureLine(policyChange));
            }
        }
        final List<FeatureLine> lines = new ArrayList<FeatureLine>(lineSet);
        lines.add(0, new FeatureLine(Constants.getString("jm.common.features.column_category"), Constants.getString("jm.common.features.column_feature"), Constants.getString("jm.common.features.column_origin")));
        final int cols = lines.iterator().next().featureColumns.length;
        if (this.columnWidths == null || this.columnWidths.length != cols) {
            this.columnWidths = new int[cols];
        }
        for (int i = 0; i < cols; ++i) {
            int colWidth = this.columnWidths[i];
            for (final FeatureLine line : lines) {
                colWidth = Math.max(colWidth, fr.func_78256_a(line.featureColumns[i].label));
            }
            this.columnWidths[i] = colWidth;
            for (final FeatureLine line : lines) {
                line.featureColumns[i].width = colWidth;
            }
        }
        int lineWidth;
        final int pad = lineWidth = 15;
        for (final int colWidth2 : this.columnWidths) {
            lineWidth += colWidth2 + pad;
        }
        lineWidth = Math.max(lineWidth, this.lastWidth);
        lineWidth = Math.max(lineWidth, this.topButtons.getWidth(pad) + pad);
        lineWidth = Math.min(lineWidth, this.field_146294_l - 20);
        this.lastWidth = lineWidth;
        for (final FeatureLine line : lines) {
            line.width = lineWidth;
        }
        (this.scrollPane = new ScrollPane(this.field_146297_k, lineWidth, 0, lines, this.lineHeight, 3)).func_193651_b(false);
        this.scrollPane.bgColor = 2236962;
        this.scrollPane.bgAlpha = 1.0f;
    }
    
    @Override
    protected void layoutButtons() {
        if (this.field_146292_n.isEmpty()) {
            this.func_73866_w_();
        }
        final FontRenderer fr = this.getFontRenderer();
        final int centerX = this.field_146294_l / 2;
        if (this.noticeLines == null) {
            int fitWidth = this.scrollPane.field_148155_a;
            while (this.noticeLines == null || this.noticeLines.size() > 2) {
                this.noticeLines = (List<String>)fr.func_78271_c(Constants.getString("jm.common.features.notice"), fitWidth);
                fitWidth += 10;
            }
        }
        final int noticeY = this.buttonReport.getY() - this.lineHeight * this.noticeLines.size() - 5;
        int overrideY = noticeY - 10 - this.buttonOverride.field_146121_g;
        final int listMinY = 40 + this.buttonDimensions.field_146121_g;
        final int listMaxY = overrideY - 5;
        final int listMinHeight = this.lineHeight * 6;
        final int listMaxHeight = listMaxY - listMinY;
        final int listIdealHeight = this.scrollPane.func_148146_j() * this.scrollPane.func_148127_b() + this.lineHeight;
        final int trialHeight = Math.min(Math.max(listMinHeight, listIdealHeight), listMaxHeight);
        if (trialHeight > this.listHeight) {
            this.listHeight = trialHeight;
        }
        final int scrollX = (this.field_146294_l - this.scrollPane.field_148155_a) / 2;
        int scrollY = listMinY;
        if (this.listHeight < listMaxHeight) {
            scrollY += (listMaxHeight - this.listHeight) / 2;
        }
        this.scrollPane.setDimensions(this.scrollPane.field_148155_a, this.listHeight, 0, 0, scrollX, scrollY);
        this.topButtons.layoutCenteredHorizontal(this.field_146294_l / 2, scrollY - this.buttonDimensions.field_146121_g - 5, true, 5);
        overrideY = Math.min(overrideY, scrollY + this.listHeight + 5);
        this.buttonOverride.centerHorizontalOn(centerX + 6).setY(overrideY);
        this.buttonOverride.setEnabled(this.isCreative);
        int lineY = noticeY;
        for (final String line : this.noticeLines) {
            this.func_73732_a(fr, line, centerX, lineY, 16777215);
            lineY += this.lineHeight;
        }
    }
    
    @Override
    public void func_73863_a(final int mouseX, final int mouseY, final float partialTicks) {
        super.func_73863_a(mouseX, mouseY, partialTicks);
        try {
            this.scrollPane.frameColor = (this.isEditable() ? 16777215 : 13421772);
            this.scrollPane.func_148128_a(mouseX, mouseY, partialTicks);
            final ScrollPane.Scrollable underMouse = this.scrollPane.getScrollableUnderMouse(mouseX, mouseY);
            if (underMouse != null) {
                final List<String> tooltip = underMouse.getTooltip();
                if (tooltip != null && !tooltip.isEmpty()) {
                    this.drawHoveringText(tooltip, mouseX, underMouse.getY() + underMouse.getHeight() + 2, this.getFontRenderer());
                }
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("Error in UI: " + LogFormatter.toString(t));
            this.closeAndReturn();
        }
    }
    
    protected void func_73864_a(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        super.func_73864_a(mouseX, mouseY, mouseButton);
        this.scrollPane.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    protected void func_146286_b(final int mouseX, final int mouseY, final int state) {
        super.func_146286_b(mouseX, mouseY, state);
    }
    
    public void func_146274_d() throws IOException {
        super.func_146274_d();
        this.scrollPane.func_178039_p();
    }
    
    protected void func_146284_a(final GuiButton guibutton) {
        if (guibutton == this.buttonDimensions) {
            this.buttonDimensions.nextValue();
            if (DimensionsButton.currentWorldProvider == null) {
                this.buttonDimensions.nextValue();
            }
            this.field_146292_n.clear();
            return;
        }
        if (guibutton == this.buttonGameType) {
            this.buttonGameType.nextValue();
            this.field_146292_n.clear();
            return;
        }
        if (guibutton == this.buttonOverride && this.isCreative) {
            return;
        }
        if (guibutton == this.buttonReport) {
            FullscreenActions.launchEulaViolationWebsite();
            return;
        }
        if (guibutton == this.buttonClose) {
            this.closeAndReturn();
        }
        this.scrollPane.func_148147_a(guibutton);
    }
    
    private boolean isEditable() {
        return this.isCreative && this.buttonOverride.isActive();
    }
    
    static {
        FeatureDialog.HIDDEN = Arrays.asList(Feature.Display.Compass, Feature.MapType.Biome, Feature.Radar.Vehicle);
    }
    
    public class FeatureLine implements ScrollPane.Scrollable, Comparable<FeatureLine>
    {
        int x;
        int y;
        int width;
        int height;
        boolean enabled;
        long timestamp;
        FeatureColumn[] featureColumns;
        String[] tooltip;
        final Feature feature;
        
        private FeatureLine(final String category, final String name, final String origin) {
            this.height = FeatureDialog.this.lineHeight;
            this.enabled = true;
            this.feature = null;
            final String color = TextFormatting.AQUA.toString();
            this.featureColumns = new FeatureColumn[] { new FeatureColumn(category, color), new FeatureColumn(name, color), new FeatureColumn(origin, color) };
            this.tooltip = new String[0];
            this.timestamp = 0L;
        }
        
        private FeatureLine(final Policy policy) {
            this.height = FeatureDialog.this.lineHeight;
            this.feature = policy.getFeature();
            this.initGui(policy);
        }
        
        private void initGui(final Policy policy) {
            this.enabled = policy.isAllowed();
            this.timestamp = policy.getTimestamp();
            final String catName = ClientFeatures.getFeatureCategoryName(this.feature);
            final String name = ClientFeatures.getFeatureName(this.feature);
            final String tooltip = ClientFeatures.getFeatureTooltip(this.feature);
            final String origin = ClientFeatures.getOriginString(policy);
            final String originColor = (origin == null) ? TextFormatting.DARK_GRAY.toString() : TextFormatting.GOLD.toString();
            if (this.featureColumns == null) {
                this.featureColumns = new FeatureColumn[] { new FeatureColumn(), new FeatureColumn(), new FeatureColumn() };
            }
            this.featureColumns[0].label = catName;
            this.featureColumns[0].color = TextFormatting.GRAY.toString();
            this.featureColumns[1].label = name;
            this.featureColumns[1].color = (this.enabled ? TextFormatting.WHITE.toString() : TextFormatting.RED.toString());
            this.featureColumns[2].label = ((origin == null) ? "-" : origin);
            this.featureColumns[2].color = originColor;
            final List<String> lines = new ArrayList<String>(3);
            lines.add(Joiner.on("").join((Object)this.featureColumns[0].color, (Object)this.featureColumns[0].label, new Object[] { " ", this.featureColumns[1].color, this.featureColumns[1].label }));
            lines.add(TextFormatting.AQUA + tooltip);
            this.tooltip = lines.toArray(new String[lines.size()]);
        }
        
        @Override
        public void setPosition(final int x, final int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public int getX() {
            return this.x;
        }
        
        @Override
        public int getY() {
            return this.y;
        }
        
        @Override
        public int getWidth() {
            return this.width;
        }
        
        @Override
        public void setScrollableWidth(final int width) {
            this.width = width;
        }
        
        @Override
        public int getFitWidth(final FontRenderer fr) {
            return this.width;
        }
        
        @Override
        public int getHeight() {
            return this.height;
        }
        
        @Override
        public void drawScrollable(final Minecraft mc, final int mouseX, final int mouseY) {
            final boolean mouseOver = this.isMouseOver(mouseX, mouseY);
            if (this.tooltip.length > 0 && mouseOver) {
                DrawUtil.drawRectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 16777215, 0.3f);
            }
            final FontRenderer fr = JmUI.fontRenderer();
            final int labelY = this.y + (this.height - fr.field_78288_b) / 2 + 1;
            final int categoryWidth = fr.func_78256_a(this.featureColumns[0].label);
            String displayName = this.enabled ? this.featureColumns[1].label : (TextFormatting.STRIKETHROUGH + this.featureColumns[1].label);
            final boolean editable = FeatureDialog.this.isEditable();
            if (mouseOver && this.feature != null && editable) {
                displayName = TextFormatting.UNDERLINE + displayName;
            }
            final boolean dropshadow = true;
            final int color = -1;
            final int pad = 10;
            final int x1 = this.x + pad + this.featureColumns[0].width + pad;
            final int x2 = x1 - categoryWidth - pad;
            final int x3 = x1 + this.featureColumns[1].width + pad;
            fr.func_175065_a(this.featureColumns[0].color + this.featureColumns[0].label, (float)x2, (float)labelY, color, dropshadow);
            fr.func_175065_a(this.featureColumns[1].color + displayName, (float)x1, (float)labelY, color, dropshadow);
            fr.func_175065_a(this.featureColumns[2].color + this.featureColumns[2].label, (float)x3, (float)labelY, color, dropshadow);
        }
        
        @Override
        public void drawPartialScrollable(final Minecraft mc, final int x, final int y, final int width, final int height) {
        }
        
        @Override
        public void clickScrollable(final Minecraft mc, final int mouseX, final int mouseY) {
            if (this.feature != null && FeatureDialog.this.isEditable()) {
                final GameType gameType = GameTypeButton.currentGameType;
                final int dim = FeatureDialog.this.dimension;
                final boolean state = ClientFeatures.instance().isAllowed(gameType, this.feature, dim);
                final EntityPlayer player = (EntityPlayer)Journeymap.clientPlayer();
                if (player != null) {
                    final String origin = player.getDisplayNameString();
                    Policy policy;
                    if (player.func_130014_f_().field_72995_K) {
                        policy = ClientFeatures.instance().setAllowed(dim, gameType, this.feature, !state, origin);
                    }
                    else {
                        final Map<Feature, Boolean> map = new HashMap<Feature, Boolean>();
                        map.put(this.feature, !state);
                        ServerAPI.INSTANCE.setPlayerFeatures(origin, player.func_110124_au(), player.field_71093_bK, gameType, map);
                        policy = Policy.update(origin, gameType, this.feature, !state);
                    }
                    this.initGui(policy);
                }
            }
        }
        
        @Override
        public List<String> getTooltip() {
            final ArrayList<String> list = new ArrayList<String>();
            if (this.tooltip != null && this.tooltip.length > 0) {
                final FontRenderer fr = JmUI.fontRenderer();
                for (final String line : this.tooltip) {
                    list.addAll(fr.func_78271_c(line, (int)(this.width * 0.66)));
                }
            }
            return list;
        }
        
        private boolean isMouseOver(final int mouseX, final int mouseY) {
            return this.getY() <= mouseY && mouseY <= this.getY() + this.getHeight() && mouseX >= this.x && mouseX < this.x + this.width;
        }
        
        @Override
        public int compareTo(final FeatureLine o) {
            int result = 0;
            if (this.feature == null) {
                return -1;
            }
            for (int i = 0; i < this.featureColumns.length; ++i) {
                if (result != 0) {
                    return result;
                }
                result = this.featureColumns[i].label.compareTo(o.featureColumns[i].label);
            }
            return result;
        }
    }
    
    private static class FeatureColumn
    {
        String label;
        String color;
        int width;
        
        FeatureColumn() {
        }
        
        FeatureColumn(final String label, final String color) {
            this.label = label;
            this.color = color;
        }
    }
    
    private static class GameTypeButton extends Button
    {
        private static GameType currentGameType;
        
        public GameTypeButton() {
            super(0, 0, "");
            if (GameTypeButton.currentGameType == null) {
                GameTypeButton.currentGameType = JourneymapClient.getGameType();
            }
            this.updateLabel();
            this.fitWidth(JmUI.fontRenderer());
        }
        
        @Override
        protected void updateLabel() {
            this.field_146126_j = this.getLabel(GameTypeButton.currentGameType);
        }
        
        private String getLabel(final GameType gameType) {
            return I18n.func_135052_a("selectWorld.gameMode", new Object[0]) + ": " + I18n.func_135052_a("selectWorld.gameMode." + gameType.func_77149_b(), new Object[0]);
        }
        
        @Override
        public int getFitWidth(final FontRenderer fr) {
            int maxWidth = 0;
            for (final GameType gameType : PlayerFeatures.VALID_GAME_TYPES) {
                maxWidth = Math.max(maxWidth, JmUI.fontRenderer().func_78256_a(this.getLabel(gameType)));
            }
            return maxWidth + 12;
        }
        
        public void nextValue() {
            int index;
            if (GameTypeButton.currentGameType == null) {
                index = 1;
            }
            else {
                index = GameTypeButton.currentGameType.func_77148_a() + 1;
                if (index > GameType.values().length + 1) {
                    index = 1;
                }
            }
            GameTypeButton.currentGameType = GameType.func_77146_a(index);
            this.updateLabel();
        }
    }
}
