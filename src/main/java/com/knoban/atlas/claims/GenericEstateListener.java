package com.knoban.atlas.claims;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Campfire;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Grindstone;
import org.bukkit.block.data.type.TurtleEgg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;


/**
 * Provides a generic EventHandler for Estates that utilizes the default EstatePermissions.
 *
 * @author Alden Bansemer (kNoAPP)
 */
public class GenericEstateListener implements Listener {

    private JavaPlugin plugin;
    private LandManager lm;

    /**
     * Creates a generic Estate Listener to handle basic permissions and events. These include permission to
     * access, inventory, break, and place inside an Estate. Further customization should be done outside of
     * this class.
     * @param plugin The plugin instance that is registering this listener
     * @param lm The LandManager to track the data of
     */
    public GenericEstateListener(@NotNull JavaPlugin plugin, @NotNull LandManager lm) {
        this.plugin = plugin;
        this.lm = lm;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Shuts down the EventHandler for this class rendering it pretty much useless. Call this if you want to clean
     * up and remove this instance.
     */
    public void unregister() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        Chunk chunk = b.getChunk();

        Estate estate = lm.getEstate(chunk);
        if(estate != null) {
            Player p = e.getPlayer();
            if(!estate.hasPermission(p.getUniqueId(), EstatePermission.BREAK.getCode())) {
                e.setCancelled(true);
                if(estate.getOwner() != null) {
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(estate.getOwner());
                    p.sendMessage("§cYou need §4" + owner.getName() + "'s §cpermission to do that.");
                } else
                    p.sendMessage("§cYou need permission to do that.");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Block b = e.getBlock();
        Chunk chunk = b.getChunk();

        Estate estate = lm.getEstate(chunk);
        if(estate != null) {
            Player p = e.getPlayer();
            if(!estate.hasPermission(p.getUniqueId(), EstatePermission.PLACE.getCode())) {
                e.setCancelled(true);
                if(estate.getOwner() != null) {
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(estate.getOwner());
                    p.sendMessage("§cYou need §4" + owner.getName() + "'s §cpermission to do that.");
                } else
                    p.sendMessage("§cYou need permission to do that.");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockClick(PlayerInteractEvent e) {
        Block b = e.getClickedBlock();
        if(b != null) {
            Chunk chunk = b.getChunk();

            Estate estate = lm.getEstate(chunk);
            if(estate != null) {
                Player p = e.getPlayer();
                if(b.getState() instanceof Container) {
                    if(!estate.hasPermission(p.getUniqueId(), EstatePermission.INVENTORY.getCode())) {
                        e.setCancelled(true);
                        if(estate.getOwner() != null) {
                            OfflinePlayer owner = Bukkit.getOfflinePlayer(estate.getOwner());
                            p.sendMessage("§cYou need §4" + owner.getName() + "'s §cpermission to do that.");
                        } else
                            p.sendMessage("§cYou need permission to do that.");
                    }
                } else if(!estate.hasPermission(p.getUniqueId(), EstatePermission.ACCESS.getCode())) {
                    BlockData bd = b.getState().getBlockData();
                    if(e.getAction() == Action.PHYSICAL || bd instanceof Openable || bd instanceof Powerable ||
                            bd instanceof Grindstone || bd instanceof TurtleEgg || bd instanceof Campfire) {
                        e.setCancelled(true);
                        if(estate.getOwner() != null) {
                            OfflinePlayer owner = Bukkit.getOfflinePlayer(estate.getOwner());
                            p.sendMessage("§cYou need §4" + owner.getName() + "'s §cpermission to do that.");
                        } else
                            p.sendMessage("§cYou need permission to do that.");
                    }
                }
            }
        }
    }
}
