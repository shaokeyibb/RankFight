package io.hikarilan.rankfight.hook;

import io.hikarilan.rankfight.RankFight;
import io.hikarilan.rankfight.beans.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PAPIHook extends PlaceholderExpansion {

    private final RankFight plugin;

    public PAPIHook(RankFight plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;

    }

    @Override
    public @NotNull String getIdentifier() {
        return "rankfight";
    }

    @Override
    public @NotNull String getAuthor() {
        return "HikariLan";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        String[] paramsSplit = params.split("_");

        if (paramsSplit.length == 1) {
            String type = paramsSplit[0];

            return getString(type, player);

        } else if (paramsSplit.length == 2) {
            String type = paramsSplit[0];
            String rank = paramsSplit[1];
            OfflinePlayer otherPlayer = Bukkit.getOfflinePlayer(rank);

            return getString(type, otherPlayer);

        } else {
            return null;
        }
    }

    private String getString(String type, OfflinePlayer player) {
        if (player == null) {
            return null;
        }

        if (type.equals("rank")) {
            return String.valueOf(PlayerData.getPlayerDataByUUID(player.getUniqueId()).getRank());

        } else if (type.equals("credit")) {
            return String.valueOf(PlayerData.getPlayerDataByUUID(player.getUniqueId()).getCredit());

        } else {
            return null;
        }
    }
}
