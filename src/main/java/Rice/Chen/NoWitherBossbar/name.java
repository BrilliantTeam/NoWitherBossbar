package Rice.Chen.NoWitherBossbar;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import io.papermc.paper.event.player.PlayerNameEntityEvent;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class Name implements Listener {

    private final NoWitherBossbar plugin;

    public Name(NoWitherBossbar plugin) {
        this.plugin = plugin;
    }

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
        List<String> hideTags = NoWitherBossbar.getInstance().getConfig().getStringList("hide-bossbar-tags");
        String lowerName = name.toLowerCase();
        return hideTags.stream()
                .noneMatch(tag -> lowerName.contains(tag.toLowerCase()));
    }

    public void updateAllBossBars() {
        AtomicInteger worldsToProcess = new AtomicInteger(plugin.getServer().getWorlds().size());
        AtomicInteger updatedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (World world : plugin.getServer().getWorlds()) {
            ServerScheduler.processEntities(plugin, world, 
                entity -> {
                    if (entity instanceof Boss boss) {
                        try {
                            BossBar bossBar = boss.getBossBar();
                            if (bossBar != null) {
                                boolean visible = determineVisibility(bossBar.getTitle());
                                bossBar.setVisible(visible);
                                updatedCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                            plugin.getLogger().warning("Failed to update BossBar for entity: " + e.getMessage());
                        }
                    }
                },
                () -> {
                    if (worldsToProcess.decrementAndGet() == 0) {
                        plugin.getLogger().info(String.format("Updated %d BossBars with %d errors", 
                            updatedCount.get(), errorCount.get()));
                    }
                }
            );
        }
    }
}