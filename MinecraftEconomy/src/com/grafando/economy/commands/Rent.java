package com.grafando.economy.commands;

import com.grafando.economy.data.Transactions;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.bukkit.Bukkit.getServer;

public class Rent implements CommandExecutor{

    private Player player;
    private Transactions transact = new Transactions();
    private Connection conn;
    private ArrayList<String> RenterLicenceId = new ArrayList<>();
    private Sign ShopSign;

    public Rent(Connection connection){
        this.conn = connection;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        player = (Player) sender;
        if (sender instanceof Player){
            int claimId = Integer.parseInt(transact.isInClaim(player.getLocation().getWorld().getName(),
                    player.getLocation().getX(), player.getLocation().getZ(), conn, "ClaimId"));
            ShopSign = (Sign) new Location(getWorldFromString(transact.selectFromClaimById(claimId, conn, "Rentsignworld")), Integer.parseInt(transact.selectFromClaimById(claimId, conn, "Rentsignx")),
                    Integer.parseInt(transact.selectFromClaimById(claimId, conn, "Rentsigny")), Integer.parseInt(transact.selectFromClaimById(claimId, conn, "Rentsigny"))).getBlock().getState();
            if (cmd.getName().equalsIgnoreCase("rent")){
                String OwnerUuid = transact.isInClaim(player.getLocation().getWorld().getName(), player.getLocation().getX(), player.getLocation().getZ(), conn, "uuid");
                if (OwnerUuid.equalsIgnoreCase(player.getUniqueId().toString())) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes
                            ('&', "&6This claim is not legible for rent - you are the owner"));
                } else {
                    if (checkRentersLicences(OwnerUuid)) {
                        if (transact.isInClaim(player.getLocation().getWorld().getName(), player.getLocation().getX(), player.getLocation().getZ(),
                                conn, "renteduuid") == null || transact.isInClaim(player.getLocation().getWorld().getName(), player.getLocation().getX(),
                                player.getLocation().getZ(), conn, "renteduuid").isEmpty()) {


                            String dateToNextBilling = transact.getSqlSelectFromWorld(conn, "DateToNextBilling");
                            String[] StrArray = dateToNextBilling.split("/");
                            int dayOfNextBilling = Integer.parseInt(StrArray[2]);
                            int monthOfNextBilling = Integer.parseInt(StrArray[1]);
                            int yearOfNextBilling = Integer.parseInt(StrArray[0]);
                            LocalDate currentDate = now();
                            LocalDate nextBillingDate = now().withYear(yearOfNextBilling).withMonth(monthOfNextBilling).withDayOfMonth(dayOfNextBilling);
                            long daysBetween = DAYS.between(currentDate, nextBillingDate);
                            int rentPrice = Integer.parseInt(transact.selectFromClaimById(claimId, conn, "rentprice"));
                            int rentPriceLeftover = Integer.valueOf((int) ((rentPrice / 14) * daysBetween));

                            if (transact.getBalance(player.getUniqueId().toString(), conn) > rentPriceLeftover) {
                                if (transact.updateRenter(claimId, player.getUniqueId().toString(), dateToNextBilling, rentPrice,
                                        ShopSign.getLocation().getWorld().getName(), ShopSign.getLocation().getX(), ShopSign.getLocation().getY(),
                                        ShopSign.getLocation().getZ(), ShopSign.getLine(1), ShopSign.getLine(2), conn)) {
                                    if (transact.updateBalance(player.getUniqueId().toString(), rentPriceLeftover, "minus", conn)) {
                                        if (transact.updatePlot(Integer.parseInt(transact.selectFromPlayersByUUID(player.getUniqueId().toString(), conn, "PlayerId")),
                                                Integer.parseInt(transact.selectFromClaimById(claimId, conn, "rentprice")), transact.getPlotId(claimId, conn), conn)) {
                                            ShopSign.setLine(0, ChatColor.translateAlternateColorCodes
                                                    ('&', "&6[rented]"));
                                            ShopSign.setLine(1, ChatColor.translateAlternateColorCodes
                                                    ('&', "&fby: &b" + player.getName()));
                                            ShopSign.setLine(2, ChatColor.translateAlternateColorCodes
                                                    ('&', "&funtil: &c" + dateToNextBilling));
                                            ShopSign.setLine(3, ChatColor.translateAlternateColorCodes
                                                    ('&', "&ffor: &a" + transact.isInClaim(ShopSign.getLocation().getWorld().getName(),
                                                            ShopSign.getLocation().getX(), ShopSign.getLocation().getZ(), conn, "rentprice")));
                                            ShopSign.update();
                                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                                    ('&', "&6you have now rented this shop until the next billing period!"));
                                        } else {
                                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                                    ('&', "&6renting this shop was unsuccessful; plotting error...."));
                                        }
                                    } else {
                                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                                ('&', "&6renting this shop was unsuccessful"));
                                        int ClaimId = Integer.parseInt(transact.isInClaim(ShopSign.getLocation().getWorld().getName(), ShopSign.getLocation().getX(),
                                                ShopSign.getLocation().getZ(), conn, "ClaimId"));
                                        transact.resetRenter(ClaimId, conn);
                                    }
                                } else {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes
                                            ('&', "&6renting this shop was unsuccessful -> claimId may not have been found"));
                                }
                            } else {
                                player.sendMessage(ChatColor.translateAlternateColorCodes
                                        ('&', "&6You do not posses the money necessary to rent this shop"));
                            }

                        } else {
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&6This shop is already rented; extend the rent with /extendrent or wait for the renter to go out of business"));
                        }
                    }else{
                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                ('&', "&6You do not posses the licence necessary to rent out this build as a shop"));
                    }
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

    public World getWorldFromString(String worldName){
        World w = null;
        Iterator<World> worlds = Bukkit.getServer().getWorlds().iterator();
        while(worlds.hasNext()){
            World w1 = worlds.next();
            if (worldName.trim().equalsIgnoreCase(w1.getName())){
                w = w1;
            }
        }
        return w;
    }
}

