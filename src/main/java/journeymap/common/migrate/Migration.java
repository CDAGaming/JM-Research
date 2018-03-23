package journeymap.common.migrate;

import journeymap.common.*;
import com.google.common.reflect.*;
import journeymap.common.log.*;
import java.util.*;

public class Migration
{
    private final String targetPackage;
    
    public Migration(final String targetPackage) {
        this.targetPackage = targetPackage;
    }
    
    public boolean performTasks() {
        boolean success = true;
        final List<MigrationTask> tasks = new ArrayList<MigrationTask>();
        try {
            final Set<ClassPath.ClassInfo> classInfoSet = (Set<ClassPath.ClassInfo>)ClassPath.from(Journeymap.class.getClassLoader()).getTopLevelClassesRecursive(this.targetPackage);
            for (final ClassPath.ClassInfo classInfo : classInfoSet) {
                final Class<?> clazz = (Class<?>)classInfo.load();
                if (MigrationTask.class.isAssignableFrom(clazz)) {
                    try {
                        final MigrationTask task = (MigrationTask)clazz.newInstance();
                        if (!task.isActive(Journeymap.JM_VERSION)) {
                            continue;
                        }
                        tasks.add(task);
                    }
                    catch (Throwable t) {
                        Journeymap.getLogger().error("Couldn't instantiate MigrationTask " + clazz, (Object)LogFormatter.toPartialString(t));
                        success = false;
                    }
                }
            }
        }
        catch (Throwable t2) {
            Journeymap.getLogger().error("Couldn't find MigrationTasks: " + t2, (Object)LogFormatter.toPartialString(t2));
            success = false;
        }
        for (final MigrationTask task2 : tasks) {
            try {
                if (task2.call()) {
                    continue;
                }
                success = false;
            }
            catch (Throwable t3) {
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
