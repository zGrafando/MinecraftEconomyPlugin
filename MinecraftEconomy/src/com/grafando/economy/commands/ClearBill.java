package com.grafando.economy.commands;

import com.grafando.economy.data.Transactions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class ClearBill implements CommandExecutor {

    private Player player;
    private Transactions transact = new Transactions();
    private Connection conn;
    private HashMap<String, Integer> Bids = new HashMap<>();

    public ClearBill(Connection connection){
        this.conn = connection;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        player = (Player) sender;
        if (sender instanceof Player){
            if (cmd.getName().equalsIgnoreCase("clearbill")) {
                if (args.length > 0){
                    int BillId = Integer.parseInt(args[0]);
                    if (BillId != 0) {
                        if (transact.getSqlSelectFromBilling(BillId, conn, "BilledUuid").equalsIgnoreCase(player.getUniqueId().toString())){
                            if (transact.removeBill(BillId, conn)){
                                player.sendMessage(ChatColor.translateAlternateColorCodes
                                        ('&', "&6 Bill cleared!"));
                            }else{
                                player.sendMessage(ChatColor.translateAlternateColorCodes
                                        ('&', "&6 Clearing the bill was unsuccessful"));
                            }
                        }else{
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&f&lThis Bill Identifier references a bill that does not belong to you"));
                        }
                    }else{
                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                ('&', "&f&lBill identifier can never be 0"));
                    }
                }else{
                    player.sendMessage(ChatColor.translateAlternateColorCodes
                            ('&', "&f&lYou must add a billnumber to specify which bill is to be read"));
                }
            }
        }
        return true;
    }
}
