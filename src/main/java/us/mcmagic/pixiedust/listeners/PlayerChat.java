package us.mcmagic.pixiedust.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import us.mcmagic.mcmagiccore.MCMagicCore;
import us.mcmagic.pixiedust.handlers.GameState;
import us.mcmagic.pixiedust.utils.ScoreboardUtil;

public class PlayerChat implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        String msg = event.getMessage();
        if (GameState.isState(GameState.IN_LOBBY)) {
            MCMagicCore.chatManager.chatMessage(player, msg);
            return;
        }
        MCMagicCore.chatManager.chatMessage(player, msg, ScoreboardUtil.getTeam(player).getNameWithBrackets() + " ");
    }
}