package Rice.Chen.NoWitherBossbar;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
  public static Main NoWitherBossbar;
  
  public void onEnable() {
    NoWitherBossbar = this;
    getServer().getPluginManager().registerEvents(new NoWitherBossbar(), (Plugin)this);
  }
}
