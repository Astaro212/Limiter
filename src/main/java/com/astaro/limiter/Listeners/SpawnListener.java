package com.astaro.limiter.Listeners;

import com.astaro.limiter.Limiter;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;

public class SpawnListener implements Listener {

    private final Limiter plugin;
    private final int limitInOrigin;
    private final int neighLimit;

    public SpawnListener(Limiter plugin) {
        this.plugin = plugin;
        this.limitInOrigin = plugin.getConfig().getInt("main-chunk-limit");
        this.neighLimit = plugin.getConfig().getInt("neighbour-limit");
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMobSpawn(EntitySpawnEvent e) {
        Entity entity = e.getEntity();

        if(plugin.getLimitManager().isExempt(entity)) return;

        Chunk origin = entity.getLocation().getChunk();


        if (plugin.getLimitManager().isEntityLimitReached(origin, limitInOrigin)) {
            e.setCancelled(true);
            return;
        }

        World world = entity.getWorld();
        int centerX = origin.getX();
        int centerZ = origin.getZ();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                if (world.isChunkLoaded(centerX + x, centerZ + z)) {
                    Chunk neigh = world.getChunkAt(centerX + x, centerZ + z);
                    if (plugin.getLimitManager().isEntityLimitReached(neigh, neighLimit)) {
                        e.setCancelled(true);
                        return;
                    }
                }

            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleSpawn(VehicleCreateEvent e) {
        Vehicle entity = e.getVehicle();

        if (plugin.getLimitManager().isExempt(entity)) return;

        Chunk origin = entity.getLocation().getChunk();
        if(plugin.getLimitManager().isEntityLimitReached(origin, limitInOrigin)){
            entity.remove();
        }
    }
}




