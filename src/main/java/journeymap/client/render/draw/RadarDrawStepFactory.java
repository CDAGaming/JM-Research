package journeymap.client.render.draw;

import journeymap.client.model.*;
import journeymap.client.render.map.*;
import journeymap.client.properties.*;
import journeymap.client.feature.*;
import journeymap.client.*;
import journeymap.common.api.feature.*;
import journeymap.client.ui.minimap.*;
import net.minecraft.entity.*;
import com.google.common.base.*;
import net.minecraft.entity.player.*;
import net.minecraft.entity.item.*;
import journeymap.client.data.*;
import journeymap.common.*;
import journeymap.common.log.*;
import journeymap.common.feature.*;
import net.minecraft.world.*;
import java.util.*;
import journeymap.client.render.texture.*;

public class RadarDrawStepFactory
{
    public List<DrawStep> prepareSteps(final List<EntityDTO> entityDTOs, final GridRenderer grid, final InGameMapProperties mapProperties) {
        final int dimension = grid.getMapView().dimension;
        final DimensionPolicies dimPolicies = ClientFeatures.instance().get(dimension);
        final GameType gameType = JourneymapClient.getGameType();
        final boolean showPassiveMobs = mapProperties.showAnimals.get() && dimPolicies.isAllowed(gameType, Feature.Radar.PassiveMob);
        final boolean showPets = mapProperties.showPets.get() && dimPolicies.isAllowed(gameType, Feature.Radar.PassiveMob);
        final boolean showVillagers = mapProperties.showVillagers.get() && dimPolicies.isAllowed(gameType, Feature.Radar.NPC);
        final boolean showHostileMobs = mapProperties.showMobs.get() && dimPolicies.isAllowed(gameType, Feature.Radar.HostileMob);
        final boolean showVehicles = mapProperties.showVehicles.get() && dimPolicies.isAllowed(gameType, Feature.Radar.Vehicle);
        final boolean showPlayers = mapProperties.showPlayers.get() && dimPolicies.isAllowed(gameType, Feature.Radar.Player);
        final EntityDisplay mobDisplay = mapProperties.mobDisplay.get();
        final EntityDisplay playerDisplay = mapProperties.playerDisplay.get();
        final boolean showMobHeading = mapProperties.showMobHeading.get();
        final boolean showPlayerHeading = mapProperties.showPlayerHeading.get();
        final boolean showEntityNames = mapProperties.showEntityNames.get();
        final List<DrawStep> drawStepList = new ArrayList<DrawStep>();
        try {
            for (final EntityDTO dto : entityDTOs) {
                try {
                    TextureImpl entityIcon = null;
                    TextureImpl locatorImg = null;
                    final Entity entity = dto.entityRef.get();
                    if (entity == null) {
                        continue;
                    }
                    if (grid.getPixel(dto.posX, dto.posZ) == null) {
                        continue;
                    }
                    final boolean isPet = !Strings.isNullOrEmpty(dto.owner);
                    if (!showPets && isPet) {
                        continue;
                    }
                    if (!showPassiveMobs && dto.passiveAnimal && (!isPet || !showPets)) {
                        continue;
                    }
                    if (!showHostileMobs && Boolean.TRUE == dto.hostile) {
                        continue;
                    }
                    if (!showVillagers && (dto.profession != null || dto.npc)) {
                        continue;
                    }
                    final boolean isPlayer = entity instanceof EntityPlayer;
                    if (!showPlayers && isPlayer) {
                        continue;
                    }
                    if (entity instanceof EntityMinecart || entity instanceof EntityBoat) {
                        entity.func_70005_c_();
                    }
                    if (!showVehicles && (entity instanceof EntityMinecart || entity instanceof EntityBoat)) {
                        continue;
                    }
                    final DrawEntityStep drawStep = DataCache.INSTANCE.getDrawEntityStep(entity);
                    if (isPlayer) {
                        locatorImg = EntityDisplay.getLocatorTexture(playerDisplay, showPlayerHeading);
                        entityIcon = EntityDisplay.getEntityTexture(playerDisplay, entity.func_70005_c_());
                        drawStep.update(playerDisplay, locatorImg, entityIcon, dto.color, showPlayerHeading, false);
                        drawStepList.add(drawStep);
                    }
                    else {
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
                }
                catch (Exception e) {
                    Journeymap.getLogger().error("Exception during prepareSteps: " + LogFormatter.toString(e));
                }
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("Throwable during prepareSteps: " + LogFormatter.toString(t));
        }
        return drawStepList;
    }
}
