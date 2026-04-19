package one.pouekdev.coordinatelist;

import com.google.common.collect.Lists;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class CListData{
    public static void saveListToFile(String fileName, List<CListWaypoint> waypointList){
        if(!Files.exists(FabricLoader.getInstance().getConfigDir().resolve("coordinatelist"))){
            try{
                Files.createDirectories(FabricLoader.getInstance().getConfigDir().resolve("coordinatelist"));
            }
            catch(IOException ignored){}
        }
        File dataDir = FabricLoader.getInstance().getConfigDir().resolve("coordinatelist").toFile();
        File file = new File(dataDir, fileName);
        try(PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)))){
            for(int i = 0; i < waypointList.size(); i++){
                String folderId = CListClient.variables.waypoints.get(i).folderId;
                if(folderId == null) folderId = "";
                String globalFolderId = CListClient.variables.waypoints.get(i).globalFolderId;
                if(globalFolderId == null) globalFolderId = "";
                writer.println(CListClient.variables.waypoints.get(i).getCoordinates() + "~" + CListClient.variables.waypoints.get(i).name.replaceAll("~", "") + "~" + CListClient.variables.waypoints.get(i).dimension + "~" + CListClient.variables.colors.get(i).getHexNoAlpha() + "~" + CListClient.variables.waypoints.get(i).render + "~" + CListClient.variables.waypoints.get(i).deathpoint + "~" + folderId + "~" + globalFolderId);
            }
        }
        catch(IOException ignored){}
    }

    public static List<CListWaypoint> loadListFromFile(String fileName){
        File dataDir = FabricLoader.getInstance().getConfigDir().resolve("coordinatelist").toFile();
        File file = new File(dataDir, fileName);
        if(!file.exists()){
            return null;
        }
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))){
            List<CListWaypoint> waypointList = Lists.newArrayList();
            String line;
            while((line = reader.readLine()) != null){
                String[] segments = line.split("~");
                if(segments.length >= 3){
                    String coords = segments[0];
                    String name = segments[1];
                    String dimension = segments[2];
                    String color = null, bool = null, deathpoint = null, folderId = null, globalFolderId = null;
                    try{
                        color = segments[3];
                        bool = segments[4];
                        deathpoint = segments[5];
                        folderId = segments[6];
                        globalFolderId = segments[7];
                    }
                    catch(IndexOutOfBoundsException ignored){}
                    CListWaypoint waypoint = new CListWaypoint(coords, name, dimension, Boolean.parseBoolean(bool), Boolean.parseBoolean(deathpoint));
                    if(folderId != null && !folderId.isEmpty()){
                        waypoint.folderId = folderId;
                    }
                    if(globalFolderId != null && !globalFolderId.isEmpty()){
                        waypoint.globalFolderId = globalFolderId;
                    }
                    if(color == null){
                        CListClient.addRandomWaypointColor();
                    }
                    else{
                        CListWaypointColor color_class = new CListWaypointColor(0, 0, 0);
                        color_class.set(color);
                        CListClient.variables.colors.add(color_class);
                    }
                    waypointList.add(waypoint);
                }
            }
            return waypointList;
        }
        catch(IOException ignored){}
        return null;
    }

    public static void deleteLegacyFile(String fileName){
        File dataDir = FabricLoader.getInstance().getConfigDir().resolve("coordinatelist").toFile();
        File file = new File(dataDir, fileName);
        if(file.exists()){
            boolean ignored = file.delete();
        }
    }

    public static void saveFoldersToFile(String fileName, List<CListFolder> folders){
        if(!Files.exists(FabricLoader.getInstance().getConfigDir().resolve("coordinatelist"))){
            try{
                Files.createDirectories(FabricLoader.getInstance().getConfigDir().resolve("coordinatelist"));
            }
            catch(IOException ignored){}
        }
        File dataDir = FabricLoader.getInstance().getConfigDir().resolve("coordinatelist").toFile();
        File file = new File(dataDir, fileName);
        try(PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)))){
            for(CListFolder folder : folders){
                String parentId = folder.parentId == null ? "" : folder.parentId;
                String dim = folder.dimension == null ? "" : folder.dimension;
                writer.println(folder.id + "~" + folder.name.replaceAll("~", "") + "~" + folder.colorHex + "~" + folder.expanded + "~" + folder.visible + "~" + parentId + "~" + dim);
            }
        }
        catch(IOException ignored){}
    }

    public static List<CListFolder> loadFoldersFromFile(String fileName){
        File dataDir = FabricLoader.getInstance().getConfigDir().resolve("coordinatelist").toFile();
        File file = new File(dataDir, fileName);
        if(!file.exists()){
            return null;
        }
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))){
            List<CListFolder> folderList = com.google.common.collect.Lists.newArrayList();
            String line;
            while((line = reader.readLine()) != null){
                String[] segments = line.split("~");
                if(segments.length >= 5){
                    String id = segments[0];
                    String name = segments[1];
                    String colorHex = segments[2];
                    boolean expanded = Boolean.parseBoolean(segments[3]);
                    boolean visible = Boolean.parseBoolean(segments[4]);
                    String parentId = null;
                    String dimension = null;
                    try{
                        parentId = segments[5];
                        if(parentId.isEmpty()) parentId = null;
                        dimension = segments[6];
                        if(dimension.isEmpty()) dimension = null;
                    }
                    catch(IndexOutOfBoundsException ignored){}
                    folderList.add(new CListFolder(id, name, colorHex, expanded, visible, parentId, dimension));
                }
            }
            return folderList;
        }
        catch(IOException ignored){}
        return null;
    }

    public static List<String> loadListFromFileLegacy(String fileName){
        File dataDir = FabricLoader.getInstance().getConfigDir().resolve("coordinatelist").toFile();
        File file = new File(dataDir, fileName);
        if(!file.exists()){
            return null;
        }
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))){
            List<String> stringList = Lists.newArrayList();
            String line;
            while((line = reader.readLine()) != null){
                stringList.add(line);
            }
            return stringList;
        }
        catch(IOException ignored){}
        return null;
    }
}
