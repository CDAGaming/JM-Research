package journeymap.client.render.draw;

import com.google.common.base.Strings;
import journeymap.client.data.DataCache;
import journeymap.client.model.EntityDTO;
import journeymap.client.properties.InGameMapProperties;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.ui.minimap.EntityDisplay;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public class RadarDrawStepFactory {
    public List<DrawStep> prepareSteps(final List<EntityDTO> entityDTOs, final GridRenderer grid, final InGameMapProperties mapProperties) {
        final boolean showAnimals = mapProperties.showAnimals.get();
        final boolean showPets = mapProperties.showPets.get();
        final boolean showVillagers = mapProperties.showVillagers.get();
        final EntityDisplay mobDisplay = mapProperties.mobDisplay.get();
        final EntityDisplay playerDisplay = mapProperties.playerDisplay.get();
        final boolean showMobHeading = mapProperties.showMobHeading.get();
        final boolean showPlayerHeading = mapProperties.showPlayerHeading.get();
        final boolean showEntityNames = mapProperties.showEntityNames.get();
        final List<DrawStep> drawStepList = new ArrayList<>();
        try {
            for (final EntityDTO dto : entityDTOs) {
                try {
                    TextureImpl entityIcon;
                    TextureImpl locatorImg;
                    final EntityLivingBase entityLiving = dto.entityLivingRef.get();
                    if (entityLiving == null) {
                        continue;
                    }
                    if (grid.getPixel(dto.posX, dto.posZ) == null) {
                        continue;
                    }
                    final boolean isPet = !Strings.isNullOrEmpty(dto.owner);
                    if (!showPets && isPet) {
                        continue;
                    }
                    if (!showAnimals && dto.passiveAnimal && (!isPet || !showPets)) {
                        continue;
                    }
                    if (!showVillagers && (dto.profession != null || dto.npc)) {
                        continue;
                    }
                    final DrawEntityStep drawStep = DataCache.INSTANCE.getDrawEntityStep(entityLiving);
                    final boolean isPlayer = entityLiving instanceof EntityPlayer;
                    if (isPlayer) {
                        locatorImg = EntityDisplay.getLocatorTexture(playerDisplay, showPlayerHeading);
                        entityIcon = EntityDisplay.getEntityTexture(playerDisplay, entityLiving.getName());
                        drawStep.update(playerDisplay, locatorImg, entityIcon, dto.color, showPlayerHeading, false);
                        drawStepList.add(drawStep);
                    } else {
                        locatorImg = EntityDisplay.getLocatorTexture(mobDisplay, showMobHeading);
                        entityIcon = EntityDisplay.getEntityTexture(mobDisplay, dto.entityIconLocation);
                        EntityDisplay actualDisplay = mobDisplay;
                        if (!mobDisplay.isDots() && entityIcon == null) {
                            actualDisplay = (mobDisplay.isLarge() ? EntityDisplay.LargeDots : EntityDisplay.SmallDots);
                            entityIcon = EntityDisplay.getEntityTexture(actualDisplay, dto.entityIconLocation);
                        }
                        drawStep.update(actualDisplay, locatorImg, entityIcon, dto.color, showMobHeading, showEntityNames);
                        drawStepList.add(drawStep);
                    }
                } catch (Exception e) {
                    Journeymap.getLogger().error("Exception during prepareSteps: " + LogFormatter.toString(e));
                }
            }
        } catch (Throwable t) {
            Journeymap.getLogger().error("Throwable during prepareSteps: " + LogFormatter.toString(t));
        }
        return drawStepList;
    }
}
