package edu.duke.ece568.amazon;

import com.google.protobuf.*;
import edu.duke.ece568.amazon.protocol.AmazonUps.*;
import edu.duke.ece568.amazon.protocol.WorldAmazon.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class AmazonServer {

  private ThreadPoolExecutor myThreadPool;
  private SeqnumFactory seqnumFactory;

  private WorldOperator worldOperator;
  
  private ServerSocket upsListener;
  private UpsOperator upsOperator;

  private ServerSocket frontendListener;
  private FrontendOperator frontendOperator;

  /**
   * This constructs an amazon server
   */
  public AmazonServer() {
    myThreadPool = new ThreadPoolExecutor(25, 50, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(100));
    seqnumFactory = new SeqnumFactory();
    worldOperator = new WorldOperator(seqnumFactory);
    upsOperator = new UpsOperator(seqnumFactory);
    frontendOperator = new FrontendOperator(seqnumFactory);
  }

  /**
   * This starts an amazon server
   */
  public void runServer() {
    System.out.println("Ready to get connection!");
    while(true) {
      getConnection();
      break;
    }
    System.out.println("Service start!");
    startService();
    System.out.println("Finish Successfully!");
  }

  /**
   * This tries to enable amazon server's connection with world simulator and ups server 
   */
  public void getConnection() {
    while (true) {
      try{
        long worldid = upsOperator.getUpsConnection(upsListener);
        System.out.println("Connect to UPS Successfully!");
        try{
          worldOperator.getWorldConnection(worldid);
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

  /**
   * This starts to deal with messages from world simulator, ups server and frontend website
   */
  public void startService() {
    myThreadPool.prestartAllCoreThreads();
    dealFrontendMessage();
    dealUpsMessage();
    dealWorldMessage();
  }

  /**
   * This deals with messages from frontend website
   */
  public void dealFrontendMessage() {
    while (true) {
      try {
        frontendOperator.handleFrontendMessage(frontendListener);
      }
      catch (IOException e) {
        System.out.println("Message from frontend: " + e);
        continue;
      }
    }
  }

  /**
   * This deals with messages from ups server
   */
  public void dealUpsMessage() {
    while (true) {
      upsOperator.handleUpsMessage(upsListener);
    }
  }

   /**
   * This deals with messages from world simulator
   */
  public void dealWorldMessage() {
    while (true) {
      worldOperator.handleWorldMessage();
    }
  }

}
