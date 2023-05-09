package one.pouekdev.coordinatelist;

import com.google.common.collect.Lists;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;

import java.io.*;
import java.util.List;

public class CListData {
    public static void saveListToFile(ServerWorld world, String fileName, List<String> stringList) {
        File dataDir = world.getServer().getSavePath(WorldSavePath.ROOT).toFile();
        File file = new File(dataDir, fileName);

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
            for (String str : stringList) {
                writer.println(str);
            }
        } catch (IOException e) {
            // empty catch
        }
    }

    public static List<String> loadListFromFile(ServerWorld world, String fileName) {
        File dataDir = world.getServer().getSavePath(WorldSavePath.ROOT).toFile();
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
