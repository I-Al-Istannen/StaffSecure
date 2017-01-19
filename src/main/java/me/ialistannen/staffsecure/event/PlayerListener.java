package me.ialistannen.staffsecure.event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.perceivedev.perceivecore.coreplugin.PerceiveCore;
import com.perceivedev.perceivecore.utilities.item.ItemFactory;
import com.perceivedev.perceivecore.utilities.time.DurationParser;

import me.ialistannen.staffsecure.StaffSecure;
import me.ialistannen.staffsecure.playerdata.PlayerData;
import me.ialistannen.staffsecure.util.Util;

/**
 * Listens for player events
 */
public class PlayerListener implements Listener {

    private final Map<UUID, ItemStack> helmetMap = new HashMap<>();

    {
        // restore helmets on reload / disable
        PerceiveCore.getInstance().getDisableManager().addListener(() -> {
            for (Map.Entry<UUID, ItemStack> entry : helmetMap.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());

                if (player == null) {
                    continue;
                }

                player.getInventory().setHelmet(entry.getValue());
            }
        });
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Util.setLoginStatus(event.getPlayer(), false);

        // restore old helmet
        if (helmetMap.containsKey(event.getPlayer().getUniqueId())) {
            event.getPlayer().getInventory().setHelmet(helmetMap.get(event.getPlayer().getUniqueId()));
        }

