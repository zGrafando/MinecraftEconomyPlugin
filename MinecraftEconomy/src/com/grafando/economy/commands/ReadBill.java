package com.grafando.economy.commands;

import com.grafando.economy.data.Transactions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;

public class ReadBill implements CommandExecutor {

    private final Transactions transact = new Transactions();
    private final Connection conn;

    public ReadBill(Connection connection){
        this.conn = connection;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (sender != null){
            if (cmd.getName().equalsIgnoreCase("readbill")) {
                if (args.length > 0){
                   int BillId = Integer.parseInt(args[0]);
                    if (BillId != 0) {
                        if (transact.getSqlSelectFromBilling(BillId, conn, "BilledUuid").equalsIgnoreCase(player.getUniqueId().toString())) {
                            String periodStart = transact.getSqlSelectFromBilling(BillId, conn, "PeriodStart");
                            String periodEnd = transact.getSqlSelectFromBilling(BillId, conn, "PeriodEnd");
                            double allDirectCollections = Double.parseDouble(transact.getSqlSelectFromBilling(BillId, conn, "AllDirectCollections"));
                            double allDirectPayments = Double.parseDouble(transact.getSqlSelectFromBilling(BillId, conn, "AllDirectPayments"));
                            double allRentIncome = Double.parseDouble(transact.getSqlSelectFromBilling(BillId, conn, "AllRentIncome"));
                            double allRentExpenditure = Double.parseDouble(transact.getSqlSelectFromBilling(BillId, conn, "AllRentExpenditure"));
                            double allShopSales = Double.parseDouble(transact.getSqlSelectFromBilling(BillId, conn, "AllShopSales"));
                            double allShopBuys = Double.parseDouble(transact.getSqlSelectFromBilling(BillId, conn, "AllShopBuys"));
                            double allUtilityExpenditure = Double.parseDouble(transact.getSqlSelectFromBilling(BillId, conn, "AllUtilityExpenditure"));
                            double totalIncome = Double.parseDouble(transact.getSqlSelectFromBilling(BillId, conn, "TotalIncome"));
                            double totalExpenditure = Double.parseDouble(transact.getSqlSelectFromBilling(BillId, conn, "TotalExpenditure"));
                            double leftoverBalance = Double.parseDouble(transact.getSqlSelectFromBilling(BillId, conn, "LeftoverBalance"));
                            double balanceBeforeTax = Double.parseDouble(transact.getSqlSelectFromBilling(BillId, conn, "BalanceBeforeTax"));
                            double balanceAfterTax = Double.parseDouble(transact.getSqlSelectFromBilling(BillId, conn, "BalanceAfterTax"));
                            double taxesPayed = Double.parseDouble(transact.getSqlSelectFromBilling(BillId, conn, "CapitalGains"));
                            double taxPercentage = Double.parseDouble(transact.getSqlSelectFromBilling(BillId, conn, "TaxPercentage"));
                            double maxMoneyAmount = Double.parseDouble(transact.getSqlSelectFromWorld(conn, "MaxMoneyAmount"));
                            double balance = Double.parseDouble(transact.selectFromPlayersByUUID(player.getUniqueId().toString(), conn, "Balance"));

                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&f&r&6Bill &r&fNr " + BillId + ":"));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&f&r&6Period from "+periodStart+" to "+periodEnd));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&9-&7-&9-&7-&9-&7-&9-&7-&9-&7-&9-&7-&9-&7-&9-&7-&9-&7-"));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&f&r&6Tax &r&f:"));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&b&l> With a Balance of "+balanceBeforeTax+" and the server's current total money amount of "+maxMoneyAmount+", a tax " +
                                            "percentage of "+taxPercentage+" was evaluated."));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&b&l> This infers a tax debt of "+taxesPayed+" resulting in a balance of "+balanceAfterTax+"."));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&9-&7-&9-&7-&9-&7-&9-&7-&9-&7-&9-&7-&9-&7-&9-&7-&9-&7-"));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&f&r&6Income &r&f:"));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&b&l> Cash-flow       &r&l&c- &r&b" + allDirectCollections));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&b&l> Sales           &r&l&c- &r&b" + allShopSales));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&b&l> Rent income     &r&l&c- &r&b" + allRentIncome));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&6&l                  -------------"));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&b&l> Income total :  &r&l&c- &r&b" + totalIncome));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&9-&7-&9-&7-&9-&7-&9-&7-&9-&7-&9-&7-&9-&7-&9-&7-&9-&7-"));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&f&r&6Expense &r&f:"));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&b&l> Payments        &r&l&c- &r&b" + allDirectPayments));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&b&l> Expenditure     &r&l&c- &r&b" + allShopBuys));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&b&l> Rent payments   &r&l&c- &r&b" + allRentExpenditure));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&b&l> Bought utility  &r&l&c- &r&b" + allUtilityExpenditure));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&6&l                  -------------"));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&b&l> Expense total : &r&l&c- &r&b" + totalExpenditure));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&9-&7-&9-&7-&9-&7-&9-&7-&9-&7-&9-&7-&9-&7-&9-&7-&9-&7-"));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&f&r&6Delta &r&f:"));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&b&l> Income          &r&b " + totalIncome));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&b&l> Expenditure     &r&b-" + totalExpenditure));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&6&l                  -------------"));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&b&l> Difference :    &r&b " + leftoverBalance));
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&b&l> Current balance : &r&b" + balance));
                        }else{
                            player.sendMessage(ChatColor.translateAlternateColorCodes
                                    ('&', "&f&lBill identifier either does not exist or belong to you"));
                        }
                    }else{
                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                ('&', "&f&lBill identifier can never be 0"));
                    }
                }else{
                    player.sendMessage(ChatColor.translateAlternateColorCodes
                            ('&', "&f&lYou must add a billnumber to specify which bill is to be read"));
                }
            }
        }
        return true;
    }
}
