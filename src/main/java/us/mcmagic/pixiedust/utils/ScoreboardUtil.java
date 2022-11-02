package us.mcmagic.pixiedust.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import us.mcmagic.mcmagiccore.MCMagicCore;
import us.mcmagic.pixiedust.PixieDust;
import us.mcmagic.pixiedust.handlers.GameTeam;
import us.mcmagic.pixiedust.handlers.PlayerData;

public class ScoreboardUtil {
    public static ScoreboardManager sbm = Bukkit.getScoreboardManager();

    public static void initialize(Player player) {
        Scoreboard sb = sbm.getNewScoreboard();
        Team yellow = sb.registerNewTeam("yellow");
        Team purple = sb.registerNewTeam("purple");
        Team spectator = sb.registerNewTeam("spectator");
        yellow.setPrefix(ChatColor.YELLOW + "");
        purple.setPrefix(ChatColor.LIGHT_PURPLE + "");
        spectator.setPrefix(ChatColor.GRAY + "");
        yellow.setDisplayName("Yellow");
        purple.setDisplayName("Purple");
        spectator.setDisplayName("Spectator");
        spectator.setCanSeeFriendlyInvisibles(true);
        player.setScoreboard(sb);
    }

    public static void setObjective(Player player) {
        PlayerData data = PixieDust.getInstance().getPlayerData(player.getUniqueId());
        Objective obj = player.getScoreboard().registerNewObjective("main", "dummy");
        obj.setDisplayName(ChatColor.WHITE + "[" + ChatColor.GOLD + "Pixie Dust Shootout" + ChatColor.WHITE + "]");
        Score yellow = obj.getScore(ChatColor.YELLOW + "Yellow: " + ChatColor.GREEN + GameUtil.getPoints(GameTeam.YELLOW));
        Score purple = obj.getScore(ChatColor.LIGHT_PURPLE + "Purple: " + ChatColor.GREEN + GameUtil.getPoints(GameTeam.PURPLE));
        Score hits;
        Score boosts;
        if (data != null) {
            hits = obj.getScore(ChatColor.GREEN + "Hits: " + chatColor(player) + "" + ChatColor.BOLD + data.getHits());
            boosts = obj.getScore(ChatColor.GREEN + "Jumps: " + chatColor(player) + "" + ChatColor.BOLD + data.getJumps());
        } else {
            hits = obj.getScore(ChatColor.GREEN + "Hits: " + ChatColor.GRAY + "Spectator");
            boosts = obj.getScore(ChatColor.GREEN + "Jumps: " + ChatColor.GRAY + "Spectator");
        }
        yellow.setScore(4);
        purple.setScore(3);
        hits.setScore(2);
        boosts.setScore(1);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        Scoreboard sb = player.getScoreboard();
        for (Player tp : Bukkit.getOnlinePlayers()) {
            GameTeam team = getTeam(tp);
            switch (team) {
                case YELLOW:
                    sb.getTeam("yellow").addEntry(tp.getName());
                    break;
                case PURPLE:
                    sb.getTeam("purple").addEntry(tp.getName());
                    break;
                case SPECTATOR:
                    sb.getTeam("spectator").addEntry(tp.getName());
                    break;
            }
        }
    }

    public static GameTeam getTeam(Player player) {
        if (GameUtil.getTeam(GameTeam.YELLOW).contains(player.getUniqueId())) {
            return GameTeam.YELLOW;
        } else if (GameUtil.getTeam(GameTeam.PURPLE).contains(player.getUniqueId())) {
            return GameTeam.PURPLE;
        }
        return GameTeam.SPECTATOR;
    }

    public static void updateJumps(Player player) {
        Scoreboard sb = player.getScoreboard();
        PlayerData data = PixieDust.getInstance().getPlayerData(player.getUniqueId());
        int old = data.getJumps() + 1;
        Score jumps = sb.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.GREEN + "Jumps: " + chatColor(player) +
                "" + ChatColor.BOLD + data.getJumps());
        jumps.setScore(1);
        sb.resetScores(ChatColor.GREEN + "Jumps: " + chatColor(player) + "" + ChatColor.BOLD + old);
    }

    public static void updateHits(Player player) {
        Scoreboard sb = player.getScoreboard();
        PlayerData data = PixieDust.getInstance().getPlayerData(player.getUniqueId());
        int old = data.getHits() - 1;
        Score hits = sb.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.GREEN + "Hits: " + chatColor(player) +
                "" + ChatColor.BOLD + data.getHits());
        hits.setScore(1);
        sb.resetScores(ChatColor.GREEN + "Hits: " + chatColor(player) + "" + ChatColor.BOLD + old);
    }

    public static void joinTeam(Player player, GameTeam team) {
        switch (team) {
            case YELLOW:
                MCMagicCore.gameManager.message(player, ChatColor.GREEN + "You are now on the " +
                        ChatColor.YELLOW + "Yellow Team");
                for (Player tp : Bukkit.getOnlinePlayers()) {
                    tp.getScoreboard().getTeam("yellow").addEntry(player.getName());
                }
                GameUtil.joinTeam(GameTeam.YELLOW, player.getUniqueId());
                return;
            case PURPLE:
                MCMagicCore.gameManager.message(player, ChatColor.GREEN + "You are now on the " +
                        ChatColor.LIGHT_PURPLE + "Purple Team");
                for (Player tp : Bukkit.getOnlinePlayers()) {
                    tp.getScoreboard().getTeam("purple").addEntry(player.getName());
                }
                GameUtil.joinTeam(GameTeam.PURPLE, player.getUniqueId());
                return;
            case SPECTATOR:
                MCMagicCore.gameManager.message(player, ChatColor.GREEN + "You are now a " +
                        ChatColor.GRAY + "Spectator!");
                for (Player tp : Bukkit.getOnlinePlayers()) {
                    tp.getScoreboard().getTeam("spectator").addEntry(player.getName());
                }
                GameUtil.joinTeam(GameTeam.SPECTATOR, player.getUniqueId());
        }
    }

    public static ChatColor chatColor(Player player) {
        if (GameUtil.getTeam(GameTeam.YELLOW).contains(player.getUniqueId())) {
            return ChatColor.YELLOW;
        }
        if (GameUtil.getTeam(GameTeam.PURPLE).contains(player.getUniqueId())) {
            return ChatColor.LIGHT_PURPLE;
        }
        return ChatColor.RED;
    }
}