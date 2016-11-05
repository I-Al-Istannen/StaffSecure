package me.ialistannen.staffsecure.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import com.perceivedev.perceivecore.command.AbstractCommandNode;
import com.perceivedev.perceivecore.command.CommandNode;
import com.perceivedev.perceivecore.command.CommandSenderType;
import com.perceivedev.perceivecore.command.TranslatedCommandNode;

import me.ialistannen.staffsecure.StaffSecure;

/**
 * The main command
 */
public class CommandStaffSecure extends TranslatedCommandNode {

    public CommandStaffSecure() {
        super(
                  new Permission(StaffSecure.getInstance().getConfig().getString("permissions.commands.staffSecure")),
                  "command.main",
                  StaffSecure.getInstance().getLanguage(),
                  CommandSenderType.ALL);

        addChild(new CommandLogin());
        addChild(new CommandRegister());
        addChild(new CommandChangePassword());
        addChild(new CommandGetPin());
        addChild(new CommandRedeemPin());
    }

    /**
     * Returns clones of the child commands, which are NOT bound to this command node
     *
     * @return The child commands, but NOT bound to this command node
     */
    public List<AbstractCommandNode> getNewChildCommands() {
        return Arrays.asList(new CommandLogin(), new CommandRegister(), new CommandChangePassword(), new CommandGetPin(), new CommandRedeemPin());
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, List<String> list, int i) {
        return getChildren().stream()
                  .filter(commandNode -> commandNode.hasPermission(commandSender) && commandNode.acceptsCommandSender(commandSender))
                  .map(CommandNode::getKeyword)
                  .collect(Collectors.toList());
    }
}
