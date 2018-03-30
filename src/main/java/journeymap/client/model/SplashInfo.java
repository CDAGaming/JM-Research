package journeymap.client.model;

import java.util.*;
import journeymap.client.ui.component.*;
import journeymap.client.ui.dialog.*;
import journeymap.client.properties.*;
import journeymap.client.ui.*;
import journeymap.common.properties.*;
import journeymap.common.*;
import journeymap.common.log.*;
import java.lang.reflect.*;

public class SplashInfo
{
    public ArrayList<Line> lines;
    
    public SplashInfo() {
        this.lines = new ArrayList<Line>();
    }
    
    public static class Line
    {
        public String label;
        public String action;
        
        public Line() {
        }
        
        public Line(final String label, final String action) {
            this.label = label;
            this.action = action;
        }
        
        public boolean hasAction() {
            return this.action != null && this.action.trim().length() > 0;
        }
        
        public void invokeAction(final JmUI returnUi) {
            if (!this.hasAction()) {
                return;
            }
            try {
                final String[] parts = this.action.split("#");
                final String className = parts[0];
                String action = null;
                if (parts.length > 1) {
                    action = parts[1];
                }
                final Class<? extends JmUI> uiClass = (Class<? extends JmUI>)Class.forName("journeymap.client.ui." + className);
                if (uiClass.equals(OptionsManager.class) && action != null) {
                    final Category category = ClientCategory.valueOf(action);
                    UIManager.INSTANCE.openOptionsManager(returnUi, category);
                    return;
                }
                if (action != null) {
                    final String arg = (parts.length == 3) ? parts[2] : null;
                    try {
                        Object instance = null;
                        if (JmUI.class.isAssignableFrom(uiClass)) {
                            instance = UIManager.INSTANCE.open((Class<Object>)uiClass, returnUi);
                        }
                        else {
                            instance = uiClass.newInstance();
                        }
                        if (arg == null) {
                            final Method actionMethod = uiClass.getMethod(action, (Class<?>[])new Class[0]);
                            actionMethod.invoke(instance, new Object[0]);
                        }
                        else {
                            final Method actionMethod = uiClass.getMethod(action, String.class);
                            actionMethod.invoke(instance, arg);
                        }
                        return;
                    }
                    catch (Exception e) {
                        Journeymap.getLogger().warn("Couldn't perform action " + action + " on " + uiClass + ": " + e.getMessage());
                    }
                }
                if (JmUI.class.isAssignableFrom(uiClass)) {
                    UIManager.INSTANCE.open(uiClass, returnUi);
                }
            }
            catch (Throwable t) {
                Journeymap.getLogger().error("Couldn't invoke action: " + this.action + ": " + LogFormatter.toString(t));
            }
        }
    }
}
