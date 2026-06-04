package org.ejectfb.cubetwitchchatfb.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ChatOptionsScreen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.ejectfb.cubetwitchchatfb.TwitchConfigScreen;

@Mixin(ChatOptionsScreen.class)
public abstract class ChatOptionsScreenMixin extends GameOptionsScreen {
    public ChatOptionsScreenMixin(Screen parent, GameOptions gameOptions, Text title) {
        super(parent, gameOptions, title);
    }

    @Inject(method = "addOptions", at = @At("TAIL"))
    private void ejectfbcubetwitchchat$addChatSettingsButton(CallbackInfo ci) {
        if (this.body == null) {
            return;
        }

        Screen screen = (Screen) (Object) this;
        this.body.addWidgetEntry(ButtonWidget.builder(Text.literal("Twitch Chat"), button -> this.client.setScreen(new TwitchConfigScreen(screen)))
                .width(310)
                .build(), null);
    }
}
