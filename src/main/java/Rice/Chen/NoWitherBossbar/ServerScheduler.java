package Rice.Chen.NoWitherBossbar;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Boss;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.bukkit.Location;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.Chunk;

public class ServerScheduler {
    private static Boolean isFolia = null;

    public static boolean isFolia() {
        if (isFolia == null) {
            try {
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
                isFolia = true;
            } catch (ClassNotFoundException e) {
                isFolia = false;
            }
        }
        return isFolia;
    }

    public static void processEntities(Plugin plugin, World world, Consumer<Entity> processor, Runnable callback) {
        if (isFolia()) {
            processFolia(plugin, world, processor, callback);
        } else {
            processNonFolia(plugin, world, processor, callback);
        }
    }

    private static void processFolia(Plugin plugin, World world, Consumer<Entity> processor, Runnable callback) {
        AtomicInteger processedEntities = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicInteger completedChunks = new AtomicInteger(0);

        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            try {
                Chunk[] loadedChunks = world.getLoadedChunks();
                
                // 如果沒有已加載的區塊，直接執行回調
                if (loadedChunks.length == 0) {
                    plugin.getServer().getGlobalRegionScheduler().run(plugin, 
                        task2 -> callback.run());
                    return;
                }

                for (Chunk chunk : loadedChunks) {
                    Location chunkLoc = new Location(world, 
                        chunk.getX() << 4, 0, chunk.getZ() << 4);

                    plugin.getServer().getRegionScheduler().execute(plugin, chunkLoc, () -> {
                        try {
                            // 只處理 Boss 類型的實體
                            for (Entity entity : chunk.getEntities()) {
                                if (entity instanceof Boss) {
                                    entity.getScheduler().run(plugin, 
                                        scheduledTask -> {
                                            try {
                                                processor.accept(entity);
                                                processedEntities.incrementAndGet();
                                            } catch (Exception ex) {
                                                errorCount.incrementAndGet();
                                                plugin.getLogger().warning("Failed to process entity: " + ex.getMessage());
                                            }
                                        },
                                        () -> errorCount.incrementAndGet()
                                    );
                                }
                            }
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                            plugin.getLogger().warning("Error processing chunk: " + e.getMessage());
                        } finally {
                            if (completedChunks.incrementAndGet() == loadedChunks.length) {
                                plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, 
                                    task2 -> {
                                        callback.run();
                                        if (processedEntities.get() > 0) {
                                            plugin.getLogger().info(String.format(
                                                "Processed %d Boss entities with %d errors in world: %s", 
                                                processedEntities.get(),
                                                errorCount.get(),
                                                world.getName()
                                            ));
                                        }
                                    }, 
                                    1
                                );
                            }
                        }
                    });
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to process world " + world.getName() + ": " + e.getMessage());
                plugin.getServer().getGlobalRegionScheduler().run(plugin, task2 -> callback.run());
            }
        });
    }

    private static void processNonFolia(Plugin plugin, World world, Consumer<Entity> processor, Runnable callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                int processed = 0;
                int errors = 0;

                try {
                    // 只處理 Boss 類型的實體
                    for (Entity entity : world.getEntities()) {
                        if (entity instanceof Boss && entity.isValid()) {
                            try {
                                processor.accept(entity);
                                processed++;
                            } catch (Exception ex) {
                                errors++;
                                plugin.getLogger().warning("Failed to process entity: " + ex.getMessage());
                            }
                        }
                    }
                } finally {
                    final int finalProcessed = processed;
                    final int finalErrors = errors;
                    
                    if (finalProcessed > 0) {
                        plugin.getLogger().info(String.format(
                            "Processed %d Boss entities with %d errors in world: %s", 
                            finalProcessed, 
                            finalErrors,
                            world.getName()
                        ));
                    }
                    callback.run();
                }
            }
        }.runTask(plugin);
    }
}