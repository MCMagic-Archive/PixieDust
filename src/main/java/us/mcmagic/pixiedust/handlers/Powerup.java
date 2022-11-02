package us.mcmagic.pixiedust.handlers;

import org.bukkit.Location;
import org.bukkit.entity.Item;

/**
 * Created by Marc on 1/27/15
 */
public class Powerup {
    private PowerupType type;
    private Location location;
    private Item item;

    public Powerup(PowerupType type, Location location, Item item) {
        this.type = type;
        this.location = location;
        this.item = item;
    }

    public PowerupType getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }

    public Item getItem() {
        return item;
    }
}
