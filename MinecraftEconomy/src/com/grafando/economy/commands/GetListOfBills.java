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

public class GetListOfBills implements CommandExecutor {

    private Player player;
    private Transactions transact = new Transactions();
    private Connection conn;
    private HashMap<String, Integer> Bids = new HashMap<>();
    private int BillChainingFlag;

    public GetListOfBills(Connection connection){
        this.conn = connection;
        this.BillChainingFlag = 0;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        player = (Player) sender;
        if (sender instanceof Player){
            if (cmd.getName().equalsIgnoreCase("listallbills")) {
                ArrayList<Integer> ListOfBills = new ArrayList<>();
                this.BillChainingFlag = 0;
                while (BillChainingFlag != 1) {
                    int BillId = (transact.selectBillIdFromUuid(player.getUniqueId().toString(), conn));
                    if (BillId != 0) {
                        ListOfBills.add(BillId);
                    } else {
                        this.BillChainingFlag = 1;
                    }
                }
                if (ListOfBills.isEmpty()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes
                            ('&', "&f&lCurrently no bill to be displayed &r&f:"));
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes
                            ('&', "&f&lCurrent bills:"));
                    for (int i = 0; i < ListOfBills.size(); i++) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes
                                ('&', "&f&l- &r&f" + ListOfBills.get(i)));
                    }
                }
            }
        }
        return true;
    }
}





