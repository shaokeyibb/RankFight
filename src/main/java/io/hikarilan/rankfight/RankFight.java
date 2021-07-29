package io.hikarilan.rankfight;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.hikarilan.rankfight.beans.ArenaData;
import io.hikarilan.rankfight.beans.ItemGift;
import io.hikarilan.rankfight.beans.PlayerData;
import io.hikarilan.rankfight.commands.RankFightCommand;
import io.hikarilan.rankfight.hook.PAPIHook;
import io.hikarilan.rankfight.serialize.ArenaDataDeserializer;
import io.hikarilan.rankfight.serialize.ItemGiftSerializer;
import io.hikarilan.rankfight.serialize.LocationSerializer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class RankFight extends JavaPlugin {

    @Getter
    private static final Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .setLenient()
            .serializeNulls()
            .setPrettyPrinting()
            .registerTypeAdapter(Location.class, new LocationSerializer())
            .registerTypeAdapter(ArenaData.class, new ArenaDataDeserializer())
            .registerTypeAdapter(ItemGift.class, new ItemGiftSerializer())
            .create();

    @Getter
    private final File playerDataDir = new File(getDataFolder(), "PlayerData");
    @Getter
    private final File arenaDataDir = new File(getDataFolder(), "ArenaData");
    @Getter
    private final File itemGiftDir = new File(getDataFolder(), "GiftData");

    @Getter
    private static RankFight instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        new Metrics(this, 12265);

        saveDefaultConfig();

        try {
            if (!playerDataDir.exists())
                Files.createDirectories(playerDataDir.toPath());
            if (!arenaDataDir.exists())
                Files.createDirectories(arenaDataDir.toPath());
            if (!itemGiftDir.exists())
                Files.createDirectories(itemGiftDir.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArenaData.readAll();
        PlayerData.readAll();
        ItemGift.readAll();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PAPIHook(this).register();
        }

        TabExecutor executor = new RankFightCommand();
        PluginCommand command = Bukkit.getPluginCommand("rankfight");
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        PlayerData.getLastLocation().forEach((playerData, location) -> Bukkit.getPlayer(playerData.getUuid()).teleport(location));
        ArenaData.saveAll();
        PlayerData.saveAll();
        ItemGift.saveAll();
    }
}
