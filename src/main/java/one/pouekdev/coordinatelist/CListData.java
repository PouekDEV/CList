package one.pouekdev.coordinatelist;

import com.google.common.collect.Lists;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

public class CListData {
    public static void saveListToFile(String fileName, List<String> stringList) {
        if (!Files.exists(FabricLoader.getInstance().getConfigDir().resolve("coordinatelist"))) {
            try {
                Files.createDirectories(FabricLoader.getInstance().getConfigDir().resolve("coordinatelist"));
            } catch (IOException ignored) {}
        }
        File dataDir = FabricLoader.getInstance().getConfigDir().resolve("coordinatelist").toFile();
        File file = new File(dataDir, fileName);

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
            for (String str : stringList) {
                writer.println(str);
            }
        } catch (IOException ignored) {}
    }

    public static List<String> loadListFromFile(String fileName) {
        File dataDir = FabricLoader.getInstance().getConfigDir().resolve("coordinatelist").toFile();
        File file = new File(dataDir, fileName);

        if (!file.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<String> stringList = Lists.newArrayList();
            String line;

            while ((line = reader.readLine()) != null) {
                stringList.add(line);
            }

            return stringList;
        } catch (IOException ignored) {}

        return null;
    }
}
