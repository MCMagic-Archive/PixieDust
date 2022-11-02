package us.mcmagic.pixiedust.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;
import us.mcmagic.pixiedust.PixieDust;
import us.mcmagic.pixiedust.handlers.GameState;
import us.mcmagic.pixiedust.handlers.GameTeam;
import us.mcmagic.pixiedust.handlers.PlayerData;
import us.mcmagic.pixiedust.utils.GameUtil;
import us.mcmagic.pixiedust.utils.ScoreboardUtil;

public class PlayerDoubleJump implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (GameUtil.getTeam(GameTeam.SPECTATOR).contains(player.getUniqueId())) {
            return;
        }
        PlayerData data = PixieDust.getInstance().getPlayerData(player.getUniqueId());
        if (GameState.isState(GameState.IN_GAME) && data.getJumps() >= 1) {
            if ((player.getGameMode() != GameMode.CREATIVE) && (!player.isFlying()) && (player.getLocation()
                    .subtract(0, 1, 0).getBlock().getType() != Material.AIR)) {
                player.setAllowFlight(true);
            }
        }
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (!GameState.isState(GameState.IN_GAME)) {
            return;
        }
        if (GameUtil.getTeam(GameTeam.SPECTATOR).contains(player.getUniqueId())) {
            return;
        }
        PlayerData data = PixieDust.getInstance().getPlayerData(player.getUniqueId());
        int amount = data.getJumps();
        if (amount >= 1) {
            event.setCancelled(true);
            player.setAllowFlight(false);
            player.setFlying(false);
            Vector jumpVelo = player.getLocation().getDirection().multiply(1.5).setY(1.5);
            player.setVelocity(jumpVelo);
            player.playSound(player.getLocation(), Sound.WITHER_SHOOT, 100, 1);
            data.addJumps(-1);
            ScoreboardUtil.updateJumps(player);
        }
    }

}
