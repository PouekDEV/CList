package one.pouekdev.coordinatelist;

public class CListWaypoint extends CListElement{
    public int x, y, z;
    public boolean deathpoint;
    public boolean locked;

    CListWaypoint(String coords, String waypointName, String waypointDimension, CListElementColor waypointColor, boolean isRendered, boolean isDeathpoint){
        String s = coords;
        s = s.replace("X", "");
        s = s.replace("Y", "");
        s = s.replace("Z", "");
        s = s.replace(" ", "");
        String[] segments = s.split(":");
        this(Integer.parseInt(segments[1]), Integer.parseInt(segments[2]), Integer.parseInt(segments[3]), waypointName, waypointDimension, waypointColor, isRendered, isDeathpoint);
    }

    CListWaypoint(int x, int y, int z, String waypointName, String waypointDimension, CListElementColor waypointColor, boolean isRendered, boolean isDeathpoint){
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = waypointName;
        this.dimension = waypointDimension;
        this.color = waypointColor;
        this.parent = null;
        this.render = isRendered;
        this.deathpoint = isDeathpoint;
        this.locked = false;
    }
}
