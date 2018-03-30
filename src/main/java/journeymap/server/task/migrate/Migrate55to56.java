package journeymap.server.task.migrate;

import journeymap.common.migrate.*;
import journeymap.common.log.*;
import net.minecraftforge.common.*;
import journeymap.server.properties.legacy.*;
import journeymap.server.properties.*;
import journeymap.common.api.feature.*;
import java.util.*;
import net.minecraft.world.*;
import journeymap.common.feature.*;

public class Migrate55to56 extends BaseMigrate implements MigrationTask
{
    public Migrate55to56() {
        super("5.5", "5.6");
    }
    
    @Override
    public Boolean call() throws Exception {
        final GlobalProperties55 legacyGlobal = new GlobalProperties55();
        boolean playerTeleport = false;
        final boolean opTeleport = true;
        try {
            if (legacyGlobal.getFile().exists()) {
                legacyGlobal.load();
                playerTeleport = legacyGlobal.teleportEnabled.get();
                final GlobalProperties opGlobal = new GlobalProperties(true);
                if (!opGlobal.getFile().exists()) {
                    opGlobal.useWorldId.set(legacyGlobal.useWorldId.get());
                    this.migrate(legacyGlobal, opGlobal, opTeleport);
                }
                final GlobalProperties playerGlobal = new GlobalProperties(false);
                if (!playerGlobal.getFile().exists()) {
                    playerGlobal.useWorldId.set(legacyGlobal.useWorldId.get());
                    this.migrate(legacyGlobal, playerGlobal, playerTeleport);
                }
            }
        }
        catch (Exception e) {
            this.logger.error("Error migrating " + legacyGlobal + ": " + LogFormatter.toPartialString(e));
        }
        for (final Integer dim : DimensionManager.getIDs()) {
            final DimensionProperties55 legacyDim = new DimensionProperties55(dim);
            try {
                if (legacyDim.getFile().exists()) {
                    legacyDim.load();
                    final DimensionProperties opDim = new DimensionProperties(dim, true);
                    if (!opDim.getFile().exists()) {
                        opDim.enabled.set(legacyDim.enabled.get());
                        this.migrate(legacyDim, opDim, opTeleport);
                    }
                    final DimensionProperties playerDim = new DimensionProperties(dim, false);
                    if (!playerDim.getFile().exists()) {
                        playerDim.enabled.set(legacyDim.enabled.get());
                        this.migrate(legacyDim, playerDim, playerTeleport);
                    }
                }
            }
            catch (Exception e2) {
                this.logger.error("Error migrating " + legacyDim + ": " + LogFormatter.toPartialString(e2));
            }
        }
        return true;
    }
    
    public void migrate(final PermissionProperties55 legacy, final PermissionProperties properties, final boolean teleportAllowed) {
        final boolean isOp = properties.isOp();
        final boolean surfaceAllowed = isOp ? legacy.opSurfaceMappingEnabled.get() : legacy.surfaceMappingEnabled.get();
        final boolean topoAllowed = isOp ? legacy.opTopoMappingEnabled.get() : legacy.topoMappingEnabled.get();
        final boolean caveAllowed = isOp ? legacy.opCaveMappingEnabled.get() : legacy.caveMappingEnabled.get();
        final boolean radarAllowed = legacy.radarEnabled.get() && (isOp ? legacy.opRadarEnabled.get() : legacy.playerRadarEnabled.get());
        final String origin = "Legacy Server Config";
        final Set<GameType> gameTypes = PlayerFeatures.VALID_GAME_TYPES;
        final PolicyTable policies = properties.policies;
        policies.setAllowed(gameTypes, Feature.Action.Teleport, teleportAllowed, origin);
        policies.setAllowed(gameTypes, Feature.MapType.Day, surfaceAllowed, origin);
        policies.setAllowed(gameTypes, Feature.MapType.Night, surfaceAllowed, origin);
        policies.setAllowed(gameTypes, Feature.MapType.Topo, topoAllowed, origin);
        policies.setAllowed(gameTypes, Feature.MapType.Underground, caveAllowed, origin);
        policies.setAllowed(gameTypes, Feature.Radar.PassiveMob, radarAllowed && legacy.animalRadarEnabled.get(), origin);
        policies.setAllowed(gameTypes, Feature.Radar.HostileMob, radarAllowed && legacy.mobRadarEnabled.get(), origin);
        policies.setAllowed(gameTypes, Feature.Radar.Player, radarAllowed && legacy.playerRadarEnabled.get(), origin);
        policies.setAllowed(gameTypes, Feature.Radar.NPC, radarAllowed && legacy.villagerRadarEnabled.get(), origin);
        properties.save();
    }
}
