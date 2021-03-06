package journeymap.common.migrate;

import com.google.common.reflect.ClassPath;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Migration {
    private final String targetPackage;

    public Migration(final String targetPackage) {
        this.targetPackage = targetPackage;
    }

    public boolean performTasks() {
        boolean success = true;
        final List<MigrationTask> tasks = new ArrayList<>();
        try {
            final Set<ClassPath.ClassInfo> classInfoSet = ClassPath.from(Journeymap.class.getClassLoader()).getTopLevelClassesRecursive(this.targetPackage);
            for (final ClassPath.ClassInfo classInfo : classInfoSet) {
                final Class<?> clazz = classInfo.load();
                if (MigrationTask.class.isAssignableFrom(clazz)) {
                    try {
                        final MigrationTask task = (MigrationTask) clazz.newInstance();
                        if (!task.isActive(Journeymap.JM_VERSION)) {
                            continue;
                        }
                        tasks.add(task);
                    } catch (Throwable t) {
                        Journeymap.getLogger().error("Couldn't instantiate MigrationTask " + clazz, LogFormatter.toPartialString(t));
                        success = false;
                    }
                }
            }
        } catch (Throwable t2) {
            Journeymap.getLogger().error("Couldn't find MigrationTasks: " + t2, LogFormatter.toPartialString(t2));
            success = false;
        }
        for (final MigrationTask task2 : tasks) {
            try {
                if (task2.call()) {
                    continue;
                }
                success = false;
            } catch (Throwable t3) {
                Journeymap.getLogger().fatal(LogFormatter.toString(t3));
                success = false;
            }
        }
        if (!success) {
            Journeymap.getLogger().fatal("Some or all of JourneyMap migration failed! You may experience significant errors.");
        }
        return success;
    }
}
