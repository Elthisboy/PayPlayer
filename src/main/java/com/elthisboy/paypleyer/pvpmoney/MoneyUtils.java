package com.elthisboy.paypleyer.pvpmoney;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.network.ServerPlayerEntity;

public final class MoneyUtils {

    private MoneyUtils() {}

    public static int getMoney(ServerPlayerEntity player, Scoreboard scoreboard, ScoreboardObjective objective) {
        return scoreboard.getOrCreateScore(player, objective).getScore();
    }

    public static void setMoney(ServerPlayerEntity player, Scoreboard scoreboard, ScoreboardObjective objective, int amount) {
        scoreboard.getOrCreateScore(player, objective).setScore(Math.max(0, amount));
    }

    public static void addMoney(ServerPlayerEntity player, Scoreboard scoreboard, ScoreboardObjective objective, int amount) {
        var score = scoreboard.getOrCreateScore(player, objective);
        score.setScore(Math.max(0, score.getScore() + amount));
    }

    public static void removeMoney(ServerPlayerEntity player, Scoreboard scoreboard, ScoreboardObjective objective, int amount) {
        var score = scoreboard.getOrCreateScore(player, objective);
        score.setScore(Math.max(0, score.getScore() - amount));
    }
}
