# EjectFB CubeTwitchChatFB

Client-side Fabric mod for Minecraft 1.21.11. It mirrors public Twitch chat messages into the normal Minecraft chat without Twitch API tokens.

Messages are rendered as:

`[Twitch: chatter]: message`

The prefix is purple and the message text is white.

## Settings

Open `Options` -> chat settings -> `Twitch chat`.

Use the toggle to enable or disable the bridge. When enabled, the field appears with `enabled=true`; enter a Twitch channel name or `https://twitch.tv/channel` URL and press the check button.

The config is stored in `config/ejectfbcubetwitchchat.json`.

## Build

Build the mod with Fabric Loom:

```bash
gradle build
```

The jar will be in `build/libs/`. Fabric mods need Loom because Minecraft dependencies must be remapped before packaging.
