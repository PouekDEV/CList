package one.pouekdev.coordinatelist;

import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;

public class CListWaypoint {
    public static String coordinates;
    public static String name;
    public static String dimension;
    CListWaypoint(String coords, String waypoint_name, String waypoint_dimension){
        coordinates = coords;
        name = waypoint_name;
        dimension = waypoint_dimension;
    }
    public String getName(){
        return name;
    }
    public String getCoordinates(){
        return coordinates;
    }
    public String getDimensionValue(){
        return dimension;
    }
    public void setName(String value){
        name = value;
    }
    public Text getDimension(){
        String s = dimension;
        s = s.replace("minecraft:","");
        s = s.replace("_"," ");
        s = StringUtils.capitalize(s);
        return Text.literal(s);
    }
    public String getDimensionString(){
        String s = dimension;
        s = s.replace("minecraft:","");
        s = s.replace("_"," ");
        s = StringUtils.capitalize(s);
        return s;
    }
    public int getX(){
        String s = coordinates;
        s = s.replace("X","");
        s = s.replace("Y","");
        s = s.replace("Z","");
        s = s.replace(" ","");
        String[] segments = s.split(":");
        return Integer.parseInt(segments[1]);
    }
    public int getY(){
        String s = coordinates;
        s = s.replace("X","");
        s = s.replace("Y","");
        s = s.replace("Z","");
        s = s.replace(" ","");
        String[] segments = s.split(":");
        return Integer.parseInt(segments[2]);
    }
    public int getZ(){
        String s = coordinates;
        s = s.replace("X","");
        s = s.replace("Y","");
        s = s.replace("Z","");
        s = s.replace(" ","");
        String[] segments = s.split(":");
        return Integer.parseInt(segments[3]);
    }
}
