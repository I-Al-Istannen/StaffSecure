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
import me.ialistannen.staffsecure.playerdata.PlayerTokenManager;
import me.ialistannen.staffsecure.util.Util;

/**
 * Allows you to redeem a pin and change your password
 */
class CommandRedeemPin extends TranslatedCommandNode {

    CommandRedeemPin() {
        super(
                  new Permission(StaffSecure.getInstance().getConfig().getString("permissions.commands.redeemPin")),
                  "command.redeem.pin",
                  StaffSecure.getInstance().getLanguage(),
                  CommandSenderType.PLAYER);
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, List<String> list, int i) {
        return Collections.emptyList();
    }

    @Override
    protected CommandResult executePlayer(Player player, String... args) {
        if (args.length < 2) {
            return CommandResult.SEND_USAGE;
        }

        String token = args[0];

        PlayerTokenManager tokenManager = StaffSecure.getInstance().getPlayerTokenManager();

        if (!tokenManager.isCorrectToken(player.getUniqueId(), token)) {
            player.sendMessage(Util.trWithPrefix("command.redeem.pin.incorrect.token", token));
            return CommandResult.SUCCESSFULLY_INVOKED;
        }

        tokenManager.removeToken(player.getUniqueId());

        String newPassword = args[1];

        PlayerDataManager dataManager = StaffSecure.getInstance().getPlayerDataManager();
        dataManager.addPlayer(player.getUniqueId(), newPassword);

        player.sendMessage(Util.trWithPrefix("command.redeem.pin.password.changed.password", newPassword));

        return CommandResult.SUCCESSFULLY_INVOKED;
    }
}
