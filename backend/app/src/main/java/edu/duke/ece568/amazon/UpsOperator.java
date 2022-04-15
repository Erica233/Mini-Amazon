package edu.duke.ece568.amazon;

import com.google.protobuf.*;
import edu.duke.ece568.amazon.protocol.AmazonUps.*;
import edu.duke.ece568.amazon.protocol.WorldAmazon.*;
import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;

public class UpsOperator {

  private final int upsPort = 6666;
  private ServerSocket upsListener;
  private Socket upsSocket;
  private InputStream in;
  private OutputStream out;
  private WorldUpsSwitcher switcher;
  private SeqnumFactory seqnumFactory;
  
  /**
   * This constructs a UPS operator
   */
  public UpsOperator(SeqnumFactory seqnumFactory) {
    this.seqnumFactory = seqnumFactory;
  }

  /**
   * This sets the world-UPS switcher
   */
  public void setSwitcher(WorldUpsSwitcher switcher) {
    this.switcher = switcher;
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
      new MessageOperator().receiveMessage(connectRequest, in);
      if (connectRequest.hasWorldid()) {
        System.out.println("UPS connection: worldid is " + connectRequest.getWorldid());
        AUConnected.Builder connectResponse = AUConnected.newBuilder();
        connectResponse.setWorldConnectionStatus(true);
        connectResponse.setSeqnum(connectRequest.getSeqnum());
        new MessageOperator().sendMessage(connectResponse.build(), out);  
        return connectRequest.getWorldid();
      }
    }
  }

  /**
   * This handles messages sent from the UPS server
   */
  public void handleUpsMessage() {}

  public void pickPackage(long packageId, APurchaseMore arrived) {
    APack.Builder pack = APack.newBuilder();
    int whnum = arrived.getWhnum();
    List<AProduct> things = arrived.getThingsList();
    pack.setWhnum(whnum);
    pack.addAllThings(things);
    pack.setShipid(packageId);
    pack.setSeqnum(arrived.getSeqnum());

    AUPack.Builder auPack = AUPack.newBuilder();
    String upsAccount = new DatabaseOperator().getUpsAccount(packageId);
    int destx = new DatabaseOperator().getDestx(packageId);
    int desty = new DatabaseOperator().getDesty(packageId);
    auPack.setPackage(pack);
    auPack.setUpsAccount(upsAccount);
    auPack.setDestx(destx);
    auPack.setDesty(desty);
    
    AURequestPickup.Builder request = AURequestPickup.newBuilder();
    request.setPack(auPack);
    long seqnum = seqnumFactory.createSeqnum();
    request.setSeqnum(seqnum);

    AUCommand.Builder command = AUCommand.newBuilder();
    command.addPickupRequest(request);
    sendMessageToUps(seqnum, command);
  }

  /**
   * This sends commands to the UPS server
   */
  public synchronized void sendMessageToUps(long seqnum, AUCommand.Builder message) {
    try {
        new MessageOperator().sendMessage(message.build(), out);
      }
      catch (IOException e) {
        System.out.println("Send message to world: " + e);
      }
  }
}
