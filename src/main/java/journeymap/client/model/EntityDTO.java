package journeymap.client.model;

import com.google.common.base.Strings;
import com.google.common.cache.CacheLoader;
import journeymap.client.properties.CoreProperties;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class EntityDTO implements Serializable {
    public final String entityId;
    public transient WeakReference<EntityLivingBase> entityLivingRef;
    public transient ResourceLocation entityIconLocation;
    public String iconLocation;
    public Boolean hostile;
    public double posX;
    public double posY;
    public double posZ;
    public int chunkCoordX;
    public int chunkCoordY;
    public int chunkCoordZ;
    public double heading;
    public String customName;
    public String owner;
    public String profession;
    public String username;
    public String biome;
    public int dimension;
    public Boolean underground;
    public boolean invisible;
    public boolean sneaking;
    public boolean passiveAnimal;
    public boolean npc;
    public int color;

    private EntityDTO(final EntityLivingBase entity) {
        this.entityLivingRef = new WeakReference<>(entity);
        this.entityId = entity.getUniqueID().toString();
    }

    public void update(final EntityLivingBase entity, boolean hostile) {
        final Minecraft mc = Minecraft.getMinecraft();
        final EntityPlayer currentPlayer = FMLClientHandler.instance().getClient().player;
        this.dimension = entity.dimension;
        this.posX = entity.posX;
        this.posY = entity.posY;
        this.posZ = entity.posZ;
        this.chunkCoordX = entity.chunkCoordX;
        this.chunkCoordY = entity.chunkCoordY;
        this.chunkCoordZ = entity.chunkCoordZ;
        this.heading = Math.round(entity.rotationYawHead % 360.0f);
        if (currentPlayer != null) {
            this.invisible = entity.isInvisibleToPlayer(currentPlayer);
        } else {
            this.invisible = false;
        }
        this.sneaking = entity.isSneaking();
        final CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
        ResourceLocation entityIcon;
        int playerColor = coreProperties.getColor(coreProperties.colorPlayer);
        ScorePlayerTeam team = null;
        try {
            team = mc.world.getScoreboard().getPlayersTeam(entity.getCachedUniqueIdString());
        } catch (Throwable t3) {
        }
        if (entity instanceof EntityPlayer) {
            this.username = StringUtils.stripControlCodes(entity.getName());
            try {
                if (team != null) {
                    playerColor = team.getColor().getColorIndex();
                } else if (currentPlayer.equals(entity)) {
                    playerColor = coreProperties.getColor(coreProperties.colorSelf);
                } else {
                    playerColor = coreProperties.getColor(coreProperties.colorPlayer);
                }
            } catch (Throwable t4) {
            }
            entityIcon = DefaultPlayerSkin.getDefaultSkinLegacy();
            try {
                final NetHandlerPlayClient client = Minecraft.getMinecraft().getConnection();
                final NetworkPlayerInfo info = client.getPlayerInfo(entity.getUniqueID());
                if (info != null) {
                    entityIcon = info.getLocationSkin();
                }
            } catch (Throwable t) {
                Journeymap.getLogger().error("Error looking up player skin: " + LogFormatter.toPartialString(t));
            }
        } else {
            this.username = null;
            entityIcon = EntityHelper.getIconTextureLocation(entity);
        }
        if (entityIcon != null) {
            this.entityIconLocation = entityIcon;
            this.iconLocation = entityIcon.toString();
        }
        String owner = null;
        if (entity instanceof EntityTameable) {
            final Entity ownerEntity = ((EntityTameable) entity).getOwner();
            if (ownerEntity != null) {
                owner = ownerEntity.getName();
            }
        } else if (entity instanceof IEntityOwnable) {
            final Entity ownerEntity = ((IEntityOwnable) entity).getOwner();
            if (ownerEntity != null) {
                owner = ownerEntity.getName();
            }
        } else if (entity instanceof EntityHorse) {
            final UUID ownerUuid = ((EntityHorse) entity).getOwnerUniqueId();
            if (currentPlayer != null && ownerUuid != null) {
                try {
                    final String playerUuid = currentPlayer.getUniqueID().toString();
                    if (playerUuid.equals(ownerUuid.toString())) {
                        owner = currentPlayer.getName();
                    }
                } catch (Throwable t2) {
                    t2.printStackTrace();
                }
            }
        }
        this.owner = owner;
        String customName = null;
        boolean passive = false;
        if (entity instanceof EntityLiving) {
            final EntityLiving entityLiving = (EntityLiving) entity;
            if (entity.hasCustomName() && entityLiving.getAlwaysRenderNameTag()) {
                customName = StringUtils.stripControlCodes(entity.getCustomNameTag());
            }
            if (!hostile && currentPlayer != null) {
                final EntityLivingBase attackTarget = ((EntityLiving) entity).getAttackTarget();
                if (attackTarget != null && attackTarget.getUniqueID().equals(currentPlayer.getUniqueID())) {
                    hostile = true;
                }
            }
            if (EntityHelper.isPassive((EntityLiving) entity)) {
                passive = true;
            }
        }
        if (entity instanceof EntityVillager) {
            final EntityVillager villager = (EntityVillager) entity;
            this.profession = villager.getProfessionForge().getCareer(villager.careerId).getName();
        } else if (entity instanceof INpc) {
            this.npc = true;
            this.profession = null;
            this.passiveAnimal = false;
        } else {
            this.profession = null;
            this.passiveAnimal = passive;
        }
        this.customName = customName;
        this.hostile = hostile;
        if (entity instanceof EntityPlayer) {
            this.color = playerColor;
        } else if (team != null) {
            this.color = team.getColor().getColorIndex();
        } else if (!Strings.isNullOrEmpty(owner)) {
            this.color = coreProperties.getColor(coreProperties.colorPet);
        } else if (this.profession != null || this.npc) {
            this.color = coreProperties.getColor(coreProperties.colorVillager);
        } else if (hostile) {
            this.color = coreProperties.getColor(coreProperties.colorHostile);
        } else {
            this.color = coreProperties.getColor(coreProperties.colorPassive);
        }
    }

    public static class SimpleCacheLoader extends CacheLoader<EntityLivingBase, EntityDTO> {
        public EntityDTO load(final EntityLivingBase entity) throws Exception {
            return new EntityDTO(entity);
        }
    }
}
