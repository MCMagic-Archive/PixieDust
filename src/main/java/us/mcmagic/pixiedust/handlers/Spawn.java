package us.mcmagic.pixiedust.handlers;

import org.bukkit.Location;

/**
 * Created by Marc on 1/23/15
 */
public class Spawn {
    private int id;
    private Location location;
    private boolean used = false;

    public Spawn(int id, Location location) {
        this.id = id;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
