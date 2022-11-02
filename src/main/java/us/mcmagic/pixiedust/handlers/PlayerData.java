package us.mcmagic.pixiedust.handlers;

import java.util.UUID;

/**
 * Created by Marc on 3/1/15
 */
public class PlayerData {
    private UUID uuid;
    private String name;
    private int jumps;
    private double delay;
    private int hits = 0;
    private int money = 0;

    public PlayerData(UUID uuid, String name, double delay, int jumps) {
        this.uuid = uuid;
        this.name = name;
        this.jumps = jumps;
        this.delay = delay;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public int getJumps() {
        return jumps;
    }

    public double getDelay() {
        return delay;
    }

    public int getHits() {
        return hits;
    }

    public int getMoney() {
        return money;
    }

    public void setJumps(int jumps) {
        this.jumps = jumps;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public void addHits(int amount) {
        hits += amount;
    }

    public void addJumps(int amount) {
        jumps += amount;
    }

    public void addMoney(int amount) {
        money += amount;
    }
}
