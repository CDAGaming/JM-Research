package journeymap.client.render.draw;

import net.minecraft.client.*;
import journeymap.client.render.texture.*;
import java.lang.ref.*;
import java.awt.geom.*;
import journeymap.common.*;
import journeymap.client.ui.minimap.*;
import journeymap.client.data.*;
import net.minecraft.entity.player.*;
import net.minecraft.scoreboard.*;
import journeymap.client.render.map.*;
import net.minecraft.entity.*;
import net.minecraft.client.entity.*;
import com.google.common.cache.*;

public class DrawEntityStep implements DrawStep
{
    static final Integer labelBg;
    static final int labelBgAlpha = 180;
    static final Integer labelFg;
    static final int labelFgAlpha = 225;
    boolean useDots;
    int elevationOffset;
    int color;
    boolean hideSneaks;
    boolean showHeading;
    boolean showName;
    Minecraft minecraft;
    TextureImpl entityTexture;
    TextureImpl locatorTexture;
    WeakReference<Entity> entityRef;
    String customName;
    String playerTeamName;
    Point2D screenPosition;
    float drawScale;
    
    private DrawEntityStep(final Entity entity) {
        this.showHeading = true;
        this.showName = true;
        this.minecraft = Minecraft.func_71410_x();
        this.drawScale = 1.0f;
        this.entityRef = new WeakReference<Entity>(entity);
        this.hideSneaks = Journeymap.getClient().getCoreProperties().hideSneakingEntities.get();
    }
    
    public void update(final EntityDisplay entityDisplay, final TextureImpl locatorTexture, final TextureImpl entityTexture, final int color, final boolean showHeading, final boolean showName) {
        final Entity entity = this.entityRef.get();
        if (showName && entity != null) {
            this.customName = DataCache.INSTANCE.getEntityDTO(entity).customName;
        }
        this.useDots = entityDisplay.isDots();
        this.color = color;
        this.locatorTexture = locatorTexture;
        this.entityTexture = entityTexture;
        this.drawScale = ((entityDisplay == EntityDisplay.SmallIcons) ? 0.6666667f : 1.0f);
        this.showHeading = showHeading;
        this.showName = showName;
        if (entity instanceof EntityPlayer) {
            final Team team = entity.func_96124_cp();
            if (team != null) {
                this.playerTeamName = ScorePlayerTeam.func_96667_a(entity.func_96124_cp(), entity.func_70005_c_());
            }
            else {
                this.playerTeamName = null;
            }
        }
    }
    
    @Override
    public void draw(final Pass pass, final double xOffset, final double yOffset, final GridRenderer gridRenderer, final double fontScale, final double rotation) {
        if (pass == Pass.Tooltip) {
            return;
        }
        final Entity entity = this.entityRef.get();
        final EntityPlayerSP player = Journeymap.clientPlayer();
        if (player == null) {
            return;
        }
        if (pass == Pass.Object) {
            if (entity == null || entity.field_70128_L || entity.func_82150_aj() || entity.func_98034_c((EntityPlayer)player) || !entity.field_70175_ag || (this.hideSneaks && entity.func_70093_af())) {
                this.screenPosition = null;
                return;
            }
            this.screenPosition = gridRenderer.getPixel(entity.field_70165_t, entity.field_70161_v);
        }
        if (this.screenPosition != null) {
            double heading;
            if (entity instanceof EntityLivingBase) {
                heading = ((EntityLivingBase)entity).field_70759_as;
            }
            else {
                heading = entity.field_70177_z;
            }
            final double drawX = this.screenPosition.getX() + xOffset;
            final double drawY = this.screenPosition.getY() + yOffset;
            float alpha = 1.0f;
            if (entity.field_70163_u > player.field_70163_u) {
                alpha = 1.0f - Math.max(0.1f, (float)((entity.field_70163_u - player.field_70163_u) / 32.0));
            }
            if (entity instanceof EntityPlayer) {
                this.drawPlayer(pass, drawX, drawY, gridRenderer, alpha, heading, fontScale, rotation);
            }
            else {
                this.drawCreature(pass, drawX, drawY, gridRenderer, alpha, heading, fontScale, rotation);
            }
        }
    }
    
