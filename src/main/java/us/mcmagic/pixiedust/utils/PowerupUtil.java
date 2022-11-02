package us.mcmagic.pixiedust.utils;

import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;
import us.mcmagic.mcmagiccore.MCMagicCore;
import us.mcmagic.mcmagiccore.particles.ParticleEffect;
import us.mcmagic.mcmagiccore.particles.ParticleUtil;
import us.mcmagic.pixiedust.PixieDust;
import us.mcmagic.pixiedust.handlers.GameState;
import us.mcmagic.pixiedust.handlers.GameTeam;
import us.mcmagic.pixiedust.handlers.PlayerData;
import us.mcmagic.pixiedust.handlers.PowerupType;
import us.mcmagic.pixiedust.listeners.PlayerInteract;
import us.mcmagic.pixiedust.threads.GameThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by Marc on 1/27/15
 */
public class PowerupUtil implements Listener {
    private static HashMap<PowerupType, Location> locations = new HashMap<>();
    private static HashMap<UUID, PowerupType> playerMap = new HashMap<>();
    private static HashMap<UUID, Integer> timerMap = new HashMap<>();
    private static List<UUID> delay = new ArrayList<>();
    private static boolean launchActive = false;

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        if (event.getEntity().hasMetadata("game")) {
            event.setCancelled(event.getEntity().getMetadata("game").get(0).asBoolean());
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (!GameState.isState(GameState.IN_GAME)) {
            event.setCancelled(true);
            return;
        }
        if (GameUtil.getTeam(GameTeam.SPECTATOR).contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        Item item = event.getItem();
        Material type = item.getItemStack().getType();
        event.setCancelled(true);
        final Player player = event.getPlayer();
        switch (type) {
            case DIAMOND_BOOTS:
                if (playerMap.containsKey(player.getUniqueId())) {
                    if (!delay.contains(player.getUniqueId())) {
                        MCMagicCore.gameManager.message(player, ChatColor.RED + "You already have a Powerup!");
                        delay.add(player.getUniqueId());
                        Bukkit.getScheduler().runTaskLater(PixieDust.getInstance(), () ->
                                delay.remove(player.getUniqueId()), 100L);
                    }
                    return;
                }
                item.remove();
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 100, 2);
                GameThread.pickupPowerup(player, getType(type), item.getItemStack());
                activatePowerup(player, getType(type));
                return;
            case SLIME_BLOCK:
                if (playerMap.containsKey(player.getUniqueId())) {
                    if (!delay.contains(player.getUniqueId())) {
                        MCMagicCore.gameManager.message(player, ChatColor.RED + "You already have a Powerup!");
                        delay.add(player.getUniqueId());
                        Bukkit.getScheduler().runTaskLater(PixieDust.getInstance(), () ->
                                delay.remove(player.getUniqueId()), 100L);
                    }
                    return;
                }
                item.remove();
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 100, 2);
                playerMap.put(player.getUniqueId(), getType(type));
                GameThread.pickupPowerup(player, getType(type), item.getItemStack());
                return;
            case ARROW:
                if (playerMap.containsKey(player.getUniqueId())) {
                    if (!delay.contains(player.getUniqueId())) {
                        MCMagicCore.gameManager.message(player, ChatColor.RED + "You already have a Powerup!");
                        delay.add(player.getUniqueId());
                        Bukkit.getScheduler().runTaskLater(PixieDust.getInstance(), () ->
                                delay.remove(player.getUniqueId()), 100L);
                    }
                    return;
                }
                if (PlayerInteract.seeker.contains(player.getUniqueId())) {
                    if (!delay.contains(player.getUniqueId())) {
                        MCMagicCore.gameManager.message(player, ChatColor.RED + "You already have the " + PowerupType.SEEKER.getDisplayName()
                                + ChatColor.RED + " Powerup!");
                        delay.add(player.getUniqueId());
                        Bukkit.getScheduler().runTaskLater(PixieDust.getInstance(), () ->
                                delay.remove(player.getUniqueId()), 100L);
                    }
                    return;
                }
                item.remove();
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 100, 2);
                GameThread.pickupPowerup(player, getType(type), item.getItemStack());
                return;
            default:
                event.setCancelled(false);
        }
    }

    public static void activatePowerup(final Player player, PowerupType type) {
        PlayerData data = PixieDust.getInstance().getPlayerData(player.getUniqueId());
        switch (type) {
            case JUMP:
                MCMagicCore.gameManager.message(player, "You gained an " + type.getDisplayName() +
                        ChatColor.GREEN + "!");
                Scoreboard sb = player.getScoreboard();
                int old = data.getJumps();
                data.addJumps(1);
                Score jumps = sb.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.GREEN + "Jumps: " +
                        ScoreboardUtil.chatColor(player) + "" + ChatColor.BOLD + data.getJumps());
                jumps.setScore(1);
                sb.resetScores(ChatColor.GREEN + "Jumps: " + ScoreboardUtil.chatColor(player) + "" + ChatColor.BOLD + old);
                data.addMoney(1);
                return;
            case LAUNCH:
                if (launchActive) {
                    MCMagicCore.gameManager.message(player, ChatColor.RED + "You have to wait a moment to use this!");
                    return;
                }
                player.getInventory().setItem(4, new ItemStack(Material.AIR));
                launchActive = true;
                MCMagicCore.gameManager.broadcast("" + ScoreboardUtil.chatColor(player) + player.getName() +
                        ChatColor.GREEN + " has used the " + type.getDisplayName() + ChatColor.GREEN + " Powerup!");
                ParticleEffect effect = ParticleEffect.HUGE_EXPLOSION;
                Vector vector = new Vector(0, 1.5, 0);
                for (UUID uuid : ScoreboardUtil.getTeam(player).equals(GameTeam.YELLOW) ?
                        GameUtil.getTeam(GameTeam.PURPLE) : GameUtil.getTeam(GameTeam.YELLOW)) {
                    Player tp = Bukkit.getPlayer(uuid);
                    if (tp.hasPotionEffect(PotionEffectType.SPEED)) {
                        ParticleUtil.spawnParticle(effect, tp.getLocation(), 0, 0, 0, 0, 1);
                        tp.setVelocity(vector);
                    }
                }
                playerMap.remove(player.getUniqueId());
                Bukkit.getScheduler().runTaskLater(PixieDust.getInstance(), () -> launchActive = false, 100L);
                data.addMoney(1);
        }
    }

    private static String timerMessage(int i) {
        switch (i) {
            case 10:
                return ChatColor.GREEN + "▉▉▉▉▉▉▉▉▉▉";
            case 9:
                return ChatColor.GREEN + "▉▉▉▉▉▉▉▉▉" + ChatColor.RED + "▉";
            case 8:
                return ChatColor.GREEN + "▉▉▉▉▉▉▉▉" + ChatColor.RED + "▉▉";
            case 7:
                return ChatColor.GREEN + "▉▉▉▉▉▉▉" + ChatColor.RED + "▉▉▉";
            case 6:
                return ChatColor.GREEN + "▉▉▉▉▉▉" + ChatColor.RED + "▉▉▉▉";
            case 5:
                return ChatColor.GREEN + "▉▉▉▉▉" + ChatColor.RED + "▉▉▉▉▉";
            case 4:
                return ChatColor.GREEN + "▉▉▉▉" + ChatColor.RED + "▉▉▉▉▉▉";
            case 3:
                return ChatColor.GREEN + "▉▉▉" + ChatColor.RED + "▉▉▉▉▉▉▉";
            case 2:
                return ChatColor.GREEN + "▉▉" + ChatColor.RED + "▉▉▉▉▉▉▉▉";
            case 1:
                return ChatColor.GREEN + "▉" + ChatColor.RED + "▉▉▉▉▉▉▉▉▉";
            default:
                return "";
        }
    }

    private static void stopTimer(UUID uuid) {
        Integer taskID = timerMap.remove(uuid);
        if (taskID != null) {
            Bukkit.getScheduler().cancelTask(taskID);
        }
    }

    public static Location getLocation(PowerupType type) {
        return locations.get(type);
    }

    public static Material getMaterial(PowerupType type) {
        switch (type) {
            case JUMP:
                return Material.DIAMOND_BOOTS;
            case LAUNCH:
                return Material.SLIME_BLOCK;
            case SEEKER:
                return Material.ARROW;
            default:
                return Material.AIR;
        }
    }

    public static PowerupType getType(Material type) {
        switch (type) {
            case DIAMOND_BOOTS:
                return PowerupType.JUMP;
            case SLIME_BLOCK:
                return PowerupType.LAUNCH;
            case ARROW:
                return PowerupType.SEEKER;
            default:
                return null;
        }
    }

    public static void addLocation(PowerupType type, Location loc) {
        locations.put(type, loc);
    }
}
