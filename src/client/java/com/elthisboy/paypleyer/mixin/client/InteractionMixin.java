package com.elthisboy.paypleyer.mixin.client;

import com.elthisboy.paypleyer.PayPlayerClient;
import com.elthisboy.paypleyer.PayPlayer;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class InteractionMixin {

    @Inject(method = "interactEntity", at = @At("HEAD"))
    private void onInteractEntity(PlayerEntity player, Entity entity, Hand hand,
                                  CallbackInfoReturnable<ActionResult> cir) {
        // Guardamos el nombre del jugador objetivo antes de que se procese el click
        if (entity instanceof PlayerEntity target) {
            ItemStack held = player.getStackInHand(hand);
            if (held.isOf(net.minecraft.registry.Registries.ITEM
                    .get(net.minecraft.util.Identifier.of(PayPlayer.MOD_ID, "charge_money")))
                    || held.isOf(net.minecraft.registry.Registries.ITEM
                    .get(net.minecraft.util.Identifier.of(PayPlayer.MOD_ID, "give_money")))) {
                PayPlayerClient.lastTargetName = target.getName().getString();
            }
        }
    }
}
