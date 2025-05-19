package com.knoban.atlas.missions;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.knoban.atlas.missions.bossbar.BossBarAnimationHandler;
import com.knoban.atlas.missions.bossbar.BossBarAnimationHandler.BossBarInformation;
import com.knoban.atlas.missions.bossbar.BossBarConstant;
import com.knoban.atlas.rewards.Reward;
import com.knoban.atlas.utils.Tools;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a mission that can be created by server administrators and completed by {@link Player}s for
 * {@link Reward}s. This class works closely with {@link MissionManager}. Extend this class in order to create
 * new missions. Be sure to include a {@link MissionInfo} annotation at the top of your extended class containing
 * the name of the mission type. Additionally, register your mission to the {@link Missions} class so that it is
 * visable in other parts of the code.
 * @author Alden Bansemer (kNoAPP)
 */
public abstract class Mission implements Listener, Comparable<Mission> {

    protected final Plugin plugin;
    protected final BossBarAnimationHandler animationHandler;

    protected final MissionInfo info = getClass().getAnnotation(MissionInfo.class);
    protected final String uuid;
    protected final Map<String, Object> extraData;
    private final DatabaseReference reference;
    private final ValueEventListener listener;

    protected boolean active;
    protected long maxProgress;
    protected long startTime, runTime, endTime;
    protected ItemStack missionItem;

    protected String display;
    protected String[] description;
    protected Material material;
    protected BossBarInformation bossBarInformation;

    protected Reward reward;

    protected final ConcurrentHashMap<UUID, Long> progressMap = new ConcurrentHashMap<>();

