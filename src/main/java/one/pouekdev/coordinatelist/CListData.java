package one.pouekdev.coordinatelist;

import com.google.common.collect.Lists;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.util.List;

public class CListData {
    public static void saveListToFile(String fileName, List<String> stringList) {
        File dataDir = FabricLoader.getInstance().getConfigDir().resolve("coordinatelist").toFile();
        File file = new File(dataDir, fileName);

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
            for (String str : stringList) {
                writer.println(str);
            }
        } catch (IOException e) {
            // empty catch
        }
    }

    public static List<String> loadListFromFile(String fileName) {
        File dataDir = FabricLoader.getInstance().getConfigDir().resolve("CList").toFile();
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
        } catch (IOException e) {
            // empty catch
        }

        return null;
    }
}
