package ca.bungo.textbubble.api.types;

import ca.bungo.textbubble.TextBubble;
import com.mojang.authlib.minecraft.TelemetrySession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.entity.CraftTextDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Main handler class for all things related to Text Bubbles
 * This class handles all creation, deletion, timers, and stack management for Text Bubbles
 * This content is all automated so there are no methods you should need to directly call unless trying to bypass the API
 * */
public class Bubble {

    private final String owningPlayerUUID;
    private final Map<String, Deque<String>> bubbleContent;
    private final Map<String, Deque<Integer>> bubbleTimes;
    private final Map<String, BukkitTask> bukkitTimers;
    private final Map<String, TextDisplay> textDisplays;

    /**
     * Create a new Bubble Object
     * This will not create the entity
     * @param owningPlayerUUID UUID of the player who owns this text bubble handler
     * */
    public Bubble(String owningPlayerUUID) {
        this.owningPlayerUUID = owningPlayerUUID;

        this.bubbleContent = new HashMap<>();
        this.textDisplays = new HashMap<>();
        this.bubbleTimes = new HashMap<>();
        this.bukkitTimers = new HashMap<>();
    }


    public void rebuildTextBubble(@NotNull String entityUUID) {
        TextDisplay textDisplay = textDisplays.get(entityUUID);
        Deque<String> stack = bubbleContent.get(entityUUID);

        if(stack == null || stack.isEmpty()) {
            deleteDisplay(entityUUID);
            return;
        }

        Component bubbleComponent = Component.empty();
        for(String message : stack.reversed()){
            if(message.equals(stack.peekFirst()))
                bubbleComponent = bubbleComponent.append(MiniMessage.miniMessage().deserialize(message));
            else
                bubbleComponent = bubbleComponent.append(MiniMessage.miniMessage().deserialize(message)).appendNewline();
        }

        if(textDisplay != null)
            updateDisplay(textDisplay, bubbleComponent);
        else
            buildDisplay(entityUUID, bubbleComponent);

        if(bukkitTimers.containsKey(entityUUID)) return;
        else{
            Deque<Integer> timers = bubbleTimes.get(entityUUID);
            bukkitTimers.put(entityUUID,
                Bukkit.getServer().getScheduler().runTaskLater(TextBubble.getInstance(), () -> {
                    bukkitTimers.remove(entityUUID);
                    removeTopText(entityUUID);
                }, timers.removeLast())
            );
        }
    }

    public void buildDisplay(@NotNull String entityUUID, Component bubbleComponent) {
        Player owningPlayer = getOwningPlayer();
        if(owningPlayer == null) return;
        ServerPlayer serverPlayer = ((CraftPlayer)owningPlayer).getHandle();
        Level level = serverPlayer.level();

        Display.TextDisplay nmsDisplay = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);
        ServerEntity se = new ServerEntity(serverPlayer.serverLevel(), nmsDisplay, 0, false, packet -> {}, Set.of());

        sendPlayerPacket(new ClientboundAddEntityPacket(
            nmsDisplay, se
        ));

        TextDisplay display = (TextDisplay) nmsDisplay.getBukkitEntity();
        display.text(bubbleComponent);
        display.setLineWidth(240);
        display.setSeeThrough(false);
        Transformation transformation = display.getTransformation();

        display.setTransformation(new Transformation(
                transformation.getTranslation().add(0, 0.3f, 0),
                transformation.getLeftRotation(),
                transformation.getScale(),
                transformation.getRightRotation()
        ));

        display.setBillboard(org.bukkit.entity.Display.Billboard.VERTICAL);

        Entity owningEntity = Bukkit.getEntity(UUID.fromString(entityUUID));
        if(owningEntity == null) return;

        owningEntity.addPassenger(display);

        sendPlayerPacket(new ClientboundSetEntityDataPacket(nmsDisplay.getId(), Objects.requireNonNull(nmsDisplay.getEntityData().getNonDefaultValues())));

        textDisplays.put(entityUUID, display);
    }

    public void updateDisplay(@NotNull TextDisplay display, Component bubbleComponent) {
        display.text(bubbleComponent);

        Display.TextDisplay nmsDisplay = ((CraftTextDisplay) display).getHandle();

        sendPlayerPacket(new ClientboundSetEntityDataPacket(
                nmsDisplay.getId(),
                Objects.requireNonNull(nmsDisplay.getEntityData().getNonDefaultValues())
        ));
    }

    public void deleteDisplay(@NotNull String entityUUID) {
        TextDisplay display = textDisplays.remove(entityUUID);
        bubbleContent.remove(entityUUID);

        if(display != null){
            CraftTextDisplay craftTextDisplay = (CraftTextDisplay) display;
            Display.TextDisplay nmsDisplay = craftTextDisplay.getHandle();

            sendPlayerPacket(new ClientboundRemoveEntitiesPacket(
                    nmsDisplay.getId()
            ));
        }
    }

    public void addText(@NotNull String entityUUID, @NotNull String ... messages ) {
        Deque<String> bubbleStack;
        Deque<Integer> timeStack;
        if(bubbleContent.containsKey(entityUUID)){
            bubbleStack = bubbleContent.get(entityUUID);
            timeStack = bubbleTimes.get(entityUUID);
        }
        else {
            bubbleStack = new ArrayDeque<>();
            timeStack = new ArrayDeque<>();
            bubbleContent.put(entityUUID, bubbleStack);
            bubbleTimes.put(entityUUID, timeStack);
        }

        for(String message : messages){
            bubbleStack.push(message);
            timeStack.push(40 + message.split(" ").length*4);
        }

        rebuildTextBubble(entityUUID);
    }

    public void removeTopText(@NotNull String entityUUID) {
        Deque<String> stack = bubbleContent.get(entityUUID);
        if(stack == null || stack.isEmpty()) {
            deleteDisplay(entityUUID);
            return;
        }

        stack.removeLast();
        rebuildTextBubble(entityUUID);
    }

    private void sendPlayerPacket(@NotNull Packet<?> packet){
        Player player = getOwningPlayer();
        if(player == null) return;

        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        serverPlayer.connection.send(packet);
    }

    @Nullable
    public Player getOwningPlayer() { return Bukkit.getPlayer(UUID.fromString(owningPlayerUUID)); }

    @NotNull
    public String getOwningPlayerUUID() { return owningPlayerUUID; }
    public Map<String, Deque<String>> getTextBubbleStacks() { return bubbleContent; }
    public Deque<String> getTextBubbleStack(@NotNull String uuid) { return bubbleContent.get(uuid); }

}
