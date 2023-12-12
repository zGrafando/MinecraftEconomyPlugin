package com.grafando.economy.commands;

import com.grafando.economy.data.Transactions;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.util.ArrayList;

public class AdjustRentPrice implements CommandExecutor{

    private Player player;
    private Transactions transact = new Transactions();
    private Connection conn;
    private ArrayList<String> RenterLicenceId = new ArrayList<>();

    public AdjustRentPrice(Connection connection){
        this.conn = connection;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        player = (Player) sender;
        if (sender instanceof Player){
            if (cmd.getName().equalsIgnoreCase("adjustrentprice")){
                if (transact.isInClaim(player.getLocation().getWorld().getName(), player.getLocation().getX(), player.getLocation().getZ(),
                        conn, "uuid") != null && transact.isInClaim(player.getLocation().getWorld().getName(), player.getLocation().getX(),
                        player.getLocation().getZ(), conn, "uuid").equalsIgnoreCase(player.getUniqueId().toString()) && checkRentersLicences(player.getUniqueId().toString())){
                    if (args[0] == null || args[0].isEmpty()){
                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                ('&', "&6No Command-parameter detected; you must add the new price to your statement"));
                    }else{
                        if (!args[0].chars().allMatch(Character::isDigit) || Integer.parseInt(args[0]) < 0) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&6Command parameter must be number above 0"));
                        }else{
                            int newPrice = Integer.parseInt(args[0]);
                            int claimId = Integer.parseInt(transact.isInClaim(player.getLocation().getWorld().getName(), player.getLocation().getX(),
                                    player.getLocation().getZ(), conn, "ClaimId"));
                            transact.updateRentPrice(newPrice, claimId, conn);
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&6Rent price successfully adjusted"));
                        }
                    }
                }else{
                    player.sendMessage(ChatColor.translateAlternateColorCodes
                            ('&', "&6This is either not your claim; or if it is, you do not have a necessary renting licence"));
                }
            }
        }
        return true;
    }

    public boolean checkRentersLicences(String uuid){
        RenterLicenceId.clear();
        RenterLicenceId = transact.getAllLicences(RenterLicenceId, 0, conn);
        boolean Statingus = false;
        for (int i = 0; i < RenterLicenceId.size(); i++){
            if (RenterLicenceId.get(i).equalsIgnoreCase(uuid)){
                Statingus = true;
            }
        }
        return Statingus;
    }
}


