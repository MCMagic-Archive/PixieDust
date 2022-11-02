package us.mcmagic.pixiedust.utils;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;
import org.json.JSONArray;
import org.json.JSONObject;
import us.mcmagic.mcmagiccore.MCMagicCore;
import us.mcmagic.mcmagiccore.arcade.GameStartEvent;
import us.mcmagic.mcmagiccore.arcade.ServerState;
import us.mcmagic.mcmagiccore.bungee.BungeeUtil;
import us.mcmagic.mcmagiccore.itemcreator.ItemCreator;
import us.mcmagic.pixiedust.PixieDust;
import us.mcmagic.pixiedust.handlers.*;
import us.mcmagic.pixiedust.threads.GameThread;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GameUtil implements Listener {
    private static List<UUID> yellow = new ArrayList<>();
    private static List<UUID> purple = new ArrayList<>();
    private static List<UUID> spectator = new ArrayList<>();
    private static int yellowPoints = 0;
    private static int purplePoints = 0;
    public static ItemStack yellowTeamWool;
    public static ItemStack purpleTeamWool;
    public static ItemStack leaveBed = new ItemCreator(Material.BED, ChatColor.GREEN + "Return to Arcade",
            Collections.singletonList(ChatColor.YELLOW + "Right-Click to return to the Arcade"));
    public static ItemStack clear = new ItemStack(Material.AIR);
    public static ItemStack[] yellowArmor;
    public static ItemStack[] purpleArmor;
    public static ItemStack wand;
    public static Location lobby;
    public static int minPlayers;
    public static int maxPlayers;
    private HashMap<Integer, Spawn> spawns = new HashMap<>();
    public static final HashMap<UUID, Integer> playerPixie = new HashMap<>();
    public static final HashMap<String, Integer> triplePixie = new HashMap<>();
    private static int currentSpawn = 1;
    private static boolean over = false;
    private String map;

    public GameUtil() {
        yellowTeamWool = new ItemCreator(Material.WOOL, 1, (byte) 4, ChatColor.YELLOW + "Join the Yellow Team",
                Arrays.asList(ChatColor.GOLD + "Click to join the", ChatColor.GOLD + "Yellow Team!"));
        purpleTeamWool = new ItemCreator(Material.WOOL, 1, (byte) 10, ChatColor.LIGHT_PURPLE + "Join the Purple Team",
                Arrays.asList(ChatColor.GOLD + "Click to join the", ChatColor.GOLD + "Purple Team!"));
        // Yellow
        ItemStack yhel = new ItemStack(Material.LEATHER_HELMET, 1);
        ItemStack yche = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
        ItemStack yleg = new ItemStack(Material.LEATHER_LEGGINGS, 1);
        ItemStack yboo = new ItemStack(Material.LEATHER_BOOTS, 1);
        LeatherArmorMeta yhelm = (LeatherArmorMeta) yhel.getItemMeta();
        LeatherArmorMeta ychem = (LeatherArmorMeta) yche.getItemMeta();
        LeatherArmorMeta ylegm = (LeatherArmorMeta) yleg.getItemMeta();
        LeatherArmorMeta yboom = (LeatherArmorMeta) yboo.getItemMeta();
        yhelm.setColor(Color.fromRGB(255, 222, 0));
        ychem.setColor(Color.fromRGB(255, 222, 0));
        ylegm.setColor(Color.fromRGB(255, 222, 0));
        yboom.setColor(Color.fromRGB(255, 222, 0));
        yhelm.setDisplayName(ChatColor.YELLOW + "Yellow Armor");
        ychem.setDisplayName(ChatColor.YELLOW + "Yellow Armor");
        ylegm.setDisplayName(ChatColor.YELLOW + "Yellow Armor");
        yboom.setDisplayName(ChatColor.YELLOW + "Yellow Armor");
        yhel.setItemMeta(yhelm);
        yche.setItemMeta(ychem);
        yleg.setItemMeta(ylegm);
        yboo.setItemMeta(yboom);
        // Purple
        ItemStack phel = new ItemStack(Material.LEATHER_HELMET, 1);
        ItemStack pche = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
        ItemStack pleg = new ItemStack(Material.LEATHER_LEGGINGS, 1);
        ItemStack pboo = new ItemStack(Material.LEATHER_BOOTS, 1);
        LeatherArmorMeta phelm = (LeatherArmorMeta) phel.getItemMeta();
        LeatherArmorMeta pchem = (LeatherArmorMeta) pche.getItemMeta();
        LeatherArmorMeta plegm = (LeatherArmorMeta) pleg.getItemMeta();
        LeatherArmorMeta pboom = (LeatherArmorMeta) pboo.getItemMeta();
        phelm.setColor(Color.fromRGB(39, 31, 155));
        pchem.setColor(Color.fromRGB(39, 31, 155));
        plegm.setColor(Color.fromRGB(39, 31, 155));
        pboom.setColor(Color.fromRGB(39, 31, 155));
        phelm.setDisplayName(ChatColor.DARK_PURPLE + "Purple Armor");
        pchem.setDisplayName(ChatColor.DARK_PURPLE + "Purple Armor");
        plegm.setDisplayName(ChatColor.DARK_PURPLE + "Purple Armor");
        pboom.setDisplayName(ChatColor.DARK_PURPLE + "Purple Armor");
        phel.setItemMeta(phelm);
        pche.setItemMeta(pchem);
        pleg.setItemMeta(plegm);
        pboo.setItemMeta(pboom);
        wand = new ItemCreator(Material.BLAZE_ROD, ChatColor.YELLOW + "Pixie Wand",
                Collections.singletonList(ChatColor.GOLD + "Click to Shoot!"));
        ItemMeta meta = wand.getItemMeta();
        meta.addEnchant(Enchantment.DIG_SPEED, 1, false);
        wand.setItemMeta(meta);
        yellowArmor = new ItemStack[]{yboo, yleg, yche, yhel};
        purpleArmor = new ItemStack[]{pboo, pleg, pche, phel};
        File file = new File("map.yml");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String line; (line = br.readLine()) != null; ) {
                map = line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadConfigurations();
//        double x = config.getDouble("spawn.x");
//        double y = config.getDouble("spawn.y");
//        double z = config.getDouble("spawn.z");
//        float yaw = config.getInt("spawn.yaw");
//        float pitch = config.getInt("spawn.pitch");
//        lobby = new Location(PixieDust.world, x, y, z, yaw, pitch);
//        minPlayers = config.getInt("min-players");
//        maxPlayers = config.getInt("max-players");
//        for (int i = 1; i < config.getInt("spawn-amount") + 1; i++) {
//            spawns.add(new Spawn(i, new Location(PixieDust.world, config.getDouble("spawns." + i + ".x"),
//                    config.getDouble("spawns." + i + ".y"), config.getDouble("spawns." + i + ".z"))));
//        }
    }

    private void loadConfigurations() {
        String locations = "https://spreadsheets.google.com/feeds/cells/1v4GPRag2QkY0uYue3nWnunApy_7GSWmavJdLFeR7FlA/1/public/values?alt=json";
        JSONObject obj = readJsonFromUrl(locations);
        if (obj == null) {
            return;
        }
        JSONArray array = obj.getJSONObject("feed").getJSONArray("entry");
        boolean isMap = false;
        for (int i = 0; i < array.length(); i++) {
            JSONObject ob = array.getJSONObject(i);
            JSONObject d = ob.getJSONObject("content");
            JSONObject id = ob.getJSONObject("title");
            String column = id.getString("$t");
            Integer row = Integer.parseInt(column.substring(1, 2));
            if (column.substring(0, 1).equalsIgnoreCase("a")) {
                isMap = d.getString("$t").equalsIgnoreCase(getMap());
                continue;
            }
            if (!isMap) {
                continue;
            }
            String data = d.getString("$t");
            String[] split = data.split(":");
            String type = split[0].toLowerCase();
            String info = split[1];
            if (type.equals("max")) {
                maxPlayers = Integer.parseInt(info);
                continue;
            }
            if (type.equals("min")) {
                minPlayers = Integer.parseInt(info);
                continue;
            }
            String[] coords = info.split(",");
            double x = Double.parseDouble(coords[0]);
            double y = Double.parseDouble(coords[1]);
            double z = Double.parseDouble(coords[2]);
            Location loc = getLocation(x, y, z);
            if (type.equals("jump")) {
                PowerupUtil.addLocation(PowerupType.fromString("jump"), loc);
                continue;
            }
            if (type.equals("launch")) {
                PowerupUtil.addLocation(PowerupType.fromString("launch"), loc);
                continue;
            }
            if (type.equals("seeker")) {
                PowerupUtil.addLocation(PowerupType.fromString("seeker"), loc);
                continue;
            }
            float yaw = Float.parseFloat(coords[3]);
            float pitch = Float.parseFloat(coords[4]);
            loc = getLocation(x, y, z, yaw, pitch);
            if (type.equals("lobby")) {
                lobby = loc;
                continue;
            }
            int spawn = Integer.parseInt(type.replace("spawn", ""));
            if (spawns.containsKey(spawn)) {
                spawns.remove(spawn);
            }
            spawns.put(spawn, new Spawn(i + 1, loc));
        }
    }

    private Location getLocation(double x, double y, double z) {
        return getLocation(x, y, z, 0, 0);
    }

    private Location getLocation(double x, double y, double z, float yaw, float pitch) {
        return new Location(PixieDust.getWorld(), x, y, z, yaw, pitch);
    }

    private static JSONObject readJsonFromUrl(String url) {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public String getMap() {
        return map;
    }

    public static boolean noTeam(Player player) {
        return !(yellow.contains(player.getUniqueId()) || purple.contains(player.getUniqueId()));
    }

    public static List<UUID> getTeam(GameTeam team) {
        switch (team) {
            case YELLOW:
                return yellow;
            case PURPLE:
                return purple;
            case SPECTATOR:
                return spectator;
            default:
                return new ArrayList<>();
        }
    }

    public static void joinTeam(GameTeam team, UUID uuid) {
        System.out.println("Adding " + uuid.toString() + " to " + team.getName());
        switch (team) {
            case YELLOW:
                yellow.add(uuid);
                return;
            case PURPLE:
                purple.add(uuid);
                return;
            case SPECTATOR:
                spectator.add(uuid);
        }
    }

    public static void removeFromTeam(GameTeam team, UUID uuid) {
        System.out.println("Removing " + uuid.toString() + " from " + team.getName());
        switch (team) {
            case YELLOW:
                yellow.remove(uuid);
                return;
            case PURPLE:
                purple.remove(uuid);
                return;
            case SPECTATOR:
                spectator.remove(uuid);
        }
    }

    @EventHandler
    public void onGameStart(GameStartEvent event) {
        GameState.setState(GameState.IN_GAME);
        MCMagicCore.gameManager.setState(MCMagicCore.getMCMagicConfig().serverName, ServerState.INGAME);
        resetSpawns();
        Bukkit.getOnlinePlayers().stream().filter(player -> !purple.contains(player.getUniqueId()) &&
                !yellow.contains(player.getUniqueId())).forEach(player -> {
            if (purple.size() == teamSize()) {
                joinTeam(GameTeam.YELLOW, player.getUniqueId());
            } else {
                joinTeam(GameTeam.PURPLE, player.getUniqueId());
            }
        });
        for (Player player : Bukkit.getOnlinePlayers()) {
            ScoreboardUtil.setObjective(player);
            setArmor(player, ScoreboardUtil.getTeam(player));
            PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 100000, 3);
            player.addPotionEffect(speed);
            PlayerInventory pi = player.getInventory();
            pi.clear();
            pi.setItem(0, wand);
            pi.setHeldItemSlot(0);
            teleportToSpawn(player, true);
        }
        GameThread.start();
    }

    public static int getMinPlayers() {
        return minPlayers;
    }

    public static int getMaxPlayers() {
        return maxPlayers;
    }

    public static int getPoints(GameTeam team) {
        switch (team) {
            case YELLOW:
                return yellowPoints;
            case PURPLE:
                return purplePoints;
            default:
                return 0;
        }
    }

    public static void clearInventory(Player player) {
        PlayerInventory pi = player.getInventory();
        pi.clear();
        pi.setHelmet(clear);
        pi.setChestplate(clear);
        pi.setLeggings(clear);
        pi.setBoots(clear);
    }

    public static void setArmor(Player player, GameTeam team) {
        PlayerInventory pi = player.getInventory();
        switch (team) {
            case YELLOW:
                pi.setArmorContents(yellowArmor);
                return;
            case PURPLE:
                pi.setArmorContents(purpleArmor);
                return;
            case SPECTATOR:
                pi.clear();
        }
    }

    public void resetSpawns() {
        for (Spawn spawn : spawns.values()) {
            spawn.setUsed(false);
        }
    }

    public void teleportToSpawn(Player player, boolean first) {
        for (Spawn spawn : spawns.values()) {
            if (!spawn.isUsed()) {
                player.teleport(spawn.getLocation());
                spawn.setUsed(first);
                return;
            }
        }
    }

    public Location getSpawn(int spawn) {
        return spawns.get(spawn).getLocation();
    }

    public static void launchPlayer(Player player) {
        player.setVelocity(new Vector(0.0, 1, 0.0));
    }

    public void hit(final Player player) {
        if (currentSpawn == spawns.size() + 1) {
            currentSpawn = 1;
        }
        player.removePotionEffect(PotionEffectType.SPEED);
        player.teleport(getSpawn(currentSpawn));
        launchPlayer(player);
        currentSpawn++;
        Bukkit.getScheduler().runTaskLater(PixieDust.getInstance(), () -> player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 3)), 60L);
    }

    public static void win(GameTeam team) {
        if (over) {
            return;
        }
        GameState.setState(GameState.POST_GAME);
        Bukkit.getScheduler().runTaskAsynchronously(PixieDust.getInstance(), () ->
                Bukkit.getOnlinePlayers().stream().filter(tp -> yellow.contains(tp.getUniqueId()) ||
                        purple.contains(tp.getUniqueId())).forEach(tp -> {
                    PlayerData data = PixieDust.getInstance().getPlayerData(tp.getUniqueId());
                    MCMagicCore.gameManager.changeValue(tp, "pixie", MCMagicCore.gameManager.getValue(tp, "pixie") +
                            data.getHits());
                }));
        switch (team) {
            case YELLOW:
                MCMagicCore.gameManager.broadcast("The " + ChatColor.YELLOW + "Yellow Team " + ChatColor.GREEN +
                        "has won!");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.getInventory().clear();
                    PlayerData data = PixieDust.getInstance().getPlayerData(player.getUniqueId());
                    if (data == null) {
                        continue;
                    }
                    int score = data.getMoney();
                    score = (int) (score + (Math.ceil(data.getHits() / 2)));
                    if (yellow.contains(player.getUniqueId())) {
                        score += 5;
                        MCMagicCore.economy.addBalance(player.getUniqueId(), score);
                        MCMagicCore.gameManager.moneyMessage(player, score);
                    } else {
                        MCMagicCore.gameManager.moneyMessage(player, score);
                    }
                }
                break;
            case PURPLE:
                MCMagicCore.gameManager.broadcast("The " + ChatColor.DARK_PURPLE + "Purple Team " +
                        ChatColor.GREEN + "has won!");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.getInventory().clear();
                    PlayerData data = PixieDust.getInstance().getPlayerData(player.getUniqueId());
                    if (data == null) {
                        continue;
                    }
                    int score = data.getMoney();
                    score = (int) (score + (Math.ceil(data.getHits() / 2)));
                    if (purple.contains(player.getUniqueId())) {
                        score += 5;
                        MCMagicCore.economy.addBalance(player.getUniqueId(), score);
                        MCMagicCore.gameManager.moneyMessage(player, score);
                    } else {
                        MCMagicCore.gameManager.moneyMessage(player, score);
                    }
                }
                break;
        }
        Bukkit.getOnlinePlayers().stream().filter(player -> ReloadUtil.reloading.contains(player)).forEach(player -> {
            ReloadUtil.reloading.remove(player);
            player.setExp(0);
        });
        Bukkit.getScheduler().runTaskAsynchronously(PixieDust.getInstance(), () ->
                MCMagicCore.gameManager.setState(MCMagicCore.getMCMagicConfig().serverName, ServerState.RESTARTING));
        MCMagicCore.gameManager.broadcast("Returning to " + ChatColor.AQUA + "Arcade " + ChatColor.GREEN + "in 10 seconds...");
        Bukkit.getScheduler().runTaskLater(PixieDust.getInstance(), () -> {
            BungeeUtil.emptyServer();
            Bukkit.getScheduler().runTaskLater(PixieDust.getInstance(), Bukkit::shutdown, 100L);
        }, 200L);
    }

    public static void addPoint(int amount, GameTeam team) {
        switch (team) {
            case YELLOW:
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Scoreboard sb = player.getScoreboard();
                    Score score = sb.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.YELLOW + "Yellow: " +
                            ChatColor.GREEN + (yellowPoints + amount));
                    score.setScore(4);
                    sb.resetScores(ChatColor.YELLOW + "Yellow: " + ChatColor.GREEN + yellowPoints);
                }
                yellowPoints += amount;
                if (yellowPoints >= 50) {
                    GameUtil.win(team);
                    over = true;
                }
                break;
            case PURPLE:
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Scoreboard sb = player.getScoreboard();
                    Score score = sb.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.LIGHT_PURPLE + "Purple: " +
                            ChatColor.GREEN + (purplePoints + amount));
                    score.setScore(3);
                    sb.resetScores(ChatColor.LIGHT_PURPLE + "Purple: " + ChatColor.GREEN + purplePoints);
                }
                purplePoints += amount;
                if (purplePoints >= 50) {
                    GameUtil.win(team);
                    over = true;
                }
        }
    }

    public static void joinTeam(Player player, ItemStack stack) {
        if (stack.getItemMeta().getDisplayName().contains("Yellow")) {
            if (purple.contains(player.getUniqueId())) {
                purple.remove(player.getUniqueId());
            }
            if (yellow.contains(player.getUniqueId())) {
                MCMagicCore.gameManager.message(player, ChatColor.RED + "You're already on the Yellow team!");
                return;
            }
            if (yellow.size() == teamSize()) {
                MCMagicCore.gameManager.message(player, ChatColor.RED + "The Yellow team is full!");
                return;
            }
            MCMagicCore.gameManager.message(player, "You have joined the " + ChatColor.YELLOW + "Yellow Team!");
            joinTeam(GameTeam.YELLOW, player.getUniqueId());
            return;
        }
        if (stack.getItemMeta().getDisplayName().contains("Purple")) {
            if (yellow.contains(player.getUniqueId())) {
                yellow.remove(player.getUniqueId());
            }
            if (purple.contains(player.getUniqueId())) {
                MCMagicCore.gameManager.message(player, ChatColor.RED + "You're already on the Purple team!");
                return;
            }
            if (purple.size() == teamSize()) {
                MCMagicCore.gameManager.message(player, ChatColor.RED + "The Purple team is full!");
                return;
            }
            MCMagicCore.gameManager.message(player, "You have joined the " + ChatColor.LIGHT_PURPLE + "Purple Team!");
            joinTeam(GameTeam.PURPLE, player.getUniqueId());
        }
    }

    public static int teamSize() {
        if (Bukkit.getOnlinePlayers().size() % 2 == 0) {
            return Bukkit.getOnlinePlayers().size() / 2;
        } else {
            return (Bukkit.getOnlinePlayers().size() + 1) / 2;
        }
    }

    public static void setPixie(UUID uuid, Integer taskid) {
        playerPixie.put(uuid, taskid);
    }

    public static void setTriplePixie(String s, Integer taskid) {
        triplePixie.put(s, taskid);
    }

    public void joinSpectator(Player player) {
        ScoreboardUtil.joinTeam(player, GameTeam.SPECTATOR);
        ScoreboardUtil.setObjective(player);
        Bukkit.getOnlinePlayers().stream().filter(tp -> !getTeam(GameTeam.SPECTATOR).contains(tp.getUniqueId()))
                .filter(tp -> !tp.getUniqueId().equals(player.getUniqueId())).forEach(tp -> tp.hidePlayer(player));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100000, 0));
        ItemStack compass = new ItemCreator(Material.COMPASS, ChatColor.GREEN + "Spectate", new ArrayList<>());
        ItemStack bed = new ItemCreator(Material.BED, ChatColor.GREEN + "Return to Arcade", new ArrayList<>());
        PlayerInventory pi = player.getInventory();
        pi.clear();
        pi.setItem(0, compass);
        pi.setItem(8, bed);
        player.setGameMode(GameMode.CREATIVE);
        player.setFlying(true);
        teleportToSpawn(player, false);
    }

    public static void openSpectateInventory(Player player) {
        List<UUID> playerList = Bukkit.getOnlinePlayers().stream().filter(tp ->
                !getTeam(GameTeam.SPECTATOR).contains(tp.getUniqueId())).map((Function<Player, UUID>) Entity::getUniqueId)
                .collect(Collectors.toList());
        int size = 9;
        if (playerList.size() <= 18 && playerList.size() > 9) {
            size = 18;
        }
        if (playerList.size() <= 27 && playerList.size() > 18) {
            size = 27;
        }
        if (playerList.size() <= 36 && playerList.size() > 27) {
            size = 36;
        }
        if (playerList.size() <= 45 && playerList.size() > 36) {
            size = 45;
        }
        if (playerList.size() <= 54 && playerList.size() > 45) {
            size = 54;
        }
        Inventory inv = Bukkit.createInventory(player, size, ChatColor.BLUE
                + "Spectating Inventory");
        for (UUID tpuuid : playerList) {
            Player tp = Bukkit.getPlayer(tpuuid);
            ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
            SkullMeta hm = (SkullMeta) head.getItemMeta();
            hm.setDisplayName(ChatColor.GREEN + tp.getDisplayName());
            hm.setOwner(tp.getDisplayName());
            hm.setLore(Collections.singletonList(ChatColor.GREEN + "Click to spectate " + tp.getName()));
            head.setItemMeta(hm);
            inv.addItem(head);
        }
        player.openInventory(inv);
    }
}