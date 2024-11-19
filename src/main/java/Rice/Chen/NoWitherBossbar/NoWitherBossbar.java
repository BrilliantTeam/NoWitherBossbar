package Rice.Chen.NoWitherBossbar;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 主插件類，用於初始化插件功能。
 * 繼承自 Bukkit 的 JavaPlugin 類，提供插件的啟動與相關功能註冊。
 */
public class NoWitherBossbar extends JavaPlugin {

    // 靜態實例，用於全局訪問插件實例
    public static NoWitherBossbar NoWitherBossbar;

    /**
     * 當插件啟用時調用。
     * 負責初始化插件實例並註冊事件監聽器。
     */
    @Override
    public void onEnable() {
        // 設置靜態實例為當前插件實例
        NoWitherBossbar = this;

        // 註冊事件監聽器 Name
        // 這裡將當前插件實例傳遞給 Bukkit 的事件管理器進行註冊
        getServer().getPluginManager().registerEvents(new Name(), (Plugin) this);
    }
}
