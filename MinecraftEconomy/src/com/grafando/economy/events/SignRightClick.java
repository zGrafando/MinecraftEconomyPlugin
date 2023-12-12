package com.grafando.economy.events;

import com.grafando.economy.data.Consolidation;
import com.grafando.economy.data.Transactions;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;


public class SignRightClick implements Listener {

    private final Transactions transact = new Transactions();
    private final Consolidation consolidation = new Consolidation();
    private Sign ShopSign;
    private Player player;
    private Chest donatingChest;
    private ItemStack donatingStack;
    private ArrayList<String> RenterLicenceId = new ArrayList<>();
    private ResultSet rs;
    private Connection conn;

    public SignRightClick(Connection connection){
        this.conn = connection;
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent ClickSign) {
        if (ClickSign != null && ClickSign.getClickedBlock() != null) {
            player = ClickSign.getPlayer();
            if (ClickSign.getClickedBlock().isPassable()) {
                if (ClickSign.getClickedBlock().getType().equals(Material.SPRUCE_SIGN) ||
                        ClickSign.getClickedBlock().getType().equals(Material.SPRUCE_WALL_SIGN) ||
                        ClickSign.getClickedBlock().getType().equals(Material.ACACIA_SIGN) ||
                        ClickSign.getClickedBlock().getType().equals(Material.ACACIA_WALL_SIGN) ||
                        ClickSign.getClickedBlock().getType().equals(Material.BIRCH_SIGN) ||
                        ClickSign.getClickedBlock().getType().equals(Material.BIRCH_WALL_SIGN) ||
                        ClickSign.getClickedBlock().getType().equals(Material.CRIMSON_SIGN) ||
                        ClickSign.getClickedBlock().getType().equals(Material.CRIMSON_WALL_SIGN) ||
                        ClickSign.getClickedBlock().getType().equals(Material.DARK_OAK_SIGN) ||
                        ClickSign.getClickedBlock().getType().equals(Material.DARK_OAK_WALL_SIGN) ||
                        ClickSign.getClickedBlock().getType().equals(Material.JUNGLE_SIGN) ||
                        ClickSign.getClickedBlock().getType().equals(Material.JUNGLE_WALL_SIGN) ||
                        ClickSign.getClickedBlock().getType().equals(Material.OAK_SIGN) ||
                        ClickSign.getClickedBlock().getType().equals(Material.OAK_WALL_SIGN) ||
                        ClickSign.getClickedBlock().getType().equals(Material.WARPED_SIGN) ||
                        ClickSign.getClickedBlock().getType().equals(Material.WARPED_WALL_SIGN)){
                    ShopSign = (Sign) ClickSign.getClickedBlock().getState();
                    if (ClickSign.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                        if (player.getInventory().getItemInMainHand().getType().equals(Material.REDSTONE)) {
                            if (transact.isInClaim(ClickSign.getClickedBlock().getLocation().getWorld().getName(), ClickSign.getClickedBlock().getLocation().getX(),
                                    ClickSign.getClickedBlock().getLocation().getZ(), conn, "renteduuid") == null || !transact.isInClaim(ClickSign.getClickedBlock().getLocation().getWorld().getName(),
                                    ClickSign.getClickedBlock().getLocation().getX(), ClickSign.getClickedBlock().getLocation().getZ(),
                                    conn, "renteduuid").equalsIgnoreCase(player.getUniqueId().toString())){
                                if (transact.isInClaim(ClickSign.getClickedBlock().getLocation().getWorld().getName(),
                                        ClickSign.getClickedBlock().getLocation().getX(), ClickSign.getClickedBlock().getLocation().getZ(),
                                        conn, "uuid").isEmpty()){
                                    player.sendMessage(ChatColor.translateAlternateColorCodes
                                            ('&', "&8You cannot create a trade within areas that do not belong to you"));
                                }else{
                                    if (transact.isInClaim(ClickSign.getClickedBlock().getLocation().getWorld().getName(), ClickSign.getClickedBlock().getLocation().getX(),
                                            ClickSign.getClickedBlock().getLocation().getZ(), conn, "uuid").equalsIgnoreCase(player.getUniqueId().toString())){
                                        if (ShopSign.getLine(0).equalsIgnoreCase("[rent]")) {
                                            ShopSign.setLine(0, ChatColor.translateAlternateColorCodes('&', "&d[rent]"));
                                            ShopSign.update();
                                            if (ShopSign.getLine(3).chars().allMatch(Character::isDigit)) {
                                                int claimId = Integer.parseInt(transact.isInClaim(ShopSign.getLocation().getWorld().getName(), ShopSign.getLocation().getX(),
                                                        ShopSign.getLocation().getZ(), conn, "ClaimId"));
                                                if (transact.getPlotId(claimId, conn) > 0) {
                                                    player.sendMessage(ChatColor.translateAlternateColorCodes
                                                            ('&', "&6 This plot is already a registered renting property"));
                                                }else{
                                                    int LicenceId = Integer.parseInt(transact.selectLicenceByUUID(player.getUniqueId().toString(), conn, "LicenceId"));
                                                    LocalDateTime in7days = LocalDateTime.now();
                                                    String Date = in7days.getYear() + "/" + in7days.getMonthValue() + "/" + in7days.getDayOfMonth() + "/" + in7days.getHour() + "/" + in7days.getMinute();
                                                    if (transact.updateRentPrice(Integer.parseInt(ShopSign.getLine(3)), claimId, conn)) {
                                                        if (transact.createPlot(claimId, Date, -1, LicenceId,
                                                                Integer.parseInt(transact.selectFromClaimById(claimId, conn, "rentprice")), conn)) {
                                                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                                                    ('&', "&6 rent sign created!"));
                                                        } else {
                                                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                                                    ('&', "&6 plot not created; insert unsuccessful"));
                                                        }
                                                    }else{
                                                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                                                ('&', "&6 plot not created; updating rent price was unsuccessful"));
                                                    }
                                                }
                                            } else {
                                                ClickSign.getClickedBlock().breakNaturally();
                                                player.sendMessage(ChatColor.translateAlternateColorCodes
                                                        ('&', "&6 Last line must be numeric"));
                                            }
                                        }else if (ShopSign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', "&d[rent]"))){
                                            ShopSign.setLine(3, transact.isInClaim(ShopSign.getLocation().getWorld().getName(), ShopSign.getLocation().getX(),
                                                    ShopSign.getLocation().getZ(), conn, "rentprice"));
                                            ShopSign.update();
                                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                                    ('&', "&6Rentprice updated"));
                                        }else if (ShopSign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', "&6[rented]"))){
                                            ShopSign.setLine(3, transact.isInClaim(ShopSign.getLocation().getWorld().getName(), ShopSign.getLocation().getX(),
                                                    ShopSign.getLocation().getZ(), conn, "rentprice"));
                                            ShopSign.update();
                                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                                    ('&', "&6Rentprice updated"));
                                        }
                                    }else {
                                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                                ('&', "&8You have not rented this claim - confer with claim owner"));
                                    }
                                }
                            }else {
                                if (!transact.checkIfCreationInProgress(player, conn)) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes
                                            ('&', "&8No item stored in location..."));
                                } else {
                                    if (!transact.getOwnerFromSign(ClickSign.getClickedBlock().getLocation().getWorld().getName(),
                                            ClickSign.getClickedBlock().getX(), ClickSign.getClickedBlock().getY(),
                                            ClickSign.getClickedBlock().getZ(), conn).isEmpty()) {
                                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                                ('&', "&8This sign is already linked to a chest!"));
                                    } else {
                                        rs = transact.getCreationParameters(player, conn);
                                        try {
                                            if (rs.next()) {
                                                if (!transact.getTradeSetup(ShopSign.getWorld().getName(), ShopSign.getX(), ShopSign.getY(), ShopSign.getZ(), conn).next()) {
                                                    if (transact.registerNewTradeSetup(ClickSign.getClickedBlock().getWorld().getName(), ClickSign.getClickedBlock().getLocation().getX(),
                                                            ClickSign.getClickedBlock().getLocation().getY(), ClickSign.getClickedBlock().getLocation().getZ(),
                                                            player.getUniqueId().toString(), rs.getString("Chest_World"), rs.getDouble("Chest_X"), rs.getDouble("Chest_Y"),
                                                            rs.getDouble("Chest_Z"), rs.getString("InvIndices"), rs.getString("StackCnt"), conn)) {
                                                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                                                ('&', "&6 Trade created!"));
                                                        transact.closeTradeCreationProcess(player.getUniqueId().toString(), conn);
                                                    } else {
                                                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                                                ('&', "&8 Trade creation not successful"));
                                                        ClickSign.setCancelled(true);
                                                    }
                                                } else {
                                                    player.sendMessage(ChatColor.translateAlternateColorCodes
                                                            ('&', "&8 This sign was already configured"));
                                                    ClickSign.setCancelled(true);
                                                }
                                            } else {
                                                player.sendMessage(ChatColor.translateAlternateColorCodes
                                                        ('&', "&8 No creation parameters found for some reason"));
                                                ClickSign.setCancelled(true);
                                            }
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                        switch (ShopSign.getLine(0)) {
                                            case "[buy]":
                                                ShopSign.setLine(0, ChatColor.translateAlternateColorCodes
                                                        ('&', "&b[buy]"));
                                                ShopSign.update();
                                                if (ShopSign.getLine(3).chars().allMatch(Character::isDigit)) {
                                                    player.sendMessage(ChatColor.translateAlternateColorCodes
                                                            ('&', "&6 Trade sign created!"));
                                                    transact.closeTradeCreationProcess(player.getUniqueId().toString(), conn);
                                                } else {
                                                    ClickSign.getClickedBlock().breakNaturally();
                                                    player.sendMessage(ChatColor.translateAlternateColorCodes
                                                            ('&', "&6 Last line must be numeric"));
                                                }
                                                break;
                                            case "[sell]":
                                                ShopSign.setLine(0, ChatColor.translateAlternateColorCodes
                                                        ('&', "&a[sell]"));
                                                ShopSign.update();
                                                if (ShopSign.getLine(3).chars().allMatch(Character::isDigit)) {
                                                    player.sendMessage(ChatColor.translateAlternateColorCodes
                                                            ('&', "&6 Trade sign created!"));
                                                } else {
                                                    ClickSign.getClickedBlock().breakNaturally();
                                                    player.sendMessage(ChatColor.translateAlternateColorCodes
                                                            ('&', "&6 Last line must be numeric"));
                                                }
                                                break;
                                            default:
                                                ClickSign.getClickedBlock().breakNaturally();
                                        }
                                        if (ShopSign.getLine(3) == "[bal]") {
                                            ClickSign.getClickedBlock().breakNaturally();
                                        }
                                    }
                                }
                            }
                        } else {
                            if (ShopSign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', "&b[buy]"))) {
                                try {
                                    if (transact.getBalance(player.getUniqueId().toString(), conn) > Integer.parseInt(ShopSign.getLine(3))){
                                        rs = transact.getTradeSetup(ClickSign.getClickedBlock().getWorld().getName(), ClickSign.getClickedBlock().getX(),
                                                ClickSign.getClickedBlock().getY(), ClickSign.getClickedBlock().getZ(), conn);
                                        String owner = rs.getString("OwnerUUID");
                                        List<String> AmountList = Arrays.asList(rs.getString("StackCnt").split(","));
                                        List<String> IndexList = Arrays.asList(rs.getString("InvIndices").split(","));
                                        donatingChest = (Chest) new Location(getWorldFromString(rs.getString("Chest_World")), rs.getInt("Chest_X"),
                                                rs.getInt("Chest_Y"), rs.getInt("Chest_Z")).getBlock().getState();
                                        ItemStack tempItem = new ItemStack(Material.BEDROCK, 1);
                                        boolean Status = true;
                                        for (int i = 0; i < IndexList.size(); i++) {
                                            if (player.getInventory().firstEmpty() > -1) {
                                                player.getInventory().setItem(player.getInventory().firstEmpty(), tempItem);
                                            } else {
                                                Status = false;
                                                player.sendMessage(ChatColor.translateAlternateColorCodes
                                                        ('&', "&6 Not enough space in inventory"));
                                                ClickSign.setCancelled(true);
                                            }
                                        }
                                        if (Status) {
                                            player.getInventory().remove(tempItem);
                                            for (int i = 0; i < IndexList.size(); i++) {
                                                if (donatingChest.getInventory().first(Objects.requireNonNull(Material.getMaterial(IndexList.get(i)))) > -1) {
                                                    int tempSlot = donatingChest.getInventory().first(new ItemStack(Objects.requireNonNull(Material.getMaterial(IndexList.get(i))),
                                                            Integer.parseInt(AmountList.get(i))));
                                                    donatingStack = donatingChest.getInventory().getItem(tempSlot);
                                                    player.getInventory().setItem(player.getInventory().firstEmpty(), donatingStack);
                                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5acquired " + donatingStack.getAmount() +
                                                            "x " + donatingStack.getType() + " for: " + ShopSign.getLine(3)));
                                                    donatingChest.getInventory().removeItem(donatingStack);
                                                }else{
                                                    ShopSign.setLine(0, ChatColor.translateAlternateColorCodes
                                                            ('&', "&4[buy]"));
                                                    ShopSign.update();
                                                    player.sendMessage(ChatColor.translateAlternateColorCodes
                                                            ('&', "&6 Item no longer available"));
                                                    transact.simpleResetTradeSetup(rs.getString("Sign_World"), rs.getDouble("Sign_X"),
                                                            rs.getDouble("Sign_Y"), rs.getDouble("Sign_Z"), rs.getString("OwnerUUID"), conn);
                                                    ClickSign.setCancelled(true);
                                                }
                                            }
                                            for (int i = 0; i < IndexList.size(); i++) {
                                                int tempSlot = donatingChest.getInventory().first(Objects.requireNonNull(Material.getMaterial(IndexList.get(i))));
                                                ItemStack tempStack = donatingChest.getInventory().getItem(tempSlot);
                                                if (!donatingChest.getInventory().contains(tempStack)) {
                                                    ShopSign.setLine(0, ChatColor.translateAlternateColorCodes
                                                            ('&', "&4[buy]"));
                                                    ShopSign.update();
                                                    transact.simpleResetTradeSetup(rs.getString("Sign_World"), rs.getDouble("Sign_X"),
                                                            rs.getDouble("Sign_Y"), rs.getDouble("Sign_Z"), rs.getString("OwnerUUID"), conn);
                                                    ClickSign.setCancelled(true);
                                                }
                                            }
                                            transact.updateBalance(player.getUniqueId().toString(), Integer.parseInt(ShopSign.getLine(3)), "minus",  conn);
                                            transact.updateBalance(owner, Integer.parseInt(ShopSign.getLine(3)), "plus", conn);
                                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                                    ('&', "&6 Transaction complete! You now have a total balance of: " + transact.getBalance(player.getUniqueId().toString(), conn)));
                                            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                                            String message = player.getUniqueId().toString()+";"+owner+";buy;"+ShopSign.getLine(3)+";"+timeStamp+"  |:| -->"
                                                    +player.getName()+"("+player.getUniqueId().toString()+") has bought from "+"("+owner+") for "
                                                    +ShopSign.getLine(3)+" at: "+timeStamp;
                                            consolidation.writeTradeFile(message);
                                        }
                                    }else{
                                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                                ('&', "&6 You do not have enough money to buy these items"));

                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }else if (ShopSign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', "&5[bal]"))){
                                player.sendMessage(ChatColor.translateAlternateColorCodes
                                        ('&', "&6 balance:" + transact.getBalance(player.getUniqueId().toString(), conn)));
                            }else if (ShopSign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', "&a[sell]"))){
                                try {
                                    rs = transact.getTradeSetup(ClickSign.getClickedBlock().getWorld().getName(), ClickSign.getClickedBlock().getX(),
                                            ClickSign.getClickedBlock().getY(), ClickSign.getClickedBlock().getZ(), conn);
                                    String owner = rs.getString("OwnerUUID");
                                    List<String> AmountList = Arrays.asList(rs.getString("StackCnt").split(","));
                                    List<String> IndexList = Arrays.asList(rs.getString("InvIndices").split(","));
                                    donatingChest = (Chest) new Location(getWorldFromString(rs.getString("Chest_World")), rs.getInt("Chest_X"),
                                            rs.getInt("Chest_Y"), rs.getInt("Chest_Z")).getBlock().getState();
                                    boolean Status = true;
                                    for (int i = 0; i < IndexList.size(); i++) {
                                        if (player.getInventory().containsAtLeast(new ItemStack(Objects.requireNonNull(Material.getMaterial(IndexList.get(i))),
                                                Integer.parseInt(AmountList.get(i))), Integer.parseInt(AmountList.get(i)))){
                                            continue;
                                        }else{
                                            Status = false;
                                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                                    ('&', "&6Player does not have specified items to sell"));

                                        }
                                    }if (transact.getBalance(owner, conn) > Integer.parseInt(ShopSign.getLine(3))){
                                        Status = false;
                                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                                ('&', "&6Owner does not have enough money to buy this item from you"));
                                    }
                                    if (Status){
                                        for (int i = 0; i < IndexList.size(); i++){
                                            ItemStack tempItem = new ItemStack(Material.getMaterial(IndexList.get(i)), Integer.parseInt(AmountList.get(i)));
                                            player.getInventory().removeItem(tempItem);
                                            donatingChest.getInventory().addItem(tempItem);
                                        }
                                        transact.updateBalance(player.getUniqueId().toString(), Integer.parseInt(ShopSign.getLine(3)), "plus",  conn);
                                        transact.updateBalance(owner, Integer.parseInt(ShopSign.getLine(3)), "minus", conn);
                                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                                ('&', "&6 Transaction complete! You now have a total balance of: " + transact.getBalance(player.getUniqueId().toString(), conn)));
                                        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                                        String message = player.getUniqueId().toString()+";"+owner+";sell;"+ShopSign.getLine(3)+";"+timeStamp+"  |:| -->"
                                                +player.getName()+"("+player.getUniqueId().toString()+") has sold to "+owner+" for "
                                                +ShopSign.getLine(3)+" at: "+timeStamp;
                                        consolidation.writeTradeFile(message);
                                    }
                                }catch(SQLException e){
                                    e.printStackTrace();
                                }
                            }else if (ShopSign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', "&d[rent]"))){
                                String OwnerUuid = transact.isInClaim(ShopSign.getLocation().getWorld().getName(), ShopSign.getLocation().getX(), ShopSign.getLocation().getZ(), conn, "uuid");
                                if (OwnerUuid.equalsIgnoreCase(player.getUniqueId().toString())) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes
                                            ('&', "&6This claim is not legible for rent - you are the owner"));
                                } else {
                                    if (checkRentersLicences(OwnerUuid)) {
                                        if (transact.isInClaim(ShopSign.getLocation().getWorld().getName(), ShopSign.getLocation().getX(), ShopSign.getLocation().getZ(),
                                                conn, "renteduuid") == null || transact.isInClaim(ShopSign.getLocation().getWorld().getName(), ShopSign.getLocation().getX(),
                                                ShopSign.getLocation().getZ(), conn, "renteduuid").isEmpty()) {

                                            int claimId = Integer.parseInt(transact.isInClaim(ShopSign.getLocation().getWorld().getName(),
                                                    ShopSign.getLocation().getX(), ShopSign.getLocation().getZ(), conn, "ClaimId"));
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
                    }
                }
            }
        }
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
        WorldCreator creator = new WorldCreator("world");
        World w = creator.createWorld();
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
