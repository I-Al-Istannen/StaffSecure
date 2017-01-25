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
 * Registers the player
 */
class CommandRegister extends TranslatedCommandNode {

    CommandRegister() {
        super(
                  new Permission(StaffSecure.getInstance().getConfig().getString("permissions.commands.register")),
                  "command.register",
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

        String password = args[0];

        PlayerDataManager passwordManager = StaffSecure.getInstance().getPlayerDataManager();

        if (passwordManager.isRegistered(player.getUniqueId())) {
            player.sendMessage(Util.trWithPrefix("command.register.already.registered"));
            return CommandResult.SUCCESSFULLY_INVOKED;
        }

        passwordManager.addPlayer(player.getUniqueId(), password);

        player.sendMessage(Util.trWithPrefix("command.register.successfully.registered"));

        return CommandResult.SUCCESSFULLY_INVOKED;
    }
}
