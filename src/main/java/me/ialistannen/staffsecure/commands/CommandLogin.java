package me.ialistannen.staffsecure.commands;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import com.perceivedev.perceivecore.command.CommandResult;
import com.perceivedev.perceivecore.command.CommandSenderType;
import com.perceivedev.perceivecore.command.TranslatedCommandNode;
import com.perceivedev.perceivecore.utilities.time.DurationParser;

import me.ialistannen.staffsecure.StaffSecure;
import me.ialistannen.staffsecure.event.PlayerAuthenticateEvent;
import me.ialistannen.staffsecure.playerdata.PlayerData;
import me.ialistannen.staffsecure.playerdata.PlayerDataManager;
import me.ialistannen.staffsecure.util.Util;

/**
 * Allows you to login
 */
class CommandLogin extends TranslatedCommandNode {

    CommandLogin() {
        super(
                  new Permission(StaffSecure.getInstance().getConfig().getString("permissions.commands.login")),
                  "command.login",
                  StaffSecure.getInstance().getLanguage(),
                  CommandSenderType.PLAYER);
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, List<String> list, int i) {
        return Collections.emptyList();
    }

    @Override
    protected CommandResult executePlayer(Player player, String... args) {
        if (args.length < 1) {
            return CommandResult.SEND_USAGE;
        }

        PlayerDataManager passwordManager = StaffSecure.getInstance().getPlayerDataManager();

        if (!passwordManager.isRegistered(player.getUniqueId())) {
            player.sendMessage(Util.trWithPrefix("status.not.registered"));
            return CommandResult.SUCCESSFULLY_INVOKED;
        }

        if (Util.isLoggedIn(player)) {
            player.sendMessage(Util.trWithPrefix("status.already.logged.in"));
            return CommandResult.SUCCESSFULLY_INVOKED;
        }

        boolean correct = passwordManager.isCorrect(player.getUniqueId(), args[0]);

        if (correct) {
            player.sendMessage(Util.trWithPrefix("command.login.successfully.logged.in"));
            Util.setLoginStatus(player, true);

            PlayerData playerData = passwordManager.getPlayerData(player.getUniqueId());

            // update last IP
            if (!player.getAddress().getAddress().equals(playerData.getLastAddress())) {
                playerData.setLastAddress(player.getAddress().getAddress());
                StaffSecure.getInstance().getPlayerDataManager().saveToFile(playerData, player.getUniqueId());
            }

            Bukkit.getPluginManager().callEvent(new PlayerAuthenticateEvent(player));
            Util.setFailedLoginAttempts(player, 0);
        } else {
            int failedAttempts = Util.getFailedLoginAttempts(player) + 1;
            int maxFailedAttempts = StaffSecure.getInstance().getConfig().getInt("max.login.attempts");
            
            player.sendMessage(Util.trWithPrefix("command.login.password.incorrect", failedAttempts, maxFailedAttempts));
            Util.increaseFailedLoginAttempts(player);

            if (failedAttempts >= maxFailedAttempts) {
                banPlayerForFailedLogin(player, failedAttempts);
                return CommandResult.SUCCESSFULLY_INVOKED;
            }
        }

        return CommandResult.SUCCESSFULLY_INVOKED;
    }

    private void banPlayerForFailedLogin(Player player, int failedAttempts) {
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        long duration;
        try {
            duration = DurationParser.parseDuration(StaffSecure.getInstance().getConfig().getString("max.login.ban.duration"));
        } catch (RuntimeException e) {
            StaffSecure.getInstance().getLogger().log(Level.WARNING, "Invalid ban duration entered: '"
                      + StaffSecure.getInstance().getConfig().getString("max.login.ban.duration")
                      + "'.", e);
            return;
        }
        String message = Util.tr("failed.login.banned", DurationFormatUtils.formatDurationWords(duration, true, true), failedAttempts);

        banList.addBan(player.getName(), message, new Date(System.nanoTime() + duration), null);
        
        // reset attempts
        Util.setFailedLoginAttempts(player, 0);
        
        player.kickPlayer(message);
    }
}
