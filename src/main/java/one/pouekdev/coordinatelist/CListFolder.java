package one.pouekdev.coordinatelist;

import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class CListFolder extends CListElement{
    public List<CListFolder> folders;
    public List<CListWaypoint> waypoints;
    public boolean extended;

    CListFolder(String name, String dimension, CListElementColor color, boolean isRendered, boolean extended){
        this.name = name;
        this.dimension = dimension;
        this.color = color;
        this.parent = null;
        this.folders = Lists.newArrayList();
        this.render = isRendered;
        this.extended = extended;
        this.waypoints = Lists.newArrayList();
    }

    public void toggleExtended(){
        this.extended = !this.extended;
        CListVariables.savedSinceLastUpdate = false;
    }
}
