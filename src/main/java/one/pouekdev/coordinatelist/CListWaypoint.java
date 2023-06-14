package one.pouekdev.coordinatelist;

import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;

public class CListWaypoint {
    public String coordinates;
    public String name;
    public String dimension;
    public boolean render;
    CListWaypoint(String coords, String waypoint_name, String waypoint_dimension, boolean is_rendered){
        this.coordinates = coords;
        this.name = waypoint_name;
        this.dimension = waypoint_dimension;
        this.render = is_rendered;
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
    public void setVisibility(boolean value){
        this.render = value;
    }
    public void toggleVisibility(){
        CList.LOGGER.info(String.valueOf(this.render));
        this.render = !this.render;
    }
    public Text getDimension(){
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
}
