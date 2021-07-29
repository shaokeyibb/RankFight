package io.hikarilan.rankfight.serialize;

import com.google.gson.*;
import io.hikarilan.rankfight.beans.ArenaData;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.reflect.Type;
import java.util.UUID;

public class ArenaDataDeserializer implements JsonDeserializer<ArenaData> {

    @Override
    public ArenaData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject object = json.getAsJsonObject();

        JsonObject playerLocationRaw = object.getAsJsonObject("playerLocation");
        JsonObject otherPlayerLocationRaw = object.getAsJsonObject("otherPlayerLocation");

        return ArenaData.createArenaData(object.getAsJsonPrimitive("name").getAsString(),
                new Location(Bukkit.getWorld(UUID.fromString(playerLocationRaw.getAsJsonPrimitive("world").getAsString())),
                        playerLocationRaw.getAsJsonPrimitive("x").getAsInt(),
                        playerLocationRaw.getAsJsonPrimitive("y").getAsInt(),
                        playerLocationRaw.getAsJsonPrimitive("z").getAsInt()),
                new Location(Bukkit.getWorld(UUID.fromString(otherPlayerLocationRaw.getAsJsonPrimitive("world").getAsString())),
                        otherPlayerLocationRaw.getAsJsonPrimitive("x").getAsInt(),
                        otherPlayerLocationRaw.getAsJsonPrimitive("y").getAsInt(),
                        otherPlayerLocationRaw.getAsJsonPrimitive("z").getAsInt()));
    }
}
