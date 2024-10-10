package ca.bungo.textbubble.commands;

import ca.bungo.textbubble.TextBubble;
import ca.bungo.textbubble.api.types.Bubble;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandDevelopmentTest extends Command {


    public CommandDevelopmentTest(@NotNull String name) {
        super(name);
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
        if(!(commandSender instanceof Player player)) {return false;}

        TextBubble.getInstance().getTextBubbleAPI().sendPrivateMessage(
                player.getUniqueId().toString(),
                player.getUniqueId().toString(),
                "This is a private message!"
        );

        return false;
    }
}
