package io.hikarilan.rankfight.beans;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import io.hikarilan.rankfight.RankFight;
import lombok.Data;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

@Data
public class ItemGift {

    private static final RankFight plugin = RankFight.getPlugin(RankFight.class);

    @Getter
    private static final Set<ItemGift> data = Sets.newHashSet();

    public static @Nullable ItemGift getItemGiftByItemStack(ItemStack item) {
        return data.parallelStream().filter((itemGift -> itemGift.getItem().equals(item))).findFirst().orElse(null);
    }

    public static @NotNull ItemGift createItemGift(int creditNeeded, ItemStack item) {
        ItemGift gift = new ItemGift(creditNeeded, item);
        data.add(gift);
        return gift;
    }

    public static void readAll() {
        File[] jsons = plugin.getItemGiftDir().listFiles(file -> "json".equalsIgnoreCase(Files.getFileExtension(file.getName())));
        if (jsons == null) return;
        Arrays.stream(jsons)
                .parallel().forEach(file -> {
            try {
                data.add(RankFight.getGson().fromJson(new FileReader(file), ItemGift.class));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    public static void saveAll() {
        data.forEach(itemGift -> {
            String name = itemGift.getItem().getItemMeta().getDisplayName() == null ? itemGift.getItem().getType().name() : itemGift.getItem().getItemMeta().getDisplayName();
            File file = new File(plugin.getItemGiftDir(), name + ".json");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    try (Writer writer = Files.newWriter(file, StandardCharsets.UTF_8)) {
                        RankFight.getGson().toJson(itemGift, writer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                try {
                    try (Writer writer = Files.newWriter(file, StandardCharsets.UTF_8)) {
                        RankFight.getGson().toJson(itemGift, writer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private final int creditNeeded;
    private final ItemStack item;

    private ItemGift(int creditNeeded, ItemStack item) {
        this.creditNeeded = creditNeeded;
        this.item = item;
    }
}
