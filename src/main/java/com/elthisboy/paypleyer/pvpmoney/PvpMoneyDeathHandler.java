package com.elthisboy.paypleyer.pvpmoney;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

            sendVictimFeedback(victim, lossAmount, config);
            sendKillerFeedback(killer, stealAmount, victim.getName().getString(), config);
            playVictimEffects(victim, config);
            playKillerEffects(killer, config);
        } else {
            sendVictimFeedbackNoKiller(victim, lossAmount, config);
            playVictimEffects(victim, config);
        }
    }

    private static void sendVictimFeedback(ServerPlayerEntity victim, int lossAmount, PvpMoneyConfig config) {
        String amount = String.valueOf(lossAmount);
        if (config.chatMessages) {
            victim.sendMessage(Text.translatable("message.pvpmoney.lost", amount)
                    .copy().formatted(Formatting.RED), false);
        }
        if (config.actionBarMessages) {
            victim.sendMessage(Text.translatable("message.pvpmoney.loss_small", amount)
                    .copy().formatted(Formatting.DARK_RED), true);
        }
    }

    private static void sendVictimFeedbackNoKiller(ServerPlayerEntity victim, int lossAmount, PvpMoneyConfig config) {
        String amount = String.valueOf(lossAmount);
        if (config.chatMessages) {
            victim.sendMessage(Text.translatable("message.pvpmoney.lost_nokiller", amount)
                    .copy().formatted(Formatting.RED), false);
        }
        if (config.actionBarMessages) {
            victim.sendMessage(Text.translatable("message.pvpmoney.loss_small", amount)
                    .copy().formatted(Formatting.DARK_RED), true);
        }
    }

    private static void sendKillerFeedback(ServerPlayerEntity killer, int stealAmount, String victimName, PvpMoneyConfig config) {
        String amount = String.valueOf(stealAmount);
        if (config.chatMessages) {
            killer.sendMessage(Text.translatable("message.pvpmoney.stole", amount, victimName)
                    .copy().formatted(Formatting.GREEN), false);
        }
        if (config.actionBarMessages) {
            killer.sendMessage(Text.translatable("message.pvpmoney.received_small", amount)
                    .copy().formatted(Formatting.GOLD), true);
        }
    }

    private static void playVictimEffects(ServerPlayerEntity victim, PvpMoneyConfig config) {
        if (config.playSounds) {
            victim.playSoundToPlayer(
                    SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(),
                    SoundCategory.PLAYERS,
                    config.victimSoundVolume,
                    config.victimSoundPitch
            );
        }
        if (config.particlesEnabled && victim.getServerWorld() != null) {
            ServerWorld world = victim.getServerWorld();
            world.spawnParticles(
                    ParticleTypes.ANGRY_VILLAGER,
                    victim.getX(), victim.getY() + 1.5, victim.getZ(),
                    4, 0.3, 0.3, 0.3, 0.01
            );
        }
    }

    private static void playKillerEffects(ServerPlayerEntity killer, PvpMoneyConfig config) {
        if (config.playSounds) {
            killer.playSoundToPlayer(
                    SoundEvents.ENTITY_PLAYER_LEVELUP,
                    SoundCategory.PLAYERS,
                    config.killerSoundVolume,
                    config.killerSoundPitch
            );
        }
        if (config.particlesEnabled && killer.getServerWorld() != null) {
            ServerWorld world = killer.getServerWorld();
            world.spawnParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    killer.getX(), killer.getY() + 1.5, killer.getZ(),
                    5, 0.3, 0.4, 0.3, 0.01
            );
        }
    }
}
