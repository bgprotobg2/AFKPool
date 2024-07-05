package bgprotobg.net.afk;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShardsCommand implements CommandExecutor {

    private final AFKHandler afkHandler;
    private final ShopMenu shopMenu;
    private final Map<UUID, Location[]> selection;
    private final Plugin plugin;

    public ShardsCommand(Plugin plugin, AFKHandler afkHandler, ShopMenu shopMenu) {
        this.plugin = plugin;
        this.afkHandler = afkHandler;
        this.shopMenu = shopMenu;
        this.selection = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§eUsage: /shards <command>");
            return true;
        }

        if (args[0].equalsIgnoreCase("help")) {
            if (!sender.hasPermission("shards.help")) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }
            sender.sendMessage("§eAvailable commands:");
            sender.sendMessage("§e/shards wand <player> - Give the player an AFK wand.");
            sender.sendMessage("§e/shards complete - Complete the AFK zone selection.");
            sender.sendMessage("§e/shards cancel - Cancel the AFK zone selection.");
            sender.sendMessage("§e/shards give <player> <amount> - Give shards to a player.");
            sender.sendMessage("§e/shards take <player> <amount> - Take shards from a player.");
            sender.sendMessage("§e/shards balance [player] - Check your or another player's shard balance.");
            sender.sendMessage("§e/shards pay <player> <amount> - Pay shards to another player.");
            sender.sendMessage("§e/shards shop - Open the shards shop.");
            return true;
        }

        if (args[0].equalsIgnoreCase("wand")) {
            if (!sender.hasPermission("shards.wand")) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use this command.");
                return true;
            }

            Player player = (Player) sender;
            if (args.length < 2) {
                player.sendMessage("§cPlease specify a player.");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage("§cPlayer not found.");
                return true;
            }

            ItemStack wand = afkHandler.getAFKWand();
            target.getInventory().addItem(wand);
            target.sendMessage("§aYou have received the AFK Wand.");
            return true;
        } else if (args[0].equalsIgnoreCase("complete")) {
            if (!sender.hasPermission("shards.complete")) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use this command.");
                return true;
            }

            Player player = (Player) sender;
            Location[] locs = selection.get(player.getUniqueId());
            if (locs == null || locs[0] == null || locs[1] == null) {
                player.sendMessage("§cYou need to select two blocks first.");
                return true;
            }

            afkHandler.addAFKZone(player, locs[0], locs[1]);
            selection.remove(player.getUniqueId());
            player.sendMessage("§aAFK zone created successfully.");
            return true;
        } else if (args[0].equalsIgnoreCase("cancel")) {
            if (!sender.hasPermission("shards.cancel")) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use this command.");
                return true;
            }

            Player player = (Player) sender;
            selection.remove(player.getUniqueId());
            player.sendMessage("§aAFK zone selection canceled.");
            return true;
        } else if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("shards.give")) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage("§cUsage: /shards give <player> <amount>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found.");
                return true;
            }

            try {
                int amount = Integer.parseInt(args[2]);
                afkHandler.addShards(target, amount);
                sender.sendMessage("§aGave " + amount + " shards to " + target.getName() + ".");
                target.sendMessage("§aYou have received " + amount + " shards.");
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid amount.");
            }
            return true;
        } else if (args[0].equalsIgnoreCase("take")) {
            if (!sender.hasPermission("shards.take")) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage("§cUsage: /shards take <player> <amount>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found.");
                return true;
            }

            try {
                int amount = Integer.parseInt(args[2]);
                afkHandler.removeShards(target, amount);
                sender.sendMessage("§aTook " + amount + " shards from " + target.getName()+ ".");
                target.sendMessage("§c" + amount + " shards have been taken from you.");
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid amount.");
            }
            return true;
        } else if (args[0].equalsIgnoreCase("balance")) {
            if (!sender.hasPermission("shards.balance")) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use this command.");
                return true;
            }

            Player player = (Player) sender;
            if (args.length == 1) {
                int balance = afkHandler.getShards(player);
                player.sendMessage("§aYou have " + balance + " shards.");
            } else {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cPlayer not found.");
                    return true;
                }

                int balance = afkHandler.getShards(target);
                player.sendMessage("§a" + target.getName() + " has " + balance + " shards.");
            }
            return true;
        } else if (args[0].equalsIgnoreCase("pay")) {
            if (!sender.hasPermission("shards.pay")) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use this command.");
                return true;
            }

            Player player = (Player) sender;
            if (args.length < 3) {
                player.sendMessage("§cUsage: /shards pay <player> <amount>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage("§cPlayer not found.");
                return true;
            }

            try {
                int amount = Integer.parseInt(args[2]);
                afkHandler.payShards(player, target, amount);
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid amount.");
            }
            return true;
        } else if (args[0].equalsIgnoreCase("shop")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use this command.");
                return true;
            }

            Player player = (Player) sender;
            shopMenu.openShop(player);
            return true;
    } else if (args[0].equalsIgnoreCase("reload")) {
        if (!sender.hasPermission("shards.reload")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }
        reloadShopConfig();
        sender.sendMessage("§aReloaded shop.yml configuration.");
        return true;
    }
        sender.sendMessage("§cUnknown command. Usage: /shards <command>");
        return true;
}
    private void reloadShopConfig() {
        File shopFile = new File(plugin.getDataFolder(), "shop.yml");
        if (!shopFile.exists()) {
            plugin.saveResource("shop.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(shopFile);
        shopMenu.loadShopItems(config);
    }


    public void setSelection(Player player, Location loc1, Location loc2) {
        selection.put(player.getUniqueId(), new Location[]{loc1, loc2});
    }

    public Location[] getSelection(UUID playerId) {
        return selection.get(playerId);
    }
}
