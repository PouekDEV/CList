package one.pouekdev.coordinatelist;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import org.jspecify.annotations.NonNull;

public class CListCommand implements ClientCommandRegistrationCallback{
    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, @NonNull CommandBuildContext buildContext){
        dispatcher.register(ClientCommands.literal("clist")
                .then(ClientCommands.argument("x", IntegerArgumentType.integer(Integer.MIN_VALUE, Integer.MAX_VALUE))
                        .then(ClientCommands.argument("y", IntegerArgumentType.integer(Integer.MIN_VALUE, Integer.MAX_VALUE))
                                .then(ClientCommands.argument("z", IntegerArgumentType.integer(Integer.MIN_VALUE, Integer.MAX_VALUE))
                                        .executes(ctx -> {
                                            int x = IntegerArgumentType.getInteger(ctx, "x");
                                            int y = IntegerArgumentType.getInteger(ctx, "y");
                                            int z = IntegerArgumentType.getInteger(ctx, "z");
                                            CListClient.addNewWaypoint(x, y, z, false, true);
                                            return 0;
                                        })
                                )
                        )
                )
        );
    }
}
