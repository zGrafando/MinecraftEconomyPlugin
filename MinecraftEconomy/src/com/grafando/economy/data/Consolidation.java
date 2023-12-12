package com.grafando.economy.data;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.*;

public class Consolidation {

    private File Logfile;
    private File Logfolder;

    public void createDirectory() {
        // Logfolder = new File("C:\\Users\\Pius\\Desktop\\Plugins\\Server\\Files\\TradingLogs");
        if (!Logfolder.exists()) {
            // Logfolder.mkdir();
        }
    }

    public void writeTradeFile(String content) {
    }

    public void writeTransactionFile(String content) {
    }
}