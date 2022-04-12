package edu.duke.ece568.amazon;

import com.google.protobuf.*;
import edu.duke.ece568.amazon.protocol.AmazonUps.*;
import edu.duke.ece568.amazon.protocol.WorldAmazon.*;
import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;

public class UpsOperator {

  private final String upsHost = "vcm-24306.vm.duke.edu";
  private final int upsPort = 6666;
  private ServerSocket upsListener;
  private Socket upsSocket;
  private InputStream in;
  private OutputStream out;
  private SeqnumFactory seqnumFactory;
  
  /**
   * This constructs a UPS operator
   */
  public UpsOperator(SeqnumFactory seqnumFactory) {
    this.seqnumFactory = seqnumFactory;
  }

  /**
   * This tries to make a socket connection betweeen amazon server and the ups server 
   */
  public long getUpsConnection() throws IOException {
    while (true) {
      upsListener = new ServerSocket(upsPort);
      upsSocket = upsListener.accept();
      in = upsSocket.getInputStream();
      out = upsSocket.getOutputStream();
      UAConnect.Builder connectRequest = UAConnect.newBuilder();
      new MessageOperator().receiveMessage(connectRequest, input);
      if (connectRequest.hasWorldid()) {
        System.out.println("UPS connection: worldid is " + connectRequest.getWorldid());
        AUConnected.Builder connectResponse = AUConnected.newBuilder();
        connectResponse.setWorldConnectionStatus(true);
        connectResponse.setSeqnum(connectRequest.getSeqnum());
        new MessageOperator().sendMessage(connectResponse.build(), output);
        return connectRequest.getWorldid();
      }
    }
  }

  /**
   * This handles messages sent from the UPS server
   */
  public void handleUpsMessage(ServerSocket upsListener) {}

}
