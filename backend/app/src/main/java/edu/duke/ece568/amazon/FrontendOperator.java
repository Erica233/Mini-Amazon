package edu.duke.ece568.amazon;

import com.google.protobuf.*;
import edu.duke.ece568.amazon.protocol.AmazonUps.*;
import edu.duke.ece568.amazon.protocol.WorldAmazon.*;
import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class FrontendOperator {

  private final int frontendPort = 5678;
  private ServerSocket frontendListener;
  
  /**
   * This constructs a frontend operator
   */
  public FrontendOperator() throws IOException {
    frontendListener = new ServerSocket(frontendPort);
  }

  /**
   * This handles messages sent from the frontend,
   * and return the package ID if received
   */
  public void handleFrontendMessage(WorldOperator worldOperator) {
    try {
      Socket frontendSocket = frontendListener.accept();
      Thread th = new Thread() {
        @Override()
        public void run() {
          try {
            InputStream in = frontendSocket.getInputStream();
            InputStreamReader inputReader = new InputStreamReader(in);
            BufferedReader bufferReader = new BufferedReader(inputReader);
            String buffer = bufferReader.readLine();
            int packageId = Integer.parseInt(buffer);
            System.out.println("Receive package " + packageId + " from frontend");
            worldOperator.purchaseProduct(packageId);
          } 
          catch (IOException e) {
            System.out.println("Message from frontend: " + e);
          }
        }
      };
      th.start();
    }
    catch (IOException e) {
      System.out.println("Message from frontend: " + e);
    }
  }
}
