package io.hikarilan.rankfight.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

public class ColorUtils {

    public static String translateChatColor(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> translateChatColor(List<String> text) {
        return text.parallelStream().map(ColorUtils::translateChatColor).collect(Collectors.toList());
    }

}
