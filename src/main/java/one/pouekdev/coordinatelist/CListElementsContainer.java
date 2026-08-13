package one.pouekdev.coordinatelist;

import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class CListElementsContainer{
    public List<CListFolder> folders;
    public List<CListWaypoint> waypoints;

    CListElementsContainer(){
        folders = Lists.newArrayList();
        waypoints = Lists.newArrayList();
    }

    private void getAllWaypoints(CListFolder folder, List<CListWaypoint> waypoints, boolean ignoreHidden){
        if(folder.render || !ignoreHidden){
            if(folder.waypoints != null){
                for(CListWaypoint waypoint : folder.waypoints){
                    if(waypoint.render || !ignoreHidden){
                        waypoints.addFirst(waypoint);
                    }
                }
            }
            if(!folder.folders.isEmpty()){
                for(CListFolder f : folder.folders){
                    getAllWaypoints(f, waypoints, ignoreHidden);
                }
            }
        }
    }

    public List<CListWaypoint> getAllWaypoints(boolean ignoreHidden){
        List<CListWaypoint> waypoints = Lists.newArrayList();
        waypoints.addAll(this.waypoints);
        for(CListFolder folder: this.folders){
            getAllWaypoints(folder, waypoints, ignoreHidden);
        }
        return waypoints;
    }

    private void assignParents(CListFolder parent, CListFolder folder, int depth){
        if(folder.parent == null && depth > 0){
            folder.parent = parent;
        }
        for(CListWaypoint waypoint : folder.waypoints){
            if(waypoint.parent == null){
                waypoint.parent = folder;
            }
        }
        for(CListFolder f : folder.folders){
            assignParents(folder, f, depth + 1);
        }
    }

    public void assignParents(){
        for(CListFolder folder: this.folders){
            assignParents(null, folder, 0);
        }
    }
}
