package us.mcmagic.pixiedust.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.mcmagic.pixiedust.PixieDust;
import us.mcmagic.pixiedust.handlers.PlayerData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReloadUtil {
    public static List<UUID> reloading = new ArrayList<>();

    public static void setReloading(final Player player) {
        reloading.add(player.getUniqueId());
        PlayerData data = PixieDust.getInstance().getPlayerData(player.getUniqueId());
        double delay = data.getDelay();
        final Long ticks = (long) delay * 20;
        player.setExp(1.0F);
        final int infoxp = Bukkit.getScheduler().runTaskTimer(PixieDust.getInstance(), () -> {
            float xp = player.getExp();
            xp -= getdecr(ticks);
            if (xp >= 1.0F) {
                xp = 1.0F;
            }
            player.setExp(xp);
        }, 0L, 2L).getTaskId();
        Bukkit.getScheduler().runTaskLater(PixieDust.getInstance(), () -> {
            player.setExp(0.0F);
            Bukkit.getScheduler().cancelTask(infoxp);
            reloading.remove(player.getUniqueId());
        }, ticks);
    }

    private static float getdecr(Long time) {
        float result;
        float temp = (float) time;
        result = 100.0F / (temp / 2.0F) / 100.0F;
        return result;
    }
}