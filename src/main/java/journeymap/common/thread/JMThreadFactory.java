package journeymap.common.thread;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class JMThreadFactory implements ThreadFactory
{
    static final AtomicInteger threadNumber;
    static final String namePrefix = "JM-";
    final ThreadGroup group;
    final String name;
    
    public JMThreadFactory(final String name) {
        this.name = "JM-" + name;
        final SecurityManager securitymanager = System.getSecurityManager();
        this.group = ((securitymanager == null) ? Thread.currentThread().getThreadGroup() : securitymanager.getThreadGroup());
    }
    
    @Override
    public Thread newThread(final Runnable runnable) {
        final String fullName = this.name + "-" + JMThreadFactory.threadNumber.getAndIncrement();
        final Thread thread = new Thread(this.group, runnable, fullName);
        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }
        if (thread.getPriority() != 5) {
            thread.setPriority(5);
        }
        return thread;
    }
    
    static {
        threadNumber = new AtomicInteger(1);
    }
}
