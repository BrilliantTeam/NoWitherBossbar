package Rice.Chen.NoWitherBossbar;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class NoWitherBossbar extends JavaPlugin {

    private static NoWitherBossbar NoWitherBossbar;
    private Name nameListener;

    @Override
    public void onEnable() {
        NoWitherBossbar = this;

        saveDefaultConfig();

        nameListener = new Name(this);
        getServer().getPluginManager().registerEvents(nameListener, this);

        NoWitherBossbarCommand commandExecutor = new NoWitherBossbarCommand(this);
        getCommand("nowitherbar").setExecutor(commandExecutor);
        getCommand("nowitherbar").setTabCompleter(commandExecutor);
        
        getLogger().info("NoWitherBossbar has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("NoWitherBossbar has been disabled!");
    }

    public static NoWitherBossbar getInstance() {
        return NoWitherBossbar;
    }

    public Name getNameListener() {
        return nameListener;
    }
}