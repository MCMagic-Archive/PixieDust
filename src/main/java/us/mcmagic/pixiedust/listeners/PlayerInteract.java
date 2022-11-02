package us.mcmagic.pixiedust.listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import us.mcmagic.mcmagiccore.MCMagicCore;
import us.mcmagic.mcmagiccore.bungee.BungeeUtil;
import us.mcmagic.pixiedust.PixieDust;
import us.mcmagic.pixiedust.handlers.GameTeam;
import us.mcmagic.pixiedust.handlers.PowerupType;
import us.mcmagic.pixiedust.threads.Shoot;
import us.mcmagic.pixiedust.utils.GameUtil;
import us.mcmagic.pixiedust.utils.PowerupUtil;
import us.mcmagic.pixiedust.utils.ReloadUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class PlayerInteract implements Listener {
    public static HashSet<Byte> ignoreBlocks = getIgnoredBlocks();
    public static List<UUID> seeker = new ArrayList<>();

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.PHYSICAL)) {
            return;
        }
        event.setCancelled(true);
        final Player player = event.getPlayer();
        final ItemStack item = player.getInventory().getItemInHand();
        if (item == null) {
            return;
        }
        if (!item.getType().equals(Material.BLAZE_ROD)) {
            switch (item.getType()) {
                case WOOL:
                    GameUtil.joinTeam(player, item);
                    return;
                case COMPASS:
                    if (!GameUtil.getTeam(GameTeam.SPECTATOR).contains(player.getUniqueId())) {
                        return;
                    }
                    GameUtil.openSpectateInventory(player);
                    return;
                case BED:
                    player.sendMessage(ChatColor.BLUE + "Now joining the " + ChatColor.AQUA + "Arcade...");
                    BungeeUtil.sendToServer(player, "Arcade");
                    return;
                case SLIME_BLOCK:
                    PowerupUtil.activatePowerup(player, PowerupType.LAUNCH);
                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 100, 2);
                    return;
            }
            return;
        }
        if (ReloadUtil.reloading.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Reloading!");
            return;
        }
        ReloadUtil.setReloading(player);
        List<Block> los = player.getLineOfSight(getIgnoredBlocks(), 70);
        int index = los.size() - 2;
        final Location source = player.getEyeLocation();
        final Location destination;
        int multiplier;
        if (seeker.contains(player.getUniqueId())) {
            Player tp = nearestEnemy(player);
            if (tp != null) {
                destination = tp.getLocation().add(.5, .5, .5);
                player.playSound(player.getLocation(), Sound.FIZZ, 10, 0);
                seeker.remove(player.getUniqueId());
                multiplier = 1;
            } else {
                MCMagicCore.gameManager.message(player, ChatColor.RED +
                        "No enemy found within 50 blocks, firing normally.");
                player.playSound(player.getLocation(), Sound.FIZZ, 10, 1);
                destination = los.get((index < 0) ? 0 : index).getLocation().add(.5, .5, .5);
                multiplier = 0;
            }
        } else {
            destination = los.get((index < 0) ? 0 : index).getLocation().add(.5, .5, .5);
            player.playSound(player.getLocation(), Sound.FIZZ, 10, 1);
            multiplier = 0;
        }
        GameUtil.setPixie(player.getUniqueId(), Bukkit.getScheduler().runTaskTimer(PixieDust.getInstance(),
                new Shoot(player, source, destination, multiplier), 0L, 1L).getTaskId());
    }

    private static void onFireworkExplode(Player player) {
        Integer taskID = GameUtil.playerPixie.remove(player.getUniqueId());
        if (taskID != null) {
            Bukkit.getScheduler().cancelTask(taskID);
        }
    }

    public static double[] getIncrement(Location destination, Location source, int max) {
        Location vector = destination.subtract(source);
        double xInc = vector.getX() / ((double) max), yInc = vector.getY() / ((double) max), zInc = vector.getZ()
                / ((double) max);
        return new double[]{xInc, yInc, zInc};
    }

    private static Player nearestEnemy(Player player) {
        Player tp = null;
        List<UUID> players;
        if (GameUtil.getTeam(GameTeam.YELLOW).contains(player.getUniqueId())) {
            players = GameUtil.getTeam(GameTeam.PURPLE);
        } else {
            players = GameUtil.getTeam(GameTeam.YELLOW);
        }
        for (UUID uuid : players) {
            Player temp = Bukkit.getPlayer(uuid);
            if (temp.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }
            if (tp == null) {
                tp = temp;
                continue;
            }
            if (temp.getLocation().distance(player.getLocation()) < tp.getLocation().distance(player.getLocation())
                    && temp.getLocation().distance(player.getLocation()) < 50) {
                tp = temp;
            }
        }
        return tp;
    }

    @SuppressWarnings("deprecation")
    static HashSet<Byte> getIgnoredBlocks() {
        HashSet<Byte> materials = new HashSet<>();
        materials.add((byte) Material.AIR.getId());
        materials.add((byte) Material.CARPET.getId());
        materials.add((byte) Material.TORCH.getId());
        materials.add((byte) Material.DOUBLE_PLANT.getId());
        materials.add((byte) Material.WATER.getId());
        materials.add((byte) Material.STATIONARY_WATER.getId());
        materials.add((byte) Material.LAVA.getId());
        materials.add((byte) Material.STATIONARY_LAVA.getId());
        materials.add((byte) Material.VINE.getId());
        return materials;
    }
}