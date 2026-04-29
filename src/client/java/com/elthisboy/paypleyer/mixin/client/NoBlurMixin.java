package com.elthisboy.paypleyer.mixin.client;

import com.elthisboy.paypleyer.screen.AmountScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class NoBlurMixin {

    @Inject(method = "renderBlur", at = @At("HEAD"), cancellable = true)
    private void cancelBlurForPayPlayerScreens(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof AmountScreen) {
            ci.cancel();
        }
    }
}
