package net.welcomescreen.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class WelcomeScreenCommands {

    private static final Logger LOGGER = LogManager.getLogger("WelcomeScreen");

    public static void runCommands(MinecraftServer server, ServerPlayerEntity player) {
        for (String command : WelcomeScreenData.COMMAND_LIST) {
            try {
                server.getCommandManager().executeWithPrefix(new ServerCommandSource(createErrorOutput(command), player.getPos(), player.getRotationClient(), player.getServerWorld(), 2,
                        player.getName().getString(), player.getName(), server, player), command);
            } catch (Exception e) {
                LOGGER.error("Error occurred while running welcome screen command \"{}\". {}", command, e.toString());
            }
        }
    }

    private static CommandOutput createErrorOutput(String command) {
        return new CommandOutput() {

            @Override
            public void sendMessage(Text message) {
                LOGGER.warn("Welcome screen command \"{}\" failed: {}", command, message.getString());
            }

            @Override
            public boolean shouldReceiveFeedback() {
                return false;
            }

            @Override
            public boolean shouldTrackOutput() {
                return true;
            }

            @Override
            public boolean shouldBroadcastConsoleToOps() {
                return false;
            }
        };
    }

}
