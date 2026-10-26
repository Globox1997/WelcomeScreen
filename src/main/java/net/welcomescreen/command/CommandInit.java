package net.welcomescreen.command;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.welcomescreen.data.WelcomeScreenManager;
import net.welcomescreen.layout.WelcomeLayout;
import net.welcomescreen.network.WelcomeServerPacket;

public class CommandInit {

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("welcomescreen").requires(source -> source.hasPermissionLevel(WelcomeServerPacket.EDIT_PERMISSION_LEVEL))
                    .then(CommandManager.literal("edit").executes(context -> executeEdit(context.getSource())))
                    .then(CommandManager.literal("show").executes(context -> executeShow(context.getSource(), List.of(context.getSource().getPlayerOrThrow())))
                            .then(CommandManager.argument("targets", EntityArgumentType.players())
                                    .executes(context -> executeShow(context.getSource(), EntityArgumentType.getPlayers(context, "targets")))))
                    .then(CommandManager.literal("reset").executes(context -> executeReset(context.getSource()))));
        });
    }

    private static int executeEdit(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        if (!WelcomeServerPacket.sendEditor(player, WelcomeScreenManager.getActiveLayout().orElseGet(WelcomeLayout::createDefault))) {
            source.sendError(Text.translatable("commands.welcomescreen.edit.missing_client"));
            return 0;
        }
        return 1;
    }

    private static int executeShow(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        Optional<WelcomeLayout> layout = WelcomeScreenManager.getActiveLayout();
        if (layout.isEmpty()) {
            source.sendError(Text.translatable("commands.welcomescreen.show.no_layout"));
            return 0;
        }
        for (ServerPlayerEntity player : players) {
            WelcomeServerPacket.sendWelcomeScreen(player, layout.get());
        }
        source.sendFeedback(() -> Text.translatable("commands.welcomescreen.show.success", players.size()), false);
        return players.size();
    }

    private static int executeReset(ServerCommandSource source) {
        if (!WelcomeScreenManager.hasWorldLayout()) {
            source.sendError(Text.translatable("commands.welcomescreen.reset.nothing"));
            return 0;
        }
        if (!WelcomeScreenManager.resetWorldLayout(source.getServer())) {
            source.sendError(Text.translatable("commands.welcomescreen.reset.failed"));
            return 0;
        }
        source.sendFeedback(() -> Text.translatable("commands.welcomescreen.reset.success"), true);
        return 1;
    }
}
