package journeymap.client.api;

import journeymap.client.api.event.ClientEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface IClientPlugin {
    void initialize(final IClientAPI p0);

    String getModId();

    void onEvent(final ClientEvent p0);
}
