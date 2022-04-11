package edu.duke.ece568.amazon;

import com.google.protobuf.*;
import edu.duke.ece568.amazon.protocol.AmazonUps.*;
import edu.duke.ece568.amazon.protocol.WorldAmazon.*;
import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;

public class FrontendOperator {

  private SeqnumFactory seqnumFactory;
  private final int frontendPort = 5678;

  public FrontendOperator(SeqnumFactory seqnumFactory) {
    this.seqnumFactory = seqnumFactory;
  }

  public void handleFrontendMessage(ServerSocket frontendListener) throws IOException {
    try {
      frontendListener = new ServerSocket(frontendPort);
      Socket frontendSocket = frontendListener.accept();
      if (frontendSocket != null) {
        InputStream input = frontendSocket.getInputStream();
        OutputStream output = frontendSocket.getOutputStream();
        ObjectInputStream objectInput = new ObjectInputStream(input);
        long packageId = objectInput.readLong();
        ObjectOutputStream objectOutput = new ObjectOutputStream(output);
        objectOutput.writeBytes("received!");
        objectOutput.flush();
        purchaseProduct(packageId, output);
      }
    }
    catch (IOException e) {
      System.out.println("Send message to world: " + e);
    }
  }

  public void purchaseProduct(long packageId, OutputStream out) throws IOException {
    APurchaseMore.Builder purchase = new DatabaseOperator().getPurchaseProduct(packageId);
    long seqnum = seqnumFactory.createSeqnum();
    purchase.setSeqnum(seqnum);
    ACommands.Builder command = ACommands.newBuilder();
    command.addBuy(purchase);
    command.setSimspeed(200);
    new MessageOperator().sendMessage(command.build(), out);
  }

}
