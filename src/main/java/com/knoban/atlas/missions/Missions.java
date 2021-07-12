package com.knoban.atlas.missions;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class Missions {

    private static final Missions INSTANCE = new Missions();

    private final List<Class<? extends Mission>> missions = new ArrayList<>();
    private final Map<String, Class<? extends Mission>> missionsByName = new HashMap<>();

    public void addMission(@NotNull Class<? extends Mission> mission) {
        missions.add(mission);
        missionsByName.put(mission.getAnnotation(MissionInfo.class).name(), mission);
    }

    public void removeMission(@NotNull Class<? extends Mission> mission) {
        missions.remove(mission);
        missionsByName.remove(mission.getAnnotation(MissionInfo.class).name());
    }

    @NotNull
    public List<Class<? extends Mission>> getMissions() {
        return Collections.unmodifiableList(missions);
    }

    @NotNull
    public Map<String, Class<? extends Mission>> getMissionByName() {
        return Collections.unmodifiableMap(missionsByName);
    }

    @NotNull
    public static Missions getInstance() {
        return INSTANCE;
    }
}
