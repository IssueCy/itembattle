package de.merix.itembattle.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StartCommand implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    final Set<Player> playersInGame = new HashSet<>();
    final Map<Player, Set<Integer>> playerItemIds = new HashMap<>();
    public String prefix = "§7[§6ItemBattle§7] §f";

    public StartCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("start")) {
            startGame();
            return true;
        }
        return false;
    }

    private void startGame() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.setGameMode(GameMode.SURVIVAL);
            onlinePlayer.setHealth(20);
            onlinePlayer.setFoodLevel(20);
            onlinePlayer.setSaturation(20);
            onlinePlayer.getInventory().clear();
            playersInGame.add(onlinePlayer);
            playerItemIds.put(onlinePlayer, new HashSet<>());
        }

        Bukkit.broadcastMessage(prefix + ChatColor.GREEN + "ItemBattle startet!");

        Bukkit.getScheduler().runTaskLater(plugin, this::endGame, 60 * 60 * 20L);
    }

    private void endGame() {
        Map<Player, Integer> playerItemCount = new HashMap<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            Set<Integer> itemIds = playerItemIds.getOrDefault(player, new HashSet<>());
            int uniqueItemCount = itemIds.size();
            playerItemCount.put(player, uniqueItemCount);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setGameMode(GameMode.ADVENTURE);
            playersInGame.remove(player);

            int uniqueItemCount = playerItemCount.getOrDefault(player, 0);
            player.sendMessage(prefix + ChatColor.DARK_PURPLE + "Du hast " + uniqueItemCount + " unterschiedliche Items gesammelt.");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_DEATH, 2, 1);

            for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
                if (!otherPlayer.equals(player)) {
                    otherPlayer.sendMessage(prefix + ChatColor.LIGHT_PURPLE + player.getName() + " hat " + uniqueItemCount + " unterschiedliche Items gesammelt.");
                    otherPlayer.playSound(otherPlayer.getLocation(), Sound.ENTITY_ENDERMAN_DEATH, 2, 1);
                }
            }
        }

        playerItemIds.clear();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setGameMode(GameMode.ADVENTURE);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playersInGame.remove(player);
        playerItemIds.remove(player);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (playersInGame.contains(player)) {
            ItemStack newItem = event.getItem().getItemStack();
            int newItemId = newItem.getType().ordinal();
            if (!playerItemIds.get(player).contains(newItemId)) {
                playerItemIds.get(player).add(newItemId);
                player.sendTitle(ChatColor.GREEN + "+1", ChatColor.GOLD + newItem.getType().toString(), 10, 70, 20);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2, 1);
            }
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (playersInGame.contains(player)) {
            ItemStack newItem = event.getCurrentItem();
            int newItemId = newItem.getType().ordinal();
            if (!playerItemIds.get(player).contains(newItemId)) {
                playerItemIds.get(player).add(newItemId);
                player.sendTitle(ChatColor.GREEN + "+1", ChatColor.GOLD + newItem.getType().toString(), 10, 70, 20);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2, 1);
            }
        }
    }
}
