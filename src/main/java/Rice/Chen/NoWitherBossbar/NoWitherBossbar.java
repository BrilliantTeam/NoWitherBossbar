package Rice.Chen.NoWitherBossbar;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class NoWitherBossbar extends JavaPlugin {
  public static NoWitherBossbar NoWitherBossbar;
  
  public void onEnable() {
    NoWitherBossbar = this;
    getServer().getPluginManager().registerEvents(new name(), (Plugin)this);
  }
}
