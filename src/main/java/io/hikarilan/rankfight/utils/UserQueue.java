package io.hikarilan.rankfight.utils;

import com.google.common.collect.Sets;
import io.hikarilan.rankfight.RankFight;
import io.hikarilan.rankfight.beans.PlayerData;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

public class UserQueue {

    private static UserQueue instance;
    private static RankFight plugin = RankFight.getPlugin(RankFight.class);

    @Getter
    private final Set<PlayerData> pool = Sets.newHashSet();

    public static UserQueue getInstance() {
        if (instance == null) instance = new UserQueue();
        return instance;
    }

    private UserQueue() {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onQuit(PlayerQuitEvent e) {
                PlayerData data = PlayerData.getPlayerDataByUUID(e.getPlayer().getUniqueId());
                if (pool.contains(data)) {
                    pool.remove(data);
                    PlayerData.getLastLocation().remove(data);
                }
            }
        }, plugin);
    }

    public void addQueue(@NotNull PlayerData playerData, BiConsumer<PlayerData, PlayerData> onComplete) {
        Optional<PlayerData> other = pool.parallelStream()
                .filter(otherPlayer -> Math.abs(playerData.getCredit() - otherPlayer.getCredit()) <= Configuration.getInstance().getCreditInterval())
                .min(Comparator.comparingInt(PlayerData::getCredit));
        if (other.isPresent()) {
            onComplete.accept(playerData, other.get());
            pool.remove(other.get());
        } else {
            pool.add(playerData);
            runPoolRemovalTask(playerData);
        }
    }

    private void runPoolRemovalTask(@NotNull PlayerData playerData) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            pool.remove(playerData);
            Player player = Bukkit.getPlayer(playerData.getUuid());
            if (player != null){
                player.sendMessage("由于等待超时，您已被移出排位队列");
            }
        }, Configuration.getInstance().getQueueTimeoutSec() * 20);
    }

}
