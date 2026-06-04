package org.ejectfb.cubetwitchchatfb;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public final class TwitchCubeChatClient implements ClientModInitializer {
    public static final String MOD_ID = "ejectfbcubetwitchchat";

    private static TwitchConfig config;
    private static TwitchIrcChatClient chatClient;

    @Override
    public void onInitializeClient() {
        config = TwitchConfig.load();
        chatClient = new TwitchIrcChatClient(MinecraftClient.getInstance());
        reloadConnection();
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
