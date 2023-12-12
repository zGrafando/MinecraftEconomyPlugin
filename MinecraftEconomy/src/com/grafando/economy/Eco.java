package com.grafando.economy;

import com.grafando.economy.commands.*;
import com.grafando.economy.data.Consolidation;
import com.grafando.economy.data.Transactions;
import com.grafando.economy.events.*;
import com.mojang.datafixers.TypeRewriteRule;
import com.mysql.fabric.xmlrpc.base.Array;
import jdk.internal.org.objectweb.asm.tree.InnerClassNode;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static java.time.LocalDate.now;

public class Eco extends JavaPlugin {

    private Transactions transaction = new Transactions();
    private Connection conn = transaction.getConnection();
    private Consolidation consolidation = new Consolidation();
    private ArrayList<String> RentingList = new ArrayList<>();
    private ArrayList<Integer> allPlotIds = new ArrayList<>();
    private HashMap<Integer, String> mapBillIdToUuId =new HashMap<>();

    @Override
    public void onEnable() {
        super.onEnable();
        Objects.requireNonNull(this.getCommand("bal")).setExecutor(new CheckBalance(conn));
        Objects.requireNonNull(this.getCommand("pay")).setExecutor(new Pay(conn));
        Objects.requireNonNull(this.getCommand("extendrent")).setExecutor(new ExtendRent(conn));
        Objects.requireNonNull(this.getCommand("unrent")).setExecutor(new Unrent(conn));
        Objects.requireNonNull(this.getCommand("getshopinfo")).setExecutor(new ShopInfo(conn));
        Objects.requireNonNull(this.getCommand("adjustrentprice")).setExecutor(new AdjustRentPrice(conn));
        Objects.requireNonNull(this.getCommand("bidrent")).setExecutor(new BidRent(conn));
        Objects.requireNonNull(this.getCommand("clearbids")).setExecutor(new ClearBids(conn));
        Objects.requireNonNull(this.getCommand("clearallbills")).setExecutor(new ClearAllBills(conn));
        Objects.requireNonNull(this.getCommand("clearbill")).setExecutor(new ClearBill(conn));
        Objects.requireNonNull(this.getCommand("readbill")).setExecutor(new ReadBill(conn));
        Objects.requireNonNull(this.getCommand("listallbills")).setExecutor(new GetListOfBills(conn));
        getServer().getPluginManager().registerEvents(new ChestRightClick(conn, this), this);
        getServer().getPluginManager().registerEvents(new SignLeftClick(conn), this);
        getServer().getPluginManager().registerEvents(new SignRightClick(conn), this);
        getServer().getPluginManager().registerEvents(new SignBreakEvent(conn), this);
        getServer().getPluginManager().registerEvents(new PromptAtJoin(conn), this);
        //consolidation.createDirectory();
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[Economy]: Plugin is enabled!");
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                String dateToNextBilling = transaction.getSqlSelectFromWorld(conn, "DateToNextBilling");
                String[] StrArray = dateToNextBilling.split("/");
                int dayOfNextBilling = Integer.parseInt(StrArray[2]);
                int monthOfNextBilling = Integer.parseInt(StrArray[1]);
                int yearOfNextBilling = Integer.parseInt(StrArray[0]);
                LocalDate currentDate = now();
                if (now().withYear(yearOfNextBilling).withMonth(monthOfNextBilling).withDayOfMonth(dayOfNextBilling).isEqual(currentDate) ||
                        now().withYear(yearOfNextBilling).withMonth(monthOfNextBilling).withDayOfMonth(dayOfNextBilling).isBefore(currentDate)){
                    LocalDate nextBillingDate = currentDate.plusDays(14);
                    LocalDate previousBD = currentDate.minusDays(14);
                    String datePrevious = previousBD.getYear()+"/"+previousBD.getMonthValue()+"/"+previousBD.getDayOfMonth();
                    String date = nextBillingDate.getYear()+"/"+nextBillingDate.getMonthValue()+"/"+nextBillingDate.getDayOfMonth();
                    transaction.updateWorldSetNextBillingDate(date, conn);

                    mapBillIdToUuId.clear();

                    ArrayList<String> AllUuIds = new ArrayList<>();
                    AllUuIds = transaction.getAllPlayerUuIds(AllUuIds, 0, conn);
                    for (int i = 0; i < AllUuIds.size(); i++) {
                        transaction.createBill(AllUuIds.get(i), conn, datePrevious, dateToNextBilling);
                        int BillId = Integer.parseInt(transaction.getSqlSelectSpecificFromBillingByUuid(AllUuIds.get(i), datePrevious,dateToNextBilling, conn, "BillId"));
                        mapBillIdToUuId.put(BillId, AllUuIds.get(i));
                    }
                    calcIncome();

                        /**
                    if (transaction.selectRentProcessingFlag(conn) == 0){
                        transaction.updateRentProcessingFlag(1, conn);

                        ArrayList<String> AllUuIds = new ArrayList<>();
                        AllUuIds = transaction.getAllPlayerUuIds(AllUuIds, 0, conn);
                        for (int i = 0; i < AllUuIds.size(); i++){
                            int BillId;
                            if (transaction.selectBillIdFromUuid(AllUuIds.get(i), conn) == 0 ){
                            //    transaction.createBill(AllUuIds.get(i), conn);
                            }
                            BillId = transaction.selectBillIdFromUuid(AllUuIds.get(i), conn);
                            if (transaction.selectLicenceByUUID(AllUuIds.get(i), conn, "LicenceId") != null && !transaction.selectLicenceByUUID(AllUuIds.get(i),
                                    conn, "LicenceId").isEmpty()){
                                int amountOfPlotsLicenced = Integer.parseInt(transaction.selectLicenceByUUID(AllUuIds.get(i), conn, "AmntOfPlotsLicenced"));
                                int plotFee = Integer.parseInt(transaction.getSqlSelectFromWorld(conn, "LicenceFee"));
                                plotTaxes = 0;
                                plotTaxes = amountOfPlotsLicenced * plotFee;
                                int licenceId = Integer.parseInt(transaction.selectLicenceByUUID(AllUuIds.get(i), conn, "LicenceId"));
                                allPlotIds.clear();
                                allPlotIds = transaction.getAllPlotIdsByLicence(allPlotIds, licenceId, 0, conn, "LicenceId");
                                TotalRentSum = 0;
                                if (!allPlotIds.isEmpty()){
                                    for (int c = 0; c < allPlotIds.size(); c++){
                                        if (transaction.selectFromCrossRentingByPlotId(allPlotIds.get(c), conn, "CurrentRenter") != null &&
                                                !transaction.selectFromCrossRentingByPlotId(allPlotIds.get(c), conn, "CurrentRenter").isEmpty() &&
                                                Integer.parseInt(transaction.selectFromCrossRentingByPlotId(allPlotIds.get(c), conn, "CurrentRenter")) != 0){
                                            transaction.updateBillKeyOnRentBill(BillId, allPlotIds.get(c), conn);
                                            int rentPrice = Integer.parseInt(transaction.selectFromCrossRentingByPlotId(allPlotIds.get(c), conn, "CurrentRentingPrice"));
                                            TotalRentSum = TotalRentSum + rentPrice;
                                            transaction.updateBalance(AllUuIds.get(i), rentPrice, "plus", conn);
                                        }else{
                                            transaction.resetBillKeyOnRenting(allPlotIds.get(c), conn);
                                        }
                                    }
                                }
                                allPlotIds.clear();
                                RentPayment = 0;
                                allPlotIds = transaction.getAllPlotIds(allPlotIds, 0, conn);
                                boolean Status = false;
                                if (!allPlotIds.isEmpty()) {
                                    for (int c = 0; c < allPlotIds.size(); c++) {
                                        int ClaimId = Integer.parseInt(transaction.selectFromCrossRentingByPlotId(allPlotIds.get(c), conn, "ClaimId"));
                                        if (transaction.getSqlSelectFromShopBidById(Integer.parseInt(transaction.selectFromClaimById(ClaimId, conn, "CurrentHighestBidder")),
                                                conn, "BidderUUID").equalsIgnoreCase(AllUuIds.get(i)) || transaction.selectFromClaimById(ClaimId, conn,
                                                "RenterUuidToBe").equalsIgnoreCase(AllUuIds.get(i))) {
                                            int HighestBid = 0;
                                            if (transaction.selectFromClaimById(ClaimId, conn, "CurrentHighestBidder") != null && !transaction.selectFromClaimById(
                                                    ClaimId, conn, "CurrentHighestBidder").isEmpty()) {
                                                HighestBid = Integer.parseInt(transaction.selectFromClaimById(ClaimId, conn, "CurrentHighestBidder"));
                                            }
                                            if (transaction.selectFromClaimById(ClaimId, conn, "RenterUuidToBe") != null && !transaction.selectFromClaimById(
                                                    ClaimId, conn, "RenterUuidToBe").isEmpty()) {
                                                if (HighestBid == 0) {
                                                    if (transaction.selectFromClaimById(ClaimId, conn,"RenterUuidToBe").equalsIgnoreCase(AllUuIds.get(i))) {
                                                        int tempPlayerId = Integer.parseInt(transaction.selectFromPlayersByUUID
                                                                (transaction.selectFromClaimById(ClaimId, conn, "RenterUuidToBe"), conn, "PlayerId"));
                                                        transaction.updateCurrentRenter(tempPlayerId, allPlotIds.get(c), conn);
                                                        transaction.updateBalance(transaction.selectFromClaimById(ClaimId, conn, "RenterUuidToBe"), Integer.parseInt(transaction.selectFromClaimById(
                                                                ClaimId, conn, "rentprice")), "minus", conn);
                                                        Status = true;
                                                        if (AllUuIds.get(i).equalsIgnoreCase(transaction.selectFromClaimById(ClaimId, conn, "RenterUuidToBe"))) {
                                                            RentPayment = RentPayment + Integer.parseInt(transaction.selectFromClaimById(ClaimId, conn, "rentprice"));
                                                        }
                                                    }
                                                } else {
                                                    if (transaction.selectFromClaimById(ClaimId, conn, "RenterUuidToBe").equalsIgnoreCase(transaction.
                                                            getSqlSelectFromShopBidById(HighestBid, conn, "BidderUUID"))) {
                                                        if (transaction.selectFromClaimById(ClaimId, conn, "RenterUuidToBe").equalsIgnoreCase(AllUuIds.get(i))) {
                                                            transaction.updateCurrentRentPrice(Integer.parseInt(transaction.selectFromClaimById(ClaimId, conn, "Rentprice")), ClaimId, conn);
                                                            int tempPlayerId = Integer.parseInt(transaction.selectFromPlayersByUUID
                                                                    (transaction.selectFromClaimById(ClaimId, conn, "RenterUuidToBe"), conn, "PlayerId"));
                                                            transaction.updateCurrentRenter(tempPlayerId, allPlotIds.get(c), conn);
                                                            transaction.updateBalance(transaction.selectFromClaimById(ClaimId, conn, "RenterUuidToBe"), Integer.parseInt(transaction.
                                                                    selectFromClaimById(ClaimId, conn, "Rentprice")), "minus", conn);
                                                            Status = true;
                                                            if (AllUuIds.get(i).equalsIgnoreCase(transaction.selectFromClaimById(ClaimId, conn, "RenterUuidToBe"))) {
                                                                RentPayment = RentPayment + Integer.parseInt(transaction.selectFromClaimById(ClaimId, conn, "rentprice"));
                                                            }
                                                        }
                                                    } else {
                                                        if (transaction.getSqlSelectFromShopBidById(HighestBid, conn, "BidderUUID").equalsIgnoreCase(AllUuIds.get(i))) {
                                                            if (Integer.parseInt(transaction.getSqlSelectFromShopBidById(HighestBid, conn, "Bid")) > Integer.parseInt
                                                                    (transaction.selectFromClaimById(ClaimId, conn, "RentPrice"))) {
                                                                int tempPlayerId = Integer.parseInt(transaction.selectFromPlayersByUUID
                                                                        (transaction.getSqlSelectFromShopBidById(HighestBid, conn, "BidderUUID"), conn, "PlayerId"));
                                                                transaction.updateCurrentRenter(tempPlayerId, allPlotIds.get(c), conn);
                                                                transaction.updateBalance(transaction.getSqlSelectFromShopBidById(HighestBid, conn, "BidderUUID"), Integer.parseInt(transaction.
                                                                        getSqlSelectFromShopBidById(HighestBid, conn, "Bid")), "minus", conn);
                                                                Status = true;
                                                                transaction.updateCurrentRentPrice(Integer.parseInt
                                                                        (transaction.getSqlSelectFromShopBidById(HighestBid, conn, "Bid")), allPlotIds.get(c), conn);
                                                                transaction.updateRentPrice(Integer.parseInt(transaction.getSqlSelectFromShopBidById(HighestBid, conn, "Bid")), ClaimId, conn);
                                                                if (AllUuIds.get(i).equalsIgnoreCase(transaction.getSqlSelectFromShopBidById(HighestBid, conn, "BidderUUID"))) {
                                                                    RentPayment = RentPayment + Integer.parseInt(transaction.getSqlSelectFromShopBidById(HighestBid, conn, "Bid"));
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (transaction.getSqlSelectFromShopBidById(HighestBid, conn, "BidderUUID").equalsIgnoreCase(AllUuIds.get(i))) {
                                                    if (HighestBid != 0) {
                                                        if (Integer.parseInt(transaction.getSqlSelectFromShopBidById(HighestBid, conn, "Bid")) > Integer.parseInt
                                                                (transaction.selectFromClaimById(ClaimId, conn, "RentPrice"))) {
                                                            int tempPlayerId = Integer.parseInt(transaction.selectFromPlayersByUUID
                                                                    (transaction.getSqlSelectFromShopBidById(HighestBid, conn, "BidderUUID"), conn, "PlayerId"));
                                                            transaction.updateCurrentRenter(tempPlayerId, allPlotIds.get(c), conn);
                                                            transaction.updateBalance(transaction.getSqlSelectFromShopBidById(HighestBid, conn, "BidderUUID"), Integer.parseInt(transaction.
                                                                    getSqlSelectFromShopBidById(HighestBid, conn, "Bid")), "minus", conn);
                                                            Status = true;
                                                            transaction.updateCurrentRentPrice(Integer.parseInt
                                                                    (transaction.getSqlSelectFromShopBidById(HighestBid, conn, "Bid")), allPlotIds.get(c), conn);
                                                            transaction.updateRentPrice(Integer.parseInt(transaction.getSqlSelectFromShopBidById(HighestBid, conn, "Bid")), ClaimId, conn);
                                                        } else {
                                                            transaction.updateCurrentRentPrice(0, allPlotIds.get(c), conn);
                                                            transaction.updateCurrentRenter(0, allPlotIds.get(c), conn);
                                                        }
                                                    } else {
                                                        transaction.updateCurrentRentPrice(0, allPlotIds.get(c), conn);
                                                        transaction.updateCurrentRenter(0, allPlotIds.get(c), conn);
                                                    }
                                                }
                                            }
                                            if (Status) {
                                                transaction.resetRenterToBe(ClaimId, conn);
                                                transaction.resetHighestBidder(ClaimId, conn);
                                                Bids = transaction.getAllShopBids(Bids, 0, ClaimId, conn);
                                                for (Map.Entry<String, Integer> entry : Bids.entrySet()) {
                                                    String Name = entry.getKey();
                                                    int Bid = entry.getValue();
                                                    transaction.resetBid(Bid, Name, conn);
                                                }
                                            }
                                        }
                                    }
                                }
                                // all my current rentings
                                allPlotIds.clear();
                                int playerId = Integer.parseInt(transaction.selectFromPlayersByUUID(AllUuIds.get(i), conn, "PlayerId"));
                                allPlotIds = transaction.getAllPlotIdsByLicence(allPlotIds, playerId, 0, conn, "RenterId");
                                if (!allPlotIds.isEmpty()){
                                    for (int c = 0; c < allPlotIds.size(); c++){
                                        int ClaimId = Integer.parseInt(transaction.selectFromCrossRentingByPlotId(allPlotIds.get(c), conn, "ClaimId"));
                                        int HighestBid = 0;
                                        if (transaction.selectFromClaimById(ClaimId, conn, "CurrentHighestBidder") != null && !transaction.selectFromClaimById(
                                                ClaimId, conn, "CurrentHighestBidder").isEmpty()){
                                            HighestBid = Integer.parseInt(transaction.selectFromClaimById(ClaimId, conn, "CurrentHighestBidder"));
                                        }
                                        if (transaction.selectFromClaimById(ClaimId, conn, "RenterUuidToBe") != null && !transaction.selectFromClaimById(
                                                ClaimId, conn, "RenterUuidToBe").isEmpty()){
                                            if (HighestBid == 0){
                                                int tempPlayerId = Integer.parseInt(transaction.selectFromPlayersByUUID
                                                        (transaction.selectFromClaimById(ClaimId, conn, "RenterUuidToBe"), conn, "PlayerId"));
                                                transaction.updateCurrentRenter(tempPlayerId, allPlotIds.get(c), conn);
                                                transaction.updateBalance(transaction.selectFromClaimById(ClaimId, conn, "RenterUuidToBe"), Integer.parseInt(transaction.selectFromClaimById(
                                                        ClaimId, conn, "rentprice")), "minus", conn);
                                                RentPayment = RentPayment + Integer.parseInt(transaction.selectFromClaimById(ClaimId, conn, "rentprice"));
                                            }else {
                                                if (transaction.selectFromClaimById(ClaimId, conn, "RenterUuidToBe").equalsIgnoreCase(transaction.
                                                        getSqlSelectFromShopBidById(HighestBid, conn, "BidderUUID"))) {
                                                    transaction.updateCurrentRentPrice(Integer.parseInt(transaction.selectFromClaimById(ClaimId, conn, "Rentprice")), ClaimId, conn);
                                                    int tempPlayerId = Integer.parseInt(transaction.selectFromPlayersByUUID
                                                            (transaction.selectFromClaimById(ClaimId, conn, "RenterUuidToBe"), conn, "PlayerId"));
                                                    transaction.updateCurrentRenter(tempPlayerId, allPlotIds.get(c), conn);
                                                    transaction.updateBalance(transaction.selectFromClaimById(ClaimId, conn, "RenterUuidToBe"), Integer.parseInt(transaction.
                                                            selectFromClaimById(ClaimId, conn, "Rentprice")), "minus", conn);
                                                    RentPayment = RentPayment + Integer.parseInt(transaction.selectFromClaimById(ClaimId, conn, "rentprice"));
                                                }else{
                                                    if (Integer.parseInt(transaction.getSqlSelectFromShopBidById(HighestBid, conn, "Bid")) > Integer.parseInt
                                                            (transaction.selectFromClaimById(ClaimId, conn, "RentPrice"))) {
                                                        int tempPlayerId = Integer.parseInt(transaction.selectFromPlayersByUUID
                                                                (transaction.getSqlSelectFromShopBidById(HighestBid, conn, "BidderUUID"), conn, "PlayerId"));
                                                        transaction.updateCurrentRenter(tempPlayerId, allPlotIds.get(c), conn);
                                                        transaction.updateBalance(transaction.getSqlSelectFromShopBidById(HighestBid, conn, "BidderUUID"), Integer.parseInt(transaction.
                                                                getSqlSelectFromShopBidById(HighestBid, conn, "Bid")), "minus", conn);
                                                        transaction.updateCurrentRentPrice(Integer.parseInt
                                                                (transaction.getSqlSelectFromShopBidById(HighestBid, conn, "Bid")), allPlotIds.get(c), conn);
                                                        transaction.updateRentPrice(Integer.parseInt(transaction.getSqlSelectFromShopBidById(HighestBid, conn, "Bid")), ClaimId, conn);
                                                    }
                                                }
                                            }
                                        }else{
                                            if (HighestBid != 0) {
                                                if (Integer.parseInt(transaction.getSqlSelectFromShopBidById(HighestBid, conn, "Bid")) > Integer.parseInt
                                                        (transaction.selectFromClaimById(ClaimId, conn, "RentPrice"))) {
                                                    int tempPlayerId = Integer.parseInt(transaction.selectFromPlayersByUUID
                                                            (transaction.getSqlSelectFromShopBidById(HighestBid, conn, "BidderUUID"), conn, "PlayerId"));
                                                    transaction.updateCurrentRenter(tempPlayerId, allPlotIds.get(c), conn);
                                                    transaction.updateBalance(transaction.getSqlSelectFromShopBidById(HighestBid, conn, "BidderUUID"), Integer.parseInt(transaction.
                                                            getSqlSelectFromShopBidById(HighestBid, conn, "Bid")), "minus", conn);
                                                    transaction.updateCurrentRentPrice(Integer.parseInt
                                                            (transaction.getSqlSelectFromShopBidById(HighestBid, conn, "Bid")), allPlotIds.get(c), conn);
                                                    transaction.updateRentPrice(Integer.parseInt(transaction.getSqlSelectFromShopBidById(HighestBid, conn, "Bid")), ClaimId, conn);
                                                }else{
                                                    transaction.updateCurrentRentPrice(0, allPlotIds.get(c), conn);
                                                    transaction.updateCurrentRenter(0, allPlotIds.get(c), conn);
                                                }
                                            }else{
                                                transaction.updateCurrentRentPrice(0, allPlotIds.get(c), conn);
                                                transaction.updateCurrentRenter(0, allPlotIds.get(c), conn);
                                            }
                                        }
                                        transaction.resetRenterToBe(ClaimId, conn);
                                        transaction.resetHighestBidder(ClaimId, conn);
                                        Bids = transaction.getAllShopBids(Bids, 0, ClaimId, conn);
                                        for (Map.Entry<String, Integer> entry: Bids.entrySet()){
                                            String Name = entry.getKey();
                                            int Bid = entry.getValue();
                                            transaction.resetBid(Bid, Name, conn);
                                        }
                                    }
                                }
                            }
                            int TotalRevenue = TotalRentSum  - plotTaxes  - RentPayment;
                            transaction.updateBillingDirection("plus", BillId, conn);
                            transaction.updateTotalBillingAmount(TotalRevenue, BillId, conn);
                            transaction.updateTotalLicenceFee(plotTaxes, BillId, conn);
                            transaction.updateTotalRentProfit(TotalRentSum, BillId, conn);
                            transaction.updateTotalRentExpenditure(RentPayment, BillId, conn);
                        }
                    }
                }else{
                    if (transaction.selectRentProcessingFlag(conn) == 1){
                        transaction.updateRentProcessingFlag(0, conn);
                    }
                }
                         **/

                }

                RentingList = transaction.getAllTrustedParties(RentingList, 0, conn);
                for (int i = 0; i < RentingList.size(); i++){
                    String[] Splitter = RentingList.get(i).split("/");
                    LocalDateTime thisTime = LocalDateTime.now();
                    LocalDateTime thenTime = LocalDateTime.MIN;
                    thenTime.withYear(Integer.parseInt(Splitter[5]));
                    thenTime.withMonth(Integer.parseInt(Splitter[6]));
                    thenTime.withDayOfMonth(Integer.parseInt(Splitter[7]));
                    thenTime.withHour(Integer.parseInt(Splitter[8]));
                    thenTime.withMinute(Integer.parseInt(Splitter[9]));
                    if (thenTime.isAfter(thisTime)){
                        Sign ShopSign = (Sign) Bukkit.getWorld(Splitter[1]).getBlockAt(new Location(getWorldFromString(Splitter[1]),
                                Integer.parseInt(Splitter[2]), Integer.parseInt(Splitter[3]), Integer.parseInt(Splitter[4]))).getState();
                        int ClaimId = Integer.parseInt(Splitter[0]);
                        ShopSign.setLine(0, ChatColor.translateAlternateColorCodes
                                ('&', "&d[rent]"));
                        ShopSign.setLine(1, ChatColor.translateAlternateColorCodes
                                ('&', transaction.selectFromClaimById(ClaimId, conn, "signvalue1")));
                        ShopSign.setLine(2, ChatColor.translateAlternateColorCodes
                                ('&', transaction.selectFromClaimById(ClaimId, conn, "signvalue2")));
                        ShopSign.setLine(3, ChatColor.translateAlternateColorCodes
                                ('&', transaction.selectFromClaimById(ClaimId, conn, "rentprice")));
                        ShopSign.update();
                        transaction.resetRenter(ClaimId, conn);
                        transaction.resetRenterOnPlot(transaction.getPlotId(ClaimId, conn), conn);
                    }
                }
            }
        };
        runnable.runTaskLater(this, 36000);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Economy]: Plugin is disabled!");
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

    public void resetTradeDataCreationOnDelay(String player){
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                transaction.closeTradeCreationProcess(player, conn);
            }
        };
        runnable.runTaskLater(this, 6000);
    }

    public void createBill(){

    }

    public void calcIncome(){
        for (int i : mapBillIdToUuId.keySet()) {
            String AllUuIds = mapBillIdToUuId.get(i);
            double balanceBeforeTax = Double.parseDouble(transaction.selectFromPlayersByUUID(AllUuIds, conn, "Balance"));
            double TaxesOwed = payTaxes(mapBillIdToUuId.get(i), i);
            transaction.updateBalance(AllUuIds, TaxesOwed, "minus", conn);
            double balanceAfterTax = Double.parseDouble(transaction.selectFromPlayersByUUID(AllUuIds, conn, "Balance"));
            transaction.updateMaxMoneyAmount(Integer.parseInt(transaction.getSqlSelectFromWorld(conn, "MoneayAmount")) + TaxesOwed, conn);
            double directCollections = Double.parseDouble(transaction.selectFromPlayersByUUID(AllUuIds, conn, "AllDirectCollection"));
            double allShopBuys = Double.parseDouble(transaction.selectFromPlayersByUUID(AllUuIds, conn, "AllShopSales"));
            double TotalRentIncome = Double.parseDouble(transaction.selectFromPlayersByUUID(AllUuIds, conn, "AllRentIncome"));

            transaction.updateBillingBalanceBeforeTax(balanceBeforeTax, i , conn);
            transaction.updateBillingBalanceAfterTax(balanceAfterTax, i, conn);
            //rentIncome

            int licenceId = Integer.parseInt(transaction.selectLicenceByUUID(AllUuIds, conn, "LicenceId"));
            allPlotIds.clear();
            allPlotIds = transaction.getAllPlotIdsByLicence(allPlotIds, licenceId, 0, conn, "LicenceId");
            if (!allPlotIds.isEmpty()){
                for (int c = 0; c < allPlotIds.size(); c++){
                    if (transaction.selectFromCrossRentingByPlotId(allPlotIds.get(c), conn, "CurrentRenter") != null &&
                            !transaction.selectFromCrossRentingByPlotId(allPlotIds.get(c), conn, "CurrentRenter").isEmpty() &&
                            Integer.parseInt(transaction.selectFromCrossRentingByPlotId(allPlotIds.get(c), conn, "CurrentRenter")) != 0){
                        int playerIdOfRenter = Integer.parseInt(transaction.selectFromCrossRentingByPlotId(allPlotIds.get(c), conn, "CurrentRenter"));
                        String playerUuidOfRenter = transaction.getSqlSelectUuidFromPlayersById(playerIdOfRenter, conn);
                        double rentPrice = Double.parseDouble(transaction.selectFromCrossRentingByPlotId(allPlotIds.get(c), conn, "CurrentRentingPrice"));
                        transaction.updateBalance(playerUuidOfRenter, rentPrice, "minus", conn);
                        transaction.updateBalance(AllUuIds, rentPrice, "plus", conn);
                        transaction.updateAllRentExpenditure(playerUuidOfRenter, rentPrice, conn);
                        TotalRentIncome = TotalRentIncome + rentPrice;
                    }else{
                        transaction.resetBillKeyOnRenting(allPlotIds.get(c), conn);
                    }
                }
            }
            transaction.updateBillingAllDirectCollections(directCollections, i, conn);
            transaction.updateBillingAllDirectPayments(Double.parseDouble(transaction.selectFromPlayersByUUID(AllUuIds, conn, "AllDirectPayments")), i, conn);
            transaction.updateBillingAllRentIncome(TotalRentIncome, i, conn);
            double TotalIncome = TotalRentIncome + allShopBuys + directCollections;
            transaction.updateBillingAllShopSales(Double.parseDouble(transaction.selectFromPlayersByUUID(AllUuIds, conn, "AllShopSales")), i, conn);
            transaction.updateBillingAllShopBuys(Double.parseDouble(transaction.selectFromPlayersByUUID(AllUuIds, conn, "AllShopBuys")), i, conn);
            transaction.updateBillingAllUtilityExpenditure(Double.parseDouble(transaction.selectFromPlayersByUUID(AllUuIds, conn, "AllUtilityExpenditure")), i, conn);
            transaction.updateBillingTotalIncome(TotalIncome, i, conn);

        }
        for (int i : mapBillIdToUuId.keySet()) {
           String Uuid = mapBillIdToUuId.get(i);
           transaction.updateBillingAllRentExpenditure(Double.parseDouble(transaction.selectFromPlayersByUUID(Uuid, conn, "AllRentExpenditure")), i, conn);
           double TotalExpenditure = Double.parseDouble(transaction.getSqlSelectFromBilling(i, conn, "AllRentExpenditure")) + Double.parseDouble(transaction.getSqlSelectFromBilling(i,
                   conn, "AllUtilityExpenditure")) + Double.parseDouble(transaction.getSqlSelectFromBilling(i, conn, "AllShopBuys")) + Double.parseDouble
                   (transaction.getSqlSelectFromBilling(i, conn, "AllDirectPayments"));
           double TotalIncome = Double.parseDouble(transaction.getSqlSelectFromBilling(i, conn, "TotalIncome"));
           double balance = Double.parseDouble(transaction.selectFromPlayersByUUID(Uuid,conn, "Balance"));
           double totalProfit = balance + TotalIncome;
           double BalLeftOver = totalProfit - TotalExpenditure;
           transaction.updateBillingLeftOverBalance(BalLeftOver, i, conn);
           transaction.updateBillingTotalExpenditure(TotalExpenditure, i, conn);
        }

    }

    public double payTaxes(String PlayerUUID, int i){
        int MaxMoneyAmt = Integer.parseInt(transaction.getSqlSelectFromWorld(conn, "MoneyAmount"));
        double playerBalance = transaction.getBalance(PlayerUUID, conn);
        double zwProzDivRes = MaxMoneyAmt / playerBalance;
        double TaxPercentage = 100 / zwProzDivRes;
        double playerbal1perc = playerBalance /100;
        double EffectiveTaxAmt = playerbal1perc * TaxPercentage;
        transaction.updateBillingTaxPercentage(TaxPercentage, i, conn);
        return EffectiveTaxAmt;
    }
}
