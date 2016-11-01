package me.ialistannen.staffsecure;

import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import com.perceivedev.perceivecore.command.AbstractCommandNode;
import com.perceivedev.perceivecore.command.CommandSystemUtil;
import com.perceivedev.perceivecore.command.CommandTree;
import com.perceivedev.perceivecore.command.DefaultCommandExecutor;
import com.perceivedev.perceivecore.command.DefaultTabCompleter;
import com.perceivedev.perceivecore.language.I18N;
import com.perceivedev.perceivecore.language.MessageProvider;

import me.ialistannen.staffsecure.commands.CommandStaffSecure;
import me.ialistannen.staffsecure.event.PlayerListener;
import me.ialistannen.staffsecure.playerdata.PlayerData;
import me.ialistannen.staffsecure.playerdata.PlayerDataManager;
import me.ialistannen.staffsecure.playerdata.PlayerTokenManager;
import me.ialistannen.staffsecure.util.Util;

public final class StaffSecure extends JavaPlugin {

    private static StaffSecure instance;

    private MessageProvider    language;
    private PlayerDataManager  playerDataManager;
    private PlayerTokenManager playerTokenManager;

    public void onEnable() {
        instance = this;

        ConfigurationSerialization.registerClass(PlayerData.class);

        {
            I18N.copyDefaultFiles(this, true, "me.ialistannen.staffsecure.language");
            language = new I18N(this, "me.ialistannen.staffsecure.language");
            language.setLanguage(Locale.forLanguageTag(getConfig().getString("language")));
        }

        playerDataManager = new PlayerDataManager(getDataFolder().toPath().resolve("data"));
        playerTokenManager = new PlayerTokenManager();

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        
        reload();
    }

    private void reload() {
        reloadConfig();

        CommandSystemUtil.unregisterCommand(Util.tr("command.main.keyword"));

        language.reload();

        CommandTree tree = new CommandTree();
        CommandExecutor executor = new DefaultCommandExecutor(tree, getLanguage());
        TabCompleter tabCompleter = new DefaultTabCompleter(tree);
        AbstractCommandNode node = new CommandStaffSecure();

        tree.addTopLevelChildAndRegister(node, executor, tabCompleter, this);
        tree.attachHelp(node, "staffsecure.help", getLanguage());
    }

    @Override
    public void onDisable() {
        // prevent the old instance from still being around.
        instance = null;
    }

    /**
     * @return The {@link PlayerTokenManager}
     */
    public PlayerTokenManager getPlayerTokenManager() {
        return playerTokenManager;
    }

    /**
     * @return The {@link PlayerDataManager}
     */
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    /**
     * @return The current language
     */
    public MessageProvider getLanguage() {
        return language;
    }

    /**
     * Returns the plugins instance
     *
     * @return The plugin instance
     */
    public static StaffSecure getInstance() {
        return instance;
    }
}
