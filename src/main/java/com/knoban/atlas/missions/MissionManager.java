package com.knoban.atlas.missions;

import com.google.firebase.database.*;
import com.knoban.atlas.data.firebase.AtlasFirebase;
import com.knoban.atlas.gui.GUI;
import com.knoban.atlas.missions.bossbar.BossBarAnimationHandler;
import com.knoban.atlas.rewards.Reward;
import com.knoban.atlas.rewards.Rewards;
import com.knoban.atlas.scheduler.ClockedTask;
import com.knoban.atlas.scheduler.ClockedTaskManager;
import com.knoban.atlas.utils.Items;
import com.knoban.atlas.utils.SoundBundle;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MissionManager implements Listener {

    private final Plugin plugin;
    private final AtlasFirebase firebase;
    private final String missionDatabasePath;
    private final BossBarAnimationHandler animationHandler;

    private final DatabaseReference missionsTimelineReference;
    private final ChildEventListener listener;

    private final ConcurrentHashMap<String, Mission> allMissions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Mission> activeMissions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ClockedTask> missionTasks = new ConcurrentHashMap<>();

    public MissionManager(Plugin plugin, AtlasFirebase firebase, String missionDatabasePath) {
        this.plugin = plugin;
        this.firebase = firebase;
        this.missionDatabasePath = missionDatabasePath;
        this.animationHandler = new BossBarAnimationHandler(plugin);

        // Storage of mission timeline with info relating to mission type, uuid, max progress, expirations, etc.
        this.missionsTimelineReference = firebase.getDatabase().getReference(missionDatabasePath + "/timeline");

        missionsTimelineReference.addChildEventListener(listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                // Mission added to timeline. We need to prepare it to go live in memory.
                // Called once initially and again each time an event fires.
                addMission(snapshot.getKey(), (Map<String, Object>) snapshot.getValue());
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                // Mission modified. Likely means the expiration time or max progress changed.
                // Usually we never want to do this. We'd just make a new mission.
                // But we will support it anyway for the dumbass who wants to change the mission mid-way through
                // and upset players.
                removeMission(snapshot.getKey());
                addMission(snapshot.getKey(), (Map<String, Object>) snapshot.getValue());
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                // Mission removed from timeline. We need to remove it from memory.
                removeMission(snapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
                // Won't happen. Doesn't make sense in this context. Don't implement.
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Database error. Print it.
                plugin.getLogger().warning("Mission listener error: " + error.getMessage());
            }
        });
    }

    /**
     * Tells the manager to query the database for the current missions then re-create the missions and rewards.
     * This is done automagically by Firebase if the database changes. But if we were to say, add a reward or a mission.
     * This method allows the manager to properly decipher the database data into an mission.
     * <br><br>
     * This will not delete any progress players have made on their current missions.
     * <br><br>
     * See {@link Missions} or {@link Rewards} for a use case.
     */
    public void refresh() {
        missionsTimelineReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for(DataSnapshot child : snapshot.getChildren()) {
                    removeMission(child.getKey());
                    addMission(child.getKey(), (Map<String, Object>) child.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Database error. Print it.
                plugin.getLogger().warning("Mission listener error: " + error.getMessage());
            }
        });
    }

    private void addMission(String missionId, Map<String, Object> missionData) {
        plugin.getLogger().info("Mission loading: " + missionId);
        String type = (String) missionData.get("type");
        if(type == null) {
            plugin.getLogger().warning("Missing mission type on: " + missionId);
            plugin.getLogger().warning("This was likely caused by an incorrect manual entry to Firebase. You can safely ignore this warning.");
            return;
        }

        Class<? extends Mission> missionClass = Missions.getInstance().getMissionByName().get(type);
        if(missionClass == null) {
            plugin.getLogger().warning("Failed to parse mission class: " + type);
            plugin.getLogger().warning("This was likely caused by an incorrect manual entry to Firebase. You can safely ignore this warning.");
            return;
        }

        Mission instance;
        try {
            instance = missionClass
                    .getConstructor(Plugin.class, BossBarAnimationHandler.class, DatabaseReference.class, String.class, Map.class)
                    .newInstance(plugin, animationHandler, firebase.getDatabase().getReference(missionDatabasePath + "/progress")
                            .child(missionId), missionId, missionData);
        } catch(Exception e) {
            plugin.getLogger().warning("Failed to create mission (" + missionId + "): " + type + " [" + e.getMessage() + "]");
            plugin.getLogger().warning("This was likely caused by an incorrect manual entry to Firebase. You can safely ignore this warning.");
            return;
        }

        allMissions.put(missionId, instance);

        long currentTime = System.currentTimeMillis();
        if(currentTime < instance.getStartTime()) {
            plugin.getLogger().info("Mission enqueued: " + instance);

            // Plan to start the mission!
            ClockedTask task = new ClockedTask(instance.getStartTime(), true, () -> {
                missionTasks.remove(missionId);
                startActiveMission(missionId, instance);
            });
            missionTasks.put(missionId, task);
            ClockedTaskManager.getManager().addTask(task);
        } else if(instance.getStartTime() <= currentTime && currentTime < instance.getEndTime()) {
            // Mission is active! Plan to end the mission.
            startActiveMission(missionId, instance);
        } else
            plugin.getLogger().info("Mission already finished: " + instance);// Otherwise, nothing more is needed if the mission is already ended.
    }

    /**
     * Helper method
     */
    private void startActiveMission(String missionId, Mission instance) {
        activeMissions.put(missionId, instance);
        instance.setActive(true);
        plugin.getLogger().info("Mission started: " + instance);

        ClockedTask task = new ClockedTask(instance.getEndTime(), true, () -> {
            activeMissions.remove(missionId);
            instance.setActive(false);
            missionTasks.remove(missionId);

            plugin.getLogger().info("Mission ended: " + instance);
        });
        missionTasks.put(missionId, task);
        ClockedTaskManager.getManager().addTask(task);
    }

    private void removeMission(String missionId) {
        plugin.getLogger().info("Mission unloading: " + missionId);

        allMissions.remove(missionId);
        Mission mission = activeMissions.remove(missionId);
        if(mission != null)
            mission.setActive(false);
        ClockedTask task = missionTasks.remove(missionId);
        if(task != null)
            task.setCancelled(true);
    }

    public void registerDataListener(UUID uuid) {
        for(Mission active : activeMissions.values())
            active.registerPlayerDataListener(uuid);
    }

    public void unregisterDataListener(UUID uuid) {
        for(Mission active : activeMissions.values())
            active.unregisterPlayerDataListener(uuid);
    }

    public Map<String, Mission> getMissions() {
        return Collections.unmodifiableMap(allMissions);
    }

    public Map<String, Mission> getActiveMissions() {
        return Collections.unmodifiableMap(activeMissions);
    }

    public void openMissionGUI(Player player) {
        openMissionGUI(player, true);
    }

    public void openMissionGUI(Player player, boolean withOpenSounds) {
        int activeMissionCount = activeMissions.values().size();
        final boolean expandGUI = activeMissionCount > 9;

        GUI gui = withOpenSounds ? new GUI(plugin, "Missions", expandGUI ? 54 : 27,
                new SoundBundle(Sound.BLOCK_CHEST_OPEN, 1F, 0.9F),
                null,
                new SoundBundle(Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1F, 1F),
                new SoundBundle(Sound.ENTITY_LINGERING_POTION_THROW, 1F, 1.5F))
                : new GUI(plugin, "Missions", expandGUI ? 54 : 27,
                null,
                null,
                new SoundBundle(Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1F, 1F),
                new SoundBundle(Sound.ENTITY_LINGERING_POTION_THROW, 1F, 1.5F));

        // Explanation Item
        gui.setSlot(4, Items.MISSIONS_EXPLANATION_ITEM);

        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            PriorityQueue<Mission> queue = new PriorityQueue<>(activeMissions.values());
            boolean currentExpandGUI = queue.size() > 9;
            if(currentExpandGUI == expandGUI)
                updateMissionGUI(player, gui, queue);
            else { // On inventory resize...
                player.closeInventory();
                openMissionGUI(player, false);
            }
        }, 0L, 20L);
        gui.setOnDestroyCallback(task::cancel);

        gui.openInv(player);
    }

    private void updateMissionGUI(Player player, GUI gui, Queue<Mission> missions) {
        UUID uuid = player.getUniqueId();
        ItemStack air = new ItemStack(Material.AIR);
        for(int i=9; i<27; i++)
            gui.setSlot(i, air);

        MissionLayout layout = MissionLayout.of(missions.size());
        for(int i : layout.getLayout()) {
            Mission mission = missions.poll();
            assert mission != null;
            gui.setSlot(9+i, mission.getMissionItem(uuid));
            Reward reward = mission.getReward();

            if(reward == null)
                continue;

            /*if(reward instanceof SpecificReward)
                gui.setSlot(18+i, ((SpecificReward) reward).getIcon(player));
            else*/
            gui.setSlot(18+i, mission.getReward().getIcon());
        }

        if(missions.isEmpty())
            return;

        for(int i=36; i<54; i++)
            gui.setSlot(i, air);

        layout = MissionLayout.of(missions.size());
        for(int i : layout.getLayout()) {
            Mission mission = missions.poll();
            gui.setSlot(36+i, mission.getMissionItem(uuid));
            Reward reward = mission.getReward();

            if(reward == null)
                continue;

            gui.setSlot(45+i, mission.getReward().getIcon());
        }
    }

    public void safeShutdown() {
        missionsTimelineReference.removeEventListener(listener);
        for(Mission mission : new ArrayList<>(allMissions.values()))
            removeMission(mission.getUuid());
        HandlerList.unregisterAll(this);
    }
}
