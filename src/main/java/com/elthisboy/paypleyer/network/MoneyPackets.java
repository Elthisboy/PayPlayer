package com.elthisboy.paypleyer.network;

import com.elthisboy.paypleyer.PayPlayer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MoneyPackets {

    public static final int ACTION_CHARGE = 0;
    public static final int ACTION_GIVE   = 1;

    public record MoneyTransactionPayload(int action, int amount, String targetName)
            implements CustomPayload {

        public static final CustomPayload.Id<MoneyTransactionPayload> ID =
                new CustomPayload.Id<>(Identifier.of(PayPlayer.MOD_ID, "money_transaction"));

        public static final PacketCodec<PacketByteBuf, MoneyTransactionPayload> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.INTEGER, MoneyTransactionPayload::action,
                        PacketCodecs.INTEGER, MoneyTransactionPayload::amount,
                        PacketCodecs.STRING,  MoneyTransactionPayload::targetName,
                        MoneyTransactionPayload::new
                );

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void registerServerHandlers() {
        PayloadTypeRegistry.playC2S().register(
                MoneyTransactionPayload.ID,
                MoneyTransactionPayload.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                MoneyTransactionPayload.ID,
                (payload, context) -> {
                    ServerPlayerEntity sender = context.player();
                    MinecraftServer server = sender.getServer();
                    int action    = payload.action();
                    int amount    = payload.amount();
                    String target = payload.targetName();

                    server.execute(() -> handleTransaction(server, sender, action, amount, target));
                }
        );
    }

    private static void handleTransaction(MinecraftServer server, ServerPlayerEntity sender,
                                          int action, int amount, String targetName) {

        ServerPlayerEntity target = server.getPlayerManager().getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(Text.literal("§c✗ Player §e" + targetName + "§c not found on the server."), false);
            return;
        }
        if (amount <= 0) {
            sender.sendMessage(Text.literal("§c✗ Amount must be greater than 0."), false);
            return;
        }

        var cfg = com.elthisboy.paypleyer.config.PayPlayerConfig.get();
        String sbName = cfg.scoreboardName;
        String sym    = cfg.currencySymbol;

        var scoreboard = server.getScoreboard();
        var objective  = scoreboard.getNullableObjective(sbName);
        if (objective == null) {
            sender.sendMessage(Text.literal("§c✗ Scoreboard §e" + sbName + "§c does not exist.\n§7Create it with: §f/scoreboard objectives add " + sbName + " dummy"), false);
            return;
        }

        if (action == ACTION_CHARGE) {
            var targetScore = scoreboard.getOrCreateScore(target, objective);
            int targetCurrent = targetScore.getScore();

            if (targetCurrent < amount) {
                sender.sendMessage(Text.literal(
                        "§c✗ §e" + target.getName().getString()
                        + "§c doesn't have enough " + cfg.currencyName
                        + "§c. §7Has: §6" + sym + targetCurrent), false);
                target.sendMessage(Text.literal(
                        "§e⚠ §e" + sender.getName().getString()
                        + "§e tried to charge you §6" + sym + amount
                        + "§e but you don't have enough funds."), false);
                return;
            }

            targetScore.setScore(targetCurrent - amount);

            // If chargeToSelf: add the amount to the sender's account
            if (cfg.chargeToSelf) {
                var senderScore = scoreboard.getOrCreateScore(sender, objective);
                senderScore.setScore(senderScore.getScore() + amount);
            }

            // Sender feedback
            sender.sendMessage(Text.literal(
                    "§a✔ Charged §6" + sym + amount
                    + "§a to §e" + target.getName().getString()), false);
            sender.sendMessage(Text.literal(
                    "§a+" + sym + amount + " §7from §e" + target.getName().getString()), true);

            // Target feedback
            target.sendMessage(Text.literal(
                    "§c✗ §e" + sender.getName().getString()
                    + "§c charged you §6" + sym + amount), false);
            target.sendMessage(Text.literal(
                    "§c-" + sym + amount + " §7charged by §e" + sender.getName().getString()), true);

        } else if (action == ACTION_GIVE) {
            // If giveFromSelf: check and deduct from sender's account
            if (cfg.giveFromSelf) {
                var senderScore = scoreboard.getOrCreateScore(sender, objective);
                int senderCurrent = senderScore.getScore();
                if (senderCurrent < amount) {
                    sender.sendMessage(Text.literal(
                            "§c✗ You don't have enough " + cfg.currencyName
                            + "§c. §7You have: §6" + sym + senderCurrent), false);
                    return;
                }
                senderScore.setScore(senderCurrent - amount);
            }

            var targetScore = scoreboard.getOrCreateScore(target, objective);
            targetScore.setScore(targetScore.getScore() + amount);

            // Sender feedback
            sender.sendMessage(Text.literal(
                    "§a✔ Sent §6" + sym + amount
                    + "§a to §e" + target.getName().getString()), false);
            sender.sendMessage(Text.literal(
                    "§c-" + sym + amount + " §7sent to §e" + target.getName().getString()), true);

            // Target feedback
            target.sendMessage(Text.literal(
                    "§a✔ §e" + sender.getName().getString()
                    + "§a sent you §6" + sym + amount), false);
            target.sendMessage(Text.literal(
                    "§a+" + sym + amount + " §7from §e" + sender.getName().getString()), true);
        }
    }
}
