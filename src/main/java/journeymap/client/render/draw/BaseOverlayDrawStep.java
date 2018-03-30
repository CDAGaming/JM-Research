package journeymap.client.render.draw;

import journeymap.client.api.display.*;
import java.awt.geom.*;
import journeymap.client.api.util.*;
import journeymap.client.render.map.*;
import journeymap.client.api.model.*;
import journeymap.common.api.feature.*;
import java.util.*;
import com.google.common.base.*;
import javax.annotation.*;

public abstract class BaseOverlayDrawStep<T extends Overlay> implements OverlayDrawStep
{
    public final T overlay;
    protected Rectangle2D.Double screenBounds;
    protected Point2D.Double titlePosition;
    protected Point2D.Double labelPosition;
    protected UIState lastUiState;
    protected boolean dragging;
    protected boolean enabled;
    protected String[] labelLines;
    protected String[] titleLines;
    
    protected BaseOverlayDrawStep(final T overlay) {
        this.screenBounds = new Rectangle2D.Double();
        this.titlePosition = new Point2D.Double();
        this.labelPosition = new Point2D.Double();
        this.lastUiState = null;
        this.dragging = false;
        this.enabled = true;
        this.overlay = overlay;
    }
    
    protected abstract void updatePositions(final GridRenderer p0, final double p1);
    
    protected void drawText(final DrawStep.Pass pass, final double xOffset, final double yOffset, final GridRenderer gridRenderer, final double fontScale, final double rotation) {
        final TextProperties textProperties = this.overlay.getTextProperties();
        if (textProperties.isActiveIn(gridRenderer.getUIState())) {
            if (pass == DrawStep.Pass.Text) {
                if (this.labelPosition != null) {
                    if (this.labelLines == null) {
                        this.updateTextFields();
                    }
                    if (this.labelLines != null) {
                        final double x = this.labelPosition.x + xOffset;
                        final double y = this.labelPosition.y + yOffset;
                        DrawUtil.drawLabels(this.labelLines, x, y, DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, textProperties.getBackgroundColor(), textProperties.getBackgroundOpacity(), textProperties.getColor(), textProperties.getOpacity(), textProperties.getScale() * fontScale, textProperties.hasFontShadow(), rotation);
                    }
                }
            }
            else if (pass == DrawStep.Pass.Tooltip && this.titlePosition != null) {
                if (this.titleLines == null) {
                    this.updateTextFields();
                }
                if (this.titleLines != null) {
                    final double x = this.titlePosition.x + 5.0 + xOffset;
                    final double y = this.titlePosition.y + yOffset;
                    DrawUtil.drawLabels(this.titleLines, x, y, DrawUtil.HAlign.Right, DrawUtil.VAlign.Above, textProperties.getBackgroundColor(), textProperties.getBackgroundOpacity(), textProperties.getColor(), textProperties.getOpacity(), textProperties.getScale() * fontScale, textProperties.hasFontShadow(), rotation);
                }
            }
        }
    }
    
    @Override
    public boolean isOnScreen(final double xOffset, final double yOffset, final GridRenderer gridRenderer, final double rotation) {
        if (!this.enabled) {
            return false;
        }
        final UIState uiState = gridRenderer.getUIState();
        if (!this.overlay.isActiveIn(uiState)) {
            return false;
        }
        boolean draggingDone = false;
        if (xOffset != 0.0 || yOffset != 0.0) {
            this.dragging = true;
        }
        else {
            draggingDone = this.dragging;
            this.dragging = false;
        }
        if (draggingDone || uiState.ui == Feature.Display.Minimap || this.overlay.getNeedsRerender() || !Objects.equals(uiState, this.lastUiState)) {
            this.lastUiState = uiState;
            this.updatePositions(gridRenderer, rotation);
            this.overlay.clearFlagForRerender();
        }
        return this.screenBounds != null && gridRenderer.isOnScreen(this.screenBounds);
    }
    
    protected void updateTextFields() {
        if (this.labelPosition != null) {
            final String labelText = this.overlay.getLabel();
            if (!Strings.isNullOrEmpty(labelText)) {
                this.labelLines = labelText.split("\n");
            }
            else {
                this.labelLines = null;
            }
        }
        if (this.titlePosition != null) {
            final String titleText = this.overlay.getTitle();
            if (!Strings.isNullOrEmpty(titleText)) {
                this.titleLines = titleText.split("\n");
            }
            else {
                this.titleLines = null;
            }
        }
    }
    
    @Override
    public void setTitlePosition(@Nullable final Point2D.Double titlePosition) {
        this.titlePosition = titlePosition;
    }
    
    @Override
    public int getDisplayOrder() {
        return this.overlay.getDisplayOrder();
    }
    
    @Override
    public String getModId() {
        return this.overlay.getModId();
    }
    
    @Override
    public Rectangle2D.Double getBounds() {
        return this.screenBounds;
    }
    
    @Override
    public Overlay getOverlay() {
        return this.overlay;
    }
    
    @Override
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }
}
