package one.pouekdev.coordinatelist;

import eu.midnightdust.lib.config.MidnightConfig;

public class CListConfig extends MidnightConfig {
    @Entry(min=5,max=200) public static int multiplier = 10;
    @Entry(min=0) public static int renderDistance = 0;
    @Entry public static boolean waypointsToggled = true;
    @Entry public static boolean canPlaceDeathpoints = true;
    @Entry public static boolean waypointTextBackground = true;
    @Entry public static boolean squareWaypoints = false;
}
