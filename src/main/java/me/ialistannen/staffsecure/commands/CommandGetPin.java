package me.ialistannen.staffsecure.commands;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import me.ialistannen.bukkitutilities.command.CommandResult;
import me.ialistannen.bukkitutilities.command.CommandSenderType;
import me.ialistannen.bukkitutilities.command.TranslatedCommandNode;
import me.ialistannen.staffsecure.StaffSecure;
import me.ialistannen.staffsecure.playerdata.PlayerTokenManager;
import me.ialistannen.staffsecure.util.Util;

/**
 * Generates a Pin to login if you forgot your password
 */
class CommandGetPin extends TranslatedCommandNode {


    CommandGetPin() {
        super(
                  new Permission(StaffSecure.getInstance().getConfig().getString("permissions.commands.getPin")),
                  "command.get.pin",
                  StaffSecure.getInstance().getLanguage(),
                  CommandSenderType.PLAYER);
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, List<String> list, int i) {
        return Collections.emptyList();
    }

    @Override
    protected CommandResult executePlayer(Player player, String... args) {

        if(Util.isLoggedIn(player)) {
            player.sendMessage(Util.trWithPrefix("status.already.logged.in"));
            return CommandResult.SUCCESSFULLY_INVOKED;
        }
        
        PlayerTokenManager tokenManager = StaffSecure.getInstance().getPlayerTokenManager();
        
        if(tokenManager.contains(player.getUniqueId())) {
            player.sendMessage(Util.trWithPrefix("command.get.pin.token.already.generated"));
            return CommandResult.SUCCESSFULLY_INVOKED;
        }
        
        String token = tokenManager.generateAndAddToken(player.getUniqueId());
        
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        
        console.sendMessage(Util.tr("command.get.pin.console.token", token, player.getName()));
        
        player.sendMessage(Util.tr("command.get.pin.player.token.generated"));

        return CommandResult.SUCCESSFULLY_INVOKED;
    }
}
