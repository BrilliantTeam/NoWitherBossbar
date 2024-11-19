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

import java.util.Optional;

public class NoWitherBossbar implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void on(@NotNull EntityAddToWorldEvent event) {
        Optional.of(event.getEntity())
                .map(entity -> entity instanceof Boss boss ? boss : null)
                .map(Boss::getBossBar)
                .ifPresent(bar -> {
                    boolean visible = determineVisibility(bar.getTitle());
                    bar.setVisible(visible);
                });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(@NotNull PlayerNameEntityEvent event) {
        Optional.of(event.getEntity())
                .map(entity -> entity instanceof Boss boss ? boss : null)
                .map(Boss::getBossBar)
                .ifPresent(bar -> {
                    String name = event.getName() instanceof TextComponent component ? component.content() : "";
                    boolean visible = determineVisibility(name);
                    bar.setVisible(visible);
                });
    }

    private static boolean determineVisibility(@NotNull String name) {
        return !name.toLowerCase().contains("nbr");
    }
}
