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
  public void handleUpsMessage() {
    try {
      UACommand.Builder response = UACommand.newBuilder();
      new MessageOperator().receiveMessage(response, in);
      parseUpsMessage(response.build());
    }
    catch (IOException e) {
      System.out.println("Message from UPS: " + e);
    }
  }

  /**
   * This parses different kinds of messages contained in the response,
   * and pass them to specific methods for further operations 
   */
  public void parseUpsMessage(UACommand message) throws IOException {
    System.out.println("Message from UPS: " + message);
    List<UAReadyForPickup> pickupReadyList = message.getPickupReadyList();
    for (UAReadyForPickup ready : pickupReadyList) {
      handleArrivedTruck(ready);
    }

    List<UAPackageDelivered> deliveredList = message.getPackageDeliveredList();
    for (UAPackageDelivered delivered : deliveredList) {
      handleDeliveredPackage(delivered);
    }

    List<UAIsAssociated> resultList = message.getLinkResultList();
    for (UAIsAssociated result : resultList) {
      handleLinkResult(result);
    }

    List<Err> errorList = message.getErrorList();
    for (Err error : errorList) {
      System.out.println("Message from UPS: " + error.getErrorInfo());
    }
  }

  /**
   * This asks the UPS server for package pick-up
   */
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
   * This handles packages on an arrived truck
   */
  public void handleArrivedTruck(UAReadyForPickup ready) {
    int truckId = ready.getTruckid();
    List<AUPack> aupackageList = ready.getPackagesList();
    for (AUPack aupack : aupackageList) {
      long packageId = aupack.getPackage().getShipid();
      new DatabaseOperator().updateTruckId(packageId, truckId);
      String status = new DatabaseOperator().getPackageStatus(packageId);
      if (status.equals("packed")) {
        switcher.requestLoadPackage(packageId, truckId);
      }
    }
  }

  /**
   * This handles delivered packages
   */
  public void handleDeliveredPackage(UAPackageDelivered delivered) {
    long packageId = delivered.getPackageid();
    new DatabaseOperator().updatePackageStatus(packageId, "delivered");
  }

  /**
   * This handles the result of UPS accout verification
   */
  public void handleLinkResult(UAIsAssociated result) {
    long packageId = result.getPackageid();
    boolean valid = result.getCheckResult();
    String upsAccount = new DatabaseOperator().getUpsAccount(packageId);
    if (!upsAccount.isEmpty()) {
      if (valid) {
        new DatabaseOperator().updateUpsAccount(packageId, upsAccount);
      }
      else {
        new DatabaseOperator().updateUpsAccount(packageId, "invalid account");
      }
    }
  }

  public void deliverTruck(int truckId) {
    AUReadyForDelivery.Builder delivery = AUReadyForDelivery.newBuilder();
    delivery.setTruckid(truckId);
    long seqnum = seqnumFactory.createSeqnum();
    delivery.setSeqnum(seqnum);

    AUCommand.Builder command = AUCommand.newBuilder();
    command.addDeliveryReady(delivery);
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
