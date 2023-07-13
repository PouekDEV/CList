package one.pouekdev.coordinatelist;

import eu.midnightdust.lib.config.MidnightConfig;

public class CListConfig extends MidnightConfig {
    @Entry(min=1,max=200) public static int multiplier = 10;
    @Entry public static boolean can_place_deathpoints = true;
}
