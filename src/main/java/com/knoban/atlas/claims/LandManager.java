package com.knoban.atlas.claims;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Think of this LandManager class as a collection of Land (where each Land has one World)
 * @author Alden Bansemer (kNoAPP)
 */
public class LandManager implements Listener {

    private JavaPlugin plugin;

    protected HashMap<UUID, Landlord> landlords = new HashMap<>();
    protected HashMap<UUID, Land> landByWorld = new HashMap<>();

    private LandManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Get a {@link Landlord} from a player's uuid. Landlords hold all land-claim related data on a player.
     * @param uuid The uuid of the player to get the {@link Landlord}
     * @return The {@link Landlord} of a player
     */
    @NotNull
    public Landlord getLandlord(@NotNull UUID uuid) {
        Landlord landlord = landlords.get(uuid);
        if(landlord == null) {
            landlord = new Landlord(uuid);
            landlords.put(uuid, landlord);
        }
        return landlord;
    }

    /**
     * Gets data regarding land claims in a {@link Chunk}. Returns an value which is null if the {@link Chunk} in
     * question hasn't been claimed.
     * @param chunk The {@link Chunk} to get the claim data of
     * @return An {@link Estate} containing claim data (or null if no data is present)
     */
    @Nullable
    public Estate getEstate(@NotNull Chunk chunk) {
        Land land = landByWorld.get(chunk.getWorld().getUID());
        if(land == null)
            return null;

        return land.allEstates.get(chunk.getChunkKey());
    }

    /**
     * Sets a {@link Chunk} as property of a player. Optionally also wiping all previous {@link Estate} permissions. If
     * a null owner uuid is passed, this will instead act as if you called {@code removeEstate(Chunk)}.
     * @param chunk The {@link Chunk} to add a owner to
     * @param owner The owner's uuid
     * @param wipeAllPermissions If true, existing permissions for the {@link Estate} will be wiped
     */
    public void setEstate(@NotNull Chunk chunk, @Nullable UUID owner, boolean wipeAllPermissions) {
        if(owner == null)
            removeEstate(chunk);

        Estate estate = getEstate(chunk);
        if(estate != null) {
            removeEstateFromLandlord(estate);
            if(wipeAllPermissions)
                estate.permissions.clear();

            addEstateToLandlord(estate, owner);
        } else {
            estate = new Estate(chunk);
            Land land = landByWorld.get(chunk.getWorld().getUID());
            land.allEstates.put(chunk.getChunkKey(), estate);

            addEstateToLandlord(estate, owner);
        }
    }

    /**
     * Removes all data and ownership of an {@link Estate} from a passed {@link Chunk}.
     * @param chunk The {@link Chunk} to remove data from
     */
    public void removeEstate(@NotNull Chunk chunk) {
        Estate estate = getEstate(chunk);
        if(estate != null) {
            Land land = landByWorld.get(chunk.getWorld().getUID());
            land.allEstates.remove(chunk.getChunkKey());

            removeEstateFromLandlord(estate);
        }
    }

    /**
     * A helper method to update a Landlord's owned {@link Estate}s
     */
    private void removeEstateFromLandlord(@NotNull Estate estate) {
        if(estate.owner != null) {
            Landlord landlord = getLandlord(estate.owner);
            landlord.ownedEstates.remove(estate);
            estate.permissions.remove(estate.owner);
        }
        estate.owner = null;
    }

    /**
     * A helper method to update a Landlord's owned {@link Estate}s
     */
    private void addEstateToLandlord(@NotNull Estate estate, @Nullable UUID owner) {
        estate.owner = owner;
        if(estate.owner != null) {
            Landlord landlord = getLandlord(estate.owner);
            landlord.ownedEstates.add(estate);
            estate.permissions.put(estate.owner, EstatePermission.FULL.getCode());
        }
    }

