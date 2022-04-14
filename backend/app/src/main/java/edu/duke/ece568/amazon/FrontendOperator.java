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
  public FrontendOperator() {}

  /**
   * This handles messages sent from the frontend,
   * and return the package ID if received
   */
  public long handleFrontendMessage() throws IOException {
    try {
      frontendListener = new ServerSocket(frontendPort);
      Socket frontendSocket = frontendListener.accept();
      if (frontendSocket != null) {
        InputStream input = frontendSocket.getInputStream();
        OutputStream output = frontendSocket.getOutputStream();
        ObjectInputStream objectInput = new ObjectInputStream(input);
        long packageId = objectInput.readLong();
        ObjectOutputStream objectOutput = new ObjectOutputStream(output);
        objectOutput.writeLong(packageId);
        objectOutput.flush();
        frontendSocket.close();
        return packageId;
      }
    }
    catch (IOException e) {
      System.out.println("Message from frontend: " + e);
      return -1;
    }
    return -1;
  }

}
