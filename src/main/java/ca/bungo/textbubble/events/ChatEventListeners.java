package ca.bungo.textbubble.events;

import ca.bungo.textbubble.TextBubble;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatEventListeners implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String message = ((TextComponent)event.message()).content();

        Bukkit.getScheduler().runTask(TextBubble.getInstance(), () ->
                TextBubble.getInstance().getTextBubbleAPI().sendGlobalMessage(
                player.getUniqueId().toString(),
                message
        ));

    }

}
