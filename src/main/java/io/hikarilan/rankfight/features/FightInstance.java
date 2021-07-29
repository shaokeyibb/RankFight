package io.hikarilan.rankfight.features;

import com.google.common.collect.Lists;
import io.hikarilan.rankfight.RankFight;
import io.hikarilan.rankfight.beans.ArenaData;
import io.hikarilan.rankfight.beans.PlayerData;
import io.hikarilan.rankfight.utils.ColorUtils;
import io.hikarilan.rankfight.utils.Configuration;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@Data
public class FightInstance {

    @Getter
    private final static List<FightInstance> data = Lists.newArrayList();

    private final ArenaData arena;
    private final PlayerData player;
    private final PlayerData anotherPlayer;

    static {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onQuit(PlayerQuitEvent e) {
                checkPlayer(e);
            }

            @EventHandler
            public void onCommand(PlayerCommandPreprocessEvent e){
                Optional<FightInstance> find = data.parallelStream().filter(fightInstance -> fightInstance.getPlayer().getUuid().equals(e.getPlayer().getUniqueId()) || fightInstance.getAnotherPlayer().getUuid().equals(e.getPlayer().getUniqueId())).findAny();
                if (find.isPresent()) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(ColorUtils.translateChatColor("&c单挑过程中不能使用指令"));
                }
            }

            @EventHandler
            public void onDeath(PlayerDeathEvent e) {
                checkPlayer(e);
            }

            @EventHandler
            public void onRespawn(PlayerRespawnEvent e) {
                Location loc = PlayerData.getLastLocation().get(PlayerData.getPlayerDataByUUID(e.getPlayer().getUniqueId()));
                if (loc != null) {
                    e.setRespawnLocation(loc);
                    PlayerData.getLastLocation().remove(PlayerData.getPlayerDataByUUID(e.getPlayer().getUniqueId()));
                }
            }

        }, RankFight.getInstance());
    }

    public FightInstance(ArenaData arena, PlayerData player, PlayerData anotherPlayer) {
        this.arena = arena;
        this.player = player;
        this.anotherPlayer = anotherPlayer;
        Bukkit.getPlayer(player.getUuid()).teleport(arena.getPlayerLocation());

        Bukkit.getPlayer(anotherPlayer.getUuid()).teleport(arena.getOtherPlayerLocation());
    }

    public static FightInstance createFightInstance(ArenaData arena, PlayerData player, PlayerData anotherPlayer) {
        FightInstance instance = new FightInstance(arena, player, anotherPlayer);
        data.add(instance);
        return instance;
    }

    public static @Nullable FightInstance getFightInstanceByArena(ArenaData arena) {
        return data.stream().filter(fightInstance -> fightInstance.getArena().equals(arena)).findFirst().orElse(null);
    }

    private static void checkPlayer(PlayerEvent e) {
        Optional<FightInstance> find = data.parallelStream().filter(fightInstance -> fightInstance.getPlayer().getUuid().equals(e.getPlayer().getUniqueId()) || fightInstance.getAnotherPlayer().getUuid().equals(e.getPlayer().getUniqueId())).findAny();
        if (find.isPresent()) {
            PlayerData win;
            PlayerData fall;
            if (e.getPlayer().equals(Bukkit.getPlayer(find.get().getPlayer().getUuid()))) {
                fall = find.get().getPlayer();
                win = find.get().getAnotherPlayer();
            } else {
                fall = find.get().getAnotherPlayer();
                win = find.get().getPlayer();
            }
            Bukkit.getPlayer(win.getUuid()).teleport(PlayerData.getLastLocation().get(win));
            Bukkit.getPlayer(fall.getUuid()).teleport(PlayerData.getLastLocation().get(fall));
            Bukkit.getPlayer(win.getUuid()).sendTitle("由于对方玩家已离开，单挑结束", "您 积分 + " + Configuration.getInstance().getCreditWin(), 10, 20, 70);
            Bukkit.getPlayer(fall.getUuid()).sendTitle("您被击败了", "", 10, 20, 70);
            win.setCredit(win.getCredit() + Configuration.getInstance().getCreditWin());
            win.setShopCredit(win.getShopCredit() + Configuration.getInstance().getCreditWin());
            data.remove(find.get());
            PlayerData.getLastLocation().remove(win);
            PlayerData.getLastLocation().remove(fall);
        }
    }


    private static void checkPlayer(PlayerDeathEvent e) {
        Optional<FightInstance> find = data.parallelStream().filter(fightInstance -> fightInstance.getPlayer().getUuid().equals(e.getEntity().getUniqueId()) || fightInstance.getAnotherPlayer().getUuid().equals(e.getEntity().getUniqueId())).findAny();
        if (find.isPresent()) {
            PlayerData win;
            PlayerData fall;
            if (e.getEntity().equals(Bukkit.getPlayer(find.get().getPlayer().getUuid()))) {
                fall = find.get().getPlayer();
                win = find.get().getAnotherPlayer();
            } else {
                fall = find.get().getAnotherPlayer();
                win = find.get().getPlayer();
            }
            Bukkit.getPlayer(win.getUuid()).teleport(PlayerData.getLastLocation().get(win));
            Bukkit.getPlayer(fall.getUuid()).teleport(PlayerData.getLastLocation().get(fall));
            Bukkit.getPlayer(win.getUuid()).sendTitle("由于对方玩家已死亡，单挑结束", "您 积分 + " + Configuration.getInstance().getCreditWin(), 10, 20, 70);
            Bukkit.getPlayer(fall.getUuid()).sendTitle("您被击败了", "", 10, 20, 70);
            win.setCredit(win.getCredit() + Configuration.getInstance().getCreditWin());
            win.setShopCredit(win.getShopCredit() + Configuration.getInstance().getCreditWin());
            data.remove(find.get());
            PlayerData.getLastLocation().remove(win);
        }
    }

}
