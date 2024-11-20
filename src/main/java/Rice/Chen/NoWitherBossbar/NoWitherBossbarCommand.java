package Rice.Chen.NoWitherBossbar;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NoWitherBossbarCommand implements CommandExecutor, TabCompleter {

    private final NoWitherBossbar plugin;

    public NoWitherBossbarCommand(NoWitherBossbar plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // 檢查是否有 nowitherbossbar 權限
        if (!sender.hasPermission("nowitherbossbar.admin")) {
            sender.sendMessage(Component.text("[NoWitherBossbar] ").color(NamedTextColor.WHITE)
                    .append(Component.text("You don't have permission to use this command!")
                    .color(NamedTextColor.RED)));
            return true;
        }

        if (args.length == 0) {
            // 顯示插件基本信息
            sender.sendMessage(Component.text("=== NoWitherBossbar ===").color(NamedTextColor.GOLD));
            sender.sendMessage(Component.text("Current hide tags: ").color(NamedTextColor.YELLOW)
                    .append(Component.text(String.join(", ", plugin.getConfig().getStringList("hide-bossbar-tags")))
                            .color(NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("Commands:").color(NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("- /nwb reload: Reload configuration").color(NamedTextColor.WHITE));
            sender.sendMessage(Component.text("- /nwb list: List current hide tags").color(NamedTextColor.WHITE));
            sender.sendMessage(Component.text("==== by. RiceChen_ ====").color(NamedTextColor.GOLD));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                // 重新加載配置文件
                plugin.reloadConfig();
                // 更新所有現有的 Boss 血條
                plugin.getNameListener().updateAllBossBars();
                sender.sendMessage(Component.text("[NoWitherBossbar] ").color(NamedTextColor.WHITE)
                        .append(Component.text("Configuration reloaded successfully!")
                        .color(NamedTextColor.GREEN)));
                sender.sendMessage(Component.text("[NoWitherBossbar] ").color(NamedTextColor.WHITE)
                        .append(Component.text("Current hide tags: ")
                        .color(NamedTextColor.YELLOW)
                        .append(Component.text(String.join(", ", plugin.getConfig().getStringList("hide-bossbar-tags")))
                            .color(NamedTextColor.WHITE))));
                return true;
            }
            case "list" -> {
                // 列出當前的隱藏標記
                List<String> tags = plugin.getConfig().getStringList("hide-bossbar-tags");
                sender.sendMessage(Component.text("[NoWitherBossbar] ").color(NamedTextColor.WHITE)
                        .append(Component.text("Current hide tags:").color(NamedTextColor.YELLOW)));
                for (String tag : tags) {
                    sender.sendMessage(Component.text("[NoWitherBossbar] ").color(NamedTextColor.WHITE)
                            .append(Component.text("- ").color(NamedTextColor.GRAY)
                            .append(Component.text(tag).color(NamedTextColor.WHITE))));
                }
                return true;
            }
            default -> {
                sender.sendMessage(Component.text("[NoWitherBossbar] ").color(NamedTextColor.WHITE)
                        .append(Component.text("Unknown command. Use /nwb for help.")
                        .color(NamedTextColor.RED)));
                return false;
            }
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        // 添加權限檢查到 Tab 補全
        if (!sender.hasPermission("nowitherbossbar.admin")) {
            return new ArrayList<>();
        }
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("reload");
            completions.add("list");
            return completions;
        }

        return completions;
    }
}