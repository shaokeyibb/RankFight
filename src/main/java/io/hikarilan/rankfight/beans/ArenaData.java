package io.hikarilan.rankfight.beans;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import io.hikarilan.rankfight.RankFight;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;

@Data
public class ArenaData {

    private static final RankFight plugin = RankFight.getPlugin(RankFight.class);

    @Getter
    private static final Set<ArenaData> data = Sets.newHashSet();

    public static void readAll() {
        File[] jsons = plugin.getArenaDataDir().listFiles(file -> "json".equalsIgnoreCase(Files.getFileExtension(file.getName())));
        if (jsons == null) return;
        Arrays.stream(jsons)
                .parallel().forEach(file -> {
            try {
                data.add(plugin.getGson().fromJson(new FileReader(file), ArenaData.class));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    public static void saveAll() {
        data.forEach(arenaData -> {
            File file = new File(plugin.getArenaDataDir(), arenaData.getName() + ".json");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    try (Writer writer = Files.newWriter(file, StandardCharsets.UTF_8)) {
                        RankFight.getGson().toJson(arenaData, writer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    try (Writer writer = Files.newWriter(file, StandardCharsets.UTF_8)) {
                        RankFight.getGson().toJson(arenaData, writer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static @NotNull ArenaData createArenaData(String name, Location loc1, Location loc2) {
        ArenaData arena = new ArenaData(name, loc1, loc2);
        data.add(arena);
        return arena;
    }

    public static @Nullable ArenaData getArenaDataByName(String name) {
        return data.parallelStream().filter(arenaData -> arenaData.name.equals(name)).findFirst().orElse(null);
    }

    private final String name;
    private final Location playerLocation;
    private final Location otherPlayerLocation;

    private ArenaData(@NotNull String name, @NotNull Location playerLocation, @NotNull Location otherPlayerLocation) {
        this.name = name;
        this.playerLocation = playerLocation;
        this.otherPlayerLocation = otherPlayerLocation;
    }
}
