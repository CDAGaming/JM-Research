package journeymap.client.render.ingame;

import net.minecraft.util.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.entity.*;
import journeymap.client.properties.*;
import journeymap.common.*;
import journeymap.client.waypoint.*;
import journeymap.client.api.display.*;
import journeymap.common.log.*;
import net.minecraft.client.entity.*;
import java.util.*;
import net.minecraft.util.text.*;
import net.minecraft.client.renderer.*;
import org.lwjgl.opengl.*;
import journeymap.client.cartography.color.*;
import journeymap.client.render.draw.*;
import journeymap.client.api.model.*;
import journeymap.client.render.texture.*;
import net.minecraft.util.math.*;
import net.minecraftforge.fml.client.*;
import journeymap.client.*;
import java.io.*;

public class RenderWaypointBeacon
{
    static final ResourceLocation beam;
    static Minecraft mc;
    static RenderManager renderManager;
    static String distanceLabel;
    static WaypointProperties waypointProperties;
    
    public static void resetStatTimers() {
    }
    
    public static void renderAll() {
        try {
            RenderWaypointBeacon.waypointProperties = Journeymap.getClient().getWaypointProperties();
            final Collection<Waypoint> waypoints = WaypointStore.INSTANCE.getAll();
            final EntityPlayerSP player = Journeymap.clientPlayer();
            if (player == null) {
                return;
            }
            for (final Waypoint wp : waypoints) {
                if (!wp.isDisplayed(player.field_71093_bK)) {}
                try {
                    doRender(wp);
                }
                catch (Throwable t) {
                    Journeymap.getLogger().error("EntityWaypoint failed to render for " + wp + ": " + LogFormatter.toString(t));
                }
            }
        }
        catch (Throwable t2) {
            Journeymap.getLogger().error("Error rendering waypoints: " + LogFormatter.toString(t2));
        }
    }
    
