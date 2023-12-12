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

public class BidRent implements CommandExecutor{

    private Player player;
    private Transactions transact = new Transactions();
    private Connection conn;
    private HashMap<String, Integer> Bids = new HashMap<>();
    private String EstablishedPlayerName;

    public BidRent(Connection connection){
        this.conn = connection;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        player = (Player) sender;
        if (sender instanceof Player){
            if (cmd.getName().equalsIgnoreCase("bidrent")) {
                if (!args[0].chars().allMatch(Character::isDigit) || Integer.parseInt(args[0]) < 0) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes
                            ('&', "&6Command parameter must be number above 0"));
                }else {
                    if (args[0] != null && !args[0].isEmpty()) {
                        Bids.clear();
                        int claimId = Integer.parseInt(transact.isInClaim(player.getLocation().getWorld().getName(), player.getLocation().getX(),
                                player.getLocation().getZ(), conn, "ClaimId"));
                        Bids = transact.getAllShopBids(Bids, 0, claimId, conn);
                        boolean Status = false;
                        for (Map.Entry<String, Integer> entry : Bids.entrySet()) {
                            String Name = entry.getKey();
                            int Bid = entry.getValue();
                            if (player.getName().equalsIgnoreCase(Name)) {
                                Status = true;
                                EstablishedPlayerName = Name;
                            }
                        }
                        if (Status) {
                            transact.updateShopBid(Integer.parseInt(args[0]), EstablishedPlayerName, conn);
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&6You have successfully updated your bid for this shop"));
                        } else {
                            transact.insertShopBid(claimId, Integer.parseInt(args[0]), player.getUniqueId().toString(), player.getName(), conn);
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&6You have successfully placed a bid for this shop"));
                        }
                        int ShopBidId = Integer.parseInt(transact.getSqlSelectFromShopBidByNameAndBid(player.getName(), Integer.parseInt(args[0]), conn, "ShopBidId"));
                        if (transact.selectFromClaimById(claimId, conn, "CurrentHighestBidder").isEmpty()){
                            transact.updateHighestBidder(ShopBidId, claimId, conn);
                        }else{
                            if (Integer.parseInt(transact.getSqlSelectFromShopBidById(Integer.parseInt(transact.selectFromClaimById
                                    (claimId, conn, "CurrentHighestBidder")), conn, "Bid")) < Integer.parseInt(args[0])){
                                transact.updateHighestBidder(ShopBidId, claimId, conn);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
