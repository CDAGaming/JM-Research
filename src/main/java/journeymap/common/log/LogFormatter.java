package journeymap.common.log;

import java.io.*;

public class LogFormatter
{
    public static final String LINEBREAK;
    private static int OutOfMemoryWarnings;
    private static int LinkageErrorWarnings;
    
    public static String toString(final Throwable thrown) {
        checkErrors(thrown);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(baos);
        thrown.printStackTrace(ps);
        ps.flush();
        return baos.toString();
    }
    
    private static void checkErrors(Throwable thrown) {
        int maxRecursion = 5;
        if (thrown != null && LogFormatter.OutOfMemoryWarnings < 5 && LogFormatter.LinkageErrorWarnings < 5) {
            while (thrown != null && maxRecursion > 0) {
                if (thrown instanceof StackOverflowError) {
                    return;
                }
                if (thrown instanceof OutOfMemoryError) {
                    ++LogFormatter.OutOfMemoryWarnings;
                    thrown.printStackTrace(System.err);
                    break;
                }
                if (thrown instanceof LinkageError) {
                    ++LogFormatter.LinkageErrorWarnings;
                    thrown.printStackTrace(System.err);
                    break;
                }
                if (!(thrown instanceof Exception)) {
                    continue;
                }
                thrown = ((Exception)thrown).getCause();
                --maxRecursion;
            }
        }
    }
    
    public static String toPartialString(final Throwable t) {
        final StringBuilder sb = new StringBuilder(t.toString());
        final StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        for (final StackTraceElement ste : t.getStackTrace()) {
            sb.append("\n\tat " + ste);
            if (ste.getClassName().equals(caller.getClassName()) && ste.getMethodName().equals(caller.getMethodName())) {
                break;
            }
        }
        return sb.toString();
    }
    
    static {
        LINEBREAK = System.getProperty("line.separator");
        LogFormatter.OutOfMemoryWarnings = 0;
        LogFormatter.LinkageErrorWarnings = 0;
    }
}
