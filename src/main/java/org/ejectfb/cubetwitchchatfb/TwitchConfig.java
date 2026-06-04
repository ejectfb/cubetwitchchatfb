package org.ejectfb.cubetwitchchatfb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public record TwitchConfig(boolean enabled, String twitchChannel) {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DEFAULT_URL = "";

    public TwitchConfig {
        twitchChannel = twitchChannel == null ? "" : twitchChannel;
    }

    public static TwitchConfig defaults() {
        return new TwitchConfig(false, DEFAULT_URL);
    }

    public static TwitchConfig load() {
        Path path = path();
        if (!Files.exists(path)) {
            TwitchConfig config = defaults();
            config.save();
            return config;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            TwitchConfig config = GSON.fromJson(reader, TwitchConfig.class);
            return config == null ? defaults() : config;
        } catch (IOException ignored) {
            return defaults();
        }
    }

    public void save() {
        Path path = path();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException ignored) {
        }
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve(TwitchCubeChatClient.MOD_ID + ".json");
    }
}
