package us.mcmagic.pixiedust.threads;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import us.mcmagic.mcmagiccore.MCMagicCore;
import us.mcmagic.mcmagiccore.itemcreator.ItemCreator;
import us.mcmagic.mcmagiccore.particles.ParticleEffect;
import us.mcmagic.mcmagiccore.particles.ParticleUtil;
import us.mcmagic.pixiedust.PixieDust;
import us.mcmagic.pixiedust.handlers.Powerup;
import us.mcmagic.pixiedust.handlers.PowerupType;
import us.mcmagic.pixiedust.listeners.PlayerInteract;
import us.mcmagic.pixiedust.utils.PowerupUtil;
import us.mcmagic.pixiedust.utils.ScoreboardUtil;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Marc on 1/27/15
 */
public class GameThread {
    private static HashMap<String, Integer> list = new HashMap<>();
    private static HashMap<PowerupType, Powerup> map = new HashMap<>();
    private static int time = 600;

    public static void start() {
        list.put("main", Bukkit.getScheduler().runTaskTimer(PixieDust.getInstance(), new Runnable() {
            int i = 1;

            @Override
            public void run() {
                if (i > 3) {
                    i = 1;
                }
                PowerupType type = PowerupType.fromInt(i);
                if (map.containsKey(type)) {
                    i++;
                    return;
                }
                final Location loc = PowerupUtil.getLocation(type);
                if (type != null) {
                    if (!loc.getChunk().isLoaded()) {
                        loc.getChunk().load();
                    }
                    final Item item = loc.getWorld().dropItem(loc, new ItemCreator(PowerupUtil.getMaterial(type)));
                    item.setMetadata("game", new FixedMetadataValue(PixieDust.getInstance(), true));
                    item.setCustomName(type.getDisplayName());
                    item.setCustomNameVisible(true);
                    item.setVelocity(new Vector(0, 0, 0));
                    Powerup powerup = new Powerup(type, loc, item);
                    map.put(type, powerup);
                    MCMagicCore.gameManager.broadcast("A " + type.getDisplayName() + ChatColor.GREEN +
                            " Powerup has spawned!");
                    i++;
                }
            }
        }, 0, 600L).getTaskId());
    }

    public static void pickupPowerup(Player player, PowerupType type, ItemStack item) {
        if (map.containsKey(type)) {
            map.remove(type);
        }
        MCMagicCore.gameManager.broadcast("" + ScoreboardUtil.chatColor(player) + player.getName() +
                ChatColor.GREEN + " has picked up the " + type.getDisplayName() + ChatColor.GREEN + " Powerup!");
        ParticleUtil.spawnParticle(ParticleEffect.FIREWORKS_SPARK, player.getLocation().add(0, 1.3, 0), 0.1f, 0.1f, 0.1f, 0.1f, 15);
        if (type.equals(PowerupType.SEEKER)) {
            PlayerInteract.seeker.add(player.getUniqueId());
            MCMagicCore.gameManager.message(player, "The next Trail you shoot will target the nearest Enemy Player!");
        }
        if (type.equals(PowerupType.LAUNCH)) {
            ItemStack i = new ItemCreator(item.getType(), ChatColor.GOLD + "Click to Activate!", new ArrayList<>());
            player.getInventory().setItem(4, i);
        }
    }
}