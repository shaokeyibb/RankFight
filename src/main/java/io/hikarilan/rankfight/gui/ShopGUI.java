package io.hikarilan.rankfight.gui;

import com.google.common.collect.Lists;
import io.hikarilan.rankfight.RankFight;
import io.hikarilan.rankfight.beans.ItemGift;
import io.hikarilan.rankfight.beans.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Optional;

public class ShopGUI {


    private static Inventory getShopGUI() {
        Inventory inventory = Bukkit.createInventory(new ShopGUIHolder(), 54, "积分商店");
        inventory.addItem(ItemGift.getData().stream()
                .filter(itemGift -> itemGift.getItem().getItemMeta() != null)
                .map(itemGift -> {
                    ItemStack item = itemGift.getItem().clone();
                    ItemMeta meta = item.getItemMeta();
                    List<String> lore;
                    if (meta.hasLore()) {
                        lore = meta.getLore();
                    } else {
                        lore = Lists.newArrayList();
                    }
                    lore.add("");
                    lore.add("————<>————");
                    lore.add("购买所需积分:" + itemGift.getCreditNeeded());
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    return item;
                }).distinct().toArray(ItemStack[]::new));
        return inventory;
    }

    public static InventoryView openShopGUI(Player player) {
        return player.openInventory(getShopGUI());
    }

    private static class ShopGUIHolder implements InventoryHolder {

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    static {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onClickItem(InventoryClickEvent e) {
                if (e.getClickedInventory() != null && e.getClickedInventory().getHolder() instanceof ShopGUIHolder) {
                    e.setCancelled(true);
                    if (e.isLeftClick() && e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                        Optional<ItemGift> gift = ItemGift.getData().parallelStream().filter(itemGift -> {
                            ItemStack item = e.getCurrentItem().clone();
                            ItemMeta meta = item.getItemMeta();
                            List<String> lore = meta.getLore();
                            int index = lore.indexOf("————<>————");
                            lore.remove(index + 1);
                            lore.remove(index);
                            lore.remove(index - 1);
                            meta.setLore(lore);
                            item.setItemMeta(meta);
                            return itemGift.getItem().equals(item);
                        }).findFirst();
                        PlayerData data = PlayerData.getPlayerDataByUUID(e.getWhoClicked().getUniqueId());
                        if (data.getShopCredit() >= gift.get().getCreditNeeded()) {
                            e.getWhoClicked().getInventory().addItem(gift.get().getItem());
                            data.setShopCredit(data.getShopCredit() - gift.get().getCreditNeeded());
                            e.getWhoClicked().sendMessage("购买成功！");
                        } else {
                            e.getWhoClicked().sendMessage("您的积分不足以购买此物品");
                        }
                        e.getWhoClicked().closeInventory();
                    }
                }
            }
        }, RankFight.getInstance());
    }
}
