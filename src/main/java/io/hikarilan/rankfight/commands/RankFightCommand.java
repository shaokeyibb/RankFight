package io.hikarilan.rankfight.commands;

import com.google.common.collect.Lists;
import io.hikarilan.rankfight.beans.ArenaData;
import io.hikarilan.rankfight.beans.ItemGift;
import io.hikarilan.rankfight.beans.PlayerData;
import io.hikarilan.rankfight.features.FightInstance;
import io.hikarilan.rankfight.gui.ShopGUI;
import io.hikarilan.rankfight.utils.ColorUtils;
import io.hikarilan.rankfight.utils.Configuration;
import io.hikarilan.rankfight.utils.UserQueue;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RankFightCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.translateChatColor("&c&l该指令仅允许玩家执行"));
            return true;
        }

        if (args[0].equalsIgnoreCase("admin")) {
            return invokeAdminCommand((Player) sender, command, label, args);
        } else {
            return invokeUserCommand((Player) sender, command, label, args);
        }
    }

    public boolean invokeAdminCommand(Player player, Command command, String label, String[] args) {
        if (!player.hasPermission("rankfight.admin")) {
            player.sendMessage(ColorUtils.translateChatColor("&c无权操作"));
            return true;
        }

        if (args.length < 2) {
            showHelp(player);
            return true;
        }

        String subCommand = args[1];
        switch (subCommand) {
            case "status":
                if (args.length != 3) {
                    showHelp(player);
                    return true;
                }

                Player checkedPlayer = Bukkit.getPlayer(args[2]);
                if (checkedPlayer == null) {
                    player.sendMessage(ColorUtils.translateChatColor("&c指定玩家不存在"));
                    return true;
                }

                PlayerData playerData = PlayerData.getPlayerDataByUUID(checkedPlayer.getUniqueId());
                ColorUtils.translateChatColor(Lists.newArrayList(
                        "玩家 " + checkedPlayer.getName() + " 当前的积分为：" + playerData.getCredit(),
                        "玩家 " + checkedPlayer.getName() + " 当前可用的商店积分：" + playerData.getShopCredit(),
                        "玩家 " + checkedPlayer.getName() + " 当前的排名为：" + playerData.getRank()
                )).forEach(player::sendMessage);
                return true;

            case "addArena":
                if (args.length != 9) {
                    showHelp(player);
                    return true;
                }

                String name = args[2];

                if (ArenaData.getArenaDataByName(name) != null) {
                    player.sendMessage(ColorUtils.translateChatColor("&c指定竞技场名称已存在"));
                    return true;
                }

                List<Integer> locationOriginal;
                try {
                    locationOriginal = Arrays.stream(Arrays.copyOfRange(args, 3, 9)).map(Integer::parseInt).collect(Collectors.toList());
                } catch (NumberFormatException e) {
                    player.sendMessage(ColorUtils.translateChatColor("&c请输入正确的坐标"));
                    return true;
                }
                Location loc1 = new Location(player.getWorld(), locationOriginal.get(0), locationOriginal.get(1), locationOriginal.get(2));
                Location loc2 = new Location(player.getWorld(), locationOriginal.get(3), locationOriginal.get(4), locationOriginal.get(5));

                ArenaData data = ArenaData.createArenaData(name, loc1, loc2);

                player.sendMessage(ColorUtils.translateChatColor("&b竞技场 " + data.getName() + " 已成功创建"));

                return true;

            case "removeArena":
                if (args.length != 3) {
                    showHelp(player);
                    return true;
                }

                String name2 = args[2];

                ArenaData arenaData = ArenaData.getArenaDataByName(name2);

                if (arenaData == null) {
                    player.sendMessage(ColorUtils.translateChatColor("&c指定竞技场不存在"));
                    return true;
                }

                ArenaData.getData().remove(arenaData);

                player.sendMessage(ColorUtils.translateChatColor("&b竞技场 " + arenaData.getName() + " 已成功移除"));

                return true;

            case "addItemGift":
                if (args.length != 3) {
                    showHelp(player);
                    return true;
                }

                int creditNeeded;
                try {
                    creditNeeded = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ColorUtils.translateChatColor("&c请输入正确的积分数"));
                    return true;
                }

                ItemStack handItem = player.getInventory().getItemInMainHand();
                if (handItem == null || handItem.getType() == Material.AIR) {
                    player.sendMessage(ColorUtils.translateChatColor("&c请确保手中持有有效的物品"));
                    return true;
                }

                if (ItemGift.getItemGiftByItemStack(handItem) != null) {
                    player.sendMessage(ColorUtils.translateChatColor("&c指定物品已存在于商店中"));
                    return true;
                }

                ItemGift.createItemGift(creditNeeded, handItem);

                player.sendMessage(ColorUtils.translateChatColor("&b物品已成功添加至商店"));

                return true;

            case "removeItemGift":
                if (args.length != 2) {
                    showHelp(player);
                    return true;
                }

                ItemStack handItem2 = player.getInventory().getItemInMainHand();
                ItemGift item = ItemGift.getItemGiftByItemStack(handItem2);
                if (item == null) {
                    player.sendMessage(ColorUtils.translateChatColor("&c指定物品不存在于商店中"));
                    return true;
                }

                ItemGift.getData().remove(ItemGift.getData().stream().parallel().filter(itemGift -> itemGift == item).findFirst().get());

                player.sendMessage(ColorUtils.translateChatColor("&b物品已成功移除"));

                return true;

            case "setCredit":

                if (args.length < 4 || args.length > 5) {
                    showHelp(player);
                    return true;
                }

                Player checkedPlayer2 = Bukkit.getPlayer(args[2]);
                if (checkedPlayer2 == null) {
                    player.sendMessage(ColorUtils.translateChatColor("&c指定玩家不存在"));
                    return true;
                }

                if (args.length == 4) {
                    int credit;
                    try {
                        credit = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ColorUtils.translateChatColor("&c请输入正确的积分数"));
                        return true;
                    }

                    PlayerData data2 = PlayerData.getPlayerDataByUUID(checkedPlayer2.getUniqueId());
                    data2.setCredit(credit);

                    player.sendMessage(ColorUtils.translateChatColor("积分设置成功"));

                    return true;

                } else {
                    int credit;
                    int shopCredit;
                    try {
                        credit = Integer.parseInt(args[3]);
                        shopCredit = Integer.parseInt(args[4]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ColorUtils.translateChatColor("&c请输入正确的积分数"));
                        return true;
                    }

                    PlayerData data2 = PlayerData.getPlayerDataByUUID(checkedPlayer2.getUniqueId());
                    data2.setCredit(credit);
                    data2.setShopCredit(shopCredit);

                    player.sendMessage(ColorUtils.translateChatColor("积分设置成功"));

                    return true;
                }

            case "reload":
                PlayerData.saveAll();
                ArenaData.saveAll();
                ItemGift.saveAll();

                Configuration.getInstance().reload();

                player.sendMessage(ColorUtils.translateChatColor("&b已成功重载插件"));

                return true;

            default:
                showHelp(player);
                return true;
        }
    }

    public boolean invokeUserCommand(Player player, Command command, String label, String[] args) {
        PlayerData playerData = PlayerData.getPlayerDataByUUID(player.getUniqueId());

        String subCommand = args[0];
        switch (subCommand) {
            case "join":

                if (args.length > 3) {
                    showHelp(player);
                    return true;
                }

                if (UserQueue.getInstance().getPool().contains(PlayerData.getPlayerDataByUUID(player.getUniqueId()))) {
                    player.sendMessage(ColorUtils.translateChatColor("&c您已加入一个排位队列，不能重复加入"));
                    return true;
                }

                if (args.length == 2) {
                    ArenaData arena = ArenaData.getArenaDataByName(args[1]);

                    if (arena == null) {
                        player.sendMessage(ColorUtils.translateChatColor("&c指定竞技场不存在"));
                        return true;
                    }

                    PlayerData.getLastLocation().put(playerData, Bukkit.getPlayer(playerData.getUuid()).getLocation());
                    UserQueue.getInstance().addQueue(playerData, (onePlayer, anotherPlayer) -> {
                        String queue = "已匹配到实力相当的对手，正在进入竞技场...";
                        Bukkit.getPlayer(onePlayer.getUuid()).sendMessage(queue);
                        Bukkit.getPlayer(anotherPlayer.getUuid()).sendMessage(queue);
                        FightInstance.createFightInstance(arena, onePlayer, anotherPlayer);
                    });
                } else {
                    Optional<ArenaData> arena = ArenaData.getData().stream().findFirst();

                    if (!arena.isPresent()) {
                        player.sendMessage(ColorUtils.translateChatColor("&c无竞技场可用，请先创建竞技场"));
                        return true;
                    }

                    PlayerData.getLastLocation().put(playerData, Bukkit.getPlayer(playerData.getUuid()).getLocation());
                    UserQueue.getInstance().addQueue(playerData, (onePlayer, anotherPlayer) -> {
                        String queue = "已匹配到实力相当的对手，正在进入竞技场...";
                        Bukkit.getPlayer(onePlayer.getUuid()).sendMessage(queue);
                        Bukkit.getPlayer(anotherPlayer.getUuid()).sendMessage(queue);
                        FightInstance.createFightInstance(arena.get(), onePlayer, anotherPlayer);
                    });
                }
                player.sendMessage("已加入排位等待队列");
                return true;

            case "status":
                if (args.length != 1) {
                    showHelp(player);
                    return true;
                }

                ColorUtils.translateChatColor(Lists.newArrayList(
                        "您当前的积分为：" + playerData.getCredit(),
                        "您当前可用的商店积分：" + playerData.getShopCredit(),
                        "您当前的排名为：" + playerData.getRank()
                )).forEach(player::sendMessage);
                return true;

            case "shop":
                ShopGUI.openShopGUI(player);
                return true;

            case "list":
                if (args.length != 1) {
                    showHelp(player);
                    return true;
                }

                player.sendMessage("--- 竞技场列表 ---");
                ArenaData.getData().forEach(arenaData -> {
                    StringBuilder builder = new StringBuilder();
                    builder.append(arenaData.getName()).append(" -- ");
                    if (FightInstance.getFightInstanceByArena(arenaData) == null) {
                        builder.append("&2可用");
                    } else {
                        builder.append("&4不可用");
                    }
                    player.sendMessage(ColorUtils.translateChatColor(builder.toString()));
                });
                return true;

            default:
                showHelp(player);
                return true;
        }
    }

    public void showHelp(CommandSender sender) {
        List<String> generalHelp = ColorUtils.translateChatColor(Lists.newArrayList(
                "--- RankFight 插件帮助 ---"
        ));

        List<String> userHelp = ColorUtils.translateChatColor(Lists.newArrayList(
                "/rankfight join [竞技场名称（可选）]-- 加入或创建一场单挑",
                "/rankfight list -- 查看可用的竞技场列表",
                "/rankfight status -- 查看自己的排名和积分",
                "/rankfight shop -- 查看积分商店"
        ));

        List<String> adminHelp = ColorUtils.translateChatColor(Lists.newArrayList(
                "/rankfight admin status <玩家名> -- 查看指定玩家的排名和积分",
                "/rankfight admin addArena <竞技场名称> <x1> <y1> <z1> <x2> <y2> <z2> -- 创建一个竞技场",
                "/rankfight admin removeArena <竞技场名称> -- 移除一个竞技场",
                "/rankfight admin addItemGift <所需积分> -- 添加手持物品为商店奖励",
                "/rankfight admin removeItemGift -- 移除手持物品为商店奖励",
                "/rankfight admin setCredit <玩家名> <积分> [商店积分（可选）] -- 设置指定玩家的积分和商店积分",
                "/rankfight admin reload -- 重载插件配置文件并强制存储数据到磁盘",
                "——————————————",
                "Developed by HikariLan,permitted under GNU Public License Version 3."
        ));

        generalHelp.forEach(sender::sendMessage);
        userHelp.forEach(sender::sendMessage);
        if (sender.hasPermission("rankfight.admin"))
            adminHelp.forEach(sender::sendMessage);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender.hasPermission("rankfight.admin")) {
            return suggestAdminCommand(sender, command, alias, args);
        } else {
            return suggestUserCommand(sender, command, alias, args);
        }
    }

    public List<String> suggestAdminCommand(CommandSender sender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 1:
                return Lists.newArrayList("admin", "join", "list", "status", "shop");
            case 2:
                if (!args[0].equals("admin")) break;
                return Lists.newArrayList("status", "addArena", "removeArena", "addItemGift", "removeItemGift", "setCredit", "reload");
            case 3:
                switch (args[1]) {
                    case "status":
                    case "setCredit":
                        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
                    case "removeArena":
                        return ArenaData.getData().parallelStream().map(ArenaData::getName).collect(Collectors.toList());
                }
        }
        return suggestUserCommand(sender, command, alias, args);
    }

    public List<String> suggestUserCommand(CommandSender sender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 1:
                return Lists.newArrayList("join", "list", "status", "shop");
            case 2:
                if (args[0].equals("join"))
                    return ArenaData.getData().parallelStream().map(ArenaData::getName).collect(Collectors.toList());
        }
        return Lists.newArrayList("");
    }
}
