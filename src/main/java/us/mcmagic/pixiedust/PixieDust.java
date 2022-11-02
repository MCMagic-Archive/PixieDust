package us.mcmagic.pixiedust;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import us.mcmagic.mcmagiccore.MCMagicCore;
import us.mcmagic.mcmagiccore.arcade.ServerState;
import us.mcmagic.pixiedust.commands.Commandhub;
import us.mcmagic.pixiedust.commands.Commandstartgame;
import us.mcmagic.pixiedust.handlers.GameState;
import us.mcmagic.pixiedust.handlers.PlayerData;
import us.mcmagic.pixiedust.listeners.*;
import us.mcmagic.pixiedust.utils.GameUtil;
import us.mcmagic.pixiedust.utils.PowerupUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

public class PixieDust extends JavaPlugin {
    public static HashMap<UUID, Double> delayMap = new HashMap<>();
    private HashMap<UUID, PlayerData> data = new HashMap<>();
    private static PixieDust instance;
    public static GameUtil gameUtil;
    public static World world;

    @Override
    public void onEnable() {
        instance = this;
        GameState.setState(GameState.SERVER_STARTING);
        world = Bukkit.getWorld("world");
        gameUtil = new GameUtil();
        world.setGameRuleValue("keepInventory", "true");
        world.setGameRuleValue("doMobSpawning", "false");
        registerCommands();
        registerListeners();
        MCMagicCore.gameManager.setGameData("Pixie Dust Shootout", "     Pixie Dust Shootout",
                new String[]{ChatColor.GOLD + "Using your " + ChatColor.YELLOW + "Pixie Wand, " + ChatColor.GOLD +
                        "hit players on the", ChatColor.RED + "Enemy Team" + ChatColor.GOLD + "! The first team to " +
                        ChatColor.BLUE + "50 hits" + ChatColor.GOLD + " wins!"}, gameUtil.getMinPlayers(),
                gameUtil.getMaxPlayers(), 15);
        Bukkit.getLogger().info("Let's shoot some Pixie Dust!");
        Bukkit.getScheduler().runTaskLater(this, () -> {
            GameState.setState(GameState.IN_LOBBY);
            MCMagicCore.gameManager.setState(MCMagicCore.getMCMagicConfig().serverName, ServerState.ONLINE);
        }, 50L);
    }

    @Override
    public void onDisable() {
        Bukkit.getWorlds().get(0).getEntities().stream().filter(entity -> !entity.getType().equals(EntityType.PLAYER))
                .forEach(Entity::remove);
        getLogger().info("I think I ran out of Pixie Dust...");
        MCMagicCore.gameManager.setState(MCMagicCore.getMCMagicConfig().serverName, ServerState.RESTARTING);
        MCMagicCore.gameManager.setPlayerCount(MCMagicCore.getMCMagicConfig().serverName, 0);
    }

    public static PixieDust getInstance() {
        return instance;
    }

    public static World getWorld() {
        return world;
    }

    public void addPlayerData(UUID uuid, PlayerData data) {
        this.data.put(uuid, data);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return data.get(uuid);
    }

    public void removePlayerData(UUID uuid) {
        data.remove(uuid);
    }

    private void registerCommands() {
        getCommand("hub").setExecutor(new Commandhub());
        getCommand("hub").setAliases(Collections.singletonList("lobby"));
        getCommand("startgame").setExecutor(new Commandstartgame());
    }

    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new BlockListener(), this);
        pm.registerEvents(new GameUtil(), this);
        pm.registerEvents(new PowerupUtil(), this);
        pm.registerEvents(new PlayerChat(), this);
        pm.registerEvents(new PlayerCombust(), this);
        pm.registerEvents(new PlayerDamage(), this);
        pm.registerEvents(new PlayerDoubleJump(), this);
        pm.registerEvents(new PlayerDropItem(), this);
        pm.registerEvents(new PlayerHunger(), this);
        pm.registerEvents(new PlayerInteract(), this);
        pm.registerEvents(new PlayerInventoryClick(), this);
        pm.registerEvents(new PlayerJoinAndLeave(), this);
        pm.registerEvents(new WeatherListener(), this);
    }
}
