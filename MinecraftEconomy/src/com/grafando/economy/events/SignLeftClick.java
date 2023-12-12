package com.grafando.economy.events;

import com.grafando.economy.data.Transactions;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class SignLeftClick implements Listener {

    private Transactions transact = new Transactions();
    private Sign ShopSign;
    private Player player;
    private Chest donatingChest;
    private ResultSet rs;
    private Connection conn;

    public SignLeftClick(Connection connection) {
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
                        ClickSign.getClickedBlock().getType().equals(Material.WARPED_WALL_SIGN)) {
                    ShopSign = (Sign) ClickSign.getClickedBlock().getState();
                    if (ClickSign.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                        if (player.getInventory().getItemInMainHand().getType().equals(Material.REDSTONE)) {
                            if (transact.isInClaim(ClickSign.getClickedBlock().getLocation().getWorld().getName(), ClickSign.getClickedBlock().getLocation().getX(),
                                    ClickSign.getClickedBlock().getLocation().getZ(), conn, "renteduuid") == null ||  !transact.isInClaim(ClickSign.getClickedBlock().getLocation().getWorld().getName(),
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
                                                player.sendMessage(ChatColor.translateAlternateColorCodes
                                                        ('&', "&6 rent sign created!"));
                                            } else {
                                                ClickSign.getClickedBlock().breakNaturally();
                                                player.sendMessage(ChatColor.translateAlternateColorCodes
                                                        ('&', "&6 Last line must be numeric"));
                                            }
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
                                                }
                                            } else {
                                                player.sendMessage(ChatColor.translateAlternateColorCodes
                                                        ('&', "&8 No creation parameters found for some reason"));
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
                            if (ShopSign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', "&b[buy]")) ||
                                    ShopSign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', "&a[sell]"))) {
                                try {
                                    rs = transact.getTradeSetup(ClickSign.getClickedBlock().getWorld().getName(), ClickSign.getClickedBlock().getX(),
                                            ClickSign.getClickedBlock().getY(), ClickSign.getClickedBlock().getZ(), conn);
                                    List<String> AmountList = Arrays.asList(rs.getString("StackCnt").split(","));
                                    List<String> IndexList = Arrays.asList(rs.getString("InvIndices").split(","));
                                    donatingChest = (Chest) new Location(getWorldFromString(rs.getString("Chest_World")), rs.getInt("Chest_X"),
                                            rs.getInt("Chest_Y"), rs.getInt("Chest_Z")).getBlock().getState();
                                    boolean Status = true;
                                    for (int i = 0; i < IndexList.size(); i++) {
                                        if (ShopSign.getLine(0).equalsIgnoreCase("[sell]")) {
                                            int nr = player.getInventory().first(new ItemStack(Objects.requireNonNull(Material.getMaterial(IndexList.get(i))), Integer.parseInt(AmountList.get(i))));
                                            if (nr == -1) {
                                                Status = false;
                                            }
                                        }else if (ShopSign.getLine(0).equalsIgnoreCase("[buy]")){
                                            int nr = donatingChest.getInventory().first(new ItemStack(Objects.requireNonNull(Material.getMaterial(IndexList.get(i))), Integer.parseInt(AmountList.get(i))));
                                            if (nr == -1) {
                                                Status = false;
                                            }
                                        }
                                    }
                                    if (Status) {
                                        for (int i = 0; i < IndexList.size(); i++) {
                                            ItemStack donatingStack = new ItemStack(Objects.requireNonNull(Material.getMaterial(IndexList.get(i))), Integer.parseInt(AmountList.get(i)));
                                            if (ShopSign.getLine(0).equalsIgnoreCase("[buy]")) {
                                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6buy this " +
                                                        donatingStack.getType() + " for " + ShopSign.getLine(3) + "?"));
                                            } else if (ShopSign.getLine(0).equalsIgnoreCase("[sell]")){
                                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6sell your " +
                                                        donatingStack.getType() + " for " + ShopSign.getLine(3) + "?"));
                                            }
                                            if (donatingStack.getItemMeta() instanceof Damageable){
                                                Damageable dmgItem = (Damageable) donatingStack.getItemMeta();
                                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Item Durability: " +
                                                        (donatingStack.getType().getMaxDurability() - (int)(dmgItem.getHealth()))));
                                            }
                                            if (donatingStack.getItemMeta().hasEnchants()) {
                                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6" + donatingStack.getItemMeta().getEnchants()));
                                            }
                                        }
                                    } else {
                                        if (ShopSign.getLine(0).equalsIgnoreCase("[buy]")) {
                                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                                    ('&', "&6Items for sale no longer available"));
                                            ShopSign.setLine(0, ChatColor.translateAlternateColorCodes
                                                    ('&', "&4[buy]"));
                                            ShopSign.update();
                                        }else if (ShopSign.getLine(0).equalsIgnoreCase("[sell]")){
                                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                                    ('&', "&6You do not have the specified items to sell"));
                                        }
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }else if (ShopSign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', "&5[bal]"))){
                                player.sendMessage(ChatColor.translateAlternateColorCodes
                                        ('&', "&6 balance:" + transact.getBalance(player.getUniqueId().toString(), conn)));
                            }
                        }
                    }
                }
            }
        }
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