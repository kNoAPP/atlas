package com.knoban.atlas.missions.impl;

import com.google.firebase.database.DatabaseReference;
import com.knoban.atlas.missions.Mission;
import com.knoban.atlas.missions.MissionInfo;
import com.knoban.atlas.missions.bossbar.BossBarAnimationHandler;
import com.knoban.atlas.missions.bossbar.BossBarConstant;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Map;

@MissionInfo(name = "fish")
public class FishMission extends Mission {

    private static EnumSet<Material> FISH = EnumSet.of(
            Material.TROPICAL_FISH, Material.PUFFERFISH, Material.COD, Material.SALMON
    );

    public FishMission(@NotNull Plugin plugin, @NotNull BossBarAnimationHandler animationHandler,
                       @NotNull DatabaseReference reference, @NotNull String uuid,
                       @NotNull Map<String, Object> missionData) {
        super(plugin, animationHandler, reference, uuid, missionData);
        this.display = "§bCatch " + maxProgress + " Fish";
        this.material = Material.COD;
        this.description = new String[]{"§9Use a fishing pole and", "§9catch some fish."};

        bossBarInformation.setTitle(display + " §7(§c" + BossBarConstant.PROGRESS_LEFT + " left§7)");
        bossBarInformation.setStyle(BarStyle.SOLID);
        bossBarInformation.setColor(BarColor.BLUE);
        bossBarInformation.setFlags(); // No flags
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFish(PlayerFishEvent e) {
        if(e.getState() == PlayerFishEvent.State.CAUGHT_FISH && e.getCaught() != null) {
            Item item = (Item) e.getCaught();
            if(FISH.contains(item.getItemStack().getType())) {
                Player p = e.getPlayer();
                incrementProgress(p, 1L);
            }
        }
    }
}
