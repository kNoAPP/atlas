package com.knoban.atlas.missions.impl;

import com.google.firebase.database.DatabaseReference;
import com.knoban.atlas.missions.Mission;
import com.knoban.atlas.missions.MissionInfo;
import com.knoban.atlas.missions.bossbar.BossBarAnimationHandler;
import com.knoban.atlas.missions.bossbar.BossBarConstant;
import com.knoban.atlas.utils.Tools;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@MissionInfo(name = "block-break")
public class BlockBreakMission extends Mission {

    public BlockBreakMission(@NotNull Plugin plugin, @NotNull BossBarAnimationHandler animationHandler,
                             @NotNull DatabaseReference reference, @NotNull String uuid,
                             @NotNull Map<String, Object> missionData) {
        super(plugin, animationHandler, reference, uuid, missionData);
        String materialName = (String) extraData.getOrDefault("material", "DIAMOND_ORE");
        this.material = Material.getMaterial(materialName);
        if(material == null)
            throw new IllegalArgumentException("Invalid material: " + materialName);
        this.display = "§7Break §b" + maxProgress + " " + Tools.enumNameToHumanReadable(material.name());
        this.description = new String[]{"§aFind and destroy " + "§a" + Tools.enumNameToHumanReadable(material.name())};

        bossBarInformation.setTitle(display + " §7(§e" + BossBarConstant.PROGRESS_LEFT + " left§7)");
        bossBarInformation.setStyle(BarStyle.SEGMENTED_6);
        bossBarInformation.setColor(BarColor.WHITE);
        bossBarInformation.setFlags(); // No flags
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if(e.getBlock().getType().equals(material))
            incrementProgress(p, 1L);
    }
}
