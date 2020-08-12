package com.knoban.atlas.claims;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.UUID;

/**
 * Think of this Landlord class as a Player or Server who owns Estate and can allow others to access it.
 * @author Alden Bansemer (kNoAPP)
 */
public class Landlord {

    private UUID uuid;
    protected HashSet<Estate> ownedEstates = new HashSet<>();

    /**
     * TO BE USED BY GSON ONLY! DO NOT USE!
     */
    public Landlord() {}

    /**
     * Create a Landlord from a player's unique id.
     * @param uuid The uuid of the player to build a Landlord from
     */
    protected Landlord(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * @return The uuid of the Landlord (a player's uuid)
     */
    @NotNull
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Creates a copy of all the owned estates the Landlord owns. This is purely so that the caller cannot
     * modify the list. If you need to remove owned {@link Estate} from a LandLord, see the {@link LandManager} class.
     * Likewise for adding Estates.
     * @return A copy of all the owned {@link Estate} of a Landlord
     */
    @NotNull
    public HashSet<Estate> getOwnedEstates() {
        return new HashSet<>(ownedEstates);
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Landlord))
            return false;

        Landlord landlord = (Landlord) o;
        return landlord.uuid.equals(uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
