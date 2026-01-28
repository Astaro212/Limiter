package com.astaro.limiter.Listeners;

import com.astaro.limiter.Limiter;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.event.entity.EntityTeleportEvent;

public class PortalListener implements Listener {

    private final Limiter plugin;

    public PortalListener(Limiter plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPortalEnter(EntityPortalEvent e) {
        if (e.getEntity() instanceof Player) {
            return;
        }
        if (!plugin.getConfig().getBoolean("exempts.portal-enter")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityPortalExit(EntityPortalExitEvent e) {
        Entity entity = e.getEntity();
        if (plugin.getLimitManager().isExempt(entity)) return;

        Chunk targetChunk = e.getTo().getChunk();
        int limit = plugin.getConfig().getInt("main-chunk-limit");

        if (plugin.getLimitManager().isEntityLimitReached(targetChunk, limit)) {
            e.setCancelled(true);
            entity.remove();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityGateway(EntityTeleportEvent e) {
        Entity entity = e.getEntity();

        if (e.getTo().getWorld().getEnvironment() == World.Environment.THE_END) {
            if (!plugin.getConfig().getBoolean("exempts.portal-enter")) {
                e.setCancelled(true);
                return;
            }

            if (plugin.getLimitManager().isExempt(entity)) return;

            Chunk targetChunk = e.getTo().getChunk();
            int limit = plugin.getConfig().getInt("main-chunk-limit");

            if (plugin.getLimitManager().isEntityLimitReached(targetChunk, limit)) {
                e.setCancelled(true);
            }
        }
    }
}
