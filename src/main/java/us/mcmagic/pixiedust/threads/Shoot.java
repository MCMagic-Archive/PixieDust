package us.mcmagic.pixiedust.threads;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import us.mcmagic.mcmagiccore.MCMagicCore;
import us.mcmagic.mcmagiccore.particles.ParticleEffect;
import us.mcmagic.mcmagiccore.particles.ParticleUtil;
import us.mcmagic.pixiedust.PixieDust;
import us.mcmagic.pixiedust.handlers.GameTeam;
import us.mcmagic.pixiedust.handlers.PlayerData;
import us.mcmagic.pixiedust.listeners.PlayerInteract;
import us.mcmagic.pixiedust.utils.GameUtil;
import us.mcmagic.pixiedust.utils.ScoreboardUtil;

import java.util.List;
import java.util.UUID;

/**
 * Created by Marc on 1/30/15
 */
public class Shoot implements Runnable {
    private Location loc;
    private Player player;
    private boolean done = false;
    private int iter = 0;
    private int max;
    private double[] increment;
    private int multiplier;

    public Shoot(Player player, Location loc, Location destination, int multiplier) {
        this.player = player;
        this.loc = loc;
        this.max = (int) Math.ceil(destination.distance(loc));
        this.increment = PlayerInteract.getIncrement(destination, loc, max);
        this.multiplier = multiplier;
    }

    @Override
    public void run() {
        for (int i = 0; i < 6; i++) {
            if (!done) {
                if (iter < max) {
                    loc.add(increment[0], increment[1], increment[2]);
                    ParticleUtil.spawnParticle(ParticleEffect.FIREWORKS_SPARK, loc, 0.2f, 0.2f, 0.2f, 0.1f, 2);
                    List<UUID> players;
                    if (ScoreboardUtil.getTeam(player).equals(GameTeam.YELLOW)) {
                        players = GameUtil.getTeam(GameTeam.PURPLE);
                    } else {
                        players = GameUtil.getTeam(GameTeam.YELLOW);
                    }
                    for (UUID uuid : players) {
                        Player tp = Bukkit.getPlayer(uuid);
                        if (tp == null) {
                            continue;
                        }
                        if (tp.getUniqueId().equals(player.getUniqueId())) {
                            continue;
                        }
                        if (GameUtil.getTeam(GameTeam.SPECTATOR).contains(tp.getUniqueId())) {
                            continue;
                        }
                        if (!tp.hasPotionEffect(PotionEffectType.SPEED)) {
                            continue;
                        }
                        if (tp.getLocation().add(0, 1, 0).distanceSquared(loc) < 3.0) {
                            MCMagicCore.gameManager.broadcast(ScoreboardUtil.chatColor(player) +
                                    player.getName() + ChatColor.GREEN + " hit " + ScoreboardUtil.chatColor(tp) +
                                    tp.getName());
                            GameUtil.addPoint(1, ScoreboardUtil.getTeam(player));
                            PlayerData data = PixieDust.getInstance().getPlayerData(player.getUniqueId());
                            data.addHits(1);
                            data.addMoney(multiplier);
                            ScoreboardUtil.updateHits(player);
                            explode(loc, ScoreboardUtil.getTeam(player));
                            player.playSound(player.getLocation(), Sound.LEVEL_UP, 100, 1);
                            PixieDust.gameUtil.hit(tp);
                            tp.playSound(player.getLocation(), Sound.LEVEL_UP, 100, 1);
                        }
                    }
                } else if (iter == max) {
                    explode(loc, ScoreboardUtil.getTeam(player));
                }
                iter++;
            }
        }
    }

    private void explode(Location loc, GameTeam team) {
        switch (team) {
            case YELLOW:
                done = true;
                final Firework yfw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
                FireworkMeta yfm = yfw.getFireworkMeta();
                FireworkEffect yfe = FireworkEffect.builder().with(FireworkEffect.Type.BURST).withColor(Color.YELLOW)
                        .withTrail().build();
                yfm.addEffect(yfe);
                yfm.setPower(0);
                yfw.setFireworkMeta(yfm);
                yfw.setVelocity(new Vector(0, -0.05, 0));
                onFireworkExplode(player);
                Bukkit.getScheduler().runTaskLater(PixieDust.getInstance(), yfw::detonate, 1L);
                return;
            case PURPLE:
                done = true;
                final Firework pfw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
                FireworkMeta pfm = pfw.getFireworkMeta();
                FireworkEffect pfe = FireworkEffect.builder().with(FireworkEffect.Type.BURST).withColor(Color.PURPLE)
                        .withTrail().build();
                pfm.addEffect(pfe);
                pfm.setPower(0);
                pfw.setFireworkMeta(pfm);
                pfw.setVelocity(new Vector(0, -0.05, 0));
                onFireworkExplode(player);
                Bukkit.getScheduler().runTaskLater(PixieDust.getInstance(), pfw::detonate, 1L);
        }
    }

    private void onFireworkExplode(Player player) {
        Integer taskID = GameUtil.playerPixie.remove(player.getUniqueId());
        if (taskID != null) {
            Bukkit.getScheduler().cancelTask(taskID);
        }
    }
}