package one.pouekdev.coordinatelist;

import eu.midnightdust.lib.config.EntryInfo;
import eu.midnightdust.lib.config.MidnightConfig;
import eu.midnightdust.lib.config.MidnightConfigListWidget;
import eu.midnightdust.lib.config.MidnightConfigScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class CListConfig extends MidnightConfig {
    public enum DefaultDimensionSort implements StringRepresentable{
        CURRENT(0, "dimensions.current"),
        ALL(1, "dimensions.all");

        private final String translationKey;

        DefaultDimensionSort(int id, String translationKey) {
            this.translationKey = translationKey;
        }

        @Override
        public @NonNull String getSerializedName() {
            return this.translationKey;
        }
    }

    @Entry(min=5,max=200) public static int multiplier = 10;
    @Entry(min=0) public static int renderDistance = 0;
    @Entry public static boolean waypointsToggled = true;
    @Entry public static boolean canPlaceDeathpoints = true;
    @Entry public static boolean waypointTextBackground = true;
    @Entry public static boolean squareWaypoints = false;
    @Entry public static boolean detectCoordsInChat = true;
    @Entry(min=0) public static int deathpointLimit = 10;
    @Entry public static boolean deleteDeathpointsWhenReached = true;
    @Entry public static DefaultDimensionSort defaultDimensionSort = DefaultDimensionSort.CURRENT;
    @Entry public static boolean displayTreeVisualization = true;

    @Override
    public void onTabInit(String tabName, MidnightConfigListWidget list, MidnightConfigScreen screen){
        Button button = Button.builder(Component.translatable("coordinatelist.midnightconfig.migrateSavefile.button"), _ -> CListVariables.minecraftClient.setScreen(new CListDataMigrationScreen())).bounds(screen.width - 185, 0, 150, 20).build();
        if(CListVariables.loadedLastWorld){
            button.active = false;
            button.setTooltip(Tooltip.create(Component.translatable("coordinatelist.midnightconfig.migrateSavefile.button.tooltip")));
        }
        list.addButton(List.of(button), Component.translatable("coordinatelist.midnightconfig.migrateSavefile"), new EntryInfo(null, screen.modid));
    }
}
