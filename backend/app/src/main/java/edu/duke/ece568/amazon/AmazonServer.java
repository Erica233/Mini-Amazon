package edu.duke.ece568.amazon;

import com.google.protobuf.*;
import edu.duke.ece568.amazon.protocol.AmazonUps.*;
import edu.duke.ece568.amazon.protocol.WorldAmazon.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class AmazonServer {
  private final String worldHost = "vcm-24690.vm.duke.edu";
  private final int worldPort = 23456;
  private Socket worldSocket;
  private InputStream in;
  private OutputStream out;

  private Socket upsSocket;
  private final String upsHost = "vcm-24306.vm.duke.edu";
  private final int upsPort = 8888;

  private final int frontendPort = 6666;

  private final List<AInitWarehouse> warehouseList;
  private long seqnum;

  private ThreadPoolExecutor myThreadPool;

  /**
   * This constructs an amazon server
   */
  public AmazonServer() {
    seqnum = 1;
    myThreadPool = new ThreadPoolExecutor(50, 100, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    warehouseList = new databaseOperator().getWarehouse();
  }

  public void runServer() {
    while(true) {
      try{
        getWorldConnection();
        break;
      }
      catch (Exception e) {
        System.out.println("World connection: " + e);
      }
    }
    System.out.println("Finish Successfully!");
  }

  /**
   * This sends a goolge protocol buffer message through socket,
   * adjusts from the C++ version example provided in the project pdf
   */ 
  public <T extends GeneratedMessageV3> boolean sendMessage(T message, OutputStream output) throws IOException { 
    try {
      byte[] rawData = message.toByteArray();
      int size = rawData.length;
      CodedOutputStream codedOutput = CodedOutputStream.newInstance(output);
      codedOutput.writeUInt32NoTag(size); // writeRawVarint32() is deprecated, use writeUInt32NoTag() instead
      message.writeTo(codedOutput);
      codedOutput.flush();
      return true;
    }
    catch (IOException e) {
      System.out.println("Send message: " + e);
      return false;
    }
  }

  /**
   * This receives a goolge protocol buffer message through socket,
   * adjusts from the C++ versin example provided in the project pdf
   */ 
  public <T extends GeneratedMessageV3.Builder<?>> boolean receiveMessage(T message, InputStream input) throws IOException {
    try {
      CodedInputStream codedInput = CodedInputStream.newInstance(input);
      int size = codedInput.readRawVarint32();
      int limit = codedInput.pushLimit(size);
      message.mergeFrom(codedInput);
      codedInput.popLimit(limit);
      return true;
    }
    catch (IOException e) {
      System.out.println("Receive message: " + e);
      return false;
    }
  }

  /**
   * This tries to make a socket connection betweeen amazon server and the world simulator 
   */
  public void getWorldConnection() throws IOException {
    this.worldSocket = new Socket(worldHost, worldPort);
    in = worldSocket.getInputStream();
    out = worldSocket.getOutputStream();
    AConnect.Builder connectRequest = AConnect.newBuilder();
    //connectRequest.setWorldid(1);
    connectRequest.addAllInitwh(warehouseList);
    connectRequest.setIsAmazon(true);
    AConnected.Builder connectResponse = AConnected.newBuilder();
    sendMessage(connectRequest.build(), out);
    receiveMessage(connectResponse, in);
    System.out.println("World connection: worldid is " + connectResponse.getWorldid());
    System.out.println("World connection: " + connectResponse.getResult());
  }
}
