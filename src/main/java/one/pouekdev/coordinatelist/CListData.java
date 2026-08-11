package one.pouekdev.coordinatelist;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CListData{
    private static final Path modDir = FabricLoader.getInstance().getConfigDir().resolve("coordinatelist");
    private static final File modDirFile = modDir.toFile();

    public static void saveListToFile(String fileName, CListElementsContainer dataContainer){
        if(!Files.exists(modDir)){
            try{
                Files.createDirectories(modDir);
            }
            catch(IOException ignored){}
        }
        File file = new File(modDirFile, fileName);
        try(PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)))){
            Gson gson = new Gson();
            String json = gson.toJson(dataContainer);
            writer.write(json);
        }
        catch(IOException ignored){}
    }

    public static CListElementsContainer loadListFromFile(String fileName){
        File file = new File(modDirFile, fileName);
        if(!file.exists()){
            return null;
        }
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))){
            String json = reader.readAllAsString();
            Gson gson = new Gson();
            Type listType = new TypeToken<CListElementsContainer>(){}.getType();
            return gson.fromJson(json, listType);
        }
        catch(IOException ignored){}
        return null;
    }

    public static void deleteLegacyFile(String fileName){
        File file = new File(modDirFile, fileName);
        if(file.exists()){
            boolean ignored = file.delete();
        }
    }

    public static List<CListWaypoint> loadListFromFileLegacy(String fileName){
        File file = new File(modDirFile, fileName);
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
                    String color = null, bool = null, deathpoint = null;
                    try{
                        color = segments[3];
                        bool = segments[4];
                        deathpoint = segments[5];
                    }
                    catch(IndexOutOfBoundsException ignored){}
                    CListWaypoint waypoint;
                    if(color == null){
                        waypoint = new CListWaypoint(coords, name, dimension, new CListElementColor(), Boolean.parseBoolean(bool), Boolean.parseBoolean(deathpoint));
                    }
                    else{
                        CListElementColor colorClass = new CListElementColor(0, 0, 0);
                        colorClass.set(color);
                        waypoint = new CListWaypoint(coords, name, dimension, colorClass, Boolean.parseBoolean(bool), Boolean.parseBoolean(deathpoint));
                    }
                    waypointList.add(waypoint);
                }
            }
            return waypointList;
        }
        catch(IOException ignored){}
        return null;
    }
}
