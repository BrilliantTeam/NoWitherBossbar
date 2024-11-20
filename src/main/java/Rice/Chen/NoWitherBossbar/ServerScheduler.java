package Rice.Chen.NoWitherBossbar;

import org.bukkit.World;
import org.bukkit.entity.Entity;
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
            AtomicInteger processedEntities = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);
            AtomicInteger completedChunks = new AtomicInteger(0);

            // 使用異步調度器獲取已加載的區塊
            plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
                try {
                    // 獲取所有已加載的區塊
                    Chunk[] loadedChunks = world.getLoadedChunks();
                    for (Chunk chunk : loadedChunks) {
                        // 使用 RegionScheduler 在正確的區域線程中處理每個區塊
                        Location chunkLoc = new Location(world, 
                            chunk.getX() << 4, 
                            0, 
                            chunk.getZ() << 4);

                        plugin.getServer().getRegionScheduler().execute(plugin, chunkLoc, () -> {
                            try {
                                // 在區域線程中安全地獲取區塊中的實體
                                for (Entity entity : chunk.getEntities()) {
                                    if (entity != null) {
                                        // 使用實體自己的調度器來處理它
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
                                            () -> {
                                                errorCount.incrementAndGet();
                                                plugin.getLogger().warning("Failed to schedule entity processing");
                                            }
                                        );
                                    }
                                }
                            } catch (Exception e) {
                                plugin.getLogger().warning("Error processing chunk at " + chunk.getX() + "," + chunk.getZ() + ": " + e.getMessage());
                                errorCount.incrementAndGet();
                            } finally {
                                // 如果這是最後一個區塊，執行回調
                                if (completedChunks.incrementAndGet() == loadedChunks.length) {
                                    plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task2 -> {
                                        callback.run();
                                        plugin.getLogger().info(String.format(
                                            "Processed %d entities with %d errors in world: %s", 
                                            processedEntities.get(),
                                            errorCount.get(),
                                            world.getName()
                                        ));
                                    }, 20);
                                }
                            }
                        });
                    }

                    // 如果世界中沒有已加載的區塊
                    if (loadedChunks.length == 0) {
                        plugin.getServer().getGlobalRegionScheduler().run(plugin, task2 -> {
                            callback.run();
                            plugin.getLogger().info("No loaded chunks in world: " + world.getName());
                        });
                    }
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to process world " + world.getName() + ": " + e.getMessage());
                    plugin.getServer().getGlobalRegionScheduler().run(plugin, task2 -> callback.run());
                }
            });
        } else {
            // 非 Folia 環境的處理保持不變
            new BukkitRunnable() {
                @Override
                public void run() {
                    int processed = 0;
                    int errors = 0;

                    try {
                        for (Entity entity : world.getEntities()) {
                            try {
                                if (entity.isValid()) {
                                    processor.accept(entity);
                                    processed++;
                                }
                            } catch (Exception ex) {
                                errors++;
                                plugin.getLogger().warning("Failed to process entity: " + ex.getMessage());
                            }
                        }
                    } finally {
                        final int finalProcessed = processed;
                        final int finalErrors = errors;
                        callback.run();
                        plugin.getLogger().info(String.format(
                            "Processed %d entities with %d errors in world: %s", 
                            finalProcessed, 
                            finalErrors,
                            world.getName()
                        ));
                    }
                }
            }.runTask(plugin);
        }
    }
}