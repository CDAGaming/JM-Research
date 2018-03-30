package journeymap.client.model;

import java.io.*;
import java.lang.ref.*;
import journeymap.common.*;
import net.minecraft.entity.player.*;
import net.minecraft.client.entity.*;
import net.minecraft.util.*;
import net.minecraft.client.resources.*;
import net.minecraft.client.*;
import journeymap.common.log.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.*;
import com.google.common.base.*;
import journeymap.client.properties.*;
import net.minecraft.scoreboard.*;
import net.minecraft.client.network.*;
import java.util.*;
import com.google.common.cache.*;

public class EntityDTO implements Serializable
{
    public final String entityId;
    public transient WeakReference<Entity> entityRef;
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
    
    private EntityDTO(final Entity entity) {
        this.entityRef = new WeakReference<Entity>(entity);
        this.entityId = entity.func_110124_au().toString();
    }
    
    public void update(final Entity entity, boolean hostile) {
        this.dimension = entity.field_71093_bK;
        this.posX = entity.field_70165_t;
        this.posY = entity.field_70163_u;
        this.posZ = entity.field_70161_v;
        this.chunkCoordX = entity.field_70176_ah;
        this.chunkCoordY = entity.field_70162_ai;
        this.chunkCoordZ = entity.field_70164_aj;
        if (entity instanceof EntityLivingBase) {
            this.heading = Math.round(entity.func_70079_am() % 360.0f);
        }
        else {
            this.heading = Math.round(entity.field_70177_z % 360.0f);
        }
        final EntityPlayerSP currentPlayer = Journeymap.clientPlayer();
        if (currentPlayer != null) {
            this.invisible = entity.func_98034_c((EntityPlayer)currentPlayer);
        }
        else {
            this.invisible = false;
        }
        this.sneaking = entity.func_70093_af();
        final CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
        ResourceLocation entityIcon = null;
        int playerColor = coreProperties.getColor(coreProperties.colorPlayer);
        ScorePlayerTeam team = null;
        try {
            team = Journeymap.clientWorld().func_96441_U().func_96509_i(entity.func_189512_bd());
        }
        catch (Throwable t3) {}
        if (entity instanceof EntityPlayerSP) {
            final String name = StringUtils.func_76338_a(entity.func_70005_c_());
            this.username = name;
            try {
                if (team != null) {
                    playerColor = team.func_178775_l().func_175746_b();
                }
                else if (currentPlayer.equals((Object)entity)) {
                    playerColor = coreProperties.getColor(coreProperties.colorSelf);
                }
                else {
                    playerColor = coreProperties.getColor(coreProperties.colorPlayer);
                }
            }
            catch (Throwable t4) {}
            entityIcon = DefaultPlayerSkin.func_177335_a();
            try {
                final NetHandlerPlayClient client = Minecraft.func_71410_x().func_147114_u();
                final NetworkPlayerInfo info = client.func_175102_a(entity.func_110124_au());
                if (info != null) {
                    entityIcon = info.func_178837_g();
                }
            }
            catch (Throwable t) {
                Journeymap.getLogger().error("Error looking up player skin: " + LogFormatter.toPartialString(t));
            }
        }
        else {
            this.username = null;
            entityIcon = EntityHelper.getIconTextureLocation(entity);
        }
        if (entityIcon != null) {
            this.entityIconLocation = entityIcon;
            this.iconLocation = entityIcon.toString();
        }
        String owner = null;
        if (entity instanceof EntityTameable) {
            final Entity ownerEntity = (Entity)((EntityTameable)entity).func_70902_q();
            if (ownerEntity != null) {
                owner = ownerEntity.func_70005_c_();
            }
        }
        else if (entity instanceof IEntityOwnable) {
            final Entity ownerEntity = ((IEntityOwnable)entity).func_70902_q();
            if (ownerEntity != null) {
                owner = ownerEntity.func_70005_c_();
            }
        }
        else if (entity instanceof EntityHorse) {
            final UUID ownerUuid = ((EntityHorse)entity).func_184780_dh();
            if (currentPlayer != null && ownerUuid != null) {
                try {
                    final String playerUuid = currentPlayer.func_110124_au().toString();
                    if (playerUuid.equals(ownerUuid)) {
                        owner = currentPlayer.func_70005_c_();
                    }
                }
                catch (Throwable t2) {
                    t2.printStackTrace();
                }
            }
        }
        this.owner = owner;
        String customName = null;
        boolean passive = false;
        if (entity instanceof EntityLiving) {
            final EntityLiving entityLiving = (EntityLiving)entity;
            if (entity.func_145818_k_() && entityLiving.func_174833_aM()) {
                customName = StringUtils.func_76338_a(((EntityLiving)entity).func_95999_t());
            }
            if (!hostile && currentPlayer != null) {
                final EntityLivingBase attackTarget = ((EntityLiving)entity).func_70638_az();
                if (attackTarget != null && attackTarget.func_110124_au().equals(currentPlayer.func_110124_au())) {
                    hostile = true;
                }
            }
            if (EntityHelper.isPassive((EntityLiving)entity)) {
                passive = true;
            }
        }
        if (entity instanceof EntityVillager) {
            final EntityVillager villager = (EntityVillager)entity;
            this.profession = villager.getProfessionForge().getCareer(villager.field_175563_bv).getName();
        }
        else if (entity instanceof INpc) {
            this.npc = true;
            this.profession = null;
            this.passiveAnimal = false;
        }
        else {
            this.profession = null;
            this.passiveAnimal = passive;
        }
        this.customName = customName;
        this.hostile = hostile;
        if (!(entity instanceof EntityLivingBase)) {
            this.color = coreProperties.getColor(coreProperties.colorVehicle);
        }
        else if (entity instanceof EntityPlayerSP) {
            this.color = playerColor;
        }
        else if (team != null) {
            this.color = team.func_178775_l().func_175746_b();
        }
        else if (!Strings.isNullOrEmpty(owner)) {
            this.color = coreProperties.getColor(coreProperties.colorPet);
        }
        else if (this.profession != null || this.npc) {
            this.color = coreProperties.getColor(coreProperties.colorVillager);
        }
        else if (hostile) {
            this.color = coreProperties.getColor(coreProperties.colorHostile);
        }
        else {
            this.color = coreProperties.getColor(coreProperties.colorPassive);
        }
    }
    
    public static class SimpleCacheLoader extends CacheLoader<Entity, EntityDTO>
    {
        public EntityDTO load(final Entity entity) throws Exception {
            return new EntityDTO(entity, null);
        }
    }
}
