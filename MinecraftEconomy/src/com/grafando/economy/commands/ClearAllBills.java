package com.grafando.economy.commands;

import com.grafando.economy.data.Transactions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClearAllBills implements CommandExecutor {

    private Player player;
    private Transactions transact = new Transactions();
    private Connection conn;
    private HashMap<String, Integer> Bids = new HashMap<>();
    private int BillChainingFlag;

    public ClearAllBills(Connection connection){
        this.conn = connection;
        this.BillChainingFlag = 0;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        player = (Player) sender;
        if (sender instanceof Player){
            if (cmd.getName().equalsIgnoreCase("clearallbills")) {
                ArrayList<Integer> ListOfBills = new ArrayList<>();
                this.BillChainingFlag = 0;
                while(BillChainingFlag != 1){
                    int BillId = (transact.selectBillIdFromUuid(player.getUniqueId().toString(), conn));
                    if (BillId != 0){
                        ListOfBills.add(BillId);
                    }else{
                        this.BillChainingFlag = 1;
                    }
                }
                if (ListOfBills.isEmpty()){
                    player.sendMessage(ChatColor.translateAlternateColorCodes
                            ('&', "&f&lCurrently no Bill to remove."));
                }else{
                    boolean Status = false;
                    for (int i = 0; i < ListOfBills.size(); i++){
                        if (transact.removeBill(ListOfBills.get(i), conn)){
                            Status = true;
                        }
                    }
                    if (Status){
                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                ('&', "&6 Bills cleared!"));
                    }else{
                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                ('&', "&6 Clearing the bills was unsuccessful"));
                    }
                }
            }
        }
        return true;
    }
}