    private void drawPlayer(final Pass pass, final double drawX, final double drawY, final GridRenderer gridRenderer, final float alpha, final double heading, final double fontScale, final double rotation) {
        final Entity entity = this.entityRef.get();
        if (entity == null) {
            return;
        }
        if (pass == Pass.Object) {
            if (this.locatorTexture != null) {
                DrawUtil.drawColoredEntity(drawX, drawY, this.locatorTexture, this.color, alpha, this.drawScale, this.showHeading ? heading : (-rotation));
            }
            if (this.entityTexture != null) {
                if (this.useDots) {
                    boolean flip = false;
                    this.elevationOffset = (int)(DataCache.getPlayer().posY - entity.field_70163_u);
                    if (this.elevationOffset < -1 || this.elevationOffset > 1) {
                        flip = (this.elevationOffset < -1);
                        DrawUtil.drawColoredEntity(drawX, drawY, this.entityTexture, this.color, alpha, this.drawScale, flip ? (-rotation + 180.0) : (-rotation));
                    }
                }
                else {
                    DrawUtil.drawColoredEntity(drawX, drawY, this.entityTexture, this.color, alpha, this.drawScale, -rotation);
                }
            }
        }
        if (pass == Pass.Text) {
            final int labelOffset = (this.entityTexture == null) ? 0 : ((rotation == 0.0) ? (-this.entityTexture.getHeight() / 2) : (this.entityTexture.getHeight() / 2));
            final Point2D labelPoint = gridRenderer.shiftWindowPosition((int)drawX, (int)drawY, 0, -labelOffset);
            if (this.playerTeamName != null) {
                DrawUtil.drawLabel(this.playerTeamName, labelPoint.getX(), labelPoint.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, 0, 0.8f, 16777215, 1.0f, fontScale, false, rotation);
            }
            else {
                DrawUtil.drawLabel(entity.func_70005_c_(), labelPoint.getX(), labelPoint.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, 0, 0.8f, 65280, 1.0f, fontScale, false, rotation);
            }
        }
    }
    
    private void drawCreature(final Pass pass, final double drawX, final double drawY, final GridRenderer gridRenderer, final float alpha, final double heading, final double fontScale, final double rotation) {
        final Entity entity = this.entityRef.get();
        if (entity == null) {
            return;
        }
        if (pass == Pass.Object && this.locatorTexture != null) {
            DrawUtil.drawColoredEntity(drawX, drawY, this.locatorTexture, this.color, alpha, this.drawScale, this.showHeading ? heading : (-rotation));
        }
        final int labelOffset = (this.entityTexture == null) ? 8 : ((rotation == 0.0) ? this.entityTexture.getHeight() : (-this.entityTexture.getHeight()));
        if (pass == Pass.Text && this.showName && this.customName != null) {
            final Point2D labelPoint = gridRenderer.shiftWindowPosition(drawX, drawY, 0, labelOffset);
            DrawUtil.drawCenteredLabel(this.customName, labelPoint.getX(), labelPoint.getY(), DrawEntityStep.labelBg, 180.0f, Integer.valueOf(16777215), 225.0f, fontScale, rotation);
        }
        if (pass == Pass.Object && this.entityTexture != null) {
            if (this.useDots) {
                boolean flip = false;
                this.elevationOffset = (int)(DataCache.getPlayer().posY - entity.field_70163_u);
                if (this.elevationOffset < -1 || this.elevationOffset > 1) {
                    flip = (this.elevationOffset < -1);
                    DrawUtil.drawColoredEntity(drawX, drawY, this.entityTexture, this.color, alpha, this.drawScale, flip ? (-rotation + 180.0) : (-rotation));
                }
            }
            else {
                DrawUtil.drawEntity(drawX, drawY, -rotation, this.entityTexture, alpha, this.drawScale, 0.0);
            }
        }
    }
    
    @Override
    public int getDisplayOrder() {
        return (this.customName != null) ? 1 : 0;
    }
    
    @Override
    public String getModId() {
        return "journeymap";
    }
    
    static {
        labelBg = 0;
        labelFg = 16777215;
    }
    
    public static class SimpleCacheLoader extends CacheLoader<Entity, DrawEntityStep>
    {
        public DrawEntityStep load(final Entity entity) throws Exception {
            return new DrawEntityStep(entity, null);
        }
    }
}