    /**
     * Should only be called by {@link MissionManager} to create missions.
     * @param plugin An instance of the main plugin class
     * @param uuid The unique id of the mission
     * @param missionData A json-like map of data for this mission
     */
    protected Mission(@NotNull Plugin plugin, @NotNull BossBarAnimationHandler animationHandler,
                      @NotNull DatabaseReference reference, @NotNull String uuid,
                      @NotNull Map<String, Object> missionData) {
        this.plugin = plugin;
        this.animationHandler = animationHandler;
        this.uuid = uuid;
        this.reference = reference;
        this.listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                UUID uuid = UUID.fromString(snapshot.getKey());

                Long value = snapshot.getValue(Long.class);
                Long newValue = value != null ? value : 0L;

                /**
                 * What if the same player is on two servers concurrently connected to the same firebase instance?
                 * They both try to complete the same mission. Thanks to realtime database alerts, one will complete
                 * first and notify the other. So in this case, we don't want to call setProgress because it could
                 * duplicate the rewards. We want to award the player on one server, and lock the mission on the others.
                 *
                 * But, if I manually enter a number into the database lower than the max mission progress, then we
                 * do want to notify the player with the setProgress method. Since we don't alert other instances on
                 * partial progress, its safe to assume seeing partial progress alerts means someone manually entered
                 * that data into the database.
                 */
                Player p = Bukkit.getPlayer(uuid);
                if(p != null) // If they aren't online, don't bother.
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        if(newValue >= maxProgress)
                            progressMap.put(uuid, newValue);
                        else
                            setProgress(p, newValue);
                    });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                plugin.getLogger().warning("Problem with player mission listener (" + uuid + "): " + error.getMessage());
            }
        };

        this.maxProgress = (long) missionData.getOrDefault("maxProgress", 1L);
        if(maxProgress <= 0) // You just know someone's going to do it.
            maxProgress = 1;
        this.startTime = (long) missionData.getOrDefault("startTime", 0L);
        this.runTime = (long) missionData.getOrDefault("runTime", 0L);
        this.endTime = startTime + runTime;
        this.extraData = (Map<String, Object>) missionData.getOrDefault("extraData", new TreeMap<>());

        //Defaults
        this.display = "§7Undefined";
        this.material = Material.BARRIER;
        this.description = new String[]{"§9No description."};
        this.bossBarInformation = BossBarConstant.getDefaultBossBar();

        Map<String, Object> rewardData = (Map<String, Object>) missionData.get("reward");
        if(rewardData != null) {
            try {
                this.reward = Reward.fromData(rewardData);
            } catch(Exception e) {
                plugin.getLogger().warning("Failed to parse mission reward (" + uuid + "): " + e.getMessage());
                plugin.getLogger().warning("The mission will continue without a reward.");
            }
        }
    }

    /**
     * Recreates all ItemStacks and caches them for later calls to getters.
     */
    public void cacheItemStack() {
        missionItem = new ItemStack(getMaterial());
        ItemMeta im = missionItem.getItemMeta();
        im.setDisplayName(getDisplay());
        List<String> lore = new ArrayList<>(Arrays.asList(getDescription()));
        im.setLore(lore);
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_UNBREAKABLE);
        missionItem.setItemMeta(im);
    }

    /**
     * @return The display String for the mission. Like a title.
     */
    @NotNull
    public String getDisplay() {
        return display;
    }

    /**
     * @return The material of the icon {@link ItemStack} for this mission.
     */
    @NotNull
    public Material getMaterial() {
        return material;
    }

    /**
     * @return The description of the mission broken up into Strings that can fit on an {@link ItemStack} lore.
     */
    @NotNull
    public String[] getDescription() {
        return description;
    }

    /**
     * @return The boss bar settings and style for this mission. Can be modified.
     */
    @NotNull
    public BossBarAnimationHandler.BossBarInformation getBossBarInformation() {
        return bossBarInformation;
    }

    /**
     * @return A clone of the mission item {@link ItemStack}.
     */
    @NotNull
    public ItemStack getMissionItem() {
        if(missionItem == null)
            cacheItemStack();

        return missionItem.clone();
    }

    /**
     * Creates an {@link ItemStack} with a lore containing details about the progress of the player's UUID.
     * If none are available, this call will default to the same output as {@link #getMissionItem()}.
     * @param uuid The UUID of the player
     * @return The {@link ItemStack} specific to them
     */
    @NotNull
    public ItemStack getMissionItem(UUID uuid) {
        ItemStack missionItem = getMissionItem();
        Long progress = progressMap.get(uuid);
        if(progress == null)
            return missionItem;

        ItemMeta im = missionItem.getItemMeta();

        List<String> lore = im.getLore();
        lore.add("");
        lore.add(progress < maxProgress ? "§e" + progress + " §f/ §6" + maxProgress : "§aComplete!");
        lore.add("§9" + Tools.millisToDHMSWithSpacing(getTimeLeft()));
        im.setLore(lore);
        missionItem.setItemMeta(im);

        return missionItem;
    }

    /**
     * @return The unique id of the mission. Can be used to lookup mission data in the database.
     */
    @NotNull
    public String getUuid() {
        return uuid;
    }

    /**
     * @return A whole number greater than 0 representing the amount of progress needed to complete the mission.
     */
    public long getMaxProgress() {
        return maxProgress;
    }

    /**
     * @return Milliseconds since the epoch. Represents a time at which this mission starts.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @return Milliseconds. Represents the length of this mission's runtime.
     */
    public long getRunTime() {
        return runTime;
    }

    public long getTimeLeft() {
        long currentTime = System.currentTimeMillis();
        if(endTime <= currentTime)
            return 0;

        if(currentTime <= startTime)
            return runTime;

        return endTime - currentTime;
    }

    /**
     * @return Milliseconds since the epoch. Represents a time at which this mission ends.
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * @return An immutable map of extra data specific to this mission. Can be full of data or completely empty.
     */
    @NotNull
    public Map<String, Object> getExtraData() {
        return Collections.unmodifiableMap(extraData);
    }

    /**
     * @return The reward for completing the mission. Null if there's no reward.
     */
    @Nullable
    public Reward getReward() {
        return reward;
    }

    /**
     * @return Additional static information about the mission class.
     */
    @NotNull
    public MissionInfo getInfo() {
        return info;
    }

    /**
     * Sets the progress of a {@link Player} in completing this mission. When the progress matches or exceeds the
     * {@link #getMaxProgress()} when it previously didn't, the {@link Player} is rewarded with {@link #getReward()}.
     * <br><br>
     * If a {@link Player} completes a mission, but then their progress is reset to an amount below the
     * {@link #getMaxProgress()} amount, they have the potential to earn duplicate rewards.
     * @param p The {@link Player} to set the progress of.
     * @param newProgress The new progress towards completing the mission.
     */
    public void setProgress(@NotNull Player p, long newProgress) {
        Long oldProgress = progressMap.get(p.getUniqueId());
        progressMap.put(p.getUniqueId(), newProgress);
        if(oldProgress == null)
            oldProgress = 0L;
        if(oldProgress >= maxProgress)
            return;

        incrementProgressAnimation(p, oldProgress, newProgress);
        if(newProgress >= maxProgress) {
            // Mission complete!
            reference.child(p.getUniqueId().toString()).setValue(newProgress, (error, ref) -> { // Save data
                if(error != null)
                    plugin.getLogger().warning("Problem with player mission data (" + uuid + "): " + error.getMessage());
            });

            p.sendMessage("§dCongratulations! §7You've completed a mission.");
            p.sendMessage(getDisplay());

            reward.reward(p);
        }
    }

    /**
     * Increments the progress of a {@link Player} in completing this mission. When the progress matches or exceeds the
     * {@link #getMaxProgress()} when it previously didn't, the {@link Player} is rewarded with {@link #getReward()}.
     * <br><br>
     * If a {@link Player} completes a mission, but then their progress is incremented to an amount below the
     * {@link #getMaxProgress()} amount, they have the potential to earn duplicate rewards.
     * @param p The {@link Player} to set the progress of.
     * @param amt The incremented progress towards completing the mission.
     */
    public void incrementProgress(@NotNull Player p, long amt) {
        Long oldProgress = progressMap.get(p.getUniqueId());
        if(oldProgress == null || oldProgress >= maxProgress) // Can't and shouldn't increment missing data or completed missions.
            return;
        Long newProgress = oldProgress + amt;

        progressMap.put(p.getUniqueId(), newProgress);

        incrementProgressAnimation(p, oldProgress, newProgress);
        if(newProgress >= maxProgress) {
            // Mission complete!
            reference.child(p.getUniqueId().toString()).setValue(newProgress, (error, ref) -> { // Save data
                if(error != null)
                    plugin.getLogger().warning("Problem with player mission data (" + uuid + "): " + error.getMessage());
            });

            p.sendMessage("§dCongratulations! §7You've completed a mission.");
            p.sendMessage(getDisplay());

            reward.reward(p);
        }
    }

    /**
     * Gets the progress of a {@link Player} in completing this mission. This will return null if the {@link Player} is
     * offline or their data has not yet loaded.
     * @param p The {@link Player} to get the progress of.
     * @return A Long representing the amount of progress the player has made in this mission. Null if the data is unavailable.
     */
    @Nullable
    public Long getProgress(@NotNull Player p) {
        return progressMap.get(p.getUniqueId());
    }

    /**
     * Helper thread to handle boss bar animation and grant rewards.
     */
    private void incrementProgressAnimation(@NotNull Player p, long oldValue, long newValue) {
        animationHandler.playAnimation(p, this.uuid, oldValue, newValue, maxProgress);
    }

    /**
     * Register's a {@link Player}'s unique id to the database. This will allow the server to listen to the
     * database for changes to data regarding the {@link Player}.
     * <br><br>
     * Always be sure to call {@link #unregisterPlayerDataListener(UUID)} later to avoid listener leaks leading to
     * potential $$$ overcharges.
     * @param uuid The unique id of the {@link Player}
     */
    protected void registerPlayerDataListener(@NotNull UUID uuid) {
        if(!active)
            return;

        reference.child(uuid.toString()).addValueEventListener(listener);
    }

    /**
     * Unregister's a {@link Player}'s unique id to the database. This tells the server to stop listening to the
     * database for changes to data regarding the {@link Player}.
     * <br><br>
     * Always be sure to call this after calling {@link #registerPlayerDataListener(UUID)} to avoid listener leaks
     * leading to potential $$$ overcharges.
     * @param uuid The unique id of the {@link Player}
     */
    protected void unregisterPlayerDataListener(@NotNull UUID uuid) {
        if(!active)
            return;

        reference.child(uuid.toString()).removeEventListener(listener);
        Long value = progressMap.remove(uuid);
        if(value == null) // They joined and left super quickly such that their data didn't fully load.
            return;
        reference.child(uuid.toString()).setValue(value, (error, ref) -> { // Save data
            if(error != null)
                plugin.getLogger().warning("Problem with player mission data (" + uuid + "): " + error.getMessage());
        });
    }

    /**
     * Should only be called from {@link MissionManager}. Sets this mission as active or inactive and will
     * register/unregister data listeners, event listeners, and pointers.
     * @param active True, if the mission is active and can be completed by players.
     */
    protected void setActive(boolean active) {
        if(this.active == active)
            return;

        if(active) {
            this.active = true;
            for(Player pl : Bukkit.getOnlinePlayers())
                registerPlayerDataListener(pl.getUniqueId());

            animationHandler.setSettings(uuid, bossBarInformation);
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        } else {
            HandlerList.unregisterAll(this);
            for(Player pl : Bukkit.getOnlinePlayers())
                unregisterPlayerDataListener(pl.getUniqueId());

            animationHandler.removeSettings(uuid);
            this.active = false;
        }
    }

    /**
     * @return True, if the mission is active and can be completed by players.
     */
    public boolean isActive() {
        return active;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Mission))
            return false;
        Mission other = (Mission) obj;
        return uuid.equals(other.uuid);
    }

    @Override
    public String toString() {
        return uuid + ": {active:" + active + ",type:" + info.name() + ",start:" + startTime + ",end:" + endTime + "}";
    }

    @Override
    public int compareTo(@NotNull Mission o) {
        return Long.compare(getTimeLeft(), o.getTimeLeft());
    }
}