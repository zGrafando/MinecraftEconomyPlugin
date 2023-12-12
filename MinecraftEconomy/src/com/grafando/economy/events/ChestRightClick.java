package com.grafando.economy.events;

import com.grafando.economy.Eco;
import com.grafando.economy.data.Transactions;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.util.*;


public class ChestRightClick implements Listener {

    private Transactions transact = new Transactions();
    private Player player;
    private Chest targetChest;
    private Connection conn;
    private Eco eco;
    private HashMap<Material, Integer> InvIndices = new HashMap<>();
    private List<ItemStack> Items = new ArrayList<>();

    public ChestRightClick(Connection connection, Eco economy){
        this.conn = connection;
        this.eco = economy;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent ClickChest){
        if (ClickChest != null && ClickChest.getClickedBlock() != null) {
            if (ClickChest.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                player = ClickChest.getPlayer();
                if (player.getInventory().getItemInMainHand().getType().equals(Material.REDSTONE)) {
                    if (!transact.isInClaim(ClickChest.getClickedBlock().getLocation().getWorld().getName(),
                            ClickChest.getClickedBlock().getLocation().getX(), ClickChest.getClickedBlock().getLocation().getZ(),
                            conn, "uuid").equalsIgnoreCase(player.getUniqueId().toString())) {
                        if (transact.isInClaim(ClickChest.getClickedBlock().getLocation().getWorld().getName(),
                                ClickChest.getClickedBlock().getLocation().getX(), ClickChest.getClickedBlock().getLocation().getZ(),
                                conn, "uuid").isEmpty()) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&8You cannot create a trade within areas that do not belong to you"));
                        } else {
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&8You have not rented this claim - confer with claim owner"));
                        }
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                ('&', "&2Chest was registered"));
                        if (ClickChest.getClickedBlock().getType().equals(Material.CHEST) ||
                                ClickChest.getClickedBlock().getType().equals(Material.TRAPPED_CHEST)) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&3Chest was accepted " + ClickChest.getClickedBlock().getType()));
                            targetChest = (Chest) ClickChest.getClickedBlock().getState();
                            if (ClickChest.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                                if (transact.checkIfCreationInProgress(player, conn)) {
                                    transact.closeTradeCreationProcess(player.getUniqueId().toString(), conn);
                                }
                                InvIndices.clear();
                                Items.clear();
                                if (targetChest.getBlockInventory().isEmpty()) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes
                                            ('&', "&8No items registered from chest..."));
                                } else {
                                    Items = Arrays.asList(targetChest.getBlockInventory().getContents());
                                    for (int i = 0; i < Items.size(); i++) {
                                        if (Items.get(i) != null && Items.get(i).getType() != null && Items.get(i).getAmount() > 0) {
                                            InvIndices.put(Items.get(i).getType(), Items.get(i).getAmount());
                                        }
                                    }
                                    transact.openTradeCreationProcess(player.getUniqueId().toString(), ClickChest.getClickedBlock().getWorld().getName(),
                                            targetChest.getLocation().getX(), targetChest.getLocation().getY(),
                                            targetChest.getLocation().getZ(), InvIndices, conn);
                                    eco.resetTradeDataCreationOnDelay(player.getUniqueId().toString());
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

