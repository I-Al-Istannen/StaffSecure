package me.ialistannen.staffsecure.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Called when the player logged in
 */
public class PlayerAuthenticateEvent extends PlayerEvent {

    private static final HandlerList handlerList = new HandlerList();

    public PlayerAuthenticateEvent(Player who) {
        super(who);
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    /**
     * This is needed for classes extending Event
     *
     * @return The {@link HandlerList}
     */
    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
