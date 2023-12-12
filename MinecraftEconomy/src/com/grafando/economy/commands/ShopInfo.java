package com.grafando.economy.commands;

import com.grafando.economy.data.Transactions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShopInfo implements CommandExecutor{

    private Player player;
    private Transactions transact = new Transactions();
    private Connection conn;
    private HashMap<String, Integer> Bids = new HashMap<>();

    public ShopInfo(Connection connection){
        this.conn = connection;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        player = (Player) sender;
        if (sender instanceof Player){
            if (cmd.getName().equalsIgnoreCase("getshopinfo")) {
                int claimId = Integer.parseInt(transact.isInClaim(player.getLocation().getWorld().getName(), player.getLocation().getX(),
                        player.getLocation().getZ(), conn, "ClaimId"));
                Bids.clear();
                Bids = transact.getAllShopBids(Bids, 0, claimId, conn);
                String Renter = transact.getRenterNameFromClaimId(claimId, conn);
                if (Renter == null || Renter.isEmpty()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes
                            ('&', "&e>&f&b  -&7 no &fone &7is currently renting this shop"));
                } else{
                    player.sendMessage(ChatColor.translateAlternateColorCodes
                            ('&', "&7This &dshop &7is currently &rented &7by:"));
                    player.sendMessage(ChatColor.translateAlternateColorCodes
                            ('&', "&e>&f&l  -&6 " + Renter));
                }
                player.sendMessage(ChatColor.translateAlternateColorCodes
                        ('&', "&7The current &b&lbids &7for this &dshop &7are:"));
                if (Bids.isEmpty()){
                    player.sendMessage(ChatColor.translateAlternateColorCodes
                            ('&', "&6>      &f&l- &7In this case &4absent..."));
                }else{
                    for (Map.Entry<String, Integer> entry: Bids.entrySet()){
                        String Name = entry.getKey();
                        int Bid = entry.getValue();
                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                ('&', "&6>      &f&l- &b"+Bid+" &f&l-&f by: &6"+Name));
                    }
                }
            }
        }
        return true;
    }
}