    static void doRender(final Waypoint waypoint) {
        if (RenderWaypointBeacon.renderManager.field_78734_h == null) {
            return;
        }
        RenderHelper.func_74519_b();
        try {
            final Vec3d playerVec = RenderWaypointBeacon.renderManager.field_78734_h.func_174791_d();
            final int dim = RenderWaypointBeacon.renderManager.field_78734_h.field_71093_bK;
            Vec3d waypointVec = waypoint.getVec(dim).func_72441_c(0.0, 0.118, 0.0);
            final double actualDistance = playerVec.func_72438_d(waypointVec);
            final int maxDistance = RenderWaypointBeacon.waypointProperties.maxDistance.get();
            if (maxDistance > 0 && actualDistance > maxDistance) {
                return;
            }
            float fadeAlpha = 1.0f;
            final int minDistance = RenderWaypointBeacon.waypointProperties.minDistance.get();
            if (minDistance > 0) {
                if ((int)actualDistance <= minDistance) {
                    return;
                }
                if ((int)actualDistance <= minDistance + 4) {
                    fadeAlpha = (float)(actualDistance - minDistance) / 3.0f;
                }
            }
            double viewDistance = actualDistance;
            final double maxRenderDistance = RenderWaypointBeacon.mc.field_71474_y.field_151451_c * 16;
            if (viewDistance > maxRenderDistance) {
                final Vec3d delta = waypointVec.func_178788_d(playerVec).func_72432_b();
                waypointVec = playerVec.func_72441_c(delta.field_72450_a * maxRenderDistance, delta.field_72448_b * maxRenderDistance, delta.field_72449_c * maxRenderDistance);
                viewDistance = maxRenderDistance;
            }
            final double shiftX = waypointVec.field_72450_a - RenderWaypointBeacon.renderManager.field_78730_l;
            final double shiftY = waypointVec.field_72448_b - RenderWaypointBeacon.renderManager.field_78731_m;
            final double shiftZ = waypointVec.field_72449_c - RenderWaypointBeacon.renderManager.field_78728_n;
            final boolean showStaticBeam = RenderWaypointBeacon.waypointProperties.showStaticBeam.get();
            final boolean showRotatingBeam = RenderWaypointBeacon.waypointProperties.showRotatingBeam.get();
            if (showStaticBeam || showRotatingBeam) {
                renderBeam(shiftX, -RenderWaypointBeacon.renderManager.field_78731_m, shiftZ, waypoint.getOrDefaultLabelColor(16777215), fadeAlpha, showStaticBeam, showRotatingBeam);
            }
            String waypointName = waypoint.getName();
            boolean labelHidden = false;
            if (viewDistance > 0.5 && RenderWaypointBeacon.waypointProperties.autoHideLabel.get()) {
                final int angle = 5;
                final double yaw = Math.atan2(RenderWaypointBeacon.renderManager.field_78728_n - waypointVec.field_72449_c, RenderWaypointBeacon.renderManager.field_78730_l - waypointVec.field_72450_a);
                double degrees = Math.toDegrees(yaw) + 90.0;
                if (degrees < 0.0) {
                    degrees += 360.0;
                }
                double playerYaw = RenderWaypointBeacon.renderManager.field_78734_h.func_70079_am() % 360.0f;
                if (playerYaw < 0.0) {
                    playerYaw += 360.0;
                }
                playerYaw = Math.toRadians(playerYaw);
                double playerDegrees = Math.toDegrees(playerYaw);
                degrees += angle;
                playerDegrees += angle;
                labelHidden = (Math.abs(degrees + angle - (playerDegrees + angle)) > angle);
            }
            double scale = 0.00390625 * ((viewDistance + 4.0) / 3.0);
            final MapImage icon = WaypointStore.getWaypointIcon(waypoint);
            final MapText label = WaypointStore.getWaypointLabel(waypoint);
            final TextureImpl texture = TextureCache.getTexture(icon.getImageLocation());
            final double halfTexHeight = (texture == null) ? 8.0 : (texture.getHeight() / 2);
            final boolean showName = RenderWaypointBeacon.waypointProperties.showName.get() && waypointName != null && waypointName.length() > 0;
            final boolean showDistance = RenderWaypointBeacon.waypointProperties.showDistance.get();
            if (!labelHidden && (showName || showDistance)) {
                final StringBuilder sb = new StringBuilder();
                if (RenderWaypointBeacon.waypointProperties.boldLabel.get()) {
                    sb.append(TextFormatting.BOLD);
                }
                if (showName) {
                    sb.append(waypointName);
                }
                if (showName && showDistance) {
                    sb.append(" ");
                }
                if (showDistance) {
                    sb.append(String.format(RenderWaypointBeacon.distanceLabel, actualDistance));
                }
                if (sb.length() > 0) {
                    waypointName = sb.toString();
                    GlStateManager.func_179094_E();
                    GlStateManager.func_179140_f();
                    GL11.glNormal3d(0.0, 0.0, -1.0 * scale);
                    GlStateManager.func_179137_b(shiftX, shiftY, shiftZ);
                    GlStateManager.func_179114_b(-RenderWaypointBeacon.renderManager.field_78735_i, 0.0f, 1.0f, 0.0f);
                    GlStateManager.func_179114_b(RenderWaypointBeacon.renderManager.field_78732_j, 1.0f, 0.0f, 0.0f);
                    GlStateManager.func_179139_a(-scale, -scale, scale);
                    GlStateManager.func_179132_a(true);
                    GlStateManager.func_179132_a(true);
                    GlStateManager.func_179126_j();
                    final int fontScale = RenderWaypointBeacon.waypointProperties.fontScale.get();
                    final double labelY = 0.0 - halfTexHeight - 8.0;
                    final int safeColor = RGB.labelSafe(label.getColor());
                    DrawUtil.drawLabel(waypointName, 1.0, labelY, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, label.getBackgroundColor(), 0.6f * fadeAlpha, safeColor, fadeAlpha, fontScale, label.hasFontShadow());
                    GlStateManager.func_179097_i();
                    GlStateManager.func_179132_a(false);
                    DrawUtil.drawLabel(waypointName, 1.0, labelY, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, label.getBackgroundColor(), 0.4f * fadeAlpha, safeColor, fadeAlpha, fontScale, label.hasFontShadow());
                    GlStateManager.func_179121_F();
                }
            }
            if (viewDistance > 0.1 && RenderWaypointBeacon.waypointProperties.showTexture.get()) {
                GlStateManager.func_179094_E();
                GlStateManager.func_179140_f();
                GL11.glNormal3d(0.0, 0.0, -1.0 * scale);
                GlStateManager.func_179097_i();
                GlStateManager.func_179132_a(false);
                scale *= (RenderWaypointBeacon.waypointProperties.textureSmall.get() ? 1 : 2);
                GlStateManager.func_179137_b(shiftX, shiftY, shiftZ);
                GlStateManager.func_179114_b(-RenderWaypointBeacon.renderManager.field_78735_i, 0.0f, 1.0f, 0.0f);
                GlStateManager.func_179114_b(RenderWaypointBeacon.renderManager.field_78732_j, 1.0f, 0.0f, 0.0f);
                GlStateManager.func_179139_a(-scale, -scale, scale);
                GL11.glNormal3d(0.0, 0.0, -1.0 * scale);
                DrawUtil.drawColoredImage(texture, waypoint.getOrDefaultIconColor(16777215), fadeAlpha, 0 - texture.getWidth() / 2 + 0.5, 0.0 - halfTexHeight + 0.2, 0.0);
                GlStateManager.func_179121_F();
            }
        }
        catch (Exception e) {}
        finally {
            GlStateManager.func_179132_a(true);
            GlStateManager.func_179126_j();
            GlStateManager.func_179145_e();
            GlStateManager.func_179132_a(true);
            GlStateManager.func_179089_o();
            GlStateManager.func_179084_k();
            GlStateManager.func_179106_n();
            RenderHelper.func_74518_a();
        }
    }
    
