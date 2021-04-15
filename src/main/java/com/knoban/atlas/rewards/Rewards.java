package com.knoban.atlas.rewards;

import java.util.*;

public final class Rewards {

    private static final Rewards INSTANCE = new Rewards();

    private final List<Class<? extends Reward>> rewards = new ArrayList<>();
    private final Map<String, Class<? extends Reward>> rewardsByName = new HashMap<>();

    public void addReward(Class<? extends Reward> reward) {
        rewards.add(reward);
        rewardsByName.put(reward.getAnnotation(RewardInfo.class).name(), reward);
    }

    public void removeReward(Class<? extends Reward> reward) {
        rewards.remove(reward);
        rewardsByName.remove(reward.getAnnotation(RewardInfo.class).name());
    }

    public List<Class<? extends Reward>> getRewards() {
        return Collections.unmodifiableList(rewards);
    }

    public Map<String, Class<? extends Reward>> getRewardByName() {
        return Collections.unmodifiableMap(rewardsByName);
    }

    public static Rewards getInstance() {
        return INSTANCE;
    }
}
