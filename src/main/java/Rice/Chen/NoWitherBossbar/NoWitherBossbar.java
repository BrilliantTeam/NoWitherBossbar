package Rice.Chen.NoWitherBossbar;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import io.papermc.paper.event.player.PlayerNameEntityEvent;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Boss;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class NoWitherBossbar implements Listener {

    private static final @NotNull Set<String> HIDDEN = new HashSet<>();

    @EventHandler(priority = EventPriority.LOW)
    public void on(@NotNull EntityAddToWorldEvent event) {
        Optional.of(event.getEntity())
                .map(entity -> entity instanceof Boss boss ? boss : null)
                .map(Boss::getBossBar)
                .ifPresent(bar -> checkVisible(bar, bar.getTitle()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(@NotNull PlayerNameEntityEvent event) {
        Optional.of(event.getEntity())
                .map(entity -> entity instanceof Boss boss ? boss : null)
                .map(Boss::getBossBar)
                .ifPresent(bar -> checkVisible(bar, event.getName() instanceof TextComponent component ? component.content() : ""));
    }

    private static void checkVisible(@NotNull BossBar bar, @NotNull String name) {
        boolean isHidden = name.toLowerCase().contains("nbr");
        bar.setVisible(!isHidden);
    }
}
