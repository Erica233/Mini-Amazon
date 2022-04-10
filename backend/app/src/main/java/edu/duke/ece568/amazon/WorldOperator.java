package edu.duke.ece568.amazon;

import com.google.protobuf.*;
import edu.duke.ece568.amazon.protocol.AmazonUps.*;
import edu.duke.ece568.amazon.protocol.WorldAmazon.*;
import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;

public class WorldOperator {

  private final String worldHost = "vcm-24690.vm.duke.edu";
  private final int worldPort = 23456;

  public WorldOperator() {}

  /**
   * This tries to make a socket connection betweeen amazon server and the world simulator 
   */
  public void getWorldConnection(long worldid, Socket worldSocket, InputStream in, OutputStream out) throws IOException {
    while(true) {
      worldSocket = new Socket(worldHost, worldPort);
      in = worldSocket.getInputStream();
      out = worldSocket.getOutputStream();
      AConnect.Builder connectRequest = AConnect.newBuilder();
      connectRequest.setWorldid(worldid);
      List<AInitWarehouse> warehouseList = new DatabaseOperator().getWarehouse();
      connectRequest.addAllInitwh(warehouseList);
      connectRequest.setIsAmazon(true);
      AConnected.Builder connectResponse = AConnected.newBuilder();
      new MessageOperator().sendMessage(connectRequest.build(), out);
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

}
