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

public class ClearBids implements CommandExecutor {

    private Player player;
    private Transactions transact = new Transactions();
    private Connection conn;
    private HashMap<String, Integer> Bids = new HashMap<>();

    public ClearBids(Connection connection){
        this.conn = connection;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        player = (Player) sender;
        if (sender instanceof Player){
            if (cmd.getName().equalsIgnoreCase("clearbids")) {
                int claimId = Integer.parseInt(transact.isInClaim(player.getLocation().getWorld().getName(), player.getLocation().getX(),
                        player.getLocation().getZ(), conn, "ClaimId"));
                Bids = transact.getAllShopBids(Bids, 0, claimId, conn);
                boolean Status = true;
                for (Map.Entry<String, Integer> entry: Bids.entrySet()){
                    String Name = entry.getKey();
                    int Bid = entry.getValue();
                    if (!transact.resetBid(Bid, Name, conn)){
                        Status = false;
                    }
                }
                if (!Status){
                    player.sendMessage(ChatColor.translateAlternateColorCodes
                            ('&', "&6 Clearing the bids was unsuccessful"));
                }else{
                    player.sendMessage(ChatColor.translateAlternateColorCodes
                            ('&', "&6 Bids cleared!"));
                }
            }
        }
        return true;
    }
}
