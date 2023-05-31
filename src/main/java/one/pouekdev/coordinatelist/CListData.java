package one.pouekdev.coordinatelist;

import com.google.common.collect.Lists;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
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
            for (CListWaypoint cListWaypoint : waypointList) {
                writer.println(cListWaypoint.getCoordinates() + ":" + cListWaypoint.getName() + ":" + cListWaypoint.getDimensionValue());
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
            List<CListWaypoint> waypointList = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] segments = line.split(":");
                if (segments.length == 3) {
                    String coords = segments[0];
                    String name = segments[1];
                    String dimension = segments[2];
                    CListWaypoint waypoint = new CListWaypoint(coords, name, dimension);
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