    static void renderBeam(double x, final double y, double z, final Integer color, final float alpha, final boolean staticBeam, final boolean rotatingBeam) {
        RenderWaypointBeacon.mc.field_71446_o.func_110577_a(RenderWaypointBeacon.beam);
        GL11.glTexParameterf(3553, 10242, 10497.0f);
        GlStateManager.func_179140_f();
        GlStateManager.func_179084_k();
        GlStateManager.func_179126_j();
        GlStateManager.func_179120_a(770, 1, 1, 0);
        float time = Journeymap.clientWorld().func_82737_E();
        if (RenderWaypointBeacon.mc.func_147113_T()) {
            time = Minecraft.func_71386_F() / 50L;
        }
        final float texOffset = -(-time * 0.2f - MathHelper.func_76141_d(-time * 0.1f)) * 0.6f;
        if (rotatingBeam) {
            final byte b0 = 1;
            final double d3 = time * 0.025 * (1.0 - (b0 & 0x1) * 2.5);
            final int[] rgba = RGB.ints(color, alpha * 0.45f);
            DrawUtil.startDrawingQuads(true);
            GlStateManager.func_179147_l();
            final double d4 = b0 * 0.2;
            final double d5 = Math.cos(d3 + 2.356194490192345) * d4;
            final double d6 = Math.sin(d3 + 2.356194490192345) * d4;
            final double d7 = Math.cos(d3 + 0.7853981633974483) * d4;
            final double d8 = Math.sin(d3 + 0.7853981633974483) * d4;
            final double d9 = Math.cos(d3 + 3.9269908169872414) * d4;
            final double d10 = Math.sin(d3 + 3.9269908169872414) * d4;
            final double d11 = Math.cos(d3 + 5.497787143782138) * d4;
            final double d12 = Math.sin(d3 + 5.497787143782138) * d4;
            final double d13 = 256.0f * alpha;
            final double d14 = 0.0;
            final double d15 = 1.0;
            final double d16 = -1.0f + texOffset;
            final double d17 = 256.0f * alpha * (0.5 / d4) + d16;
            DrawUtil.addVertexWithUV(x + d5, y + d13, z + d6, d15, d17, rgba);
            DrawUtil.addVertexWithUV(x + d5, y, z + d6, d15, d16, rgba);
            DrawUtil.addVertexWithUV(x + d7, y, z + d8, d14, d16, rgba);
            DrawUtil.addVertexWithUV(x + d7, y + d13, z + d8, d14, d17, rgba);
            DrawUtil.addVertexWithUV(x + d11, y + d13, z + d12, d15, d17, rgba);
            DrawUtil.addVertexWithUV(x + d11, y, z + d12, d15, d16, rgba);
            DrawUtil.addVertexWithUV(x + d9, y, z + d10, d14, d16, rgba);
            DrawUtil.addVertexWithUV(x + d9, y + d13, z + d10, d14, d17, rgba);
            DrawUtil.addVertexWithUV(x + d7, y + d13, z + d8, d15, d17, rgba);
            DrawUtil.addVertexWithUV(x + d7, y, z + d8, d15, d16, rgba);
            DrawUtil.addVertexWithUV(x + d11, y, z + d12, d14, d16, rgba);
            DrawUtil.addVertexWithUV(x + d11, y + d13, z + d12, d14, d17, rgba);
            DrawUtil.addVertexWithUV(x + d9, y + d13, z + d10, d15, d17, rgba);
            DrawUtil.addVertexWithUV(x + d9, y, z + d10, d15, d16, rgba);
            DrawUtil.addVertexWithUV(x + d5, y, z + d6, d14, d16, rgba);
            DrawUtil.addVertexWithUV(x + d5, y + d13, z + d6, d14, d17, rgba);
            DrawUtil.draw();
        }
        if (staticBeam) {
            GlStateManager.func_179129_p();
            final double d18 = 256.0f * alpha;
            final double d19 = -1.0f + texOffset;
            final double d20 = 256.0f * alpha + d19;
            x -= 0.5;
            z -= 0.5;
            GlStateManager.func_179147_l();
            GlStateManager.func_179120_a(770, 771, 1, 0);
            GlStateManager.func_179132_a(false);
            final int[] rgba2 = RGB.ints(color, alpha * 0.4f);
            DrawUtil.startDrawingQuads(true);
            DrawUtil.addVertexWithUV(x + 0.2, y + d18, z + 0.2, 1.0, d20, rgba2);
            DrawUtil.addVertexWithUV(x + 0.2, y, z + 0.2, 1.0, d19, rgba2);
            DrawUtil.addVertexWithUV(x + 0.8, y, z + 0.2, 0.0, d19, rgba2);
            DrawUtil.addVertexWithUV(x + 0.8, y + d18, z + 0.2, 0.0, d20, rgba2);
            DrawUtil.addVertexWithUV(x + 0.8, y + d18, z + 0.8, 1.0, d20, rgba2);
            DrawUtil.addVertexWithUV(x + 0.8, y, z + 0.8, 1.0, d19, rgba2);
            DrawUtil.addVertexWithUV(x + 0.2, y, z + 0.8, 0.0, d19, rgba2);
            DrawUtil.addVertexWithUV(x + 0.2, y + d18, z + 0.8, 0.0, d20, rgba2);
            DrawUtil.addVertexWithUV(x + 0.8, y + d18, z + 0.2, 1.0, d20, rgba2);
            DrawUtil.addVertexWithUV(x + 0.8, y, z + 0.2, 1.0, d19, rgba2);
            DrawUtil.addVertexWithUV(x + 0.8, y, z + 0.8, 0.0, d19, rgba2);
            DrawUtil.addVertexWithUV(x + 0.8, y + d18, z + 0.8, 0.0, d20, rgba2);
            DrawUtil.addVertexWithUV(x + 0.2, y + d18, z + 0.8, 1.0, d20, rgba2);
            DrawUtil.addVertexWithUV(x + 0.2, y, z + 0.8, 1.0, d19, rgba2);
            DrawUtil.addVertexWithUV(x + 0.2, y, z + 0.2, 0.0, d19, rgba2);
            DrawUtil.addVertexWithUV(x + 0.2, y + d18, z + 0.2, 0.0, d20, rgba2);
            DrawUtil.draw();
            GlStateManager.func_179084_k();
        }
        GlStateManager.func_179145_e();
        GlStateManager.func_179098_w();
        GlStateManager.func_179145_e();
        GlStateManager.func_179126_j();
    }
    
    static {
        beam = new ResourceLocation("textures/entity/beacon_beam.png");
        RenderWaypointBeacon.mc = FMLClientHandler.instance().getClient();
        RenderWaypointBeacon.renderManager = RenderWaypointBeacon.mc.func_175598_ae();
        RenderWaypointBeacon.distanceLabel = Constants.getString("jm.waypoint.distance_meters", "%1.0f");
    }
}
