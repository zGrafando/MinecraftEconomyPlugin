package com.grafando.economy.commands;

import com.grafando.economy.data.Transactions;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Iterator;

public class ExtendRent implements CommandExecutor {

    private Player player;
    private Transactions transact = new Transactions();
    private Connection conn;

    public ExtendRent(Connection connection){
        this.conn = connection;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        player = (Player) sender;
        if (sender instanceof Player){
            if (cmd.getName().equalsIgnoreCase("extendrent")){
                if (!transact.isInClaim(player.getLocation().getWorld().getName(), player.getLocation().getX(), player.getLocation().getZ(), conn, "renteduuid").isEmpty()){
                    if (transact.isInClaim(player.getLocation().getWorld().getName(), player.getLocation().getX(),
                            player.getLocation().getZ(), conn, "renteduuid").equalsIgnoreCase(player.getUniqueId().toString())) {
                        int ClaimId = Integer.parseInt(transact.isInClaim(player.getLocation().getWorld().getName(), player.getLocation().getX(),
                                player.getLocation().getZ(), conn, "ClaimId"));
                        if (transact.selectFromClaimById(ClaimId, conn, "CurrentHighestBidder").isEmpty()) {
                            if (transact.selectFromClaimById(ClaimId, conn, "RenterUuidToBe").isEmpty()) {
                                String timeGotten = transact.isInClaim(player.getLocation().getWorld().getName(), player.getLocation().getX(), player.getLocation().getZ(),
                                        conn, "termination");
                                String[] dateValueList = timeGotten.split("/");
                                LocalDateTime in7days = LocalDateTime.now();
                                in7days.withYear(Integer.parseInt(dateValueList[0]));
                                in7days.withMonth(Integer.parseInt(dateValueList[1]));
                                in7days.withDayOfMonth(Integer.parseInt(dateValueList[2]));
                                in7days.plusDays(7);
                                in7days.withHour(Integer.parseInt(dateValueList[3]));
                                in7days.withMinute(Integer.parseInt(dateValueList[4]));
                                int rentPrice = Integer.parseInt(transact.isInClaim(player.getLocation().getWorld().getName(), player.getLocation().getX(),
                                        player.getLocation().getZ(), conn, "rentprice"));
                                if (transact.getBalance(player.getUniqueId().toString(), conn) > rentPrice) {
                                    String Date = in7days.getYear() + "/" + in7days.getMonthValue() + "/" + in7days.getDayOfMonth() + "/" + in7days.getHour() + "/" + in7days.getMinute();
                                    String signGotten = transact.isInClaim(player.getLocation().getWorld().getName(),
                                            player.getLocation().getX(), player.getLocation().getZ(), conn, "rentsign");
                                    String[] rentSignValues = signGotten.split("/");
                                    Sign ShopSign = (Sign) player.getWorld().getBlockAt(new Location(getWorldFromString(rentSignValues[0]),
                                            Integer.parseInt(rentSignValues[1]), Integer.parseInt(rentSignValues[2]), Integer.parseInt(rentSignValues[3]))).getState();
                                    if (transact.updateRenter(Integer.parseInt(transact.isInClaim(player.getLocation().getWorld().getName(), player.getLocation().getX(),
                                            player.getLocation().getZ(), conn, "ClaimId")), player.getUniqueId().toString(), Date, rentPrice,
                                            ShopSign.getLocation().getWorld().getName(), ShopSign.getLocation().getX(), ShopSign.getLocation().getY(),
                                            ShopSign.getLocation().getZ(), ShopSign.getLine(1), ShopSign.getLine(2), conn)) {
                                        transact.updateRenterToBe(player.getUniqueId().toString(), ClaimId, conn);
                                        ShopSign.setLine(0, ChatColor.translateAlternateColorCodes
                                                ('&', "&6[rented]"));
                                        ShopSign.setLine(1, ChatColor.translateAlternateColorCodes
                                                ('&', "&fby: &b" + player.getName()));
                                        ShopSign.setLine(2, ChatColor.translateAlternateColorCodes
                                                ('&', "&funtil: &c" + Date));
                                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                                ('&', "&6you have now rented this shop for the next seven days!"));
                                    } else {
                                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                                ('&', "&6renting this shop was unsuccessful"));
                                    }
                                } else {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes
                                            ('&', "&6You do not posses the money necessary to rent this shop"));
                                }
                            }else{
                                player.sendMessage(ChatColor.translateAlternateColorCodes
                                        ('&', "&6You have already requested the rent extension. Rent becomes extendable again once next rent period starts"));
                            }
                        }else{
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&6This rent cannot be extended"));
                        }
                    }else{
                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                ('&', "&6You are not the current renter of this shop"));
                    }
                }else{
                    player.sendMessage(ChatColor.translateAlternateColorCodes
                            ('&', "&6Nothing to extend - this shop is not rented. Consult & right-click the sign within the claim "));
                }
            }
        }
        return true;
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
