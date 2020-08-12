package com.knoban.atlas;

import com.knoban.atlas.data.local.DataHandler;
import com.knoban.atlas.pm.PrivateMessagingManager;
import com.knoban.atlas.scheduler.AnnouncementManager;
import com.knoban.atlas.utils.SoundBundle;
import org.bukkit.Sound;
import org.bukkit.plugin.java.JavaPlugin;

public class Atlas extends JavaPlugin {

    private static Atlas instance;

    private DataHandler.YML config;
    private PrivateMessagingManager privateMessagingManager;

    @Override
    public void onEnable() {
        long tStart = System.currentTimeMillis();
        instance = this;
        super.onEnable();

        config = new DataHandler.YML(this, "/config.yml");
        if(config.getCachedYML().getBoolean("load-pm-commands", true)) {
            privateMessagingManager = new PrivateMessagingManager(this,
                    "§e[§7You §6-> §7%to%§e] §5%msg%",
                    "§e[§7%from% §6-> §7You§e] §5%msg%",
                    new SoundBundle(Sound.ENTITY_WANDERING_TRADER_DISAPPEARED, 1F, 1F),
                    "§e[§7%from% §6-> §7%to%§e] §5%msg%");
        }

        long tEnd = System.currentTimeMillis();
        getLogger().info("Successfully Enabled! (" + (tEnd - tStart) + " ms)");
    }

    @Override
    public void onDisable() {
        long tStart = System.currentTimeMillis();
        super.onDisable();
        long tEnd = System.currentTimeMillis();
        getLogger().info("Successfully Disabled! (" + (tEnd - tStart) + " ms)");
    }

    public static Atlas getInstance() {
        return instance;
    }

    /**
     * Returns an instance referencing config.yml
     */
    public DataHandler.YML getFileConfig() {
        return config;
    }

    public PrivateMessagingManager getPrivateMessagingManager() {
        return privateMessagingManager;
    }
}
