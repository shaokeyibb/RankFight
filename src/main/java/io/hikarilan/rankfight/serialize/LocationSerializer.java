package io.hikarilan.rankfight.serialize;

import com.google.gson.*;
import org.bukkit.Location;

import java.lang.reflect.Type;

public class LocationSerializer implements JsonSerializer<Location> {

    @Override
    public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.add("world", new JsonPrimitive(src.getWorld().getUID().toString()));
        object.add("x", new JsonPrimitive(src.getBlockX()));
        object.add("y", new JsonPrimitive(src.getBlockY()));
        object.add("z", new JsonPrimitive(src.getBlockZ()));
        return object;
    }
}
