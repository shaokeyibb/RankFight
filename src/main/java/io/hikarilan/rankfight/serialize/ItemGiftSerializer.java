package io.hikarilan.rankfight.serialize;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.hikarilan.rankfight.RankFight;
import io.hikarilan.rankfight.beans.ItemGift;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.Map;

public class ItemGiftSerializer implements JsonDeserializer<ItemGift>, JsonSerializer<ItemGift> {
    @Override
    public ItemGift deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject object = json.getAsJsonObject();

        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        JsonObject itemStackRaw = object.getAsJsonObject("item");

        return ItemGift.createItemGift(object.getAsJsonPrimitive("creditNeeded").getAsInt(),
                ItemStack.deserialize(RankFight.getGson().fromJson(itemStackRaw, type)));
    }

    @Override
    public JsonElement serialize(ItemGift src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.add("creditNeeded", new JsonPrimitive(src.getCreditNeeded()));
        object.add("item", new GsonBuilder().create().toJsonTree(src.getItem().serialize()));
        return object;
    }
}