    /**
     * Shows the entire chunk as a bunch of specific blocks at the same Y value as the first Player passed. These blocks
     * revert back to normal when the ticks expire. Send Block packets and does not actually modify the world.
     * @param chunk The chunk to show as specific blocks
     * @param selection The type of block to use to show the Chunk
     * @param ticks The amount of time (in ticks) to show the specific blocks for
     * @param showForPlayers The players to show the specific blocks to. The first player passed will have the specific
     * blocks placed at their Y value
     */
    public void flashBorder(@NotNull Chunk chunk, Material selection, long ticks, @NotNull Player... showForPlayers) {
        if(showForPlayers.length <= 0)
            return;

        List<Block> toRefresh = new ArrayList<>();
        int y = showForPlayers[0].getLocation().getBlockY() - 1;
        for(int i=0; i<16; i++) {
            for(int j=0; j<16; j+=15) {
                Block b = chunk.getBlock(i, y, j);
                int ops = 3; // This ensures we don't lag the server with too many operations (increase: expand search).
                while(b.getType() == Material.AIR && --ops >= 0) {
                    b = b.getRelative(BlockFace.DOWN);
                }
                while(b.getRelative(BlockFace.UP).getType() != Material.AIR && --ops >= 0) {
                    b = b.getRelative(BlockFace.UP);
                }
                if(ops >= 0) {
                    toRefresh.add(b);
                    for(Player pl : showForPlayers) {
                        pl.sendBlockChange(b.getLocation(), selection.createBlockData());
                    }
                }
            }
        }

        for(int j=1; j<15; j++) {
            for(int i=0; i<16; i+=15) {
                Block b = chunk.getBlock(i, y, j);
                int ops = 3; // This ensures we don't lag the server with too many operations (increase: expand search).
                while(b.getType() == Material.AIR && --ops >= 0) {
                    b = b.getRelative(BlockFace.DOWN);
                }
                while(b.getRelative(BlockFace.UP).getType() != Material.AIR && --ops >= 0) {
                    b = b.getRelative(BlockFace.UP);
                }
                if(ops >= 0) {
                    toRefresh.add(b);
                    for(Player pl : showForPlayers) {
                        pl.sendBlockChange(b.getLocation(), selection.createBlockData());
                    }
                }
            }
        }

        new BukkitRunnable() {
            public void run() {
                for(Block b : toRefresh) {
                    for(Player pl : showForPlayers) {
                        if(pl.isOnline())
                            pl.sendBlockChange(b.getLocation(), b.getBlockData());
                    }
                }
            }
        }.runTaskLater(plugin, ticks);
    }

    /**
     * Save all known {@link Estate} data to a passed data folder. The folder should already exist. This
     * will overwrite files that contain duplicate data.
     * @param dataFolder A folder to which the data should be saved
     */
    public void saveLandManager(@NotNull File dataFolder) {
        for(Landlord landlord : landlords.values()) {
            File file = new File(dataFolder, landlord.getUUID() + ".json");
            saveJSON(file, landlord);
        }
    }

    /**
     * A helper method to parse JSON from a file.
     */
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    @NotNull
    private <T> T getJSON(@NotNull File file, Class<T> clazz) {
        String cached = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            for(String line; (line = br.readLine()) != null;)
                sb.append(line);
            br.close();
            cached = sb.toString();
        } catch(IOException e) {
            plugin.getServer().getConsoleSender().sendMessage("§e[" + plugin.getName() +
                    "] Failed to read file (" + file.getName() + "): " + e.getMessage());
        }

        return gson.fromJson(cached, clazz);
    }

    /**
     * A helper method to parse JSON into a file.
     */
    private void saveJSON(@NotNull File file, @Nullable Object obj) {
        String cached = gson.toJson(obj);
        try {
            org.apache.commons.io.FileUtils.writeStringToFile(file, cached, StandardCharsets.UTF_8);
        } catch(IOException e) {
            plugin.getLogger().warning("Failed to write to file (" + file.getName() + "): " + e.getMessage());
        }
    }

    /**
     * Create a {@link LandManager} from a data folder. {@link LandManager}s should be used as singletons and contain
     * claimed land data for players. The passed folder should already exist.
     * @param plugin The plugin instance creating the {@link LandManager}
     * @param dataFolder The folder to import the data from
     * @return A {@link LandManager} containing all claims data
     */
    @NotNull
    public static LandManager createLandManager(@NotNull JavaPlugin plugin, @NotNull File dataFolder) {
        LandManager lm = new LandManager(plugin);

        for(World world : Bukkit.getWorlds()) {
            Land land = new Land();
            lm.landByWorld.put(world.getUID(), land);
        }

        for(File f : Objects.requireNonNull(dataFolder.listFiles())) {
            if(FilenameUtils.getExtension(f.getName()).equalsIgnoreCase("json")) {
                try {
                    Landlord landlord = lm.getJSON(f, Landlord.class);
                    lm.landlords.put(landlord.getUUID(), landlord);

                    for(Estate e : landlord.getOwnedEstates()) {
                        Land land = lm.landByWorld.get(e.getWorldId());
                        if(land != null) {
                            land.allEstates.put(Chunk.getChunkKey(e.getX(), e.getZ()), e);
                        }
                    }
                } catch(JsonSyntaxException e) {
                    plugin.getLogger().warning("Failed to parse Landlord (" + f.getName() + "): " + e.getMessage());
                }
            }
        }

        return lm;
    }
}
