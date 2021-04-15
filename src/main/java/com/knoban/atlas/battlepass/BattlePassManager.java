package com.knoban.atlas.battlepass;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.knoban.atlas.data.firebase.AtlasFirebase;
import com.knoban.atlas.gui.GUI;
import com.knoban.atlas.gui.GUIClickable;
import com.knoban.atlas.missions.Missions;
import com.knoban.atlas.rewards.Reward;
import com.knoban.atlas.rewards.Rewards;
import com.knoban.atlas.utils.Items;
import com.knoban.atlas.utils.SoundBundle;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BattlePassManager {

    private final Plugin plugin;

    private final DatabaseReference battlePassReference;
    private final ValueEventListener listener;

    private ArrayList<BattlePassLevel> battlePass = new ArrayList<>();

    public BattlePassManager(Plugin plugin, AtlasFirebase firebase, String battlepassDatabasePath) {
        this.plugin = plugin;
        this.battlePassReference = firebase.getDatabase().getReference(battlepassDatabasePath);

        battlePassReference.addValueEventListener(listener = getBattlePassListener());
    }

    private ValueEventListener getBattlePassListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList<BattlePassLevel> newBattlepass = new ArrayList<>();
                newBattlepass.add(null); // No such thing as level "0."
                if(snapshot.exists()) {
                    List<Object> unparsedBattlepass = (List<Object>) snapshot.getValue();
                    for(int i=1; i<unparsedBattlepass.size(); i++) {
                        Object o = unparsedBattlepass.get(i);
                        if(o == null) {
                            newBattlepass.add(null);
                            continue;
                        }

                        Map<String, Object> levelData = (Map<String, Object>) o;
                        try {
                            BattlePassLevel level = new BattlePassLevel(i, levelData);
                            newBattlepass.add(level);
                        } catch(Exception e) {
                            plugin.getLogger().warning("Misconfigured battle pass level " + i + ": " + e.getMessage());
                            newBattlepass.add(null);
                        }
                    }
                }

                battlePass = newBattlepass;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                plugin.getLogger().warning("Battle pass listener error: " + error.getMessage());
            }
        };
    }

    /**
     * Tells the manager to query the database for the current battle pass then re-create the battle pass and rewards.
     * This is done automagically by Firebase if the database changes. But if we were to say, add a reward. This method
     * allows the manager to properly decipher the database data into an actual battle pass.
     * <br><br>
     * This will not delete any progress players have made on the battle pass.
     * <br><br>
     * See {@link Missions} or {@link Rewards} for a use case.
     */
    public void refresh() {
        battlePassReference.addListenerForSingleValueEvent(getBattlePassListener());
    }

    public List<BattlePassLevel> getBattlePass() {
        return Collections.unmodifiableList(battlePass);
    }

    public int getHighestBattlePassLevel() {
        return battlePass.size() - 1;
    }

    @Nullable
    public BattlePassLevel getBattlePassLevel(int level) {
        if(level < 1 || battlePass.size() <= level)
            return null;

        BattlePassLevel bpLevel = battlePass.get(level);
        return bpLevel;
    }

    public void rewardFreeLevel(Player player, int level) {
        if(level < 1 || battlePass.size() <= level)
            return;

        BattlePassLevel bpLevel = battlePass.get(level);
        if(bpLevel == null)
            return;

        Reward free = bpLevel.getFree();
        if(free != null)
            free.reward(player);
    }

    public void rewardPremiumLevel(Player player, int level) {
        if(level < 1 || battlePass.size() <= level)
            return;

        BattlePassLevel bpLevel = battlePass.get(level);
        if(bpLevel == null)
            return;

        Reward premium = bpLevel.getPremium();
        if(premium != null)
            premium.reward(player);
    }

    /**
     * Call this when done with the battle pass object, or you will get database leaks costing you $$.
     */
    public void safeShutdown() {
        battlePassReference.removeEventListener(listener);
    }

    public void openBattlePassGUI(Player player, int currentBPLevel, boolean ownsPass,
                                  String storeURL, int page, boolean withOpenSounds) {
        GUI gui = withOpenSounds ? new GUI(plugin, "Rewards", 45,
                new SoundBundle(Sound.BLOCK_CHEST_OPEN, 1F, 0.9F),
                null,
                new SoundBundle(Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1F, 1F),
                new SoundBundle(Sound.ENTITY_LINGERING_POTION_THROW, 1F, 1.5F))
                : new GUI(plugin, "Rewards", 45,
                null,
                null,
                new SoundBundle(Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1F, 1F),
                new SoundBundle(Sound.ENTITY_LINGERING_POTION_THROW, 1F, 1.5F));

        // Explanation Item
        gui.setSlot(4, Items.BATTLEPASS_EXPLANATION_ITEM);

        int levelBase = page*9 + 1;
        for(int i=0; i<9; i++) {
            int level = levelBase + i;

            ItemStack progress;
            if(currentBPLevel >= level) {
                progress = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
                ItemMeta im = progress.getItemMeta();
                im.setDisplayName("§dLevel " + level);
                im.setLore(Arrays.asList("§7Complete!"));
                progress.setItemMeta(im);
            } else if(currentBPLevel + 1 == level) {
                progress = new ItemStack(Material.PINK_STAINED_GLASS_PANE);
                ItemMeta im = progress.getItemMeta();
                im.setDisplayName("§dLevel " + level);
                im.setLore(Arrays.asList("§7In progress..."));
                progress.setItemMeta(im);
            } else {
                progress = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
                ItemMeta im = progress.getItemMeta();
                im.setDisplayName("§fLevel " + level);
                im.setLore(Arrays.asList("§7Incomplete."));
                progress.setItemMeta(im);
            }
            gui.setSlot(18+i, progress);

            BattlePassLevel bpLevel = getBattlePassLevel(level);
            if(bpLevel == null)
                continue;

            Reward free = bpLevel.getFree();
            if(free != null) {
                gui.setSlot(9+i, free.getIcon());
            }

            Reward premium = bpLevel.getPremium();
            if(premium != null) {
                gui.setSlot(27+i, premium.getIcon());
            }
        }

        // Show previous page
        if(page > 0) {
            GUIClickable previousPage = new GUIClickable();
            previousPage.setActionOnClick((g, e) -> {
                player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1F, 1F);
                openBattlePassGUI(player, currentBPLevel, ownsPass, storeURL, page - 1, false);
            });
            gui.setSlot(36, Items.PREVIOUS_PAGE, previousPage);
        }

        // Buy the battle pass!
        if(!ownsPass) {
            GUIClickable purchasePass = new GUIClickable();
            purchasePass.setActionOnClick((g, e) -> {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.ENTITY_WANDERING_TRADER_REAPPEARED, 1F, 1F);
                player.sendMessage("§6§lBuy the Battle Pass! §fVisit our store at:");
                player.sendMessage("§b" + storeURL);
            });
            gui.setSlot(40, Items.BATTLEPASS_PURCHASE_PASS, purchasePass);
        }

        // Show next page
        if(levelBase + 9 <= getHighestBattlePassLevel()) {
            GUIClickable nextPage = new GUIClickable();
            nextPage.setActionOnClick((g, e) -> {
                player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1F, 1F);
                openBattlePassGUI(player, currentBPLevel, ownsPass, storeURL, page + 1, false);
            });
            gui.setSlot(44, Items.NEXT_PAGE, nextPage);
        }

        // Open the inventory.
        gui.openInv(player);
    }
}
