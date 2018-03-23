package journeymap.client.properties;

import journeymap.client.ui.theme.*;
import journeymap.common.properties.config.*;
import journeymap.client.ui.minimap.*;
import journeymap.common.properties.*;
import net.minecraftforge.fml.client.*;

public class MiniMapProperties extends InGameMapProperties
{
    public final BooleanField enabled;
    public final EnumField<Shape> shape;
    public final EnumField<Position> position;
    public final BooleanField showDayNight;
    public final EnumField<ThemeLabelSource> info1Label;
    public final EnumField<ThemeLabelSource> info2Label;
    public final EnumField<ThemeLabelSource> info3Label;
    public final EnumField<ThemeLabelSource> info4Label;
    public final IntegerField sizePercent;
    public final IntegerField frameAlpha;
    public final IntegerField terrainAlpha;
    public final EnumField<Orientation> orientation;
    public final IntegerField compassFontScale;
    public final BooleanField showCompass;
    public final BooleanField showReticle;
    public final EnumField<ReticleOrientation> reticleOrientation;
    protected final transient int id;
    protected boolean active;
    
    public MiniMapProperties(final int id) {
        this.enabled = new BooleanField(Category.Inherit, "jm.minimap.enable_minimap", true, true);
        this.shape = new EnumField<Shape>(Category.Inherit, "jm.minimap.shape", Shape.Circle);
        this.position = new EnumField<Position>(Category.Inherit, "jm.minimap.position", Position.TopRight);
        this.showDayNight = new BooleanField(Category.Inherit, "jm.common.show_day_night", true);
        this.info1Label = new EnumField<ThemeLabelSource>(Category.Inherit, "jm.minimap.info1_label.button", ThemeLabelSource.Blank);
        this.info2Label = new EnumField<ThemeLabelSource>(Category.Inherit, "jm.minimap.info2_label.button", ThemeLabelSource.GameTime);
        this.info3Label = new EnumField<ThemeLabelSource>(Category.Inherit, "jm.minimap.info3_label.button", ThemeLabelSource.Location);
        this.info4Label = new EnumField<ThemeLabelSource>(Category.Inherit, "jm.minimap.info4_label.button", ThemeLabelSource.Biome);
        this.sizePercent = new IntegerField(Category.Inherit, "jm.minimap.size", 1, 100, 30);
        this.frameAlpha = new IntegerField(Category.Inherit, "jm.minimap.frame_alpha", 0, 100, 100);
        this.terrainAlpha = new IntegerField(Category.Inherit, "jm.minimap.terrain_alpha", 0, 100, 100);
        this.orientation = new EnumField<Orientation>(Category.Inherit, "jm.minimap.orientation.button", Orientation.North);
        this.compassFontScale = new IntegerField(Category.Inherit, "jm.minimap.compass_font_scale", 1, 4, 1);
        this.showCompass = new BooleanField(Category.Inherit, "jm.minimap.show_compass", true);
        this.showReticle = new BooleanField(Category.Inherit, "jm.minimap.show_reticle", true);
        this.reticleOrientation = new EnumField<ReticleOrientation>(Category.Inherit, "jm.minimap.reticle_orientation", ReticleOrientation.Compass);
        this.active = false;
        this.id = id;
    }
    
    @Override
    public String getName() {
        return String.format("minimap%s", (this.id > 1) ? this.id : "");
    }
    
    public boolean isActive() {
        return this.active;
    }
    
    public void setActive(final boolean active) {
        if (this.active != active) {
            this.active = active;
            this.save();
        }
    }
    
    public int getId() {
        return this.id;
    }
    
    @Override
    public <T extends PropertiesBase> void updateFrom(final T otherInstance) {
        super.updateFrom(otherInstance);
        if (otherInstance instanceof MiniMapProperties) {
            this.setActive(((MiniMapProperties)otherInstance).isActive());
        }
    }
    
    public int getSize() {
        return (int)Math.max(128.0, Math.floor(this.sizePercent.get() / 100.0 * FMLClientHandler.instance().getClient().field_71440_d));
    }
    
    @Override
    protected void postLoad(final boolean isNew) {
        super.postLoad(isNew);
        if (isNew) {
            if (this.getId() == 1) {
                this.setActive(true);
                if (FMLClientHandler.instance().getClient() != null && FMLClientHandler.instance().getClient().field_71466_p.func_82883_a()) {
                    super.fontScale.set(2);
                    this.compassFontScale.set(2);
                }
            }
            else {
                this.setActive(false);
                this.position.set(Position.TopRight);
                this.shape.set(Shape.Rectangle);
                this.frameAlpha.set(100);
                this.terrainAlpha.set(100);
                this.orientation.set(Orientation.North);
                this.reticleOrientation.set(ReticleOrientation.Compass);
                this.sizePercent.set(30);
                if (FMLClientHandler.instance().getClient() != null && FMLClientHandler.instance().getClient().field_71466_p.func_82883_a()) {
                    super.fontScale.set(2);
                    this.compassFontScale.set(2);
                }
            }
        }
    }
}
