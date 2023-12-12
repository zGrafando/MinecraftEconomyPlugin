package com.grafando.economy.data;

import io.netty.handler.codec.memcache.binary.FullBinaryMemcacheRequest;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.entity.Stray;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class Transactions {
    Connection conn = null;
    PreparedStatement ps = null;

    String username;
    String password;

    String serverName;
    String dbms;
    String SQLSelectBalanceQuery = "SELECT Balance FROM players Where UUID = ?;";
    String SQLInsertTradeData = "INSERT INTO tradedata (OwnerUUID ,Sign_World, Sign_X, Sign_Y, Sign_Z, Chest_World" +
            ", Chest_X, Chest_Y, Chest_Z, InvIndices, StackCnt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
    String SQLSelectTrade = "SELECT OwnerUUID FROM tradedata WHERE Sign_World = ? AND Sign_X = ? AND Sign_Y = ? AND Sign_Z = ?;";
    String SQLDeleteTradeData = "DELETE FROM tradedata WHERE TradeDataId = (SELECT TradeDataId from TradeData where Sign_World = ? AND Sign_X = ? AND Sign_Y = ? AND Sign_Z = ? AND " +
            "OwnerUUID = ? AND Source_World = ? AND Source_X = ? AND Source_Y = ? AND Source_Z = ?);";
    String SQLSimpleDeleteTradeData = "DELETE FROM tradedata WHERE TradeDataId = (SELECT TradeDataId from TradeData WHERE Sign_World = ? AND Sign_X = ? AND Sign_Y = ? AND Sign_Z = ? AND " +
            "OwnerUUID = ?);";
    String SQLInsertCreation = "INSERT INTO tradedatacreation (UUID, Chest_World, Chest_X, Chest_Y, Chest_Z, InvIndices, StackCnt) Values (?, ?, ?, ?, ?, ?, ?);";
    String SQLInspectCreation = "SELECT UUID, Chest_World, Chest_X, Chest_Y, Chest_Z, InvIndices, StackCnt FROM tradedatacreation WHERE UUID = ?;";
    String SQLDeleteCreation = "DELETE FROM tradedatacreation WHERE UUID = ?;";
    String SQLInspectTradeData = "SELECT * FROM tradedata WHERE Sign_World = ? AND Sign_X = ? AND Sign_Y = ? AND Sign_Z = ?;";
    String SQLUpdateBalance = "UPDATE players SET Balance = ? Where PlayerId = (SELECT PlayerId from Players where UUID = ?);";
    String SQLEvaluateClaim = "SELECT * from claims WHERE (Block1_World = ? AND Block2_World = ?) AND " +
            "((? >= Block1_X AND ? <= Block2_X) AND (? >= Block1_Z AND ? <= Block2_Z)) OR " +
            "((? >= Block1_X AND ? <= Block2_X) AND (? <= Block1_Z AND ? >= Block2_Z)) OR" +
            "((? <= Block1_X AND ? >= Block2_X) AND (? >= Block1_Z AND ? <= Block2_Z)) OR" +
            "((? <= Block1_X AND ? >= Block2_X) AND (? <= Block1_Z AND ? >= Block2_Z))";
    String SQLUpdateRenter = "UPDATE claims SET RenterUUID = ?, RentTermination = ?, RentPrice = ?, Rentsign_World = ?, " +
            "Rentsign_X = ?, Rentsign_Y = ?, Rentsign_Z = ?, SignValue1 = ?, SignValue2 = ? WHERE ClaimId = ?;";
    String SQLResetRenter = "UPDATE claims SET RenterUUID = null, RentTermination = null, RentPrice = null, Rentsign_World = null, " +
            "Rentsign_X = null, Rentsign_Y = null, Rentsign_Z = null, SignValue1 = null, SignValue2 = null WHERE ClaimId = ?;";
    String SQLgetAllRentDates = "SELECT RentTermination, RentSign_World, RentSign_X, RentSign_Y, RentSign_Z, ClaimId from claims " +
            "where RenterUUID <> '' and ClaimId > ? ORDER BY ClaimId ASC";
    String SQLgetAllLicenceIds = "SELECT RenterLicenceUUID, LicenceId from RenterLicence where LicenceId > ? ORDER BY LicenceId ASC";
    String SQLgetAllBidsByPlayerId = "SELECT ClaimId, from renterlicence where LicenceId > ? ORDER BY LicenceId ASC";
    String SQLgetAllPlotsByLicence = "SELECT PlotId from crossrentings where ForeignRenterLicence = ? and where FullRentPeriodFlag = 1 and where PlotId > ? ORDER BY PlotId ASC;";
    String SQLgetAllPlotsByRenter = "SELECT PlotId from crossrentings where ForeignCurrentRenter = ? and where PlotId > ? ORDER BY PlotId ASC;";
    String SQLgetAllPlayerIds = "SELECT UUID from players where PlayerId > ? ORDER BY PlayerId ASC";
    String SqlgetAllPlotIds = "SELECT PlotId from crossrentings where ForeignCurrentRenter = 0 and PlotId > ? ORDER BY PlayerId ASC";
    String GetBillIdByUuid = "SELECT MIN(BillId) from billing where BilledUuid = ? and ActiveFlag = 1";
    String SqlSelectClaimById = "SELECT * from claims where ClaimId = ?;";
    String SQLSelectPlotIdFromForeignClaimId = "Select * from crossrentings where ForeignClaimId = ?;";
    String SqlInsertPlot = "Insert into crossrentings (ForeignRenterLicence, Creationdate, ForeignCurrentRenter, CurrentRentingPrice, ForeignClaimId) Values (?, ?, ?, ?, ?);";
    String SqlUpdatePlot = "Update crossrentings set ForeignCurrentRenter = ?, CurrentRentingPrice = ? where PlotId = ?;";
    String SQLResetRenterOnPlot = "Update crossrentings set ForeignCurrentRenter = null where PlotId = ?;";
    String SQLSelectFromPlayersByUUID = "Select * from players where UUID = ?;";
    String SQLSelectLicenceByUUID = "Select * from renterlicence where RenterLicenceUUID = ?;";
    String SqlUpdateRentPrice = "Update claims set RentPrice = ? where ClaimId = ?;";
    String SqlUpdateMaxMoneyAmount = "Update world set MoneyAmount = ? where idWorld = ?;";
    String SqlSelectFromShopBidByClaimId = "Select * from shopbid where ForeignClaimId = ? AND ShopBidId > ? Order By ShopBidId ASC;";
    String SQLSelectRenterNameFromClaimId = "SELECT Playername from players where PlayerId = (SELECT ForeignCurrentRenter from CrossRentings " +
            "where ForeignClaimId = ? Order By PlotId ASC)";
    String SqlInsertShopBid = "INSERT into shopbid (ForeignClaimId, Bid, BidderUUID, BidderName) VALUES (?, ?, ?, ?);";
    String SqlUpdateShopBid = "Update shopbid Set Bid = ? where BidderName = ?";
    String SQLDeleteBid = "Delete from shopbid where Bid = ? and BidderName = ?;";
    String SQLDeleteBill = "Delete from billing where BillId = ?;";
    String SqlUpdateNextRenter = "Update claims set RenterUuidToBe = ? where ClaimId = ?;";
    String SqlUpdateCurrentRenter = "Update crossrentings set ForeignCurrentRenter = ? where PlotId = ?;";
    String SqlUpdateHighestBidder = "Update claims set CurrentHighestBidder = ? where ClaimId = ?;";
    String SqlSelectFromShopBidById = "Select * From shopbid where ShopBidId = ?";
    String SqlSelectFromShopBidByNameAndBid = "Select * from shopbid where BidderName = ? and Bid = ?;";
    String SqlSelectUuidFromPlayersById = "Select UUID from players where PlayerId = ?;";
    String SQLSelectRentProcessingFlag = "Select RentProcessingFlag from world where idWorld = 1;";
    String SqlUpdateRentProcessingFlag = "Update world set RentProcessingFlag = ? where idWorld = 1;";
    String SqlUpdateCurrentRentPrice = "Update crossrentings set CurrentRentingPrice = ? where PlotId = ?;";
    String SqlSelectFromWorld = "Select * from world where idWorld = 1;";
    String SqlUpdateBillKeyOnRentings = "Update rentbill set ForeignBill = ? where RentBillId = ?;";
    String SqlResetBillKeyOnRentings = "Update crossrentings set ForeignBill = null where RentBillId = ?;";
    String SQLInsertBill = "Insert into billing (BilledPlayerUuid, ActiveFlag, PeriodStart, PeriodEnd) VALUES (?, ?, ?, ?);";
    String SqlSelectFromBill = "Select * from billing where BillId = ?;";
    String SqlSelectSpecificBillByUUID = "Select * from billing Where BilledUuid = ? and PeriodStart = ? and PeriodEnd = ?;";
    String SqlSelectFromBillById = "Select * from billing where BilledUuid = ?;";
    String SQLResetRenterToBe = "Update claims set RenterUuidToBe = null where ClaimId = ?;";
    String SQLResetHighestBidder = "Update claims set HighestBidder = 0 where ClaimId = ?;";
    String SQLUpdateBillDirection = "Update billing set Direction = ? where BillId = ?;";
    String SQLUpdateTotalLicenceFee = "Update billing set TotalLicenceFee = ? where BillId = ?;";
    String SQLUpdateTotalRentProfit = "Update billing set TotalRentProfit = ? where BillId = ?;";
    String SQlUpdateTotalRentExpenditure = "Update billing set TotalRentExpenditure = ? where BillId = ?;";
    String SQLUpdateTotalBillingAmount = "Update billing set BillingAmount = ? where BillId = ?;";
    String SQLUpdatePlayersAllRentIncome = "Update players set AllRentIncome = ? where UUID = ?;";
    String SQLUpdatePlayersAllRentExpenditure = "Update players set AllRentExpenditure = ? where UUID = ?;";
    String SQLUpdatePlayersAllDirectCollections = "Update players set AllDirectCollections = ? where UUID = ?;";
    String SQLUpdatePlayersAllDirectPayments = "Update players set AllDirectPayments = ? where UUID = ?;";
    String SqlSelectFromRenterLicence = "Select * from renterlicence where RenterLicenceUUID = ?;";
    String SQLUpdateClaimsRenterToNone = "Update claims set RenterUUID = ? where ClaimId = ?;";
    String SqlUpdateBillingAllDirectCollections = "Update billing set AllDirectCollections = ? where BillId = ?;";
    String SqlUpdateBillingAllDirectPayments = "Update billing set AllDirectPayments = ? where BillId = ?;";
    String SqlUpdateBillingAllRentIncome = "Update billing set AllRentIncome = ? where BillId = ?;";
    String SqlUpdateBillingAllRentExpenditure = "Update billing set AllRentExpenditure = ? where BillId = ?;";
    String SqlUpdateBillingAllShopSales = "Update billing set AllShopSales = ? where BillId = ?;";
    String SqlUpdateBillingAllShopBuys = "Update billing set AllShopBuys = ? where BillId = ?;";
    String SqlUpdateBillingAllUtilityExpenditure = "Update billing set AllUtilityExpenditure = ? where BillId = ?;";
    String SqlUpdateBillingTotalIncome = "Update billing set TotalIncome = ? where BillId = ?;";
    String SqlUpdateBillingTotalExpenditure = "Update billing set TotalExpenditure = ? where BillId = ?;";
    String SqlUpdateBillingLeftoverBalance = "Update billing set LeftoverBalance = ? where BillId = ?;";
    String SQLUpdateWorldSetNextBillingDate = "Update world set NextBillingDate = ? where idWorld = 1;";
    String SqlUpdateBillingBalanceBeforeTax = "Update world set BalanceBeforeTax = ? where idWorld = 1;";
    String SqlUpdateBillingBalanceAfterTax = "Update billing set LeftoverBalance = ? where BillId = ?;";
    String SqlUpdateBillingTaxPercentage = "Update billing set LeftoverBalance = ? where BillId = ?;";
    String CmaDelmRowIndexes;
    String CmaDelmRowAmnts;
    ResultSet rs;
    ResultSet ts;
    int rowsAffected;


    public Transactions() {
        this.password = "Nbiwe!12";
        this.username = "grafando";
        this.serverName = "jdbc:mysql://localhost:3306/Edgewind";
        this.dbms = "mysql";
    }


    public Connection getConnection(){
        try {
            Properties connectionParms = new Properties();
            connectionParms.put("user", this.username);
            connectionParms.put("password", this.password);
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (this.dbms.equals("mysql")) {
                conn = DriverManager.getConnection(serverName, connectionParms);
            }
            assert conn != null;
            conn.setAutoCommit(true);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        }catch(SQLException e){
            e.printStackTrace();
        }
        return conn;
    }

    public ArrayList<String> getAllTrustedParties(ArrayList<String> playerIdList, int startPos, Connection connect){
        try{
            ps = connect.prepareStatement(SQLgetAllRentDates);
            ps.setInt(1, startPos);
            rs = ps.executeQuery();
            if (rs.next()) {
                String SignString = rs.getInt("ClaimId")+"/"+rs.getString("Rentsign_World")+"/"+
                        rs.getDouble("Rentsign_X")+"/"+rs.getDouble("Rentsign_Y")+"/"+
                        rs.getDouble("Rentsign_Z")+"/"+rs.getDouble("RentTermination");
                playerIdList.add(SignString);
                getAllTrustedParties(playerIdList, rs.getInt("ClaimId"), connect);
            }else{
                return playerIdList;
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return playerIdList;
    }

    public ArrayList<String> getAllLicences(ArrayList<String> LicenceIdList, int startPos, Connection connect){
        try{
            ps = connect.prepareStatement(SQLgetAllLicenceIds);
            ps.setInt(1, startPos);
            rs = ps.executeQuery();
            if (rs.next()) {
                LicenceIdList.add(rs.getString("RenterLicenceUUID"));
                getAllLicences(LicenceIdList, rs.getInt("LicenceId"), connect);
            }else{
                return LicenceIdList;
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return LicenceIdList;
    }

    public ArrayList<String> getAllPlayerUuIds(ArrayList<String> ClaimIdList, int startPos, Connection connect){
        try{
            ps = connect.prepareStatement(SQLgetAllPlayerIds);
            ps.setInt(1, startPos);
            rs = ps.executeQuery();
            if (rs.next()) {
                ClaimIdList.add(rs.getString("UUID"));
                getAllPlayerUuIds(ClaimIdList, rs.getInt("PlayerId"), connect);
            }else{
                return ClaimIdList;
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return ClaimIdList;
    }

    public ArrayList<Integer> getAllBidsByPlayerId(ArrayList<Integer> ClaimIdList, int startPos, Connection connect){
        try{
            ps = connect.prepareStatement(SQLgetAllBidsByPlayerId);
            ps.setInt(1, startPos);
            rs = ps.executeQuery();
            if (rs.next()) {
                ClaimIdList.add(rs.getInt("ClaimId"));
                getAllBidsByPlayerId(ClaimIdList, rs.getInt("LicenceId"), connect);
            }else{
                return ClaimIdList;
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return ClaimIdList;
    }

    public ArrayList<Integer> getAllPlotIds(ArrayList<Integer> ClaimIdList, int startPos, Connection connect){
        try{
            ps = connect.prepareStatement(SqlgetAllPlotIds);
            ps.setInt(1, startPos);
            rs = ps.executeQuery();
            if (rs.next()) {
                ClaimIdList.add(rs.getInt("PlotId"));
                getAllBidsByPlayerId(ClaimIdList, rs.getInt("PlotId"), connect);
            }else{
                return ClaimIdList;
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return ClaimIdList;
    }

    public ArrayList<Integer> getAllPlotIdsByLicence(ArrayList<Integer> LicenceIdList, int ForeignLicenceId, int startPos, Connection connect, String modus){
        try{
            if (modus.equalsIgnoreCase("LicenceId")) {
                ps = connect.prepareStatement(SQLgetAllPlotsByLicence);
                ps.setInt(1, ForeignLicenceId);
            }else if(modus.equalsIgnoreCase("RenterId")){
                ps = connect.prepareStatement(SQLgetAllPlotsByRenter);
            }
            ps.setInt(2, startPos);
            rs = ps.executeQuery();
            if (rs.next()) {
                LicenceIdList.add(rs.getInt("PlotId"));
                getAllPlotIdsByLicence(LicenceIdList, ForeignLicenceId, rs.getInt("LicenceId"), connect, modus);
            }else{
                return LicenceIdList;
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return LicenceIdList;
    }

    public HashMap<String, Integer> getAllShopBids(HashMap<String, Integer> Bids, int startPos, int ClaimId, Connection connect){
        try{
            ps = connect.prepareStatement(SqlSelectFromShopBidByClaimId);
            ps.setInt(1, ClaimId);
            ps.setInt(2, startPos);
            rs = ps.executeQuery();
            if (rs.next()) {
                Bids.put(rs.getString("BidderName"), rs.getInt("Bid"));
                getAllShopBids(Bids, rs.getInt("ShopBidId"), ClaimId, connect);
            }else{
                return Bids;
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return Bids;
    }

    public String getSqlSelectFromShopBidById(int BidId, Connection connect, String modus){
        String claimowner = "";
        try{
            ps = connect.prepareStatement(SqlSelectFromShopBidById);
            ps.setInt(1, BidId);
            rs = ps.executeQuery();
            if (rs.next()){
                if (modus.equalsIgnoreCase("Bid")){
                    claimowner = String.valueOf(rs.getInt("Bid"));
                }else if (modus.equalsIgnoreCase("BidderUUID")){
                    claimowner = rs.getString("BidderUUID");
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return claimowner;
    }

    public String getSqlSelectFromBilling(int BillId, Connection connect, String modus){
        String claimowner = "";
        try{
            ps = connect.prepareStatement(SqlSelectFromBill);
            ps.setInt(1, BillId);
            rs = ps.executeQuery();
            if (rs.next()){
                if (modus.equalsIgnoreCase("BillId")){
                    claimowner = String.valueOf(rs.getInt("BillId"));
                }else if (modus.equalsIgnoreCase("BilledPlayer")){
                    claimowner = rs.getString("BilledPlayer");
                }else if (modus.equalsIgnoreCase("BilledUuid")){
                    claimowner = rs.getString("BilledUuid");
                }else if (modus.equalsIgnoreCase("Direction")){
                    claimowner = rs.getString("Direction");
                }else if (modus.equalsIgnoreCase("BillingAmount")){
                    claimowner = String.valueOf(rs.getInt("BillingAmount"));
                }else if (modus.equalsIgnoreCase("TotalLicenceFee")){
                    claimowner = String.valueOf(rs.getInt("TotalLicenceFee"));
                }else if (modus.equalsIgnoreCase("TotalRentProfit")){
                    claimowner = String.valueOf(rs.getInt("TotalRentProfit"));
                }else if (modus.equalsIgnoreCase("TotalRentExpenditures")){
                    claimowner = String.valueOf(rs.getInt("TotalRentExpenditures"));
                }else if (modus.equalsIgnoreCase("AllDirectCollections")){
                    claimowner = String.valueOf(rs.getDouble("AllDirectCollections"));
                }else if (modus.equalsIgnoreCase("AllDirectPayments")){
                    claimowner = String.valueOf(rs.getDouble("AllDirectPayments"));
                }else if (modus.equalsIgnoreCase("AllRentIncome")){
                    claimowner = String.valueOf(rs.getDouble("AllRentIncome"));
                }else if (modus.equalsIgnoreCase("AllRentExpenditure")){
                    claimowner = String.valueOf(rs.getDouble("AllRentExpenditure"));
                }else if (modus.equalsIgnoreCase("AllShopSales")){
                    claimowner = String.valueOf(rs.getDouble("AllShopSales"));
                }else if (modus.equalsIgnoreCase("AllShopBuys")){
                    claimowner = String.valueOf(rs.getDouble("AllShopBuys"));
                }else if (modus.equalsIgnoreCase("AllUtilityExpenditure")){
                    claimowner = String.valueOf(rs.getDouble("AllUtilityExpenditure"));
                }else if (modus.equalsIgnoreCase("PeriodStart")){
                    claimowner = rs.getString("PeriodStart");
                }else if (modus.equalsIgnoreCase("PeriodEnd")){
                    claimowner = rs.getString("PeriodEnd");
                }else if (modus.equalsIgnoreCase("TotalIncome")){
                    claimowner = String.valueOf(rs.getDouble("TotalIncome"));
                }else if (modus.equalsIgnoreCase("TotalExpenditure")){
                    claimowner = String.valueOf(rs.getDouble("TotalExpenditure"));
                }else if (modus.equalsIgnoreCase("LeftoverBalance")){
                    claimowner = String.valueOf(rs.getDouble("LeftoverBalance"));
                }else if (modus.equalsIgnoreCase("CapitalGains")){
                    claimowner = String.valueOf(rs.getDouble("CapitalGains"));
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return claimowner;
    }

    public String getSqlSelectFromBillingByUuid(String UUID, Connection connect, String modus){
        String claimowner = "";
        try{
            ps = connect.prepareStatement(SqlSelectFromBillById);
            ps.setString(1, UUID);
            rs = ps.executeQuery();
            if (rs.next()){
                if (modus.equalsIgnoreCase("BillId")){
                    claimowner = String.valueOf(rs.getInt("BillId"));
                }else if (modus.equalsIgnoreCase("BilledPlayer")){
                    claimowner = rs.getString("BilledPlayer");
                }else if (modus.equalsIgnoreCase("BilledUuid")){
                    claimowner = rs.getString("BilledUuid");
                }else if (modus.equalsIgnoreCase("Direction")){
                    claimowner = rs.getString("Direction");
                }else if (modus.equalsIgnoreCase("BillingAmount")){
                    claimowner = String.valueOf(rs.getInt("BillingAmount"));
                }else if (modus.equalsIgnoreCase("TotalLicenceFee")){
                    claimowner = String.valueOf(rs.getInt("TotalLicenceFee"));
                }else if (modus.equalsIgnoreCase("TotalRentProfit")){
                    claimowner = String.valueOf(rs.getInt("TotalRentProfit"));
                }else if (modus.equalsIgnoreCase("TotalRentExpenditures")){
                    claimowner = String.valueOf(rs.getInt("TotalRentExpenditures"));
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return claimowner;
    }

    public String getSqlSelectSpecificFromBillingByUuid(String UUID, String PeriodStart, String PeriodEnd, Connection connect, String modus){
        String claimowner = "";
        try{
            ps = connect.prepareStatement(SqlSelectSpecificBillByUUID);
            ps.setString(1, UUID);
            ps.setString(2, PeriodStart);
            ps.setString(3, PeriodEnd);
            rs = ps.executeQuery();
            if (rs.next()){
                if (modus.equalsIgnoreCase("BillId")) {
                    claimowner = String.valueOf(rs.getInt("BillId"));
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return claimowner;
    }

    public String getSqlSelectFromRenterLicence(String UUID, Connection connect, String modus){
        String claimowner = "";
        try{
            ps = connect.prepareStatement(SqlSelectFromRenterLicence);
            ps.setString(1, UUID);
            rs = ps.executeQuery();
            if (rs.next()){
                if (modus.equalsIgnoreCase("LicenceId")){
                    claimowner = String.valueOf(rs.getInt("LicenceId"));
                }else if (modus.equalsIgnoreCase("RenterLicenceUUID")){
                    claimowner = rs.getString("RenterLicenceUUID");
                }else if (modus.equalsIgnoreCase("AmountOfPlots")){
                    claimowner = rs.getString("AmntOfPlotsLicenced");
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return claimowner;
    }


    public String getSqlSelectFromWorld(Connection connect, String modus){
        String claimowner = "";
        try{
            ps = connect.prepareStatement(SqlSelectFromWorld);
            rs = ps.executeQuery();
            if (rs.next()){
                if (modus.equalsIgnoreCase("LicenceCost")){
                    claimowner = String.valueOf(rs.getInt("LicenceCost"));
                }else if (modus.equalsIgnoreCase("PlotCost")){
                    claimowner = String.valueOf(rs.getInt("PlotCost"));
                }else if (modus.equalsIgnoreCase("LicenceFee")){
                    claimowner = String.valueOf(rs.getInt("LicenceFee"));
                }else if (modus.equalsIgnoreCase("MoneyAmount")){
                    claimowner = String.valueOf(rs.getInt("MoneyAmount"));
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return claimowner;
    }

    public String getSqlSelectFromShopBidByNameAndBid(String Name, int Bid,  Connection connect, String modus){
        String claimowner = "";
        try{
            ps = connect.prepareStatement(SqlSelectFromShopBidByNameAndBid);
            ps.setString(1, Name);
            ps.setInt(2, Bid);
            rs = ps.executeQuery();
            if (rs.next()){
                if (modus.equalsIgnoreCase("ShopBidId")){
                    claimowner = String.valueOf(rs.getInt("PlayerId"));
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return claimowner;
    }

    public String getSqlSelectUuidFromPlayersById(int Id, Connection connect){
        String claimowner = "";
        try{
            ps = connect.prepareStatement(SqlSelectUuidFromPlayersById);
            ps.setInt(1, Id);
            rs = ps.executeQuery();
            if (rs.next()){
                claimowner = rs.getString("UUID");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return claimowner;
    }

    public String selectFromCrossRentingByPlotId(int PlotId,  Connection connect, String modus){
        String claimowner = "";
        try{
            ps = connect.prepareStatement(SqlSelectFromShopBidByNameAndBid);
            ps.setInt(1, PlotId);
            rs = ps.executeQuery();
            if (rs.next()){
                if (modus.equalsIgnoreCase("PlotId")){
                    claimowner = String.valueOf(rs.getInt("PlotId"));
                }else if (modus.equalsIgnoreCase("Licence")){
                    claimowner = String.valueOf(rs.getInt("ForeignRenterLicence"));
                }else if (modus.equalsIgnoreCase("Creationdate")){
                    claimowner = rs.getString("Creationdate");
                }else if (modus.equalsIgnoreCase("CurrentRenter")){
                    claimowner = String.valueOf(rs.getInt("ForeignCurrentRenter"));
                }else if (modus.equalsIgnoreCase("ClaimId")){
                    claimowner = String.valueOf(rs.getInt("ForeignClaimId"));
                }else if (modus.equalsIgnoreCase("CurrentRentingPrice")){
                    claimowner = String.valueOf(rs.getInt("CurrentRentingPrice"));
                }else if (modus.equalsIgnoreCase("Bill")) {
                    claimowner = String.valueOf(rs.getInt("ForeignBill"));
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return claimowner;
    }

    public String selectFromPlayersByUUID(String UUID, Connection connect, String modus){
        String claimowner = "";
        boolean stat = false;
        double FinDetails = 0;
        try{
            ps = connect.prepareStatement(SQLSelectFromPlayersByUUID);
            ps.setString(1, UUID);
            rs = ps.executeQuery();
            if (rs.next()){
                if (modus.equalsIgnoreCase("PlayerId")){
                    claimowner = rs.getString("PlayerId");
                }else if(modus.equalsIgnoreCase("Balance")) {
                    claimowner = String.valueOf(rs.getDouble("Balance"));
                }else if(modus.equalsIgnoreCase("Playername")){
                    claimowner = rs.getString("Playername");
                }else if(modus.equalsIgnoreCase("AllShopSales")){
                    FinDetails = rs.getDouble("AllShopSales");
                    stat = true;
                }else if(modus.equalsIgnoreCase("AllDirectCollections")){
                    FinDetails = rs.getDouble("AllDirectCollections");
                    stat = true;
                }else if(modus.equalsIgnoreCase("AllShopBuys")){
                    FinDetails = rs.getDouble("AllShopBuys");
                    stat = true;
                }else if(modus.equalsIgnoreCase("AllDirectPayments")){
                    FinDetails = rs.getDouble("AllDirectPayments");
                    stat = true;
                }else if(modus.equalsIgnoreCase("AllUtilityExpenditure")){
                    FinDetails = rs.getDouble("AllUtilityExpenditure");
                    stat = true;
                }else if(modus.equalsIgnoreCase("AllRentIncome")){
                    FinDetails = rs.getDouble("AllRentIncome");
                    stat = true;
                }else if(modus.equalsIgnoreCase("AllRentExpenditure")){
                    FinDetails = rs.getDouble("AllRentExpenditure");
                    stat = true;
                }
            }
            if (stat){
                claimowner = String.valueOf(FinDetails);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return claimowner;
    }

    public String selectLicenceByUUID(String UUID, Connection connect, String modus){
        String claimowner = "";
        try{
            ps = connect.prepareStatement(SQLSelectLicenceByUUID);
            ps.setString(1, UUID);
            rs = ps.executeQuery();
            if (rs.next()){
                if (modus.equalsIgnoreCase("AmountOfPlots")){
                    claimowner = String.valueOf(rs.getString("AmntOfPlotsLicenced"));
                }else if(modus.equalsIgnoreCase("LicenceId")) {
                    claimowner = String.valueOf(rs.getDouble("LicenceId"));
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return claimowner;
    }

    public int selectRentProcessingFlag(Connection connect){
        try{
            ps = connect.prepareStatement(SQLSelectRentProcessingFlag);
            rs = ps.executeQuery();
            if (rs.next()){
                return rs.getInt("RentProcessingFlag");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return 0;
    }

    public int selectBillIdFromUuid(String Uuid, Connection connect){
        try{
            ps = connect.prepareStatement(GetBillIdByUuid);
            ps.setString(1, Uuid);
            rs = ps.executeQuery();
            if (rs.next()){
                return rs.getInt("BillId");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return 0;
    }

    public String selectFromClaimById(int ClaimId, Connection connect, String modus){
        String claimowner = "";
        try{
            ps = connect.prepareStatement(SqlSelectClaimById);
            ps.setInt(1, ClaimId);
            rs = ps.executeQuery();
            if (rs.next()){
                if (modus.equalsIgnoreCase("uuid")){
                    claimowner = rs.getString("ClaimownerUUID");
                }else if(modus.equalsIgnoreCase("name")) {
                    claimowner = rs.getString("Claimowner");
                }else if (modus.equalsIgnoreCase("renteduuid")){
                    claimowner = rs.getString("RenterUUID");
                }else if (modus.equalsIgnoreCase("termination")){
                    claimowner = rs.getString("RentTermination");
                }else if (modus.equalsIgnoreCase("rentprice")){
                    claimowner = String.valueOf(rs.getInt("RentPrice"));
                }else if (modus.equalsIgnoreCase("rentsign")){
                    claimowner = rs.getString("Rentsign_World")+"/"+rs.getDouble("Rentsign_X")+
                            "/"+rs.getDouble("Rentsign_Y")+"/"+rs.getDouble("Rentsign_Z");
                }else if (modus.equalsIgnoreCase("ClaimId")){
                    claimowner = String.valueOf(rs.getInt("ClaimId"));
                }else if (modus.equalsIgnoreCase("signvalue1")){
                    claimowner = rs.getString("SignValue1");
                }else if (modus.equalsIgnoreCase("signvalue2")){
                    claimowner = rs.getString("SignValue2");
                }else if (modus.equalsIgnoreCase("Rentsignworld")){
                    claimowner = rs.getString("Rentsign_World");
                }else if (modus.equalsIgnoreCase("Rentsignx")){
                    claimowner = String.valueOf(rs.getDouble("Rentsign_X"));
                }else if (modus.equalsIgnoreCase("Rentsigny")){
                    claimowner = String.valueOf(rs.getDouble("Rentsign_Y"));
                }else if (modus.equalsIgnoreCase("Rentsignz")){
                    claimowner = String.valueOf(rs.getDouble("Rentsign_Z"));
                }else if (modus.equalsIgnoreCase("RenterUuidToBe")){
                    claimowner = rs.getString("RenterUuidToBe");
                }else if (modus.equalsIgnoreCase("CurrentHighestBidder")){
                    claimowner = String.valueOf(rs.getInt("CurrentHighestBidder"));
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return claimowner;
    }

    public String isInClaim(String World, double blockX, double blockZ, Connection connect, String modus){
        String claimowner = "";
        try{
            ps = connect.prepareStatement(SQLEvaluateClaim);
            ps.setString(1, World);
            ps.setString(2, World);
            ps.setDouble(3, blockX);
            ps.setDouble(4, blockX);
            ps.setDouble(5, blockZ);
            ps.setDouble(6, blockZ);
            ps.setDouble(7, blockX);
            ps.setDouble(8, blockX);
            ps.setDouble(9, blockZ);
            ps.setDouble(10, blockZ);
            ps.setDouble(11, blockX);
            ps.setDouble(12, blockX);
            ps.setDouble(13, blockZ);
            ps.setDouble(14, blockZ);
            ps.setDouble(15, blockX);
            ps.setDouble(16, blockX);
            ps.setDouble(17, blockZ);
            ps.setDouble(18, blockZ);
            rs = ps.executeQuery();
            if (rs.next()){
                if (modus.equalsIgnoreCase("uuid")){
                    claimowner = rs.getString("ClaimownerUUID");
                }else if(modus.equalsIgnoreCase("name")) {
                    claimowner = rs.getString("Claimowner");
                }else if (modus.equalsIgnoreCase("renteduuid")){
                    claimowner = rs.getString("RenterUUID");
                }else if (modus.equalsIgnoreCase("termination")){
                    claimowner = rs.getString("RentTermination");
                }else if (modus.equalsIgnoreCase("rentprice")){
                    claimowner = String.valueOf(rs.getInt("RentPrice"));
                }else if (modus.equalsIgnoreCase("rentsign")){
                    claimowner = rs.getString("Rentsign_World")+"/"+rs.getDouble("Rentsign_X")+
                            "/"+rs.getDouble("Rentsign_Y")+"/"+rs.getDouble("Rentsign_Z");
                }else if (modus.equalsIgnoreCase("ClaimId")){
                    claimowner = String.valueOf(rs.getInt("ClaimId"));
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return claimowner;
    }

    public double getBalance(String player, Connection connec){
        double balance = -0.1;
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLSelectBalanceQuery);
            ps.setString(1, player);
            rs = ps.executeQuery();
            if (rs.next()) {
                balance = rs.getDouble("Balance");
            }else{
                balance = -2;
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return balance;
    }

    public int getPlotId(int ClaimId, Connection connec){
        int balance = -1;
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLSelectPlotIdFromForeignClaimId);
            ps.setInt(1, ClaimId);
            rs = ps.executeQuery();
            if (rs.next()) {
                balance = rs.getInt("PlotId");
            }else{
                balance = -2;
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return balance;
    }

    public String getRenterNameFromClaimId(int ClaimId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLSelectRenterNameFromClaimId);
            ps.setInt(1, ClaimId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("Playername");
            }else{
                return "";
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return "";
    }

    public boolean resetRenterOnPlot(int PlotId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLResetRenterOnPlot);
            ps.setInt(1, PlotId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean resetBid(int Bid, String BidderName, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLDeleteBid);
            ps.setInt(1, Bid);
            ps.setString(2, BidderName);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeBill(int BillId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLDeleteBill);
            ps.setInt(1, BillId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRenterToBe(String RenterUuid, int ClaimId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateNextRenter);
            ps.setString(1, RenterUuid);
            ps.setInt(2, ClaimId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCurrentRenter(int PlayerId, int PlotId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateCurrentRenter);
            ps.setInt(1, PlayerId);
            ps.setInt(2, PlotId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAllRentIncome(String UUID, double Amnt, Connection connec){
        try{
            conn = connec;
            double rentIncome = Double.parseDouble(selectFromPlayersByUUID(UUID, connec, "AllRentIncome"));
            double newRentIncome = rentIncome + Amnt;
            ps = conn.prepareStatement(SQLUpdatePlayersAllRentIncome);
            ps.setString(1, UUID);
            ps.setDouble(2, newRentIncome);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAllRentExpenditure(String UUID, double Amnt, Connection connec){
        try{
            conn = connec;
            double rentExpenditure = Double.parseDouble(selectFromPlayersByUUID(UUID, connec, "AllRentExpenditure"));
            double newRentExpenditure = rentExpenditure + Amnt;
            ps = conn.prepareStatement(SQLUpdatePlayersAllRentExpenditure);
            ps.setDouble(1, newRentExpenditure);
            ps.setString(2, UUID);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAllDirectCollections(String UUID, double Amnt, Connection connec){
        try{
            conn = connec;
            double directCollections = Double.parseDouble(selectFromPlayersByUUID(UUID, connec, "AllDirectCollections"));
            double newDirectCollections = directCollections + Amnt;
            ps = conn.prepareStatement(SQLUpdatePlayersAllDirectCollections);
            ps.setDouble(1, newDirectCollections);
            ps.setString(2, UUID);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAllDirectPayments(String UUID, double Amnt, Connection connec){
        try{
            conn = connec;
            double directPayments = Double.parseDouble(selectFromPlayersByUUID(UUID, connec, "AllDirectPayments"));
            double newDirectPayments = directPayments + Amnt;
            ps = conn.prepareStatement(SQLUpdatePlayersAllDirectPayments);
            ps.setDouble(1, newDirectPayments);
            ps.setString(2, UUID);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateWorldSetNextBillingDate(String nextBillingDate, Connection connec){
        try{
            conn = connec;

            ps = conn.prepareStatement(SQLUpdateWorldSetNextBillingDate);
            ps.setString(1, nextBillingDate);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean setClaimsRenterUuidToNone(int ClaimId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLUpdateClaimsRenterToNone);
            ps.setString(1, "unrent");
            ps.setDouble(2, ClaimId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTotalBillingAmount(int BillingAmount, int BillId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLUpdateTotalBillingAmount);
            ps.setInt(1, BillingAmount);
            ps.setInt(2, BillId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBillingDirection(String Direction, int BillId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLUpdateBillDirection);
            ps.setString(1, Direction);
            ps.setInt(2, BillId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTotalLicenceFee(int LicenceFee, int BillId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLUpdateTotalLicenceFee);
            ps.setInt(1, LicenceFee);
            ps.setInt(2, BillId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTotalRentProfit(int RentProfit, int BillId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLUpdateTotalRentProfit);
            ps.setInt(1, RentProfit);
            ps.setInt(2, BillId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTotalRentExpenditure(int RentExpenditure, int BillId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SQlUpdateTotalRentExpenditure);
            ps.setInt(1, RentExpenditure);
            ps.setInt(2, BillId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCurrentRentPrice(int Rentprice, int PlotId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateCurrentRentPrice);
            ps.setInt(1, Rentprice);
            ps.setInt(2, PlotId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBillKeyOnRentBill(int ForeignBill, int ClaimId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateBillKeyOnRentings);
            ps.setInt(1, ForeignBill);
            ps.setInt(2, ClaimId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBillingAllDirectCollections(double DirectCollections, int BillingId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateBillingAllDirectCollections);
            ps.setDouble(1, DirectCollections);
            ps.setInt(2, BillingId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBillingAllDirectPayments(double DirectPayments, int BillingId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateBillingAllDirectPayments);
            ps.setDouble(1, DirectPayments);
            ps.setInt(2, BillingId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBillingAllRentIncome(double RentIncome, int BillingId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateBillingAllRentIncome);
            ps.setDouble(1, RentIncome);
            ps.setInt(2, BillingId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBillingAllRentExpenditure(double RentExpenditure, int BillingId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateBillingAllRentExpenditure);
            ps.setDouble(1, RentExpenditure);
            ps.setInt(2, BillingId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBillingAllShopSales(double ShopSales, int BillingId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateBillingAllShopSales);
            ps.setDouble(1, ShopSales);
            ps.setInt(2, BillingId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBillingAllShopBuys(double ShopBuys, int BillingId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateBillingAllShopBuys);
            ps.setDouble(1, ShopBuys);
            ps.setInt(2, BillingId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBillingAllUtilityExpenditure(double UtilityExpenditure, int BillingId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateBillingAllUtilityExpenditure);
            ps.setDouble(1, UtilityExpenditure);
            ps.setInt(2, BillingId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBillingTotalIncome(double TotalIncome, int BillingId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateBillingTotalIncome);
            ps.setDouble(1, TotalIncome);
            ps.setInt(2, BillingId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBillingTotalExpenditure(double TotalExpenditure, int BillingId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateBillingTotalExpenditure);
            ps.setDouble(1, TotalExpenditure);
            ps.setInt(2, BillingId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBillingBalanceBeforeTax(double BalanceBeforeTax, int BillingId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateBillingBalanceBeforeTax);
            ps.setDouble(1, BalanceBeforeTax);
            ps.setInt(2, BillingId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBillingBalanceAfterTax(double BalanceAfterTax, int BillingId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateBillingBalanceAfterTax);
            ps.setDouble(1, BalanceAfterTax);
            ps.setInt(2, BillingId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBillingTaxPercentage(double TaxPercentage, int BillingId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateBillingTaxPercentage);
            ps.setDouble(1, TaxPercentage);
            ps.setInt(2, BillingId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBillingLeftOverBalance(double LeftoverBalance, int BillingId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateBillingLeftoverBalance);
            ps.setDouble(1, LeftoverBalance);
            ps.setInt(2, BillingId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean resetBillKeyOnRenting(int ClaimId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlResetBillKeyOnRentings);
            ps.setInt(1, ClaimId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRentProcessingFlag(int Flag, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateRentProcessingFlag);
            ps.setInt(1, Flag);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateHighestBidder(int HighestBidderId, int ClaimId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateHighestBidder);
            ps.setInt(1, HighestBidderId);
            ps.setInt(2, ClaimId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public void insertShopBid(int ForeignClaimId, int Bid, String BidderUuid, String Biddername, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlInsertShopBid);
            ps.setInt(1, ForeignClaimId);
            ps.setInt(2, Bid);
            ps.setString(3, BidderUuid);
            ps.setString(4, Biddername);
            rowsAffected = ps.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void updateShopBid(int Bid, String Biddername, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateShopBid);
            ps.setInt(1, Bid);
            ps.setString(2, Biddername);
            rowsAffected = ps.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public boolean createPlot(int ClaimId, String CreationDate, int RenterId, int RenterLicence, int CurrentRentingPrice, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlInsertPlot);
            ps.setInt(1, RenterLicence);
            ps.setString(2, CreationDate);
            ps.setInt(3, RenterId);
            ps.setInt(4, CurrentRentingPrice);
            ps.setInt(5, ClaimId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }


    public boolean updatePlot(int RenterId, int CurrentRentingPrice, int PlotId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdatePlot);
            ps.setInt(1, RenterId);
            ps.setInt(2, CurrentRentingPrice);
            ps.setInt(3, PlotId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRentPrice(int RentPrice, int ClaimId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateRentPrice);
            ps.setInt(1, RentPrice);
            ps.setInt(2, ClaimId);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateMaxMoneyAmount(double newMaxMoneyAmount, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SqlUpdateMaxMoneyAmount);
            ps.setDouble(1, newMaxMoneyAmount);
            ps.setInt(2, 1);
            rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public double getBalanceOffline(OfflinePlayer player, Connection connec){
        double balance = -0.1;
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLSelectBalanceQuery);
            ps.setString(1, player.getUniqueId().toString());
            rs = ps.executeQuery();
            if (rs.next()) {
                balance = rs.getDouble("Balance");
            }else{
                balance = -2;
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return balance;
    }

    public boolean checkPlayerBalanceExistance(String player, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLSelectBalanceQuery);
            ps.setString(1, player);
            rs = ps.executeQuery();
            return rs.next();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateBalance(String player, double bal, String operation, Connection connec){
        try{
            conn = connec;
            double newBal;
            PreparedStatement ds = conn.prepareStatement(SQLSelectBalanceQuery);
            ds.setString(1,player);
            ts = ds.executeQuery();
            if (operation == "plus") {
                ts.next();
                newBal = ts.getDouble("Balance") + bal;
                ps = conn.prepareStatement(SQLUpdateBalance);
                ps.setDouble(1, newBal);
                ps.setString(2, player);
                ps.executeUpdate();
            }else if (operation == "minus"){
                ts.next();
                newBal = ts.getDouble("Balance") - bal;
                ps = conn.prepareStatement(SQLUpdateBalance);
                ps.setDouble(1, newBal);
                ps.setString(2, player);
                ps.executeUpdate();
            }
            if (ts.getDouble("Balance") < 0){
                return false;
            }else{
                return true;
            }
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRenter(int ClaimId, String renterUUID, String Enddate, int rentSum, String World,
                                Double X, Double Y, Double Z, String SignValue1, String SignValue2, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLUpdateRenter);
            ps.setString(1, renterUUID);
            ps.setString(2, Enddate);
            ps.setInt(3, rentSum);
            ps.setString(4, World);
            ps.setDouble(5, X);
            ps.setDouble(6, Y);
            ps.setDouble(7, Z);
            ps.setString(8, SignValue1);
            ps.setString(9, SignValue2);
            ps.setInt(10, ClaimId);
            rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0){
                return true;
            }else{
                return false;
            }
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean resetRenter(int ClaimId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLResetRenter);
            ps.setInt(1, ClaimId);
            rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0){
                return true;
            }else{
                return false;
            }
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean resetRenterToBe(int ClaimId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLResetRenterToBe);
            ps.setInt(1, ClaimId);
            rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0){
                return true;
            }else{
                return false;
            }
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean resetHighestBidder(int ClaimId, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLResetHighestBidder);
            ps.setInt(1, ClaimId);
            rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0){
                return true;
            }else{
                return false;
            }
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean registerNewTradeSetup(String signWorld, Double signX, Double signY, Double signZ,
                                      String owner, String sourceWorld, Double sourceX, Double sourceY,
                                      Double sourceZ, String invIndicString, String AmntString, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLInsertTradeData);
            ps.setString(1, owner);
            ps.setString(2, signWorld);
            ps.setDouble(3, signX);
            ps.setDouble(4, signY);
            ps.setDouble(5, signZ);
            ps.setString(6, sourceWorld);
            ps.setDouble(7, sourceX);
            ps.setDouble(8, sourceY);
            ps.setDouble(9, sourceZ);
            ps.setString(10, invIndicString);
            ps.setString(11, AmntString);
            rowsAffected = ps.executeUpdate();
            return (rowsAffected > 0);
        }catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean createBill(String PlayerUUID, Connection connec, String PeriodStart, String PeriodEnd){
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLInsertBill);
            ps.setString(1, PlayerUUID);
            ps.setInt(2, 1);
            ps.setString(3, PeriodStart);
            ps.setString(4, PeriodEnd);

            rowsAffected = ps.executeUpdate();
            return (rowsAffected > 0);
        }catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean simpleResetTradeSetup(String signWorld, Double signX, Double signY, Double signZ,
                                String owner, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLSimpleDeleteTradeData);
            ps.setString(1, signWorld);
            ps.setDouble(2, signX);
            ps.setDouble(3, signY);
            ps.setDouble(4, signZ);
            ps.setString(5, owner);
            rowsAffected = ps.executeUpdate();
            return (rowsAffected > 0);
        }catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public ResultSet getTradeSetup(String signWorld, int signX, int signY, int signZ, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLInspectTradeData);
            ps.setString(1, signWorld);
            ps.setDouble(2, signX);
            ps.setDouble(3, signY);
            ps.setDouble(4, signZ);
            rs = ps.executeQuery();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return rs;
    }

    public String getOwnerFromSign(String signWorld, int signX, int signY, int signZ, Connection connec){
        try{
            conn = connec;
            ps = conn.prepareStatement(SQLSelectTrade);
            ps.setString(1, signWorld);
            ps.setDouble(2, signX);
            ps.setDouble(3, signY);
            ps.setDouble(4, signZ);
            rs = ps.executeQuery();
            if (rs.next()){
                return rs.getString("OwnerUUID");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return "";
    }

    public boolean checkIfCreationInProgress(Player player, Connection connec){
        conn = connec;
        try{
            ps = conn.prepareStatement(SQLInspectCreation);
            ps.setString(1, player.getUniqueId().toString());
            rs=ps.executeQuery();
            return rs.next();

        }catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public ResultSet getCreationParameters(Player player, Connection connection){
        conn = connection;
        try{
            ps = conn.prepareStatement(SQLInspectCreation);
            ps.setString(1, player.getUniqueId().toString());
            rs = ps.executeQuery();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return rs;
    }

    public boolean openTradeCreationProcess(String playerUUID, String world, Double X, Double Y, Double Z, HashMap<Material, Integer> invIndic, Connection connection){
        try{
            for (Material k : invIndic.keySet()){
                if (CmaDelmRowAmnts == null || CmaDelmRowIndexes == null){
                    CmaDelmRowAmnts = String.valueOf(invIndic.get(k));
                    CmaDelmRowIndexes = String.valueOf(k);
                }else {
                    CmaDelmRowAmnts = CmaDelmRowAmnts + "," + invIndic.get(k);
                    CmaDelmRowIndexes = CmaDelmRowIndexes + "," + k;
                }
            }
            conn = connection;
            ps = conn.prepareStatement(SQLInsertCreation);
            ps.setString(1, playerUUID);
            ps.setString(2, world);
            ps.setDouble(3, X);
            ps.setDouble(4, Y);
            ps.setDouble(5, Z);
            ps.setString(6,CmaDelmRowIndexes);
            ps.setString(7, CmaDelmRowAmnts);
            rowsAffected = ps.executeUpdate();
            return (rowsAffected > 0);
        }catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean closeTradeCreationProcess(String playerUUID, Connection connection){
        try{
            conn = connection;
            ps = conn.prepareStatement(SQLDeleteCreation);
            ps.setString(1, playerUUID);
            rowsAffected = ps.executeUpdate();
            return (rowsAffected > 0);
        }catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }
}
