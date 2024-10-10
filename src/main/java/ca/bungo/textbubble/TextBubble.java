package ca.bungo.textbubble;

import ca.bungo.textbubble.api.TextBubbleAPI;
import ca.bungo.textbubble.commands.CommandDevelopmentTest;
import ca.bungo.textbubble.events.ChatEventListeners;
import org.bukkit.plugin.java.JavaPlugin;

public class TextBubble extends JavaPlugin {

    private static TextBubble instance;

    private TextBubbleAPI textBubbleAPI;

    public void onEnable() {
        instance = this;

        textBubbleAPI = new TextBubbleAPI();

        //this.getServer().getCommandMap().register("textbubble", new CommandDevelopmentTest("test"));
        this.getServer().getPluginManager().registerEvents(new ChatEventListeners(), this);
    }

    public void onDisable() {}

    public TextBubbleAPI getTextBubbleAPI() { return textBubbleAPI; }


    public static TextBubble getInstance() {
        return instance;
    }

}
