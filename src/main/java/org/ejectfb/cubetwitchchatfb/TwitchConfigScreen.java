package org.ejectfb.cubetwitchchatfb;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class TwitchConfigScreen extends Screen {
    private final Screen parent;
    private boolean enabled;
    private TextFieldWidget urlField;
    private ButtonWidget toggleButton;
    private ButtonWidget applyButton;
    private TextWidget descriptionLine1;
    private TextWidget descriptionLine2;

    public TwitchConfigScreen(Screen parent) {
        super(Text.literal("Twitch Chat"));
        this.parent = parent;

        TwitchConfig config = TwitchCubeChatClient.getConfig();
        this.enabled = config.enabled();
    }

    @Override
    protected void init() {
        TwitchConfig config = TwitchCubeChatClient.getConfig();
        int center = this.width / 2;

        toggleButton = ButtonWidget.builder(toggleText(), button -> {
            enabled = !enabled;
            toggleButton.setMessage(toggleText());
            updateFieldState();
        }).dimensions(center - 100, 56, 200, 20).build();
        addDrawableChild(toggleButton);

        descriptionLine1 = new TextWidget(center - 150, 100, 300, 9, Text.literal("Enter a Twitch streamer's channel name").formatted(Formatting.WHITE), this.textRenderer);
        addDrawableChild(descriptionLine1);

        descriptionLine2 = new TextWidget(center - 150, 112, 300, 9, Text.literal("to show their chat in Minecraft").formatted(Formatting.WHITE), this.textRenderer);
        addDrawableChild(descriptionLine2);

        urlField = new TextFieldWidget(this.textRenderer, center - 150, 126, 260, 20, Text.literal("Twitch channel"));
        urlField.setMaxLength(2048);
        urlField.setText(config.twitchChannel());
        addDrawableChild(urlField);

        applyButton = ButtonWidget.builder(Text.literal("✓"), button -> apply()).dimensions(center + 116, 126, 34, 20).build();
        addDrawableChild(applyButton);

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close()).dimensions(center - 100, this.height - 32, 200, 20).build());
        updateFieldState();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    private Text toggleText() {
        return Text.literal(enabled ? "Twitch Chat: ON" : "Twitch Chat: OFF");
    }

    private void updateFieldState() {
        urlField.visible = enabled;
        urlField.active = enabled;
        applyButton.visible = enabled;
        applyButton.active = enabled;
        descriptionLine1.visible = enabled;
        descriptionLine2.visible = enabled;
    }

    private void apply() {
        TwitchCubeChatClient.saveConfig(new TwitchConfig(enabled, urlField.getText().trim()));
    }
}
