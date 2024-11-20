package Rice.Chen.NoWitherBossbar;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class NoWitherBossbar extends JavaPlugin {

    private static NoWitherBossbar NoWitherBossbar;
    private Name nameListener;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        NoWitherBossbar = this;

        // 初始化配置管理器
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        nameListener = new Name(this);
        getServer().getPluginManager().registerEvents(nameListener, this);

        NoWitherBossbarCommand commandExecutor = new NoWitherBossbarCommand(this);
        getCommand("nowitherbossbar").setExecutor(commandExecutor);
        getCommand("nowitherbossbar").setTabCompleter(commandExecutor);
        
        getLogger().info("NoWitherBossbar has been enabled!");
    }

    @Override
    public void onDisable() {
        try {
            // 在關閉時重新檢查和修復配置
            if (configManager != null) {
                getLogger().info("Checking configuration before shutdown...");
                configManager.loadConfig();
                configManager.validateAndRepairConfig();
                getLogger().info("Configuration check completed.");
            }
        } catch (Exception e) {
            getLogger().severe("Error while checking configuration on shutdown: " + e.getMessage());
        }
        getLogger().info("NoWitherBossbar has been disabled!");
    }

    public static NoWitherBossbar getInstance() {
        return NoWitherBossbar;
    }

    public Name getNameListener() {
        return nameListener;
    }

    @Override
    public void reloadConfig() {
        configManager.reloadConfig();
    }

    @Override
    public FileConfiguration getConfig() {
        return configManager.getConfig();
    }
}