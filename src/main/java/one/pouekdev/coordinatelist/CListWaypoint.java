package one.pouekdev.coordinatelist;

import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;

public class CListWaypoint {
    public String coordinates;
    public String name;
    public String dimension;
    public int bug_fix;
    public boolean render;
    public boolean deathpoint;

    // To be reworked and changed from string to ints
    CListWaypoint(String coords, String waypoint_name, String waypoint_dimension, boolean is_rendered, boolean is_deathpoint){
        this.coordinates = coords;
        this.name = waypoint_name;
        this.dimension = waypoint_dimension;
        this.render = is_rendered;
        this.deathpoint = is_deathpoint;
        this.bug_fix = 0;
    }
    public String getName(){
        return this.name;
    }
    public String getCoordinates(){
        return this.coordinates;
    }
    public String getDimensionValue(){
        return this.dimension;
    }
    public void setName(String value){
        this.name = value;
    }
    public void toggleVisibility(){
        this.bug_fix += 1;
        if(bug_fix == 2){
            this.bug_fix = 0;
            this.render = !this.render;
            CListClient.variables.saved_since_last_update = false;
        }
    }
    public Text getDimensionText(){
        String s = this.dimension;
        s = s.replace("minecraft:","");
        s = s.replace("_"," ");
        s = StringUtils.capitalize(s);
        return Text.literal(s);
    }
    public String getDimensionString(){
        String s = this.dimension;
        s = s.replace("minecraft:","");
        s = s.replace("_"," ");
        s = StringUtils.capitalize(s);
        return s;
    }
    public int getX(){
        String s = this.coordinates;
        s = s.replace("X","");
        s = s.replace("Y","");
        s = s.replace("Z","");
        s = s.replace(" ","");
        String[] segments = s.split(":");
        return Integer.parseInt(segments[1]);
    }
    public int getY(){
        String s = this.coordinates;
        s = s.replace("X","");
        s = s.replace("Y","");
        s = s.replace("Z","");
        s = s.replace(" ","");
        String[] segments = s.split(":");
        return Integer.parseInt(segments[2]);
    }
    public int getZ(){
        String s = this.coordinates;
        s = s.replace("X","");
        s = s.replace("Y","");
        s = s.replace("Z","");
        s = s.replace(" ","");
        String[] segments = s.split(":");
        return Integer.parseInt(segments[3]);
    }
    public void setX(String value){
        try {
            int d = Integer.parseInt(value);
            String s = this.coordinates;
            s = s.replace("X", "");
            s = s.replace("Y", "");
            s = s.replace("Z", "");
            s = s.replace(" ", "");
            String[] segments = s.split(":");
            segments[1] = value;
            this.coordinates = "X: " + segments[1] + " Y: " + segments[2] + " Z: " + segments[3];
        } catch (NumberFormatException ignored) {}
    }
    public void setY(String value){
        try {
            int d = Integer.parseInt(value);
            String s = this.coordinates;
            s = s.replace("X","");
            s = s.replace("Y","");
            s = s.replace("Z","");
            s = s.replace(" ","");
            String[] segments = s.split(":");
            segments[2] = value;
            this.coordinates = "X: " + segments[1] + " Y: " + segments[2] + " Z: " + segments[3];
        } catch (NumberFormatException ignored) {}
    }
    public void setZ(String value){
        try {
            int d = Integer.parseInt(value);
            String s = this.coordinates;
            s = s.replace("X", "");
            s = s.replace("Y", "");
            s = s.replace("Z", "");
            s = s.replace(" ", "");
            String[] segments = s.split(":");
            segments[3] = value;
            this.coordinates = "X: " + segments[1] + " Y: " + segments[2] + " Z: " + segments[3];
        } catch (NumberFormatException ignored) {}
    }
}
