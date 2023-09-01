package net.welcomescreen.mixin;

import com.mojang.authlib.GameProfile;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.UserCache;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.welcomescreen.WelcomeScreenMain;
import net.welcomescreen.data.WelcomeScreenData;
import net.welcomescreen.network.WelcomeServerPacket;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Shadow
    @Mutable
    @Final
    private MinecraftServer server;

    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onPlayerConnectMixin(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info, GameProfile gameProfile, UserCache userCache, String string, NbtCompound nbtCompound,
            RegistryKey<World> registryKey, ServerWorld serverWorld, ServerWorld serverWorld2, String string2, WorldProperties worldProperties, ServerPlayNetworkHandler serverPlayNetworkHandler) {
        if ((nbtCompound == null || WelcomeScreenMain.CONFIG.alwaysShowWelcomeScreen) && !WelcomeScreenData.TITLE_LIST.isEmpty()) {
            WelcomeServerPacket.writeS2CWelcomeScreenPacket(serverPlayNetworkHandler);

            if (!WelcomeScreenData.COMMAND_LIST.isEmpty()) {
                if (nbtCompound == null) {
                    for (int i = 0; i < WelcomeScreenData.COMMAND_LIST.size(); i++) {
                        runCommand(server, player, WelcomeScreenData.COMMAND_LIST.get(i));
                    }
                }
            }
        }
    }

    private static void runCommand(MinecraftServer server, ServerPlayerEntity player, String command) {
        try {
            server.getCommandManager().executeWithPrefix(new ServerCommandSource(CommandOutput.DUMMY, player.getPos(), player.getRotationClient(), player.getServerWorld(), 2,
                    player.getName().getString(), player.getName(), server, player), command);
        } catch (Throwable throwable) {
        }
    }

}
