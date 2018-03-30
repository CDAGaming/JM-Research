package journeymap.server.api.impl;

import journeymap.server.api.*;
import javax.annotation.*;
import org.apache.logging.log4j.*;
import journeymap.common.*;
import java.util.*;
import com.google.common.collect.*;
import net.minecraft.world.*;
import journeymap.common.api.feature.*;
import journeymap.common.feature.*;
import net.minecraftforge.fml.common.*;
import journeymap.common.network.*;
import net.minecraft.entity.player.*;
import journeymap.server.feature.*;
import java.util.concurrent.*;
import journeymap.server.*;

@ParametersAreNonnullByDefault
public enum ServerAPI implements IServerAPI
{
    INSTANCE;
    
    private final Logger LOGGER;
    final Map<UUID, PlayerFeatures> playerFeaturesMap;
    final Multimap<UUID, Integer> pausedPlayerQueue;
    final transient ScheduledExecutorService taskExecutor;
    
    private ServerAPI() {
        this.LOGGER = Journeymap.getLogger();
        this.playerFeaturesMap = Collections.synchronizedMap(new HashMap<UUID, PlayerFeatures>());
        this.pausedPlayerQueue = (Multimap<UUID, Integer>)MultimapBuilder.hashKeys().hashSetValues().build();
        this.taskExecutor = Executors.newScheduledThreadPool(0);
        this.log("implements JourneyMap API 2.0-SNAPSHOT");
    }
    
    @Override
    public void setPlayerFeatures(final String modId, final UUID playerID, final int dimension, final GameType gameType, final Map<Feature, Boolean> featureMap) {
        final DimensionPolicies newPolicies = new DimensionPolicies(dimension);
        newPolicies.update(gameType, featureMap, modId);
        final DimensionPolicies currentPolicies = this.getFeatures(playerID).get(dimension);
        currentPolicies.update(newPolicies);
        final EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().func_184103_al().func_177451_a(playerID);
        if (player != null && !this.pausedPlayerQueue.containsEntry((Object)playerID, (Object)dimension)) {
            PacketHandler.sendDimensionPolicyPacketToPlayer(player, newPolicies, false);
        }
    }
    
    @Override
    public Map<Feature, Boolean> getPlayerFeatures(final UUID playerID, final int dimension, final GameType gameType) {
        return this.getFeatures(playerID).get(dimension).getPermissionMap(gameType);
    }
    
    @Override
    public Map<Feature, Boolean> getServerFeatures(final int dimension, final GameType gameType) {
        return ServerFeatures.createDimensionPolicies(dimension, false).getPermissionMap(gameType);
    }
    
    public boolean pauseClientPackets(final EntityPlayerMP player) {
        return this.pausedPlayerQueue.put((Object)player.func_110124_au(), (Object)player.field_71093_bK);
    }
    
    public boolean resumeClientPackets(final EntityPlayerMP player) {
        return this.pausedPlayerQueue.remove((Object)player.func_110124_au(), (Object)player.field_71093_bK);
    }
    
    public void sendDimensionPolicies(final EntityPlayerMP player, final int dimension, final int delayMs) {
        if (delayMs < 1) {
            this.sendDimensionPolicies(player, dimension);
        }
        else {
            this.taskExecutor.schedule(() -> this.sendDimensionPolicies(player, dimension), delayMs, TimeUnit.MILLISECONDS);
        }
    }
    
    public void sendDimensionPolicies(final EntityPlayerMP player, final int dimension) {
        if (JourneymapServer.isOp(player.func_110124_au())) {
            Journeymap.getLogger().info("SENDING OP FEATURES: " + dimension);
        }
        else {
            Journeymap.getLogger().info("SENDING PLAYER FEATURES: " + dimension);
        }
        final DimensionPolicies dimPolicies = this.getFeatures(player.func_110124_au()).get(dimension);
        PacketHandler.sendDimensionPolicyPacketToPlayer(player, dimPolicies, true);
    }
    
    public PlayerFeatures getFeatures(final UUID playerUUID) {
        synchronized (this.playerFeaturesMap) {
            return this.playerFeaturesMap.computeIfAbsent(playerUUID, id -> new ServerFeatures(playerUUID));
        }
    }
    
    private void log(final String message) {
        this.LOGGER.info(String.format("[%s] %s", this.getClass().getSimpleName(), message));
    }
    
    private void logError(final String message) {
        this.LOGGER.error(String.format("[%s] %s", this.getClass().getSimpleName(), message));
    }
    
    private void logDebug(final String message) {
        this.LOGGER.debug(String.format("[%s] %s", this.getClass().getSimpleName(), message));
    }
    
    void logError(final String message, final Throwable t) {
        this.LOGGER.error(String.format("[%s] %s", this.getClass().getSimpleName(), message), t);
    }
}
