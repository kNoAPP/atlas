package com.knoban.atlas.listeners;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Alden Bansemer (kNoAPP)
 */
public class CombatListener implements Listener {

    private static final HashSet<EntityDamageEvent.DamageCause> COMBAT =
            new HashSet<>(Arrays.asList(EntityDamageEvent.DamageCause.CUSTOM,
                    EntityDamageEvent.DamageCause.BLOCK_EXPLOSION, EntityDamageEvent.DamageCause.CONTACT,
                    EntityDamageEvent.DamageCause.DRAGON_BREATH, EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                    EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK,
                    EntityDamageEvent.DamageCause.FIRE, EntityDamageEvent.DamageCause.FIRE_TICK,
                    EntityDamageEvent.DamageCause.LIGHTNING, EntityDamageEvent.DamageCause.MAGIC,
                    EntityDamageEvent.DamageCause.POISON, EntityDamageEvent.DamageCause.PROJECTILE,
                    EntityDamageEvent.DamageCause.THORNS, EntityDamageEvent.DamageCause.WITHER));

    // Time in seconds players should remain in combat for after entering.
    private int combatTimer;
    private Cache<UUID, Long> inCombat;

    /**
     * Creates a {@link CombatListener} that automatically handles tracking {@link org.bukkit.entity.LivingEntity}
     * combat. Combat persists when a {@link Player} logs out or a {@link org.bukkit.entity.LivingEntity} dies.
     * @param plugin The plugin instance registering the {@link CombatListener}
     * @param combatTimer The amount of time (in seconds) {@link org.bukkit.entity.LivingEntity}s remain in combat for
     * after entering. Value defaults to 1 second if lower than 1 second.
     */
    public CombatListener(@NotNull JavaPlugin plugin, int combatTimer) {
        if(combatTimer < 1)
            combatTimer = 1;
        this.combatTimer = combatTimer;
        inCombat = CacheBuilder.newBuilder()
                .expireAfterWrite(combatTimer, TimeUnit.SECONDS).build();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Get the combat status of a {@link org.bukkit.entity.LivingEntity} using their uuid.
     * @param uuid The uuid of the {@link org.bukkit.entity.LivingEntity}
     * @return True, if they are in combat
     */
    public boolean isInCombat(@NotNull UUID uuid) {
        return inCombat.getIfPresent(uuid) != null;
    }

    /**
     * Gets the System Time of when a {@link org.bukkit.entity.LivingEntity} entered combat.
     * @param uuid The uuid of the {@link org.bukkit.entity.LivingEntity}
     * @return A Long containing the combat start System Time or null if the {@link org.bukkit.entity.LivingEntity}
     * isn't in combat
     */
    @Nullable
    public Long getTimeOfCombat(@NotNull UUID uuid) {
        return inCombat.getIfPresent(uuid);
    }

    /**
     * Gets the remaining time (in millis) of combat for a {@link org.bukkit.entity.LivingEntity} given their uuid.
     * @param uuid The uuid of the {@link org.bukkit.entity.LivingEntity}
     * @return The amount of time (in millis) until combat expires or null if the {@link org.bukkit.entity.LivingEntity}
     * isn't in combat
     */
    @Nullable
    public Long getRemainingCombatTime(@NotNull UUID uuid) {
        Long timeOfCombat = inCombat.getIfPresent(uuid);
        if(timeOfCombat == null)
            return null;

        return (timeOfCombat + 1000L*combatTimer) - System.currentTimeMillis();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if(COMBAT.contains(e.getCause())) {
            inCombat.put(e.getEntity().getUniqueId(), System.currentTimeMillis());
        }
    }
}
