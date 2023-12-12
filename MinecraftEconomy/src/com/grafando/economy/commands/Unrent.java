package com.grafando.economy.commands;

import com.grafando.economy.data.Transactions;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;

public class Unrent implements CommandExecutor{

    private Player player;
    private Transactions transact = new Transactions();
    private Connection conn;

    public Unrent(Connection connection){
        this.conn = connection;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        player = (Player) sender;
        if (sender instanceof Player){
            if (cmd.getName().equalsIgnoreCase("unrent")){
                if (transact.isInClaim(player.getLocation().getWorld().getName(), player.getLocation().getX(), player.getLocation().getZ(),
                        conn, "renteduuid") != null && !transact.isInClaim(player.getLocation().getWorld().getName(), player.getLocation().getX(), player.getLocation().getZ(), conn, "renteduuid").isEmpty()){
                    if (transact.isInClaim(player.getLocation().getWorld().getName(), player.getLocation().getX(),
                            player.getLocation().getZ(), conn, "renteduuid").equalsIgnoreCase(player.getUniqueId().toString())) {
                        int ClaimId = Integer.parseInt(transact.isInClaim(player.getLocation().getWorld().getName(), player.getLocation().getX(), player.getLocation().getZ(), conn, "ClaimId"));
                        Sign ShopSign = (Sign) player.getWorld().getBlockAt(new Location(getWorldFromString(transact.selectFromClaimById(ClaimId, conn, "Rentsignworld")),
                                Double.parseDouble(transact.selectFromClaimById(ClaimId, conn, "Rentsignx")), Double.parseDouble(transact.selectFromClaimById(ClaimId,
                                conn, "Rentsigny")), Double.parseDouble(transact.selectFromClaimById(ClaimId, conn, "Rentsignz")))).getState();
                        ShopSign.setLine(0, ChatColor.translateAlternateColorCodes
                                ('&', "&5[rent]"));
                        ShopSign.setLine(1, ChatColor.translateAlternateColorCodes
                                ('&', transact.selectFromClaimById(ClaimId, conn, "signvalue1")));
                        ShopSign.setLine(2, ChatColor.translateAlternateColorCodes
                                ('&', transact.selectFromClaimById(ClaimId, conn, "signvalue2")));
                        ShopSign.setLine(3, ChatColor.translateAlternateColorCodes
                                ('&', transact.selectFromClaimById(ClaimId, conn, "rentprice")));
                        ShopSign.update();
                        if (transact.resetRenter(ClaimId, conn)){
                            if (transact.resetRenterOnPlot(transact.getPlotId(ClaimId, conn), conn)) {
                                player.sendMessage(ChatColor.translateAlternateColorCodes
                                        ('&', "&6Unrenting was successful"));

                                String dateToNextBilling = transact.getSqlSelectFromWorld(conn, "DateToNextBilling");
                                String[] StrArray = dateToNextBilling.split("/");
                                int dayOfNextBilling = Integer.parseInt(StrArray[2]);
                                int monthOfNextBilling = Integer.parseInt(StrArray[1]);
                                int yearOfNextBilling = Integer.parseInt(StrArray[0]);
                                LocalDate currentDate = now();
                                LocalDate nextBillingDate = now().withYear(yearOfNextBilling).withMonth(monthOfNextBilling).withDayOfMonth(dayOfNextBilling);
                                long daysBetween = DAYS.between(currentDate, nextBillingDate);
                                int rentPrice = Integer.parseInt(transact.selectFromClaimById(ClaimId, conn, "rentprice"));
                                daysBetween = 14 - daysBetween;
                                int rentPriceLeftover = Integer.valueOf((int) ((rentPrice / 14) * daysBetween));

                                transact.setClaimsRenterUuidToNone(ClaimId, conn);
                                if (transact.getBalance(player.getUniqueId().toString(), conn) < rentPriceLeftover){
                                    transact.updateBalance(player.getUniqueId().toString(), transact.getBalance(player.getUniqueId().toString(), conn), "minus", conn);
                                    transact.updateBalance(transact.getSqlSelectFromRenterLicence(transact.selectFromCrossRentingByPlotId(ClaimId, conn, "ForeignRenterLicence"), conn, "RenterLicenceUUID"),
                                            transact.getBalance(player.getUniqueId().toString(), conn), "plus", conn);
                                    transact.updateAllRentIncome(transact.getSqlSelectFromRenterLicence(transact.selectFromCrossRentingByPlotId(ClaimId, conn, "ForeignRenterLicence"), conn, "RenterLicenceUUID"), transact.getBalance(player.getUniqueId().toString(), conn), conn);
                                    transact.updateAllRentExpenditure(player.getUniqueId().toString(), transact.getBalance(player.getUniqueId().toString(), conn), conn);
                                }else{
                                    transact.updateBalance(player.getUniqueId().toString(), rentPriceLeftover , "minus", conn);
                                    transact.updateBalance(transact.getSqlSelectFromRenterLicence(transact.selectFromCrossRentingByPlotId(ClaimId,
                                            conn, "ForeignRenterLicence"), conn, "RenterLicenceUUID"), rentPriceLeftover, "plus", conn);
                                    transact.updateAllRentIncome(transact.getSqlSelectFromRenterLicence(transact.selectFromCrossRentingByPlotId(ClaimId, conn, "ForeignRenterLicence"), conn, "RenterLicenceUUID"), rentPriceLeftover, conn);
                                    transact.updateAllRentExpenditure(player.getUniqueId().toString(), rentPriceLeftover, conn);
                                }

                            }else{
                                player.sendMessage(ChatColor.translateAlternateColorCodes
                                        ('&', "&6Unrenting was not sucessful; could not reset renter on plot record"));
                            }
                        }else{
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&6Unrenting was not sucessful"));
                        }
                    }else{
                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                ('&', "&6You are not the current renter of this shop"));
                    }
                }else{
                    player.sendMessage(ChatColor.translateAlternateColorCodes
                            ('&', "&6Nothing to unrent - this shop is not rented. Consult & right-click the sign within the claim "));
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
