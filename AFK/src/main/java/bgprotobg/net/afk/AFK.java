package bgprotobg.net.afk;

import org.bukkit.plugin.java.JavaPlugin;

public final class AFK extends JavaPlugin {

    private AFKHandler afkHandler;
    private ShopMenu shopMenu;

    @Override
    public void onEnable() {
        this.afkHandler = new AFKHandler(this);
        this.shopMenu = new ShopMenu(this, afkHandler);
        ShardsCommand shardsCommand = new ShardsCommand(this, afkHandler, shopMenu);
        getCommand("shards").setExecutor(shardsCommand);
        getServer().getPluginManager().registerEvents(new AFKListener(shardsCommand), this);
        getServer().getPluginManager().registerEvents(shopMenu, this);
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new AFKPlaceholder(afkHandler).register();
        }
    }

    @Override
    public void onDisable() {
    }
}
