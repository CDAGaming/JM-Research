package journeymap.client.api.display;

public interface Context
{
    public enum UI implements Context
    {
        Any, 
        Fullscreen, 
        Minimap, 
        Webmap;
    }
    
    public enum MapType implements Context
    {
        Any, 
        Day, 
        Night, 
        Underground, 
        Topo;
    }
}
