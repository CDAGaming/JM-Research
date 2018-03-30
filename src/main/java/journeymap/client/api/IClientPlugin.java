package journeymap.client.api;

import journeymap.client.api.event.ClientEvent;
import journeymap.common.api.IJmPlugin;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface IClientPlugin extends IJmPlugin<IClientAPI> {
    void initialize(final IClientAPI p0);

    String getModId();

    void onEvent(final ClientEvent p0);
}
