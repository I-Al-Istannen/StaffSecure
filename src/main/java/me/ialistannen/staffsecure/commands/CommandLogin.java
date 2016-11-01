package me.ialistannen.staffsecure.commands;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import com.perceivedev.perceivecore.command.CommandResult;
import com.perceivedev.perceivecore.command.CommandSenderType;
import com.perceivedev.perceivecore.command.TranslatedCommandNode;

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
        } else {
            player.sendMessage(Util.trWithPrefix("command.login.password.incorrect"));
        }

        return CommandResult.SUCCESSFULLY_INVOKED;
    }
}
