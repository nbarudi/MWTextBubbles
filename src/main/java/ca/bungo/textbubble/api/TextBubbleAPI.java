package ca.bungo.textbubble.api;

import ca.bungo.textbubble.TextBubble;
import ca.bungo.textbubble.api.types.Bubble;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * API Handler class which will handle all the logic for creating text bubbles for players
 * This will allow you to create messages which anyone can see and also messages which only set players can see
 * <br/>
 * <b>DO NOT CREATE A NEW INSTANCE. FETCH FROM {@link TextBubble} PLUGIN CLASS</b>
 * <br/>
 * The API makes use of the TextBubble plugin to schedule removal tasks, it will need to be running in order to function
 * */
public class TextBubbleAPI {

    private final Map<String, Bubble> bubbles;

    public TextBubbleAPI() {
        this.bubbles = new HashMap<>();
    }


    /**
     * Get the Bubble Handler for the supplied Player
     * Will create a new Bubble Object if one does not exist
     * @param owningPlayer Player which will own the Text Bubble Data
     * @return {@link Bubble} object for the Player
     * */
    public Bubble getPlayerBubble(Player owningPlayer) {
        bubbles.computeIfAbsent(owningPlayer.getUniqueId().toString(), Bubble::new);
        return bubbles.get(owningPlayer.getUniqueId().toString());
    }

    /**
     * Get the Bubble Handler based off the supplied Players UUID
     * Will create a new Bubble Object if one does not exist
     * @param owningPlayer Player which will own the Text Bubble Data
     * @return {@link Bubble} object for the Player
     * */
    public Bubble getPlayerBubbleFromUUID(String owningPlayer) {
        bubbles.computeIfAbsent(owningPlayer, k -> new Bubble(owningPlayer));
        return bubbles.get(owningPlayer);
    }

    /**
     * Send a message which all players can see in a Text Bubble
     * @param uuid UUID of the entity who sent the message, (What entity the bubble will be on)
     * @param messages All messages which will be added to the Text Bubble
     * */
    public void sendGlobalMessage(String uuid, String ... messages) {
        for(Player player : Bukkit.getOnlinePlayers()){
            if(player.getUniqueId().toString().equals(uuid)) continue;
            getPlayerBubble(player).addText(uuid, messages);
        }
    }

    /**
     * Create a Text Bubble which only a specific player can see
     * All Text Bubbles are handled through NMS which means only the "reciver" will see the text
     * @param senderUUID UUID of the Entity which sent the message
     * @param receiverUUID UUID of the Player should recieve the message
     * @param messages Messages which should be added
     * */
    public void sendPrivateMessage(String senderUUID, String receiverUUID, String ... messages) {
        Bubble bubble = getPlayerBubbleFromUUID(receiverUUID);
        bubble.addText(senderUUID, messages);
    }

}
