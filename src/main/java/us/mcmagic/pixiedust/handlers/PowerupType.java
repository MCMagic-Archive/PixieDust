package us.mcmagic.pixiedust.handlers;

import net.md_5.bungee.api.ChatColor;

/**
 * Created by Marc on 1/23/15
 */
public enum PowerupType {
    JUMP(ChatColor.LIGHT_PURPLE + "Extra Jump Boost", 0), LAUNCH(ChatColor.DARK_GREEN + "Launch", 1),
    SEEKER(ChatColor.AQUA + "Seeker", 2);

    public String disName;
    public int id;

    PowerupType(String disName, int id) {
        this.disName = disName;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return disName;
    }

    public static PowerupType fromString(String s) {
        switch (s) {
            case "jump":
                return JUMP;
            case "launch":
                return LAUNCH;
            case "seeker":
                return SEEKER;
            default:
                return null;
        }
    }

    public static PowerupType fromInt(int i) {
        switch (i) {
            case 1:
                return JUMP;
            case 2:
                return LAUNCH;
            case 3:
                return SEEKER;
            default:
                return null;
        }
    }
}
