package edu.duke.ece568.amazon;

import com.google.protobuf.*;
import edu.duke.ece568.amazon.protocol.AmazonUps.*;
import edu.duke.ece568.amazon.protocol.WorldAmazon.*;
import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;

public class WorldOperator {

  private SeqnumFactory seqnumFactory;
  private final String worldHost = "vcm-24690.vm.duke.edu";
  private final int worldPort = 23456;
  private Socket worldSocket;
  private InputStream in;
  private OutputStream out;
  

  public WorldOperator(SeqnumFactory seqnumFactory) {
    this.seqnumFactory = seqnumFactory;
  }

  /**
   * This tries to make a socket connection betweeen amazon server and the world simulator 
   */
  public void getWorldConnection(long worldid) throws IOException {
    while(true) {
      worldSocket = new Socket(worldHost, worldPort);
      in = worldSocket.getInputStream();
      out = worldSocket.getOutputStream();
      AConnect.Builder connectRequest = AConnect.newBuilder();
      connectRequest.setWorldid(worldid);
      List<AInitWarehouse> warehouseList = new DatabaseOperator().getWarehouse();
      connectRequest.addAllInitwh(warehouseList);
      connectRequest.setIsAmazon(true);
      new MessageOperator().sendMessage(connectRequest.build(), out);
      AConnected.Builder connectResponse = AConnected.newBuilder();
      new MessageOperator().receiveMessage(connectResponse, in);
      System.out.println("World connection: worldid is " + connectResponse.getWorldid());
      System.out.println("World connection: " + connectResponse.getResult());
      String result = connectResponse.getResult();
      if (result.equals("connected!")) {
        return;
      }
      else {
        continue;
      }
    }
  }

  public void handleWorldMessage() {
    try {
      AResponses.Builder response = AResponses.newBuilder();
      new MessageOperator().receiveMessage(response, in);
      parseWorldMessage(response.build());
    }
    catch (IOException e) {
      System.out.println("Message from world: " + e);
    }
  }

  public void parseWorldMessage(AResponses message) throws IOException {
    System.out.println("Message from world: " + message);
    List<APurchaseMore> arrivedList = message.getArrivedList();
    for (APurchaseMore arrived : arrivedList) {
      packAndPickPackage(arrived);
    }
    List<APacked> readyList = message.getReadyList();
    List<ALoaded> loadedList = message.getLoadedList();
    List<AErr> errorList = message.getErrorList();
    for (AErr error : errorList) {
      System.out.println("Message from world: " + error.getErr());
    }
    List<Long> acksList = message.getAcksList();
    List<APackage> packagestatusList = message.getPackagestatusList();
    if (message.hasFinished()) {
      System.out.println("Disconnect to world!");
    }
  }

  public void packAndPickPackage(APurchaseMore arrived) {

  }
}
