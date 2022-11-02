package us.mcmagic.pixiedust.handlers;

import org.bukkit.ChatColor;

/**
 * Created by Marc on 1/23/15
 */
public enum GameTeam {
    YELLOW(ChatColor.YELLOW, "Yellow"), PURPLE(ChatColor.LIGHT_PURPLE, "Purple"), SPECTATOR(ChatColor.GRAY, "Spectator");

    public ChatColor color;
    public String prefix;


    GameTeam(ChatColor color, String prefix) {
        this.color = color;
        this.prefix = prefix;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getNameWithBrackets() {
        return ChatColor.WHITE + "[" + color + prefix + ChatColor.WHITE + "]";
    }

    public String getName() {
        return prefix;
    }

    public static GameTeam fromString(String name) {
        switch (name.toLowerCase()) {
            case "yellow":
                return YELLOW;
            case "blue":
                return PURPLE;
            case "spectator":
                return SPECTATOR;
            default:
                return SPECTATOR;
        }
    }
}
