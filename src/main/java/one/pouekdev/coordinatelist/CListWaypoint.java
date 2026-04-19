package one.pouekdev.coordinatelist;

import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;

public class CListWaypoint{
    public int x, y, z;
    public String name;
    public String dimension;
    private int bugFix;
    public boolean render;
    public boolean deathpoint;
    public String folderId;
    public String globalFolderId;

    CListWaypoint(String coords, String waypointName, String waypointDimension, boolean isRendered, boolean isDeathpoint){
        String s = coords;
        s = s.replace("X", "");
        s = s.replace("Y", "");
        s = s.replace("Z", "");
        s = s.replace(" ", "");
        String[] segments = s.split(":");
        this.x = Integer.parseInt(segments[1]);
        this.y = Integer.parseInt(segments[2]);
        this.z = Integer.parseInt(segments[3]);
        this.name = waypointName;
        this.dimension = waypointDimension;
        this.render = isRendered;
        this.deathpoint = isDeathpoint;
        this.folderId = null;
        this.globalFolderId = null;
        this.bugFix = 0;
    }

    CListWaypoint(int x, int y, int z, String waypointName, String waypointDimension, boolean isRendered, boolean isDeathpoint){
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = waypointName;
        this.dimension = waypointDimension;
        this.render = isRendered;
        this.deathpoint = isDeathpoint;
        this.folderId = null;
        this.globalFolderId = null;
        this.bugFix = 0;
    }

    public String getFolderId(String category){
        return category == null ? globalFolderId : folderId;
    }

    public void setFolderId(String category, String id){
        if(category == null) globalFolderId = id;
        else folderId = id;
    }

    public String getCoordinates(){
        return "X: " + x + " Y: " + y + " Z: " + z;
    }

    public void toggleVisibility(){
        this.bugFix += 1;
        if(bugFix == 2){
            this.bugFix = 0;
            this.render = !this.render;
            CListClient.variables.savedSinceLastUpdate = false;
        }
    }

    public Component getDimensionText(){
        return Component.literal(this.getDimensionString());
    }

    public String getDimensionString(){
        String s = this.dimension;
        s = s.replace("minecraft:", "");
        s = s.replace("_", " ");
        s = s.replace(":", " ");
        s = StringUtils.capitalize(s);
        return s;
    }
}
