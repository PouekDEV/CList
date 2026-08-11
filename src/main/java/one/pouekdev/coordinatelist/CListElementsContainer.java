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

    private void navigateFolder(CListFolder folder, List<CListWaypoint> waypoints, boolean ignoreHidden){
        if(folder.render || !ignoreHidden){
            if(folder.waypoints != null){
                for(int i = 0; i < folder.waypoints.size(); i++){
                    CListWaypoint waypoint = folder.waypoints.get(i);
                    if(waypoint.render || !ignoreHidden){
                        waypoints.addFirst(waypoint);
                    }
                }
            }
            if(!folder.folders.isEmpty()){
                for(CListFolder f : folder.folders){
                    navigateFolder(f, waypoints, ignoreHidden);
                }
            }
        }
    }

    public List<CListWaypoint> getAllWaypoints(boolean ignoreHidden){
        List<CListWaypoint> waypoints = Lists.newArrayList();
        waypoints.addAll(this.waypoints);
        for(CListFolder folder: this.folders){
            navigateFolder(folder, waypoints, ignoreHidden);
        }
        return waypoints;
    }
}
