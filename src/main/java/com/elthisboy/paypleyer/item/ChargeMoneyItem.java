package com.elthisboy.paypleyer.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class ChargeMoneyItem extends Item {

    public static Consumer<Boolean> openScreenCallback = null;
    public static Runnable triggerChargeCallback = null;

    public ChargeMoneyItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.isSneaking()) {
            if (world.isClient && openScreenCallback != null) {
                openScreenCallback.accept(false);
            }
            return TypedActionResult.success(user.getStackInHand(hand));
        }
        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, net.minecraft.entity.LivingEntity entity, Hand hand) {
        if (!user.isSneaking() && entity instanceof PlayerEntity target) {
            if (user.getWorld().isClient && triggerChargeCallback != null) {
                triggerChargeCallback.run();
            }
            // En servidor: enviamos el paquete desde el cliente via callback
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}

