package journeymap.client.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class Policy {
    static Minecraft mc;

    static {
        Policy.mc = FMLClientHandler.instance().getClient();
    }

    final Feature feature;
    final boolean allowInSingleplayer;
    final boolean allowInMultiplayer;

    public Policy(final Feature feature, final boolean allowInSingleplayer, final boolean allowInMultiplayer) {
        this.feature = feature;
        this.allowInSingleplayer = allowInSingleplayer;
        this.allowInMultiplayer = allowInMultiplayer;
    }

    public static Set<Policy> bulkCreate(final boolean allowInSingleplayer, final boolean allowInMultiplayer) {
        return bulkCreate(Feature.all(), allowInSingleplayer, allowInMultiplayer);
    }

    public static Set<Policy> bulkCreate(final EnumSet<Feature> features, final boolean allowInSingleplayer, final boolean allowInMultiplayer) {
        final Set<Policy> policies = new HashSet<>();
        for (final Feature feature : features) {
            policies.add(new Policy(feature, allowInSingleplayer, allowInMultiplayer));
        }
        return policies;
    }

    public boolean isCurrentlyAllowed() {
        if (this.allowInSingleplayer == this.allowInMultiplayer) {
            return this.allowInSingleplayer;
        }
        final IntegratedServer server = Policy.mc.getIntegratedServer();
        final boolean isSinglePlayer = server != null && !server.getPublic();
        return (this.allowInSingleplayer && isSinglePlayer) || (this.allowInMultiplayer && !isSinglePlayer);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Policy policy = (Policy) o;
        return this.allowInMultiplayer == policy.allowInMultiplayer && this.allowInSingleplayer == policy.allowInSingleplayer && this.feature == policy.feature;
    }

    @Override
    public int hashCode() {
        int result = this.feature.hashCode();
        result = 31 * result + (this.allowInSingleplayer ? 1 : 0);
        result = 31 * result + (this.allowInMultiplayer ? 1 : 0);
        return result;
    }
}
