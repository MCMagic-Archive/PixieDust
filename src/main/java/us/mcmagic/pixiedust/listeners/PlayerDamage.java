package us.mcmagic.pixiedust.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerDamage implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        EntityType type = event.getEntityType();
        if (type.equals(EntityType.PLAYER) || type.equals(EntityType.ITEM_FRAME) || type.equals(EntityType.PAINTING)) {
            event.setCancelled(true);
        }
    }
}