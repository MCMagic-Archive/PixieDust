package us.mcmagic.pixiedust.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.mcmagic.mcmagiccore.bungee.BungeeUtil;

/**
 * Created by Marc on 2/22/15
 */
public class Commandhub implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can do this!");
            return true;
        }
        Player player = (Player) sender;
        player.sendMessage(ChatColor.BLUE + "Now returning to " + ChatColor.AQUA + "Arcade...");
        BungeeUtil.sendToServer(player, "Arcade");
        return true;
    }
}
