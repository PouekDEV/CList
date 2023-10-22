package one.pouekdev.coordinatelist;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CList implements ModInitializer {
	public static final String MOD_ID = "coordinatelist";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public void onInitialize() {
		MidnightConfig.init(MOD_ID, CListConfig.class);
		ClientCommandRegistrationCallback.EVENT.register(new CListCommand());
	}
}
