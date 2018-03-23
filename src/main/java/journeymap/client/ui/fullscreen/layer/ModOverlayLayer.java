package journeymap.client.ui.fullscreen.layer;

import net.minecraft.util.math.*;
import journeymap.client.api.util.*;
import net.minecraft.client.*;
import journeymap.client.render.map.*;
import journeymap.client.api.impl.*;
import journeymap.client.render.draw.*;
import journeymap.common.*;
import java.util.*;
import journeymap.client.api.display.*;
import java.awt.geom.*;

public class ModOverlayLayer implements LayerDelegate.Layer
{
    protected List<OverlayDrawStep> allDrawSteps;
    protected List<OverlayDrawStep> visibleSteps;
    protected List<OverlayDrawStep> touchedSteps;
    protected BlockPos lastCoord;
    protected Point2D.Double lastMousePosition;
    protected UIState lastUiState;
    protected boolean propagateClick;
    
    public ModOverlayLayer() {
        this.allDrawSteps = new ArrayList<OverlayDrawStep>();
        this.visibleSteps = new ArrayList<OverlayDrawStep>();
        this.touchedSteps = new ArrayList<OverlayDrawStep>();
    }
    
    private void ensureCurrent(final Minecraft mc, final GridRenderer gridRenderer, final Point2D.Double mousePosition, final BlockPos blockCoord) {
        final UIState currentUiState = gridRenderer.getUIState();
        final boolean uiStateChange = !Objects.equals(this.lastUiState, currentUiState);
        if (uiStateChange || !Objects.equals(blockCoord, this.lastCoord) || this.lastMousePosition == null) {
            this.lastCoord = blockCoord;
            this.lastUiState = currentUiState;
            this.lastMousePosition = mousePosition;
            this.allDrawSteps.clear();
            ClientAPI.INSTANCE.getDrawSteps(this.allDrawSteps, currentUiState);
            this.updateOverlayState(gridRenderer, mousePosition, blockCoord, uiStateChange);
        }
    }
    
    @Override
    public List<DrawStep> onMouseMove(final Minecraft mc, final GridRenderer gridRenderer, final Point2D.Double mousePosition, final BlockPos blockCoord, final float fontScale, final boolean isScrolling) {
        this.ensureCurrent(mc, gridRenderer, mousePosition, blockCoord);
        if (!this.touchedSteps.isEmpty()) {
            for (final OverlayDrawStep overlayDrawStep : this.touchedSteps) {
                try {
                    final Overlay overlay = overlayDrawStep.getOverlay();
                    final IOverlayListener listener = overlay.getOverlayListener();
                    this.fireOnMouseMove(listener, mousePosition, blockCoord);
                    overlayDrawStep.setTitlePosition(mousePosition);
                }
                catch (Throwable t) {
                    Journeymap.getLogger().error(t.getMessage(), t);
                }
            }
        }
        return Collections.emptyList();
    }
    
    @Override
    public List<DrawStep> onMouseClick(final Minecraft mc, final GridRenderer gridRenderer, final Point2D.Double mousePosition, final BlockPos blockCoord, final int button, final boolean doubleClick, final float fontScale) {
        this.ensureCurrent(mc, gridRenderer, mousePosition, blockCoord);
        this.propagateClick = true;
        if (!this.touchedSteps.isEmpty()) {
            for (final OverlayDrawStep overlayDrawStep : this.touchedSteps) {
                try {
                    final Overlay overlay = overlayDrawStep.getOverlay();
                    final IOverlayListener listener = overlay.getOverlayListener();
                    if (listener == null) {
                        continue;
                    }
                    final boolean continueClick = this.fireOnMouseClick(listener, mousePosition, blockCoord, button, doubleClick);
                    overlayDrawStep.setTitlePosition(mousePosition);
                    if (!continueClick) {
                        this.propagateClick = false;
                        break;
                    }
                    continue;
                }
                catch (Throwable t) {
                    Journeymap.getLogger().error(t.getMessage(), t);
                }
            }
        }
        return Collections.emptyList();
    }
    
    @Override
    public boolean propagateClick() {
        return this.propagateClick;
    }
    
    private void updateOverlayState(final GridRenderer gridRenderer, final Point2D.Double mousePosition, final BlockPos blockCoord, final boolean uiStateChange) {
        for (final OverlayDrawStep overlayDrawStep : this.allDrawSteps) {
            final Overlay overlay = overlayDrawStep.getOverlay();
            final IOverlayListener listener = overlay.getOverlayListener();
            final boolean currentlyActive = this.visibleSteps.contains(overlayDrawStep);
            final boolean currentlyTouched = this.touchedSteps.contains(overlayDrawStep);
            if (overlayDrawStep.isOnScreen(0.0, 0.0, gridRenderer, 0.0)) {
                if (!currentlyActive) {
                    this.visibleSteps.add(overlayDrawStep);
                    this.fireActivate(listener);
                }
                else if (uiStateChange) {
                    this.fireActivate(listener);
                }
                final Rectangle2D.Double bounds = overlayDrawStep.getBounds();
                if (bounds != null && bounds.contains(mousePosition)) {
                    if (currentlyTouched) {
                        continue;
                    }
                    this.touchedSteps.add(overlayDrawStep);
                }
                else {
                    if (!currentlyTouched) {
                        continue;
                    }
                    this.touchedSteps.remove(overlayDrawStep);
                    overlayDrawStep.setTitlePosition(null);
                    this.fireOnMouseOut(listener, mousePosition, blockCoord);
                }
            }
            else {
                if (currentlyTouched) {
                    this.touchedSteps.remove(overlayDrawStep);
                    overlayDrawStep.setTitlePosition(null);
                    this.fireOnMouseOut(listener, mousePosition, blockCoord);
                }
                if (!currentlyActive) {
                    continue;
                }
                this.visibleSteps.remove(overlayDrawStep);
                overlayDrawStep.setTitlePosition(null);
                this.fireDeActivate(listener);
            }
        }
    }
    
    private void fireActivate(final IOverlayListener listener) {
        if (listener != null) {
            try {
                listener.onActivate(this.lastUiState);
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
    
    private void fireDeActivate(final IOverlayListener listener) {
        if (listener != null) {
            try {
                listener.onDeactivate(this.lastUiState);
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
    
    private void fireOnMouseMove(final IOverlayListener listener, final Point2D.Double mousePosition, final BlockPos blockCoord) {
        if (listener != null) {
            try {
                listener.onMouseMove(this.lastUiState, mousePosition, blockCoord);
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
    
    private boolean fireOnMouseClick(final IOverlayListener listener, final Point2D.Double mousePosition, final BlockPos blockCoord, final int button, final boolean doubleClick) {
        if (listener != null) {
            try {
                return listener.onMouseClick(this.lastUiState, mousePosition, blockCoord, button, doubleClick);
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return true;
    }
    
    private void fireOnMouseOut(final IOverlayListener listener, final Point2D.Double mousePosition, final BlockPos blockCoord) {
        if (listener != null) {
            try {
                listener.onMouseOut(this.lastUiState, mousePosition, blockCoord);
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
