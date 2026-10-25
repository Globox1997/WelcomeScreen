package net.welcomescreen.mixin;

import com.mojang.authlib.GameProfile;

import java.util.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.UserCache;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.welcomescreen.WelcomeScreenMain;
import net.welcomescreen.data.WelcomeScreenCommands;
import net.welcomescreen.data.WelcomeScreenData;
import net.welcomescreen.network.WelcomeServerPacket;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Shadow
    @Mutable
    @Final
    private MinecraftServer server;

    @Unique
    private boolean runWelcomeCommands;

    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendCommandTree(Lnet/minecraft/server/network/ServerPlayerEntity;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onPlayerConnectMixin(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData connectedClientData, CallbackInfo info, GameProfile gameProfile, UserCache userCache,
            String string, Optional<NbtCompound> optional, RegistryKey<World> registryKey, ServerWorld serverWorld, ServerWorld serverWorld2, String string2, WorldProperties worldProperties,
            ServerPlayNetworkHandler serverPlayNetworkHandler) {
        this.runWelcomeCommands = optional.isEmpty() && !WelcomeScreenData.TITLE_LIST.isEmpty() && !WelcomeScreenData.COMMAND_LIST.isEmpty();
        if ((optional.isEmpty() || WelcomeScreenMain.CONFIG.alwaysShowWelcomeScreen) && !WelcomeScreenData.TITLE_LIST.isEmpty()) {
            WelcomeServerPacket.writeS2CWelcomeScreenPacket(player);
        }
    }

    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void onPlayerConnectTailMixin(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData connectedClientData, CallbackInfo info) {
        if (this.runWelcomeCommands) {
            this.runWelcomeCommands = false;
            WelcomeScreenCommands.runCommands(server, player);
        }
    }

}
