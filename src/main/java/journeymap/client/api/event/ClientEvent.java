package journeymap.client.api.event;

public class ClientEvent
{
    public final Type type;
    public final int dimension;
    public final long timestamp;
    private boolean cancelled;
    
    public ClientEvent(final Type type, final int dimension) {
        this.type = type;
        this.dimension = dimension;
        this.timestamp = System.currentTimeMillis();
    }
    
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    public void cancel() {
        if (this.type.cancellable) {
            this.cancelled = true;
        }
    }
    
    public enum Type
    {
        DISPLAY_UPDATE(false), 
        DEATH_WAYPOINT(true), 
        MAPPING_STARTED(false), 
        MAPPING_STOPPED(false);
        
        public final boolean cancellable;
        
        private Type(final boolean cancellable) {
            this.cancellable = cancellable;
        }
    }
}
