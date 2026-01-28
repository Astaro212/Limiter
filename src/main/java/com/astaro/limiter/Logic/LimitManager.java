package com.astaro.limiter.Logic;

import com.astaro.limiter.Limiter;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class LimitManager {

    private final Limiter plugin;
    private final List<String> exemptEntities;
    private final List<String> exemptWorlds;

    public LimitManager(Limiter plugin){
        this.plugin = plugin;
        this.exemptEntities = plugin.getConfig().getStringList("exempts.entity");
        this.exemptWorlds = plugin.getConfig().getStringList("exempts.world");
    }

    public boolean isExempt(Entity entity){

        return entity instanceof Player || exemptEntities.contains(entity.getType().name().toLowerCase()) ||
                exemptWorlds.contains(entity.getWorld().getName().toLowerCase());
    }

    public boolean isEntityLimitReached(Chunk chunk, int limit){
        int count = 0;
        for(Entity ent : chunk.getEntities()){
            if(!isExempt(ent)){
                count++;
            }
            if(count >= limit) return true;
        }
        return false;
    }

    public boolean isBlockLimitReached(Chunk chunk, Material type, int limit){
        int count = 0;
        return false;
    }

}
