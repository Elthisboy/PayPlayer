package com.elthisboy.paypleyer.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class MoneyPacketsClient {

    public static void sendMoneyTransaction(int action, int amount, String targetPlayerName) {
        ClientPlayNetworking.send(
                new MoneyPackets.MoneyTransactionPayload(action, amount, targetPlayerName)
        );
    }
}

