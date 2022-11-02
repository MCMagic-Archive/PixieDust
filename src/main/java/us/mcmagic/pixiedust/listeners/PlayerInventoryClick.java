package us.mcmagic.pixiedust.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import us.mcmagic.pixiedust.handlers.GameTeam;
import us.mcmagic.pixiedust.utils.GameUtil;

public class PlayerInventoryClick implements Listener {

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (!GameUtil.getTeam(GameTeam.SPECTATOR).contains(player.getUniqueId())) {
                return;
            }
            if (event.getInventory().getName()
                    .equals(ChatColor.BLUE + "Spectating Inventory")) {
                try {
                    ItemStack item = event.getCurrentItem();
                    String pname = ChatColor.stripColor(item.getItemMeta()
                            .getDisplayName());
                    OfflinePlayer tpo = Bukkit.getOfflinePlayer(pname);
                    if (!tpo.isOnline()) {
                        player.closeInventory();
                        GameUtil.openSpectateInventory(player);
                        return;
                    }
                    Player tp = Bukkit.getPlayer(pname);
                    if (GameUtil.getTeam(GameTeam.SPECTATOR).contains(tp.getUniqueId())) {
                        player.closeInventory();
                        GameUtil.openSpectateInventory(player);
                        return;
                    }
                    player.teleport(tp);
                    player.sendMessage(ChatColor.GREEN
                            + "You are now spectating " + tp.getName());
                    return;
                } catch (NullPointerException ignored) {
                    return;
                }
            }
            event.setCancelled(true);
            player.closeInventory();
        }
    }
}