package journeymap.client.render.draw;

import journeymap.client.api.display.*;
import journeymap.client.render.map.*;
import java.util.concurrent.*;
import journeymap.client.render.texture.*;
import journeymap.common.*;
import java.awt.geom.*;
import journeymap.client.api.model.*;

public class DrawMarkerStep extends BaseOverlayDrawStep<MarkerOverlay>
{
    private Point2D.Double markerPosition;
    private volatile Future<TextureImpl> iconFuture;
    private TextureImpl iconTexture;
    private boolean hasError;
    
    public DrawMarkerStep(final MarkerOverlay marker) {
        super(marker);
    }
    
    @Override
    public void draw(final DrawStep.Pass pass, final double xOffset, final double yOffset, final GridRenderer gridRenderer, final double fontScale, final double rotation) {
        if (!this.isOnScreen(xOffset, yOffset, gridRenderer, rotation)) {
            return;
        }
        if (pass == DrawStep.Pass.Object) {
            this.ensureTexture();
            if (!this.hasError && this.iconTexture != null) {
                final MapImage icon = ((MarkerOverlay)this.overlay).getIcon();
                DrawUtil.drawColoredSprite(this.iconTexture, icon.getDisplayWidth(), icon.getDisplayHeight(), icon.getTextureX(), icon.getTextureY(), icon.getTextureWidth(), icon.getTextureHeight(), icon.getColor(), icon.getOpacity(), this.markerPosition.x + xOffset - icon.getAnchorX(), this.markerPosition.y + yOffset - icon.getAnchorY(), 1.0f, icon.getRotation() - rotation);
            }
        }
        else {
            super.drawText(pass, xOffset, yOffset, gridRenderer, fontScale, rotation);
        }
    }
    
    protected void ensureTexture() {
        if (this.iconTexture != null) {
            return;
        }
        try {
            if (this.iconFuture == null || this.iconFuture.isCancelled()) {
                this.iconFuture = TextureCache.scheduleTextureTask((Callable<TextureImpl>)new Callable<TextureImpl>() {
                    @Override
                    public TextureImpl call() throws Exception {
                        final MapImage icon = ((MarkerOverlay)DrawMarkerStep.this.overlay).getIcon();
                        if (icon.getImageLocation() != null) {
                            return TextureCache.getTexture(icon.getImageLocation());
                        }
                        if (icon.getImage() != null) {
                            return new TextureImpl(icon.getImage());
                        }
                        return null;
                    }
                });
            }
            else if (this.iconFuture.isDone()) {
                this.iconTexture = this.iconFuture.get();
                if (this.iconTexture.isBindNeeded()) {
                    this.iconTexture.bindTexture();
                }
                this.iconFuture = null;
            }
        }
        catch (Exception e) {
            Journeymap.getLogger().error("Error getting MarkerOverlay image upperTexture: " + e, (Throwable)e);
            this.hasError = true;
        }
    }
    
    @Override
    protected void updatePositions(final GridRenderer gridRenderer, final double rotation) {
        final MapImage icon = ((MarkerOverlay)this.overlay).getIcon();
        this.markerPosition = gridRenderer.getBlockPixelInGrid(((MarkerOverlay)this.overlay).getPoint());
        final int halfBlock = (int)this.lastUiState.blockSize / 2;
        this.markerPosition.setLocation(this.markerPosition.x + halfBlock, this.markerPosition.y + halfBlock);
        final TextProperties textProperties = ((MarkerOverlay)this.overlay).getTextProperties();
        final int xShift = (rotation % 360.0 == 0.0) ? (-textProperties.getOffsetX()) : textProperties.getOffsetX();
        final int yShift = (rotation % 360.0 == 0.0) ? (-textProperties.getOffsetY()) : textProperties.getOffsetY();
        if (xShift != 0 && yShift != 0) {
            final Point2D shiftedPoint = gridRenderer.shiftWindowPosition(this.markerPosition.x, this.markerPosition.y, xShift, yShift);
            this.labelPosition.setLocation(shiftedPoint.getX(), shiftedPoint.getY());
        }
        else {
            this.labelPosition.setLocation(this.markerPosition.x, this.markerPosition.y);
        }
        this.screenBounds.setRect(this.markerPosition.x, this.markerPosition.y, this.lastUiState.blockSize, this.lastUiState.blockSize);
        this.screenBounds.add(this.labelPosition);
        final Rectangle2D.Double iconBounds = new Rectangle2D.Double(this.markerPosition.x - icon.getAnchorX(), this.markerPosition.y - icon.getAnchorY(), icon.getDisplayWidth(), icon.getDisplayHeight());
        this.screenBounds.add(iconBounds);
    }
}
