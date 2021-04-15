package com.knoban.atlas.rewards;

import com.knoban.atlas.utils.Items;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class Reward {

    protected final RewardInfo info = getClass().getAnnotation(RewardInfo.class);

    protected ItemStack icon;
    protected long amount;

    protected Reward(Map<String, Object> data) {
        this.icon = Items.DEFAULT_REWARD_ITEM;
        this.amount = (long) data.getOrDefault("amount", 1L);
        if(amount < 0)
            amount = 0;
    }

    public Reward(@NotNull ItemStack icon, long amount) {
        this.icon = icon;
        this.amount = amount;
    }

    public RewardInfo getInfo() {
        return info;
    }

    @NotNull
    public ItemStack getIcon() {
        return icon;
    }

    public void setIcon(@NotNull ItemStack icon) {
        this.icon = icon;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public abstract void reward(@NotNull Player p);

    public static Reward fromData(@NotNull Map<String, Object> data) throws Exception {
        String type = (String) data.get("type");
        if(type == null)
            throw new IllegalArgumentException("Missing reward type.");

        Class<? extends Reward> rewardClass = Rewards.getInstance().getRewardByName().get(type);
        if(rewardClass == null)
            throw new IllegalArgumentException("Invalid reward type: " + type);

        return rewardClass.getConstructor(Map.class).newInstance(data);
    }
}
