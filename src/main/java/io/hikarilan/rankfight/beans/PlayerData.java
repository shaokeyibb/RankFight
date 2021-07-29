package io.hikarilan.rankfight.beans;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import io.hikarilan.rankfight.RankFight;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class PlayerData {

    private static final RankFight plugin = RankFight.getPlugin(RankFight.class);

    @Getter
    private static final List<PlayerData> data = Lists.newArrayList();

    @Getter
    private static final Map<PlayerData, Location> lastLocation = Maps.newHashMap();

    public static @NotNull PlayerData getPlayerDataByUUID(@NotNull UUID playerUUID) {
        return data.parallelStream().filter(playerData -> playerData.getUuid().equals(playerUUID)).findFirst().orElseGet(() -> {
            PlayerData other = new PlayerData(playerUUID);
            data.add(other);
            return other;
        });
    }

    public static void readAll() {
        File[] jsons = plugin.getPlayerDataDir().listFiles(file -> "json".equalsIgnoreCase(Files.getFileExtension(file.getName())));
        if (jsons == null) return;
        Arrays.stream(jsons)
                .parallel().forEach(file -> {
            try {
                data.add(RankFight.getGson().fromJson(new FileReader(file), PlayerData.class));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    public static void saveAll() {
        data.forEach(playerData -> {
            File file = new File(plugin.getPlayerDataDir(), playerData.getUuid().toString() + ".json");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    try (Writer writer = Files.newWriter(file, StandardCharsets.UTF_8)) {
                        RankFight.getGson().toJson(playerData, writer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                try {
                    try (Writer writer = Files.newWriter(file, StandardCharsets.UTF_8)) {
                        RankFight.getGson().toJson(playerData, writer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private final UUID uuid;

    private int credit;
    private int shopCredit;

    private PlayerData(UUID uuid) {
        this.uuid = uuid;
        data.add(this);
    }

    public int getRank() {
        return PlayerData.getData().parallelStream()
                .sorted(Comparator.comparingInt(PlayerData::getCredit).reversed())
                .collect(Collectors.toList())
                .indexOf(this) + 1;
    }
}
