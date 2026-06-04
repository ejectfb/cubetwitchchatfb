package org.ejectfb.cubetwitchchatfb;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.ChatOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class TwitchCubeChatClient implements ClientModInitializer {
    public static final String MOD_ID = "ejectfbcubetwitchchat";

    private static TwitchConfig config;
    private static TwitchIrcChatClient chatClient;

    @Override
    public void onInitializeClient() {
        config = TwitchConfig.load();
        chatClient = new TwitchIrcChatClient(MinecraftClient.getInstance());
        registerSettingsButton();
        reloadConnection();
    }

    private static void registerSettingsButton() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof ChatOptionsScreen)) {
                return;
            }

            Screens.getButtons(screen).add(ButtonWidget.builder(Text.literal("Twitch Chat"), button -> client.setScreen(new TwitchConfigScreen(screen)))
                    .dimensions(scaledWidth / 2 - 100, scaledHeight - 56, 200, 20)
                    .build());
        });
    }

    public static TwitchConfig getConfig() {
        return config;
    }

    public static void saveConfig(TwitchConfig newConfig) {
        config = newConfig;
        config.save();
        reloadConnection();
    }

    public static void reloadConnection() {
        if (chatClient == null || config == null) {
            return;
        }

        if (config.enabled() && !config.twitchChannel().isBlank()) {
            chatClient.connect(config.twitchChannel());
        } else {
            chatClient.disconnect();
        }
    }
}
