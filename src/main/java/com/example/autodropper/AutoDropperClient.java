package com.example.autodropper;

import com.example.autodropper.gui.ConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class AutoDropperClient implements ClientModInitializer {

    public static final String MOD_ID = "autodropper";

    /** Touche qui ouvre le menu de configuration. Par défaut : H (rebindable dans Options > Touches). */
    public static KeyBinding openMenuKey;

    /** Touche qui active/désactive l'auto-drop. Non liée par défaut : à définir dans le menu du mod. */
    public static KeyBinding toggleKey;

    public static Config CONFIG;

    /** Compteur de ticks écoulés depuis le dernier drop (20 ticks = 1 seconde). */
    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        CONFIG = Config.load();

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autodropper.menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "key.categories.autodropper"
        ));

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autodropper.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "key.categories.autodropper"
        ));

        // Si une touche d'activation avait déjà été choisie dans le menu lors d'une session précédente,
        // on la réapplique sur la KeyBinding au démarrage.
        if (CONFIG.activationKeyCode != -1) {
            toggleKey.setBoundKey(InputUtil.Type.KEYSYM.createFromCode(CONFIG.activationKeyCode));
            KeyBinding.updateKeysByCode();
        }

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(MinecraftClient client) {
        if (client.player == null) {
            return;
        }

        // Ouverture du menu de configuration.
        while (openMenuKey.wasPressed()) {
            if (client.currentScreen == null) {
                client.setScreen(new ConfigScreen(client.currentScreen, CONFIG));
            }
        }

        // Bascule activé/désactivé, uniquement si aucun écran n'est ouvert (évite les conflits de saisie).
        while (toggleKey.wasPressed()) {
            if (client.currentScreen == null) {
                CONFIG.enabled = !CONFIG.enabled;
                tickCounter = 0;
                CONFIG.save();

                if (CONFIG.enabled) {
                    client.player.sendMessage(
                            Text.translatable("autodropper.chat.enabled", formatInterval(CONFIG.intervalSeconds)),
                            true
                    );
                } else {
                    client.player.sendMessage(Text.translatable("autodropper.chat.disabled"), true);
                }
            }
        }

        if (!CONFIG.enabled) {
            return;
        }

        // On ne compte pas de ticks pendant que le jeu est en pause ou qu'un écran est ouvert.
        if (client.currentScreen != null || client.isPaused()) {
            return;
        }

        tickCounter++;
        int intervalTicks = Math.max(1, (int) Math.round(CONFIG.intervalSeconds * 20.0));

        if (tickCounter >= intervalTicks) {
            tickCounter = 0;
            dropHeldItem(client);
        }
    }

    private void dropHeldItem(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) {
            return;
        }
        if (client.player.getMainHandStack().isEmpty()) {
            return;
        }
        client.player.dropSelectedItem(CONFIG.dropWholeStack);
    }

    private static String formatInterval(double seconds) {
        if (seconds == Math.rint(seconds)) {
            return String.valueOf((long) seconds);
        }
        return String.valueOf(seconds);
    }
}
