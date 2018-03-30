package journeymap.common.migrate;

import java.util.concurrent.*;
import journeymap.common.version.*;

public interface MigrationTask extends Callable<Boolean>
{
    boolean isActive(final Version p0);
}
