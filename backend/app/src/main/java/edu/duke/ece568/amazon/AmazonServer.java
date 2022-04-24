package edu.duke.ece568.amazon;

import com.google.protobuf.*;
import edu.duke.ece568.amazon.protocol.AmazonUps.*;
import edu.duke.ece568.amazon.protocol.WorldAmazon.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class AmazonServer {

  private SeqnumFactory seqnumFactory;
  private WorldOperator worldOperator;
  private UpsOperator upsOperator;
  private FrontendOperator frontendOperator;
  private WorldUpsSwitcher worldUpsSwitcher;

  /**
   * This constructs an amazon server
   */
  public AmazonServer() {
    seqnumFactory = new SeqnumFactory();
    worldOperator = new WorldOperator(seqnumFactory);
    upsOperator = new UpsOperator(seqnumFactory);
    try {
      frontendOperator = new FrontendOperator();
    } 
    catch (Exception e) {
       System.out.println("Create Frontend Operator: " + e);
    }
    worldUpsSwitcher = new WorldUpsSwitcher(worldOperator, upsOperator);
    worldOperator.setSwitcher(worldUpsSwitcher);
    upsOperator.setSwitcher(worldUpsSwitcher);
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
    new DatabaseOperator().cleanUpDatabase();
    System.out.println("Service start!");
    startService();
  }

  /**
   * This tries to enable amazon server's connection with world simulator and ups server 
   */
  public void getConnection() {
    while (true) {
      try{
        long worldid = upsOperator.getUpsConnection();
        System.out.println("Connect to UPS Successfully!");
        try{
          worldOperator.getWorldConnection(worldid);
          System.out.println("Connect to world Successfully!");
          break;
        }
        catch (IOException e) {
          //System.out.println("World connection: " + e);
          continue;
        }
      }
      catch (IOException e) {
        //System.out.println("UPS connection: " + e);
        continue;
      }    
    }
  }

  /**
   * This starts to deal with messages from world simulator, ups server and frontend website
   */
  public void startService() {
    Thread th1 = new Thread() {
      @Override()
      public void run() {
        dealFrontendMessage();
      }
    };
    th1.start();
    Thread th2 = new Thread() {
      @Override()
      public void run() {
        dealUpsMessage();
      }
    };
    th2.start();
    Thread th3 = new Thread() {
      @Override()
      public void run() {
        dealWorldMessage();
      }
    };
    th3.start();
  }

  /**
   * This deals with messages from frontend website
   */
  public void dealFrontendMessage() {
    while (true) {
      frontendOperator.handleFrontendMessage(worldOperator);
    }
  }

  /**
   * This deals with messages from ups server
   */
  public void dealUpsMessage() {
    while (true) {
      upsOperator.handleUpsMessage();
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
