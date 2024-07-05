package bgprotobg.net.afk;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AFKHandler {

    private final Plugin plugin;
    private final Map<UUID, Location[]> afkZones;
    private final Set<UUID> afkPlayers;
    private final Map<UUID, Integer> shardBalances;
    private final File dataFile;
    private final FileConfiguration dataConfig;

    public AFKHandler(Plugin plugin) {
        this.plugin = plugin;
        this.afkZones = new HashMap<>();
        this.afkPlayers = new HashSet<>();
        this.shardBalances = new HashMap<>();

        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadShards();
        loadAFKZones();
        startAFKChecker();
    }

    public void addAFKZone(Player player, Location pos1, Location pos2) {
        Location min = new Location(pos1.getWorld(),
                Math.min(pos1.getX(), pos2.getX()),
                0,
                Math.min(pos1.getZ(), pos2.getZ()));
        Location max = new Location(pos1.getWorld(),
                Math.max(pos1.getX(), pos2.getX()),
                255,
                Math.max(pos1.getZ(), pos2.getZ()));
        afkZones.put(player.getUniqueId(), new Location[]{min, max});
        saveAFKZones();
    }

    public boolean isInAFKZone(Player player) {
        Location[] zone = afkZones.get(player.getUniqueId());
        if (zone == null) return false;
        Location loc = player.getLocation();
        Location min = zone[0];
        Location max = zone[1];
        return loc.getX() >= min.getX() && loc.getX() <= max.getX() &&
                loc.getZ() >= min.getZ() && loc.getZ() <= max.getZ();
    }

    public void startAFKChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (isInAFKZone(player)) {
                        if (!afkPlayers.contains(player.getUniqueId())) {
                            afkPlayers.add(player.getUniqueId());
                            player.sendMessage("§aYou have entered the AFK zone.");
                        }
                    } else {
                        if (afkPlayers.contains(player.getUniqueId())) {
                            afkPlayers.remove(player.getUniqueId());
                            player.sendMessage("§cYou have left the AFK zone.");
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (afkPlayers.contains(player.getUniqueId())) {
                        addShards(player, 1);
                        player.sendMessage("§aYou have received 1 shard for being AFK.");
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1200);
    }

    public ItemStack getAFKWand() {
        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName("§b§lAFK WAND");
        meta.setLore(Arrays.asList("§7Mark the blocks to create the zone"));
        wand.setItemMeta(meta);
        return wand;
    }

    public void addShards(Player player, int amount) {
        shardBalances.put(player.getUniqueId(), shardBalances.getOrDefault(player.getUniqueId(), 0) + amount);
        saveShards();
    }

    public void removeShards(Player player, int amount) {
        shardBalances.put(player.getUniqueId(), shardBalances.getOrDefault(player.getUniqueId(), 0) - amount);
        saveShards();
    }

    public int getShards(Player player) {
        return shardBalances.getOrDefault(player.getUniqueId(), 0);
    }

    public boolean hasEnoughShards(Player player, int amount) {
        return getShards(player) >= amount;
    }

    public void payShards(Player sender, Player receiver, int amount) {
        if (hasEnoughShards(sender, amount)) {
            removeShards(sender, amount);
            addShards(receiver, amount);
            sender.sendMessage("§aYou have paid " + amount + " shards to " + receiver.getName() + ".");
            receiver.sendMessage("§aYou have received " + amount + " shards from " + sender.getName() + ".");
        } else {
            sender.sendMessage("§cYou do not have enough shards to complete this transaction.");
        }
    }

    private void saveShards() {
        for (UUID uuid : shardBalances.keySet()) {
            dataConfig.set("shards." + uuid.toString(), shardBalances.get(uuid));
        }
        saveDataConfig();
    }

    private void loadShards() {
        if (dataConfig.contains("shards")) {
            for (String uuidString : dataConfig.getConfigurationSection("shards").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                int shards = dataConfig.getInt("shards." + uuidString);
                shardBalances.put(uuid, shards);
            }
        }
    }

    private void saveAFKZones() {
        for (UUID uuid : afkZones.keySet()) {
            Location[] locs = afkZones.get(uuid);
            dataConfig.set("afkZones." + uuid.toString() + ".min", locs[0]);
            dataConfig.set("afkZones." + uuid.toString() + ".max", locs[1]);
        }
        saveDataConfig();
    }

    private void loadAFKZones() {
        if (dataConfig.contains("afkZones")) {
            for (String uuidString : dataConfig.getConfigurationSection("afkZones").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                Location min = (Location) dataConfig.get("afkZones." + uuidString + ".min");
                Location max = (Location) dataConfig.get("afkZones." + uuidString + ".max");
                afkZones.put(uuid, new Location[]{min, max});
            }
        }
    }

    private void saveDataConfig() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
