package bgprotobg.net.afk;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class AFKListener implements Listener {

    private final ShardsCommand shardsCommand;

    public AFKListener(ShardsCommand shardsCommand) {
        this.shardsCommand = shardsCommand;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() != Material.STICK) return;

        ItemStack item = event.getItem();
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().getDisplayName().equals("§b§lAFK WAND")) {

            UUID playerId = event.getPlayer().getUniqueId();
            if (event.getClickedBlock() == null) {
                return;
            }
            Location loc = event.getClickedBlock().getLocation();

            if (event.getAction().name().contains("LEFT_CLICK")) {
                shardsCommand.setSelection(event.getPlayer(), loc, null);
                event.getPlayer().sendMessage("§aFirst block selected for AFK zone.");
            } else if (event.getAction().name().contains("RIGHT_CLICK")) {
                Location[] locs = shardsCommand.getSelection(playerId);
                if (locs == null || locs[0] == null) {
                    event.getPlayer().sendMessage("§cYou need to select the first block first.");
                } else {
                    shardsCommand.setSelection(event.getPlayer(), locs[0], loc);
                    event.getPlayer().sendMessage("§aSecond block selected for AFK zone. Type /shards complete to finish or /shards cancel to cancel");
                }
            }
        }
    }
}
