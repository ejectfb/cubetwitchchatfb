package org.ejectfb.cubetwitchchatfb;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TwitchIrcChatClient {
    private static final Logger LOGGER = LoggerFactory.getLogger("TwitchCubeChat");
    private static final Pattern CHANNEL_FROM_URL = Pattern.compile("(?:https?://)?(?:www\\.)?twitch\\.tv/([^/?#]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern IRC_MESSAGE = Pattern.compile("^(?:@([^ ]+) )?:(\\S+)![^ ]+ PRIVMSG #[^ ]+ :(.+)$");
    private static final Pattern TAG_SEPARATOR = Pattern.compile(";");

    private final MinecraftClient minecraft;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private WebSocket webSocket;
    private String connectedChannel = "";
    private StringBuilder pendingMessage = new StringBuilder();

    public TwitchIrcChatClient(MinecraftClient minecraft) {
        this.minecraft = minecraft;
    }

    public synchronized void connect(String channelOrUrl) {
        String channel = extractChannel(channelOrUrl);
        if (channel.isBlank()) {
            disconnect();
            return;
        }

        if (webSocket != null && connectedChannel.equals(channel)) {
            return;
        }

        disconnect();
        connectedChannel = channel;
        LOGGER.info("Connecting to Twitch IRC channel #{}", channel);

        httpClient.newWebSocketBuilder()
                .buildAsync(URI.create("wss://irc-ws.chat.twitch.tv:443"), new Listener(channel))
                .thenAccept(socket -> webSocket = socket)
                .exceptionally(error -> {
                    LOGGER.warn("Failed to connect to Twitch IRC", error);
                    return null;
                });
    }

    public synchronized void disconnect() {
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "disabled");
            webSocket = null;
        }
        connectedChannel = "";
        pendingMessage = new StringBuilder();
    }

    private static String extractChannel(String value) {
        String trimmed = value.trim();
        Matcher matcher = CHANNEL_FROM_URL.matcher(trimmed);
        if (matcher.find()) {
            trimmed = matcher.group(1);
        }

        return trimmed.replace("#", "")
                .replaceAll("[^A-Za-z0-9_]", "")
                .toLowerCase(Locale.ROOT);
    }

    private void handleRawMessage(String raw) {
        String[] lines = raw.split("\\r?\\n");
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }

            if (line.startsWith("PING")) {
                WebSocket socket = webSocket;
                if (socket != null) {
                    socket.sendText(line.replaceFirst("PING", "PONG"), true);
                }
                continue;
            }

            Matcher matcher = IRC_MESSAGE.matcher(line);
            if (!matcher.matches()) {
                continue;
            }

            String user = displayNameFromTags(matcher.group(1), matcher.group(2));
            String message = unescapeIrc(matcher.group(3));
            postToMinecraft(user, message);
        }
    }

    private static String displayNameFromTags(String tags, String fallback) {
        if (tags != null) {
            for (String tag : TAG_SEPARATOR.split(tags)) {
                if (tag.startsWith("display-name=")) {
                    String displayName = tag.substring("display-name=".length());
                    if (!displayName.isBlank()) {
                        return unescapeIrc(displayName);
                    }
                }
            }
        }

        return fallback;
    }

    private static String unescapeIrc(String value) {
        return value.replace("\\s", " ")
                .replace("\\:", ";")
                .replace("\\r", "")
                .replace("\\n", "")
                .replace("\\\\", "\\");
    }

    private void postToMinecraft(String user, String message) {
        minecraft.execute(() -> {
            if (minecraft.player == null || minecraft.inGameHud == null) {
                return;
            }

            Text line = Text.empty()
                    .append(Text.literal("[Twitch: " + user + "]: ").formatted(Formatting.LIGHT_PURPLE))
                    .append(Text.literal(message).formatted(Formatting.WHITE));
            minecraft.inGameHud.getChatHud().addMessage(line);
        });
    }

    private final class Listener implements WebSocket.Listener {
        private final String channel;

        private Listener(String channel) {
            this.channel = channel;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            WebSocket.Listener.super.onOpen(webSocket);
            String nick = "justinfan" + (10000 + new Random().nextInt(90000));
            webSocket.sendText("CAP REQ :twitch.tv/tags twitch.tv/commands", true);
            webSocket.sendText("PASS SCHMOOPIIE", true);
            webSocket.sendText("NICK " + nick, true);
            webSocket.sendText("JOIN #" + channel, true);
            LOGGER.info("Joined Twitch IRC channel #{} as {}", channel, nick);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            pendingMessage.append(data);
            if (last) {
                handleRawMessage(pendingMessage.toString());
                pendingMessage = new StringBuilder();
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            LOGGER.warn("Twitch IRC socket error", error);
        }
    }
}
