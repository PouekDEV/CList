package one.pouekdev.coordinatelist;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

public class CListCommand implements ClientCommandRegistrationCallback {
    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(ClientCommandManager.literal("clist")
                .then(ClientCommandManager.argument("x", IntegerArgumentType.integer(Integer.MIN_VALUE,Integer.MAX_VALUE))
                .then(ClientCommandManager.argument("y", IntegerArgumentType.integer(Integer.MIN_VALUE,Integer.MAX_VALUE))
                .then(ClientCommandManager.argument("z", IntegerArgumentType.integer(Integer.MIN_VALUE,Integer.MAX_VALUE))
                .executes(ctx -> {
                    int x = IntegerArgumentType.getInteger(ctx,"x");
                    int y = IntegerArgumentType.getInteger(ctx,"y");
                    int z = IntegerArgumentType.getInteger(ctx,"z");
                    CListClient.addNewWaypoint("X: "+x+" Y: "+y+" Z: "+z,false);
                    return 0;
        })))));
    };
}