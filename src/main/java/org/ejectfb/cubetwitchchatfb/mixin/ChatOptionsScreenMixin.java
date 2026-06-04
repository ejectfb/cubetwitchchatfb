package org.ejectfb.cubetwitchchatfb.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ChatOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.ejectfb.cubetwitchchatfb.TwitchConfigScreen;

@Mixin(ChatOptionsScreen.class)
public abstract class ChatOptionsScreenMixin extends Screen {
    protected ChatOptionsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void ejectfbcubetwitchchat$addChatSettingsButton(CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Twitch Chat"), button -> this.client.setScreen(new TwitchConfigScreen(screen)))
                .dimensions(this.width / 2 - 100, this.height - 56, 200, 20)
                .build());
    }
}
