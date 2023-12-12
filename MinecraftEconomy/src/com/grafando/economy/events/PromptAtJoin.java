package com.grafando.economy.events;

import com.grafando.economy.data.Transactions;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class PromptAtJoin implements Listener{


    private Player playerJoined;
    private int InflationLevel;
    private Transactions transact = new Transactions();
    private double initBal;
    LocalDate Now = LocalDate.now();
    private LocalDate HalvingDate;
    Connection conn;
    private int BillChainingFlag;

    public PromptAtJoin(Connection connection){
        this.conn = connection;
        this.BillChainingFlag = 0;

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent PlayerJoin){
        playerJoined = PlayerJoin.getPlayer();
        promptPlayerOnBidChange(playerJoined);

    }

    public void promptPlayerOnBidChange(Player joinedPlayer){
        ArrayList<Integer> ListOfBills = new ArrayList<>();
        this.BillChainingFlag = 0;
        while(BillChainingFlag != 1){
            int BillId = (transact.selectBillIdFromUuid(joinedPlayer.getUniqueId().toString(), conn));
            if (BillId != 0){
                ListOfBills.add(BillId);
            }else{
                this.BillChainingFlag = 1;
            }
        }
        if (ListOfBills.isEmpty()){
            joinedPlayer.sendMessage(ChatColor.translateAlternateColorCodes
                    ('&', "&f&lCurrently no Bill to be displayed &r&f:"));
        }else{
            joinedPlayer.sendMessage(ChatColor.translateAlternateColorCodes
                    ('&', "&f&lCurrently you have "+ListOfBills.size()+" Bills in " +
                            "your inbox. Make sure you read and clear these as soon as possible, Thank you!"));
            joinedPlayer.sendMessage(ChatColor.translateAlternateColorCodes
                    ('&', "&f&lUse /listallbills to see all bills in the inbox, " +
                            "/readbill x to read any specific bill and /clearbill x to delete any specific bill and " +
                            "/clearallbills to delete the entire list."));
        }
    }
}



