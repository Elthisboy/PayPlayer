package com.elthisboy.paypleyer;

import com.elthisboy.paypleyer.item.ChargeMoneyItem;
import com.elthisboy.paypleyer.item.GiveMoneyItem;
import com.elthisboy.paypleyer.network.MoneyPackets;
import com.elthisboy.paypleyer.network.MoneyPacketsClient;
import com.elthisboy.paypleyer.screen.AmountScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

public class PayPlayerClient implements ClientModInitializer {

    // Nombre del último jugador apuntado al hacer click derecho
    // Se captura via mixin o via el callback de useOnEntity
    public static String lastTargetName = null;

    @Override
    public void onInitializeClient() {
        // Abrir GUI
        ChargeMoneyItem.openScreenCallback = giving ->
                MinecraftClient.getInstance().setScreen(new AmountScreen(giving));
        GiveMoneyItem.openScreenCallback = giving ->
                MinecraftClient.getInstance().setScreen(new AmountScreen(giving));

        // Click derecho sobre jugador: cobrar
        ChargeMoneyItem.triggerChargeCallback = () -> {
            if (lastTargetName != null) {
                MoneyPacketsClient.sendMoneyTransaction(
                        MoneyPackets.ACTION_CHARGE,
                        AmountScreen.currentAmount,
                        lastTargetName);
            }
        };

        // Click derecho sobre jugador: dar
        GiveMoneyItem.triggerGiveCallback = () -> {
            if (lastTargetName != null) {
                MoneyPacketsClient.sendMoneyTransaction(
                        MoneyPackets.ACTION_GIVE,
                        AmountScreen.currentAmount,
                        lastTargetName);
            }
        };
    }
}

