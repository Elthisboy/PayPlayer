package com.elthisboy.paypleyer.pvpmoney;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PvpMoneyDeathHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("payplayer");

    private PvpMoneyDeathHandler() {}

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((LivingEntity entity, DamageSource damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity victim)) return;
            LOGGER.info("[PvPMoney] Death event triggered for {}", victim.getName().getString());
            handleDeath(victim, damageSource);
        });
        LOGGER.info("[PvPMoney] System initialized");
    }

    private static void handleDeath(ServerPlayerEntity victim, DamageSource damageSource) {
        var config = PvpMoneyConfig.get();
        if (!config.enabled) {
            LOGGER.info("[PvPMoney] System is disabled in config, skipping.");
            return;
        }

        var server = victim.getServer();
        if (server == null) return;

        var scoreboard = server.getScoreboard();
        var objective = scoreboard.getNullableObjective(config.scoreboardObjective);
        if (objective == null) {
            LOGGER.warn("[PvPMoney] Scoreboard objective '{}' not found, skipping.", config.scoreboardObjective);
            return;
        }

        int currentMoney = MoneyUtils.getMoney(victim, scoreboard, objective);
        LOGGER.info("[PvPMoney] Victim money: {}", currentMoney);
        if (currentMoney <= 0) return;

        int lossAmount = (int) Math.floor(currentMoney * config.deathLosePercent);
        if (lossAmount <= 0) return;

        lossAmount = Math.min(lossAmount, currentMoney);
        MoneyUtils.removeMoney(victim, scoreboard, objective, lossAmount);

        ServerPlayerEntity killer = null;
        if (damageSource.getAttacker() instanceof ServerPlayerEntity attackerPlayer
                && attackerPlayer != victim) {
            killer = attackerPlayer;
        }

        if (killer != null) {
            LOGGER.info("[PvPMoney] Killer detected: {}", killer.getName().getString());

            int stealAmount = (int) Math.floor(currentMoney * config.killerStealPercent);
            stealAmount = Math.min(stealAmount, lossAmount);
            stealAmount = Math.max(0, stealAmount);

            if (stealAmount > 0) {
                MoneyUtils.addMoney(killer, scoreboard, objective, stealAmount);
            }

            if (config.sendMessages) {
                victim.sendMessage(
                        Text.translatable("message.pvpmoney.lost", String.valueOf(lossAmount)), false);
                killer.sendMessage(
                        Text.translatable("message.pvpmoney.stole", String.valueOf(stealAmount), victim.getName().getString()), false);
            }

            if (config.playSound) {
                victim.playSoundToPlayer(SoundEvents.ENTITY_VILLAGER_HURT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                killer.playSoundToPlayer(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.7f, 1.2f);
            }
        } else {
            if (config.sendMessages) {
                victim.sendMessage(
                        Text.translatable("message.pvpmoney.lost_nokiller", String.valueOf(lossAmount)), false);
            }

            if (config.playSound) {
                victim.playSoundToPlayer(SoundEvents.ENTITY_VILLAGER_HURT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
        }
    }
}
