package io.hikarilan.rankfight.utils;

import io.hikarilan.rankfight.RankFight;
import lombok.Getter;

public class Configuration {

    private Configuration() {
    }

    private static final RankFight plugin = RankFight.getPlugin(RankFight.class);
    private static Configuration instance;

    @Getter
    private int creditInterval;

    @Getter
    private int creditWin;

    @Getter
    private long queueTimeoutSec;

    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
            instance.reload();
        }
        return instance;
    }

    public void reload() {
        creditInterval = plugin.getConfig().getInt("CreditInterval");
        creditWin = plugin.getConfig().getInt("CreditWin");
        queueTimeoutSec = plugin.getConfig().getLong("QueueTimeoutSec");
    }
}
