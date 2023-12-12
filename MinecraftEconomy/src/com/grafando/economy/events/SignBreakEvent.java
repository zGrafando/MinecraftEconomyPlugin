package com.grafando.economy.events;

import com.grafando.economy.data.Transactions;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.sql.Connection;
import java.util.Objects;

public class SignBreakEvent implements Listener{

    private Player player;
    private Transactions transactions = new Transactions();
    private Connection conn;
    private Sign brokenBlock;

    public SignBreakEvent(Connection connection){
        this.conn = connection;
    }

    @EventHandler
    public void onPlayerMine(BlockBreakEvent ClickSign){
        if (ClickSign != null) {
            if (Objects.requireNonNull(ClickSign.getBlock()).getType().equals(Material.SPRUCE_SIGN) ||
                    ClickSign.getBlock().getType().equals(Material.SPRUCE_WALL_SIGN) ||
                    ClickSign.getBlock().getType().equals(Material.OAK_SIGN) ||
                    ClickSign.getBlock().getType().equals(Material.OAK_WALL_SIGN) ||
                    ClickSign.getBlock().getType().equals(Material.BIRCH_SIGN) ||
                    ClickSign.getBlock().getType().equals(Material.BIRCH_WALL_SIGN) ||
                    ClickSign.getBlock().getType().equals(Material.JUNGLE_SIGN) ||
                    ClickSign.getBlock().getType().equals(Material.JUNGLE_WALL_SIGN) ||
                    ClickSign.getBlock().getType().equals(Material.ACACIA_SIGN) ||
                    ClickSign.getBlock().getType().equals(Material.ACACIA_WALL_SIGN) ||
                    ClickSign.getBlock().getType().equals(Material.DARK_OAK_SIGN) ||
                    ClickSign.getBlock().getType().equals(Material.DARK_OAK_WALL_SIGN)) {
                brokenBlock = (Sign) ClickSign.getBlock().getState();
                player = ClickSign.getPlayer();
                if (transactions.isInClaim(Objects.requireNonNull(brokenBlock.getLocation().getWorld()).getName(), brokenBlock.getLocation().getX(),
                        brokenBlock.getLocation().getZ(), conn, "uuid").equalsIgnoreCase(player.getUniqueId().toString())) {
                    if (transactions.simpleResetTradeSetup(ClickSign.getBlock().getWorld().getName(), ClickSign.getBlock().getLocation().getX(),
                            ClickSign.getBlock().getLocation().getY(), ClickSign.getBlock().getLocation().getZ(), player.getUniqueId().toString(), conn)) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                ('&', "&8Trade data successfully reset"));
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                ('&', "&8No trade data found or it is not for you to delete"));
                    }
                } else {
                    boolean cancellationStatus = false;
                    if (!transactions.isInClaim(Objects.requireNonNull(brokenBlock.getLocation().getWorld()).getName(), brokenBlock.getLocation().getX(),
                            brokenBlock.getLocation().getZ(), conn, "uuid").isEmpty()){
                        if (transactions.isInClaim(brokenBlock.getLocation().getWorld().getName(), brokenBlock.getLocation().getX(),
                                brokenBlock.getLocation().getZ(), conn, "renteduuid") != null && transactions.isInClaim(Objects.requireNonNull(brokenBlock.getLocation().getWorld()).getName(), brokenBlock.getLocation().getX(),
                                brokenBlock.getLocation().getZ(), conn, "renteduuid").equalsIgnoreCase(player.getUniqueId().toString())){
                            if (transactions.simpleResetTradeSetup(ClickSign.getBlock().getWorld().getName(), ClickSign.getBlock().getLocation().getX(),
                                    ClickSign.getBlock().getLocation().getY(), ClickSign.getBlock().getLocation().getZ(), player.getUniqueId().toString(), conn)) {
                                player.sendMessage(ChatColor.translateAlternateColorCodes
                                        ('&', "&8Trade data successfully reset"));
                            } else {
                                player.sendMessage(ChatColor.translateAlternateColorCodes
                                        ('&', "&8No trade data found or it is not for you to delete"));
                            }
                        }else{
                            cancellationStatus = true;
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&8You have not rented this claim - confer with claim owner"));
                        }
                    }else{
                        cancellationStatus = true;
                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                ('&', "&8You cannot create a trade within areas that do not belong to you"));
                    }
                    if (cancellationStatus) {
                        ClickSign.setCancelled(true);
                    }
                }
            }
        }
    }
}