        // remove pending tokens
        StaffSecure.getInstance().getPlayerTokenManager().removeToken(event.getPlayer().getUniqueId());
    }

    // WOW. SO CLEAN!
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // are they required to login?
        if (!event.getPlayer().hasPermission(StaffSecure.getInstance().getConfig().getString("permissions.other.players.require.login"))) {
            Util.setLoginStatus(event.getPlayer(), true);
            return;
        }

        PlayerData playerData = StaffSecure.getInstance().getPlayerDataManager().getPlayerData(event.getPlayer().getUniqueId());

        //<editor-fold desc="Helmet changing and Blindness">
        // Changing their helmet when not logged in
        new BukkitRunnable() {
            @Override
            public void run() {
                // has already logged in
                if (Util.isLoggedIn(event.getPlayer())) {
                    return;
                }
                // here we have a not logged in player who needs to
                if (!StaffSecure.getInstance().getConfig().getBoolean("not.logged.in.change.player.helmet")) {
                    return;
                }

                helmetMap.put(event.getPlayer().getUniqueId(), event.getPlayer().getInventory().getHelmet());

                {
                    String helmetName = StaffSecure.getInstance().getConfig().getString("not.logged.in.player.helmet.material");
                    Material material = Material.matchMaterial(helmetName);
                    if (material == null) {
                        StaffSecure.getInstance().getLogger().warning("Material '" + helmetName + "' not found! Helmets will NOT be replaced.");
                        return;
                    }
                    ItemStack helmet = ItemFactory.builder(material).build();
                    event.getPlayer().getInventory().setHelmet(helmet);
                }

                if (StaffSecure.getInstance().getConfig().getBoolean("not.logged.in.blindness")) {
                    event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 60 * 5, 1, true, false));
                }
            }
        }.runTaskLater(StaffSecure.getInstance(), 2L);
        //</editor-fold>

        //<editor-fold desc="not logged in message">
        // ------------ LOGIN MESSAGE ---------------
        
        // send the messages in an interval, if they do not want messages per action
        if (!StaffSecure.getInstance().getConfig().getBoolean("not.logged.in.send.message.on.interact")) {
            startDelayedMessageDispatcher(event.getPlayer().getUniqueId());
        }
        //</editor-fold>
        
        
        // Do not auto-login them
        if (!StaffSecure.getInstance().getConfig().getBoolean("same.ip.auto.login")) {
            return;
        }
        // they haven't joined before or their data is void
        if (playerData == null || playerData.getLastAddress() == null) {
            return;
        }

        // if it was the same IP, auto-login
        if (event.getPlayer().getAddress().getAddress().equals(playerData.getLastAddress())) {
            event.getPlayer().sendMessage(Util.trWithPrefix("command.login.successfully.logged.in"));
            Util.setLoginStatus(event.getPlayer(), true);
        }
    }

    /**
     * Starts the message dispatcher. This just sends the "not logged in" message repeatedly, until they log in
     *
     * @param playerId The {@link UUID} of the player to start it for
     */
    private void startDelayedMessageDispatcher(UUID playerId) {
        String delayString = StaffSecure.getInstance().getConfig().getString("not.logged.in.send.message.delay");
        long delayTicks = 10 * 60;

        try {
            delayTicks = DurationParser.parseDurationToTicks(delayString);
        } catch (RuntimeException e) {
            StaffSecure.getInstance().getLogger().log(Level.WARNING, "Couldn't parse delay: 'not.logged.in.send.message.delay'", e);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                Player onlinePlayer = Bukkit.getPlayer(playerId);
                if (onlinePlayer == null || Util.isLoggedIn(onlinePlayer)) {
                    cancel();
                    return;
                }
                sendNotLoggedInOrRegisteredMessage(Util.trWithPrefix("status.not.logged.in"), onlinePlayer, true);
            }
        }.runTaskTimer(StaffSecure.getInstance(), 0, delayTicks);
    }

    @EventHandler
    public void onAuthenticate(PlayerAuthenticateEvent event) {
        if (helmetMap.containsKey(event.getPlayer().getUniqueId())) {
            ItemStack helmet = helmetMap.get(event.getPlayer().getUniqueId());

            event.getPlayer().getInventory().setHelmet(helmet);
            event.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
        }
    }

    //    +----------------------+
    //    |                      |
    //    |       FREEZING       |
    //    |                      |
    //    +----------------------+

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (!Util.isLoggedIn((Player) event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!Util.isLoggedIn(event.getPlayer())) {
            sendNotLoggedInOrRegisteredMessage(Util.trWithPrefix("not.logged.in.prevented.interact"), event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (!Util.isLoggedIn((Player) event.getWhoClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (Util.isLoggedIn(event.getPlayer())) {
            return;
        }

        if (!event.getFrom().toVector().equals(event.getTo().toVector())) {
            sendNotLoggedInOrRegisteredMessage(Util.trWithPrefix("not.logged.in.prevented.move"), event.getPlayer());
        }
        event.setTo(event.getFrom().setDirection(event.getTo().getDirection()));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!Util.isLoggedIn(event.getPlayer())) {
            sendNotLoggedInOrRegisteredMessage(Util.trWithPrefix("not.logged.in.prevented.chat"), event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {
        if (Util.isLoggedIn(event.getPlayer())) {
            return;
        }
        String command = event.getMessage().substring(1).split(" ")[0];

        if (StaffSecure.getInstance().getConfig().getStringList("pre.login.allowed.commands").contains(command.toLowerCase())) {
            return;
        }

        sendNotLoggedInOrRegisteredMessage(Util.trWithPrefix("not.logged.in.prevented.command", command), event.getPlayer());
        event.setCancelled(true);
    }

    /**
     * Sends the "not logged in" or "not registered" messages to the player
     *
     * @param notLoggedInMessage The message to send when they are not logged in
     * @param player The player to send it for
     * @param forceSend If true, the message will be send regardless of the settings in {@code not.logged.in.send.message.on.interact}
     */
    private void sendNotLoggedInOrRegisteredMessage(String notLoggedInMessage, Player player, boolean forceSend) {
        if (!forceSend && !StaffSecure.getInstance().getConfig().getBoolean("not.logged.in.send.message.on.interact")) {
            return;
        }

        if (StaffSecure.getInstance().getPlayerDataManager().isRegistered(player.getUniqueId())) {
            player.sendMessage(notLoggedInMessage);
        } else {
            player.sendMessage(Util.trWithPrefix("status.not.registered"));
        }
    }

    private void sendNotLoggedInOrRegisteredMessage(String notLoggedInMessage, Player player) {
        sendNotLoggedInOrRegisteredMessage(notLoggedInMessage, player, false);
    }
}
