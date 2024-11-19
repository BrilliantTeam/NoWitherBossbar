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

public class Name implements Listener {

    /**
     * 處理當實體加入到世界時觸發的事件。
     * 如果該實體是 Boss 並擁有 BossBar，則根據名稱設置 BossBar 的可見性。
     * @param event EntityAddToWorldEvent - 實體加入到世界的事件
     */
    @EventHandler(priority = EventPriority.LOW)
    public void on(@NotNull EntityAddToWorldEvent event) {
        // 嘗試取得實體的 BossBar 並設置其可見性
        Optional.of(event.getEntity())
                .map(entity -> entity instanceof Boss boss ? boss : null) // 判斷是否為 Boss 類型
                .map(Boss::getBossBar) // 獲取 Boss 的 BossBar
                .ifPresent(bar -> { 
                    // 根據名稱決定 BossBar 的可見性
                    boolean visible = determineVisibility(bar.getTitle());
                    bar.setVisible(visible);
                });
    }

    /**
     * 處理玩家命名實體時觸發的事件。
     * 如果該實體是 Boss 並擁有 BossBar，則根據新的名稱設置 BossBar 的可見性。
     * @param event PlayerNameEntityEvent - 玩家命名實體的事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(@NotNull PlayerNameEntityEvent event) {
        // 嘗試取得實體的 BossBar 並設置其可見性
        Optional.of(event.getEntity())
                .map(entity -> entity instanceof Boss boss ? boss : null) // 判斷是否為 Boss 類型
                .map(Boss::getBossBar) // 獲取 Boss 的 BossBar
                .ifPresent(bar -> {
                    // 提取事件中新的名稱（如果是 TextComponent，則獲取其內容）
                    String name = event.getName() instanceof TextComponent component ? component.content() : "";
                    // 根據名稱決定 BossBar 的可見性
                    boolean visible = determineVisibility(name);
                    bar.setVisible(visible);
                });
    }

    /**
     * 決定 BossBar 是否應該可見。
     * 如果名稱包含 "nbr"（不區分大小寫），則返回 false（不可見）。
     * @param name String - Boss 或實體的名稱
     * @return boolean - BossBar 是否應該可見
     */
    private static boolean determineVisibility(@NotNull String name) {
        return !name.toLowerCase().contains("nbr");
    }
}
