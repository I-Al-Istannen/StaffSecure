package me.ialistannen.staffsecure.commands;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import me.ialistannen.bukkitutilities.command.CommandResult;
import me.ialistannen.bukkitutilities.command.CommandSenderType;
import me.ialistannen.bukkitutilities.command.TranslatedCommandNode;
import me.ialistannen.staffsecure.StaffSecure;
import me.ialistannen.staffsecure.playerdata.PlayerDataManager;
import me.ialistannen.staffsecure.util.Util;

/**
 * Allows the user to change his password
 */
class CommandChangePassword extends TranslatedCommandNode {

    CommandChangePassword() {
        super(
                  new Permission(StaffSecure.getInstance().getConfig().getString("permissions.commands.changePassword")),
                  "command.change.password",
                  StaffSecure.getInstance().getLanguage(),
                  CommandSenderType.PLAYER
        );
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

        String newPassword = args[0];

        PlayerDataManager passwordManager = StaffSecure.getInstance().getPlayerDataManager();

        if (!passwordManager.isRegistered(player.getUniqueId())) {
            player.sendMessage(Util.trWithPrefix("status.not.registered"));
            return CommandResult.SUCCESSFULLY_INVOKED;
        }
        if (!Util.isLoggedIn(player)) {
            player.sendMessage(Util.tr("status.not.logged.in"));
            return CommandResult.SUCCESSFULLY_INVOKED;
        }

        passwordManager.addPlayer(player.getUniqueId(), newPassword);

        player.sendMessage(Util.trWithPrefix("command.change.password.changed.password", newPassword));

        return CommandResult.SUCCESSFULLY_INVOKED;
    }
}
