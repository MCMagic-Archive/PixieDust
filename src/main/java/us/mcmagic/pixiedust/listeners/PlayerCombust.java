package us.mcmagic.pixiedust.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;

/**
 * Created by Marc on 3/1/15
 */
public class PlayerCombust implements Listener {

    @EventHandler
    public void onEntityCombust(EntityCombustEvent event) {
        event.setCancelled(true);
    }
}
