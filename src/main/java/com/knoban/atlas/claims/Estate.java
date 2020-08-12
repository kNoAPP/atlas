package com.knoban.atlas.claims;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Think of this Estate class as a chunk owned by a Landlord.
 * @author Alden Bansemer (kNoAPP)
 */
public class Estate {

    protected UUID owner;
    private UUID worldId;
    private int x, z;

    protected ConcurrentHashMap<UUID, Short> permissions = new ConcurrentHashMap<>();
    private List<Object> data = new ArrayList<>();

    // Transient just means "don't include this member in parsing"
    private transient ReadWriteLock rwlock = new ReentrantReadWriteLock();

    /**
     * TO BE USED BY GSON ONLY! DO NOT USE!
     */
    public Estate() {}

    /**
     * Create a Estate from a chunk.
     * @param chunk The chunk to create the Estate from
     */
    public Estate(@NotNull Chunk chunk) {
        this.worldId = chunk.getWorld().getUID();
        this.x = chunk.getX();
        this.z = chunk.getZ();
    }

    /**
     * Get the owner of the Estate.
     * @return the owner
     */
    @Nullable
    public UUID getOwner() {
        return owner;
    }

    /**
     * Gets all UUIDs of players with some form of permission to the Estate.
     * @return the UUIDs of those players
     */
    @NotNull
    public Set<UUID> getEveryoneWithPermissions() {
        return permissions.keySet();
    }

    /**
     * Gets the raw permission set for a user in this Estate. Permission codes are mapped through bit-wise operations.
     * Each permission is flagged through one of the eight bits in the returned short. (1, 2, 4, 8, 16, 32, etc.).
     * This call is thread-safe.
     *
     * See {@link EstatePermission}.
     *
     * You should only call this method if you understand what you are doing. Otherwise just use {@code hasPermission()}
     * @param uuid The uuid of the player to get the permission of
     * @return The bitwise short flagging permissions
     */
    public short getPermissionSet(@NotNull UUID uuid) {
        return permissions.getOrDefault(uuid, (short) 0);
    }

    /**
     * Checks if a player has the passed permission.
     * This call is thread-safe.
     * @param uuid The uuid of the player to check the permission of.
     * @param permission The permission to check for. See {@link EstatePermission}.
     * @return True, if the player has the permission.
     */
    public boolean hasPermission(@NotNull UUID uuid, short permission) {
        Short permissionCode = permissions.get(uuid);
        if(permissionCode == null)
            return false;

        return (permissionCode & permission) > 0;
    }

    /**
     * Adds a permission to the passed player using {@link EstatePermission}.
     * If the permission is already added, nothing will happen.
     * This call is thread-safe.
     * @param uuid The uuid of the player to add the permission to
     * @param permission The permission to add
     */
    public void addPermission(@NotNull UUID uuid, short permission) {
        Short permissionCode = permissions.getOrDefault(uuid, (short) 0);
        short newPermissions = (short)(permissionCode | permission);

        setPermission(uuid, newPermissions);
    }

    /**
     * Removes a permission to the passed player using {@link EstatePermission}.
     * If the permission is already removed, nothing will happen.
     * This call is thread-safe.
     * @param uuid The uuid of the player to remove the permission from
     * @param permission The permission to remove
     */
    public void removePermission(@NotNull UUID uuid, short permission) {
        Short permissionCode = permissions.getOrDefault(uuid, (short) 0);
        short newPermissions = (short)(permissionCode & ~permission);

        setPermission(uuid, newPermissions);
    }

    /**
     * Sets the raw permission set for a user in this Estate. Permission codes are mapped through bit-wise operations.
     * Each permission is flagged through one of the eight bits in the returned short. (1, 2, 4, 8, 16, 32, etc.).
     * This call is thread-safe.
     *
     * See {@link EstatePermission}.
     *
     * You should only call this method if you understand what you are doing. Otherwise just use {@code addPermission()}
     * @param uuid The uuid of the player to set the permission of
     * @param permission The bitwise short flagging permission
     */
    public void setPermission(@NotNull UUID uuid, short permission) {
        if(permission == 0)
            permissions.remove(uuid);
        else
            permissions.put(uuid, permission);
    }

    /**
     * Gets the id of the {@link World} of this Estate. Commonly used in {@code Bukkit.getWorld(UUID uid)}
     * @return The {@link World}'s id this Estate is located in.
     */
    @NotNull
    public UUID getWorldId() {
        return worldId;
    }

    /**
     * Each Estate is 16x16. This returns the lower-bound x of the Estate.
     * @return The lower-bound x of this Estate's chunk.
     */
    public int getX() {
        return x;
    }

    /**
     * Each Estate is 16x16. This returns the lower-bound z of the Estate.
     * @return The lower-bound z of this Estate's chunk.
     */
    public int getZ() {
        return z;
    }

    /**
     * Get and store additional for this Estate. Entirely for extra data a plugin may need to store.
     * @return The extra data
     */
    @NotNull
    public List<Object> getData() {
        return data;
    }

    /**
     * @return The {@link Chunk} mapped to this Estate or null if the {@link World} is missing or unloaded.
     */
    @Nullable
    public Chunk getChunk() {
        World w = Bukkit.getWorld(worldId);
        if(w == null)
            return null;

        return w.getChunkAt(x, z);
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Estate))
            return false;

        Estate e = (Estate) o;
        return e.x == x && e.z == z && e.worldId.equals(worldId);
    }

    @Override
    public int hashCode() {
        int hash = worldId.hashCode();
        hash = 19 * hash + (int)(Double.doubleToLongBits(this.x) ^ Double.doubleToLongBits(this.x) >>> 32);
        hash = 19 * hash + (int)(Double.doubleToLongBits(this.z) ^ Double.doubleToLongBits(this.z) >>> 32);
        return hash;
    }
}
