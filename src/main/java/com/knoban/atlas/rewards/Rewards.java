package com.knoban.atlas.rewards;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class Rewards {

    private static final Rewards INSTANCE = new Rewards();

    private final List<Class<? extends Reward>> rewards = new ArrayList<>();
    private final Map<String, Class<? extends Reward>> rewardsByName = new HashMap<>();

    public void addReward(@NotNull Class<? extends Reward> reward) {
        rewards.add(reward);
        rewardsByName.put(reward.getAnnotation(RewardInfo.class).name(), reward);
    }

    public void removeReward(@NotNull Class<? extends Reward> reward) {
        rewards.remove(reward);
        rewardsByName.remove(reward.getAnnotation(RewardInfo.class).name());
    }

    @NotNull
    public List<Class<? extends Reward>> getRewards() {
        return Collections.unmodifiableList(rewards);
    }

    @NotNull
    public Map<String, Class<? extends Reward>> getRewardByName() {
        return Collections.unmodifiableMap(rewardsByName);
    }

    @NotNull
    public static Rewards getInstance() {
        return INSTANCE;
    }
}
