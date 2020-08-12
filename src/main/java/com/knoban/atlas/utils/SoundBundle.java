package com.knoban.atlas.utils;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * A simple way to bundle data for playing a sound.
 *
 * @author Alden Bansemer (kNoAPP)
 */
public class SoundBundle {

    private Sound sound;
    private float volume, pitch;

    /**
     * Create a bundle of sound data that can be used later
     * @param sound The sound to use
     * @param volume The volume of the sound
     * @param pitch The pitch of the sound
     */
    public SoundBundle(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    /**
     * Plays the sound for all {@link Player}s near a {@link Location}.
     * @param loc The location of the sound
     */
    public void playToWorld(Location loc) {
        loc.getWorld().playSound(loc, sound, volume, pitch);
    }

    /**
     * Plays the sound for a single {@link Player} at a {@link Location}.
     * @param p The player to play the sound for
     * @param loc The location of the sound
     */
    public void playToPlayer(Player p, Location loc) {
        p.playSound(loc, sound, volume, pitch);
    }
}
