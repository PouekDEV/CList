package one.pouekdev.coordinatelist;

import com.google.common.collect.Lists;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class CListData {
    public static void saveListToFile(String fileName, List<CListWaypoint> waypointList) {
        if (!Files.exists(FabricLoader.getInstance().getConfigDir().resolve("coordinatelist"))) {
            try {
                Files.createDirectories(FabricLoader.getInstance().getConfigDir().resolve("coordinatelist"));
            } catch (IOException ignored) {
            }
        }
        File dataDir = FabricLoader.getInstance().getConfigDir().resolve("coordinatelist").toFile();
        File file = new File(dataDir, fileName);

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)))) {
            for (int i = 0; i < waypointList.size(); i++) {
                writer.println(CListClient.variables.waypoints.get(i).getCoordinates() + "~" + CListClient.variables.waypoints.get(i).getName().replaceAll("~","") + "~" + CListClient.variables.waypoints.get(i).getDimensionValue() + "~" + CListClient.variables.colors.get(i).rgbToHexNoAlpha() + "~" + CListClient.variables.waypoints.get(i).render);
            }
        } catch (IOException ignored) {
        }
    }

    public static List<CListWaypoint> loadListFromFile(String fileName) {
        File dataDir = FabricLoader.getInstance().getConfigDir().resolve("coordinatelist").toFile();
        File file = new File(dataDir, fileName);

        if (!file.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            List<CListWaypoint> waypointList = Lists.newArrayList();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] segments = line.split("~");
                if (segments.length >= 3) {
                    String coords = segments[0];
                    String name = segments[1];
                    String dimension = segments[2];
                    String color = null,bool = null;
                    try{
                        color = segments[3];
                        bool = segments[4];
                    }
                    catch (IndexOutOfBoundsException ignored){}
                    CListWaypoint waypoint;
                    if(bool != null){
                        waypoint = new CListWaypoint(coords, name, dimension, Boolean.parseBoolean(bool));
                    }
                    else{
                        waypoint = new CListWaypoint(coords, name, dimension, true);
                    }
                    if(color == null){
                        CListClient.addRandomWaypointColor();
                    }
                    else{
                        CListWaypointColor color_class = new CListWaypointColor(0,0,0);
                        color_class.hexToRGB(color);
                        CListClient.variables.colors.add(color_class);
                    }
                    waypointList.add(waypoint);
                }
            }

            return waypointList;
        } catch (IOException ignored) {
        }

        return null;
    }

    public static void deleteLegacyFile(String fileName){
        File dataDir = FabricLoader.getInstance().getConfigDir().resolve("coordinatelist").toFile();
        File file = new File(dataDir, fileName);
        if(file.exists()){
            boolean ignored = file.delete();
        }
    }

    public static List<String> loadListFromFileLegacy(String fileName) {
        File dataDir = FabricLoader.getInstance().getConfigDir().resolve("coordinatelist").toFile();
        File file = new File(dataDir, fileName);

        if (!file.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            List<String> stringList = Lists.newArrayList();
            String line;

            while ((line = reader.readLine()) != null) {
                stringList.add(line);
            }

            return stringList;
        } catch (IOException ignored) {
        }

        return null;
    }
}
