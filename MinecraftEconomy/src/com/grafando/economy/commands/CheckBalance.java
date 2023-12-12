package com.grafando.economy.commands;

import com.grafando.economy.data.Transactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.util.ArrayList;

public class CheckBalance implements CommandExecutor {

    private Player player;
    private Player targetPlayer;
    private OfflinePlayer offlinePlayer;
    private Transactions transaction = new Transactions();
    private Connection conn;
    private boolean status;

    public CheckBalance(Connection connection){
        this.conn = connection;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        player = (Player) sender;
        if (sender instanceof Player){
            if (cmd.getName().equalsIgnoreCase("bal")){
                if (args.length < 1) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&6 Your balance is: " + (transaction.getBalance(player.getUniqueId().toString(), conn))));
                }else{
                    if (Bukkit.getPlayer(args[0]) != null) {
                        if (Bukkit.getPlayer(args[0]).isOnline()) {
                            targetPlayer = Bukkit.getPlayer(args[0]);
                            if (transaction.checkPlayerBalanceExistance(targetPlayer.getUniqueId().toString(), conn)) {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6" + targetPlayer.getName() +
                                        " has a total balance of: " + transaction.getBalance(targetPlayer.getUniqueId().toString(), conn)));
                            } else {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        "&6This Player seems not to exist"));
                            }
                        }else{
                            OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
                            for (int i = 0; i < offlinePlayers.length; i++) {
                                if (offlinePlayers[i].equals(args[0])) {
                                    offlinePlayer = offlinePlayers[i];
                                    if (transaction.checkPlayerBalanceExistance(offlinePlayer.getUniqueId().toString(), conn)) {
                                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6" + offlinePlayer.getName() +
                                                " has a total balance of: " + transaction.getBalanceOffline(offlinePlayer, conn)));
                                        status = true;
                                    }
                                }
                            }
                            if (!status){
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        "&6This Player seems not to exist"));
                            }
                        }
                    }else{
                        if (args[0] == null) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    "&6Extend this command by player name"));
                        }else{
                            String PlayerAttempt = args[0];
                            if (PlayerAttempt != null || !PlayerAttempt.isEmpty()) {
                                OfflinePlayer[] offlinePlayerList = Bukkit.getOfflinePlayers();
                                for (int i = 0; i < offlinePlayerList.length; i++) {
                                    if (offlinePlayerList[i].getName().equals(PlayerAttempt)) {
                                        offlinePlayer = offlinePlayerList[i];
                                        if (transaction.checkPlayerBalanceExistance(offlinePlayer.getUniqueId().toString(), conn)) {
                                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6" + offlinePlayer.getName() +
                                                    " has a total balance of: " + transaction.getBalanceOffline(offlinePlayer, conn)));
                                            status = true;
                                        }
                                    }
                                }
                                if (!status){
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                            "&6This Player seems not to exist"));
                                }
                            }else{
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        "&6No Value found in argument"));

                            }
                        }
                    }
                }
            }
        }

        return true;
    }
}

