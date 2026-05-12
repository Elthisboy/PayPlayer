package com.elthisboy.paypleyer.pvpmoney;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class PvpMoneyCommand {

    private PvpMoneyCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("pvpmoney")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("reload")
                                .executes(context -> {
                                    PvpMoneyConfig.reload();
                                    context.getSource().sendFeedback(
                                            () -> Text.translatable("command.pvpmoney.reload"), true);
                                    return 1;
                                })
                        )
        );
    }
}
