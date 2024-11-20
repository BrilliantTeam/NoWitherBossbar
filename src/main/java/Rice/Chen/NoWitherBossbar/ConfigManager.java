package Rice.Chen.NoWitherBossbar;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;

public class ConfigManager {
    private final NoWitherBossbar plugin;
    private final Map<String, ConfigDefault> defaultValues;
    private FileConfiguration config;
    private File configFile;

    private static class ConfigDefault {
        final Object value;
        final String[] comments;

        ConfigDefault(Object value, String... comments) {
            this.value = value;
            this.comments = comments;
        }
    }

    public ConfigManager(NoWitherBossbar plugin) {
        this.plugin = plugin;
        this.defaultValues = new LinkedHashMap<>();
        initializeDefaults();
    }

    private void initializeDefaults() {
        List<String> defaultHideTags = new ArrayList<>();
        defaultHideTags.add("nbr");
        
        defaultValues.put("hide-bossbar-tags", new ConfigDefault(defaultHideTags,
            "用於隱藏 Boss 血條的標記",
            "可以添加多個標記，血條名稱包含任意一個標記都會被隱藏"
        ));
    }

    public void loadConfig() {
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }

        this.config = YamlConfiguration.loadConfiguration(configFile);
        validateAndRepairConfig();
    }

    public void validateAndRepairConfig() {
        boolean needsSave = false;
        
        // 檢查並修復每個配置項
        for (Map.Entry<String, ConfigDefault> entry : defaultValues.entrySet()) {
            String path = entry.getKey();
            ConfigDefault defaultConfig = entry.getValue();

            if (!config.contains(path)) {
                config.set(path, defaultConfig.value);
                plugin.getLogger().warning("Missing configuration value for '" + path + "'. Adding default value.");
                needsSave = true;
            } else if (path.equals("hide-bossbar-tags")) {
                List<String> currentTags = config.getStringList(path);
                if (currentTags.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    List<String> defaultTags = (List<String>) defaultConfig.value;
                    config.set(path, defaultTags);
                    plugin.getLogger().warning("Empty hide-bossbar-tags list found. Adding default tags.");
                    needsSave = true;
                }
            }
        }

        // 如果需要保存，創建新的配置文件
        if (needsSave) {
            File tempFile = new File(plugin.getDataFolder(), "config_temp.yml");
            try {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile, StandardCharsets.UTF_8))) {
                    // 寫入頭部註解
                    writer.write("# NoWitherBossbar 配置文件");
                    writer.newLine();
                    writer.newLine();

                    // 為每個配置項寫入註解和值
                    for (Map.Entry<String, ConfigDefault> entry : defaultValues.entrySet()) {
                        String path = entry.getKey();
                        ConfigDefault defaultConfig = entry.getValue();
                        
                        // 寫入註解
                        for (String comment : defaultConfig.comments) {
                            writer.write("# " + comment);
                            writer.newLine();
                        }
                        
                        // 寫入配置項名稱
                        writer.write(path + ":");
                        writer.newLine();

                        // 寫入配置值
                        if (path.equals("hide-bossbar-tags")) {
                            List<String> tags = config.getStringList(path);
                            for (String tag : tags) {
                                writer.write("  - \"" + tag + "\"");
                                writer.newLine();
                            }
                        }
                        
                        writer.newLine();
                    }
                }

                // 備份原配置文件（如果存在）
                if (configFile.exists()) {
                    File backup = new File(plugin.getDataFolder(), "config.yml.bak");
                    Files.copy(configFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                // 替換配置文件
                Files.move(tempFile.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                // 重新加載配置
                config = YamlConfiguration.loadConfiguration(configFile);
                
                plugin.getLogger().info("Configuration has been repaired and saved with comments.");
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, e);
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        }
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, e);
        }
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public void reloadConfig() {
        loadConfig();
    }
}