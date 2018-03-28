package journeymap.common.migrate;

import journeymap.common.version.Version;

import java.util.concurrent.Callable;

public interface MigrationTask extends Callable<Boolean> {
    boolean isActive(final Version p0);
}
