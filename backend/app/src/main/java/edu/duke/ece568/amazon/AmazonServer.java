package edu.duke.ece568.amazon;

import com.google.protobuf.*;
import edu.duke.ece568.amazon.protocol.AmazonUps.*;
import edu.duke.ece568.amazon.protocol.WorldAmazon.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class AmazonServer {
  
  private Socket worldSocket;
  private InputStream in;
  private OutputStream out;

  private ServerSocket upsListener;

  private ServerSocket frontendListener;

  private long seqnum;

  private ThreadPoolExecutor myThreadPool;

  /**
   * This constructs an amazon server
   */
  public AmazonServer() {
    seqnum = 1;
    myThreadPool = new ThreadPoolExecutor(25, 50, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(100));
  }

  /**
   * This starts an amazon server
   */
  public void runServer() {
    while(true) {
      try{
        getConnection();
        break;
      }
      catch (Exception e) {
        System.out.println("Connection: " + e);
      }
    }
    startService();
    System.out.println("Finish Successfully!");
  }

  public void getConnection() throws IOException {
    while (true) {
      try{
        long worldid = new UpsOperator().getUpsConnection(upsListener);
        System.out.println("Connect to UPS Successfully!");
        try{
          new WorldOperator().getWorldConnection(worldid, worldSocket, in, out);
          System.out.println("Connect to world Successfully!");
          break;
        }
        catch (IOException e) {
          System.out.println("World connection: " + e);
          continue;
        }
      }
      catch (IOException e) {
        System.out.println("UPS connection: " + e);
        continue;
      }     
    }
  }

  public void startService() {
    myThreadPool.prestartAllCoreThreads();
    dealFrontendMessage();
    dealUpsMessage();
    dealWorldMessage();
  }

  public void dealFrontendMessage() {}

  public void dealUpsMessage() {}

  public void dealWorldMessage() {}

}
