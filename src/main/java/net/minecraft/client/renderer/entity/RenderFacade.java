package net.minecraft.client.renderer.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderFacade extends Render {
    public RenderFacade(final RenderManager unused) {
        super(unused);
    }

    public static ResourceLocation getEntityTexture(final Render render, final Entity entity) {
        return render.getEntityTexture(entity);
    }

    protected ResourceLocation getEntityTexture(final Entity entity) {
        return null;
    }

    public void doRender(final Entity entity, final double var2, final double var4, final double var6, final float var8, final float var9) {
    }
}
