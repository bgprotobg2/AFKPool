package bgprotobg.net.afk;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopMenu implements Listener {

    private final Plugin plugin;
    private final AFKHandler afkHandler;
    private final Map<Integer, ShopItem> shopItems;

    public ShopMenu(Plugin plugin, AFKHandler afkHandler) {
        this.plugin = plugin;
        this.afkHandler = afkHandler;
        this.shopItems = new HashMap<>();
        loadShopItems();
    }

    public void openShop(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, "Shard Shop");

        for (int slot : shopItems.keySet()) {
            inventory.setItem(slot, shopItems.get(slot).getItem());
        }

        player.openInventory(inventory);
    }

    public void loadShopItems() {
        File shopFile = new File(plugin.getDataFolder(), "shop.yml");
        if (!shopFile.exists()) {
            plugin.saveResource("shop.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(shopFile);
        loadShopItems(config);
    }

    public void loadShopItems(FileConfiguration config) {
        shopItems.clear();
        for (String key : config.getConfigurationSection("shop.items").getKeys(false)) {
            String path = "shop.items." + key + ".";
            Material material = Material.valueOf(config.getString(path + "display-item"));
            String displayName = ChatColor.translateAlternateColorCodes('&', config.getString(path + "display-name"));
            List<String> lore = config.getStringList(path + "lore");
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
            }
            int price = config.getInt(path + "price");
            int slot = config.getInt(path + "slot");
            List<String> rewardCommands = config.getStringList(path + "reward-commands");

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);

            shopItems.put(slot, new ShopItem(item, price, rewardCommands));
        }
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Shard Shop")) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        ShopItem shopItem = shopItems.get(slot);

        if (shopItem == null) return;

        int price = shopItem.getPrice();

        if (afkHandler.hasEnoughShards(player, price)) {
            afkHandler.removeShards(player, price);
            for (String command : shopItem.getRewardCommands()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
            }
            player.sendMessage("§aYou have purchased " + shopItem.getItem().getType() + " for " + price + " shards.");
        } else {
            player.sendMessage("§cYou do not have enough shards to purchase this item.");
        }
    }

    private static class ShopItem {
        private final ItemStack item;
        private final int price;
        private final List<String> rewardCommands;

        public ShopItem(ItemStack item, int price, List<String> rewardCommands) {
            this.item = item;
            this.price = price;
            this.rewardCommands = rewardCommands;
        }

        public ItemStack getItem() {
            return item;
        }

        public int getPrice() {
            return price;
        }

        public List<String> getRewardCommands() {
            return rewardCommands;
        }
    }
}
