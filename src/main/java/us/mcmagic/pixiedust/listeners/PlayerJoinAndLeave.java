package us.mcmagic.pixiedust.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;
import us.mcmagic.mcmagiccore.MCMagicCore;
import us.mcmagic.mcmagiccore.arcade.ServerState;
import us.mcmagic.mcmagiccore.bungee.BungeeUtil;
import us.mcmagic.pixiedust.PixieDust;
import us.mcmagic.pixiedust.handlers.GameState;
import us.mcmagic.pixiedust.handlers.GameTeam;
import us.mcmagic.pixiedust.handlers.PlayerData;
import us.mcmagic.pixiedust.utils.GameUtil;
import us.mcmagic.pixiedust.utils.ReloadUtil;
import us.mcmagic.pixiedust.utils.ScoreboardUtil;

import java.util.HashMap;
import java.util.UUID;

public class PlayerJoinAndLeave implements Listener {
    public static String gname = "pixie";
    private HashMap<UUID, Integer> jumpMap = new HashMap<>();

    @EventHandler
    public void onPlayerLoginAsync(AsyncPlayerPreLoginEvent event) {
        if (!event.getLoginResult().equals(AsyncPlayerPreLoginEvent.Result.ALLOWED)) {
            return;
        }
        if (GameState.isState(GameState.IN_LOBBY)) {
            PixieDust.delayMap.put(event.getUniqueId(), (double) (MCMagicCore.gameManager.getGameData(event.getUniqueId(), "pixie").getInts().get("delay")) / 10);
            try {
                jumpMap.put(event.getUniqueId(), MCMagicCore.gameManager.getGameData(event.getUniqueId(), "pixie").getInts().get("doublejump"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage("");
        Player player = event.getPlayer();
        ScoreboardUtil.initialize(player);
        if (!GameState.isState(GameState.IN_LOBBY) && player.hasPermission("arcade.bypass")) {
            PixieDust.gameUtil.joinSpectator(player);
            return;
        }
        PlayerData data = new PlayerData(player.getUniqueId(), player.getName(),
                PixieDust.delayMap.containsKey(player.getUniqueId()) ? PixieDust.delayMap.get(player.getUniqueId()) : 1,
                jumpMap.containsKey(player.getUniqueId()) ? jumpMap.get(player.getUniqueId()) : 1);
        PixieDust.getInstance().addPlayerData(player.getUniqueId(), data);
        for (Player tp : Bukkit.getOnlinePlayers()) {
            tp.hidePlayer(player);
            tp.showPlayer(player);
        }
        MCMagicCore.gameManager.setPlayerCount(MCMagicCore.getMCMagicConfig().serverName, Bukkit.getOnlinePlayers().size());
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        if (jumpMap.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.GREEN + "Loaded your Double Jump amount of " + ChatColor.BOLD +
                    jumpMap.get(player.getUniqueId()));
        } else {
            player.sendMessage(ChatColor.RED + "There was an error loading your Double Jump Amount, it has been set to 1.");
        }
        player.setFoodLevel(20);
        player.setExp(0);
        player.setGameMode(GameMode.ADVENTURE);
        GameUtil.clearInventory(player);
        PlayerInventory pi = player.getInventory();
        pi.setItem(8, GameUtil.leaveBed);
        pi.setItem(0, GameUtil.yellowTeamWool);
        pi.setItem(1, GameUtil.purpleTeamWool);
        player.teleport(GameUtil.lobby);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (GameState.isState(GameState.SERVER_STARTING)) {
            event.setKickMessage(ChatColor.RED + "Game Starting Up!");
            event.setResult(Result.KICK_OTHER);
            return;
        }
        if (Bukkit.getOnlinePlayers().size() == GameUtil.getMaxPlayers() && GameState.isState(GameState.IN_LOBBY)) {
            event.setKickMessage(ChatColor.RED + "That game is full!");
            event.setResult(Result.KICK_OTHER);
            return;
        }
        if (!GameState.getState().equals(GameState.IN_LOBBY)) {
            if (event.getPlayer().hasPermission("arcade.bypass")) {
                return;
            }
            event.setKickMessage(ChatColor.RED + "There is currently a game in progress!");
            event.setResult(Result.KICK_OTHER);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (GameState.isState(GameState.IN_LOBBY)) {
            removeFromAllTeams(player);
            MCMagicCore.gameManager.setPlayerCount(MCMagicCore.getMCMagicConfig().serverName, Bukkit.getOnlinePlayers().size() - 1);
            PixieDust.getInstance().removePlayerData(player.getUniqueId());
            return;
        }
        if (ScoreboardUtil.getTeam(player).equals(GameTeam.SPECTATOR)) {
            removeFromAllTeams(player);
            PixieDust.getInstance().removePlayerData(player.getUniqueId());
            return;
        }
        MCMagicCore.gameManager.setPlayerCount(MCMagicCore.getMCMagicConfig().serverName, Bukkit.getOnlinePlayers().size() - 1);
        String team = ScoreboardUtil.getTeam(player).getName();
        try {
            PixieDust.delayMap.remove(player);
        } catch (Exception ignored) {
        }
        if (!GameState.isState(GameState.POST_GAME) && !GameState.isState(GameState.IN_LOBBY)) {
            if (player.getScoreboard().getTeam(team.toLowerCase()).getSize() < 4) {
                MCMagicCore.gameManager.broadcast("The Game Has Ended Due To Too Many Players Leaving.");
                GameState.setState(GameState.POST_GAME);
                for (Player tp : Bukkit.getOnlinePlayers()) {
                    tp.getInventory().clear();
                    if (ReloadUtil.reloading.contains(tp)) {
                        ReloadUtil.reloading.remove(tp);
                        tp.setExp(0);
                    }
                }
                Bukkit.getScheduler().runTaskAsynchronously(MCMagicCore.getInstance(), () ->
                        MCMagicCore.gameManager.setState(MCMagicCore.getMCMagicConfig().serverName, ServerState.RESTARTING));
                MCMagicCore.gameManager.broadcast("Returning to " + ChatColor.AQUA + "Arcade " +
                        ChatColor.GREEN + "" + ChatColor.BOLD + "in 10 seconds...");
                Bukkit.getScheduler().runTaskLater(MCMagicCore.getInstance(), () -> {
                    BungeeUtil.emptyServer();
                    Bukkit.getScheduler().runTaskLater(MCMagicCore.getInstance(), Bukkit::shutdown, 100L);
                }, 200L);
            }
        }
        PixieDust.getInstance().removePlayerData(player.getUniqueId());
        removeFromAllTeams(player);
        event.setQuitMessage("");
    }

    public void removeFromAllTeams(Player player) {
        GameUtil.removeFromTeam(GameTeam.YELLOW, player.getUniqueId());
        GameUtil.removeFromTeam(GameTeam.PURPLE, player.getUniqueId());
        GameUtil.removeFromTeam(GameTeam.SPECTATOR, player.getUniqueId());
        for (Player tp : Bukkit.getOnlinePlayers()) {
            Scoreboard sb = tp.getScoreboard();
            try {
                sb.getTeam("yellow").removeEntry(player.getName());
            } catch (IllegalStateException | IllegalArgumentException ignored) {
            }
            try {
                sb.getTeam("purple").removeEntry(player.getName());
            } catch (IllegalStateException | IllegalArgumentException ignored) {
            }
            try {
                sb.getTeam("spectator").removeEntry(player.getName());
            } catch (IllegalStateException | IllegalArgumentException ignored) {
            }
        }
    }
}