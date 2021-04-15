package com.knoban.atlas.missions;

import java.util.*;

public final class Missions {

    private static final Missions INSTANCE = new Missions();

    private final List<Class<? extends Mission>> missions = new ArrayList<>();
    private final Map<String, Class<? extends Mission>> missionsByName = new HashMap<>();

    public void addMission(Class<? extends Mission> mission) {
        missions.add(mission);
        missionsByName.put(mission.getAnnotation(MissionInfo.class).name(), mission);
    }

    public void removeMission(Class<? extends Mission> mission) {
        missions.remove(mission);
        missionsByName.remove(mission.getAnnotation(MissionInfo.class).name());
    }

    public List<Class<? extends Mission>> getMissions() {
        return Collections.unmodifiableList(missions);
    }

    public Map<String, Class<? extends Mission>> getMissionByName() {
        return Collections.unmodifiableMap(missionsByName);
    }

    public static Missions getInstance() {
        return INSTANCE;
    }
}
