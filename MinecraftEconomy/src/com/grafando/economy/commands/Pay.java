package com.grafando.economy.commands;

import com.grafando.economy.data.Consolidation;
import com.grafando.economy.data.Transactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Pay implements CommandExecutor {

    private Consolidation consolidation = new Consolidation();
    private Player player;
    private Player targetPlayer;
    private int Amnt;
    private Transactions transaction = new Transactions();
    private Connection conn;
    private OfflinePlayer offlinePlayer;

    public Pay(Connection co){
        this.conn = co;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        player = (Player) sender;
        if (sender instanceof Player){
            if (cmd.getName().equalsIgnoreCase("pay")){
                if (args.length < 2){
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&8 You must define a player and an amount to complete a successful transaction"));
                }else{
                    if (Bukkit.getPlayer(args[0]) == null) {
                        targetPlayer = Bukkit.getPlayer(args[0]);
                        Amnt = Integer.parseInt(args[1]);
                        if (transaction.getBalance(player.getUniqueId().toString(), conn) < Amnt) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    "&8 You do not possess enough money to pay this amount"));
                        } else {
                            if (transaction.checkPlayerBalanceExistance(targetPlayer.getUniqueId().toString(), conn)) {
                                transaction.updateBalance(targetPlayer.getUniqueId().toString(), Amnt, "plus", conn);
                                transaction.updateBalance(player.getUniqueId().toString(), Amnt, "minus", conn);
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        "&8 The player " + targetPlayer + " has received the intended " + Amnt));
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        "&6 You now have " + (transaction.getBalance(player.getUniqueId().toString(), conn)) + " in your bank-account"));
                                targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6" + player.getName() + " has payed you " + Amnt));
                                transaction.updateAllDirectPayments(player.getUniqueId().toString(), Amnt, conn);
                                transaction.updateAllDirectCollections(targetPlayer.getUniqueId().toString(), Amnt, conn);
                                String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                                String message = player.getUniqueId().toString() + ";" + targetPlayer.getUniqueId().toString() + ";" + Amnt + ";" + timeStamp + "  |:| -->" +
                                        player.getName() + "(" + player.getUniqueId().toString() + ") payed " + targetPlayer.getName() +
                                        "(" + targetPlayer.getUniqueId().toString() + ") +" + Amnt + " at: " + timeStamp;
                                consolidation.writeTransactionFile(message);
                            } else {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        "&8 The player you specified does not seem to exist"));
                            }
                        }
                    }else{
                        if (args[0] == null){
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    "&8 Command Argument not found"));
                        }else {
                            String playerAttempt = args[0];
                            boolean Status = false;
                            OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
                            for (int i = 0; i < offlinePlayers.length; i++){
                                if (offlinePlayers[i].getName().equalsIgnoreCase(playerAttempt)){
                                    Status = true;
                                    offlinePlayer = offlinePlayers[i];
                                }
                            }
                            if (!Status){
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        "&8 The player specified was not found"));
                            }else{
                                Amnt = Integer.parseInt(args[1]);
                                if (transaction.getBalance(player.getUniqueId().toString(), conn) < Amnt) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                            "&8 You do not possess enough money to pay this amount"));
                                } else {
                                    if (transaction.checkPlayerBalanceExistance(targetPlayer.getUniqueId().toString(), conn)) {
                                        transaction.updateBalance(targetPlayer.getUniqueId().toString(), Amnt, "plus", conn);
                                        transaction.updateBalance(player.getUniqueId().toString(), Amnt, "minus", conn);
                                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                                "&8 The player " + targetPlayer + " has received the intended " + Amnt));
                                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                                "&6 You now have " + (transaction.getBalance(player.getUniqueId().toString(), conn)) + " in your bank-account"));
                                        targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6" + player.getName() + " has payed you " + Amnt));
                                        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                                        String message = player.getUniqueId().toString() + ";" + targetPlayer.getUniqueId().toString() + ";" + Amnt + ";" + timeStamp + "  |:| -->" +
                                                player.getName() + "(" + player.getUniqueId().toString() + ") payed " + targetPlayer.getName() +
                                                "(" + targetPlayer.getUniqueId().toString() + ") +" + Amnt + " at: " + timeStamp;
                                        consolidation.writeTransactionFile(message);
                                    } else {
                                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                                "&8 The player you specified does not seem to exist"));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }
}

