package bgprotobg.net.afk;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class AFKPlaceholder extends PlaceholderExpansion {

    private final AFKHandler afkHandler;

    public AFKPlaceholder(AFKHandler afkHandler) {
        this.afkHandler = afkHandler;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean register() {
        return super.register();
    }

    @Override
    public String getIdentifier() {
        return "afk";
    }

    @Override
    public String getAuthor() {
        return "YourName";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        if (identifier.equals("shards")) {
            int shards = afkHandler.getShards(player);
            return String.valueOf(shards);
        }

        return null;
    }
}
