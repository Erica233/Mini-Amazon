package edu.duke.ece568.amazon;

import com.google.protobuf.*;
import edu.duke.ece568.amazon.protocol.AmazonUps.*;
import edu.duke.ece568.amazon.protocol.WorldAmazon.*;
import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class UpsOperator {

  private final int upsPort = 6666;
  private ServerSocket upsListener;
  private Socket upsSocket;
  private InputStream in;
  private OutputStream out;
  private WorldUpsSwitcher switcher;
  private SeqnumFactory seqnumFactory;
  private ConcurrentHashMap<Long, ScheduledExecutorService> runningService;
  private ConcurrentHashMap<Long, ScheduledFuture> runningFuture;
  private Set<Long> ackedSeqnum;
  
  /**
   * This constructs a UPS operator
   */
  public UpsOperator(SeqnumFactory seqnumFactory) {
    this.seqnumFactory = seqnumFactory;
    runningService = new ConcurrentHashMap<> ();
    runningFuture = new ConcurrentHashMap<> ();
    Set<Long> set = new HashSet<Long>();
    ackedSeqnum = Collections.synchronizedSet(set);
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
      Thread th = new Thread() {
        @Override()
        public void run() {
          try {
            parseUpsMessage(response.build());
          }
          catch (IOException e) {
            System.out.println("Message from UPS: " + e);
          }
        }
      };
      th.start();
    }
    catch (IOException e) {
      //System.out.println("Message from UPS: " + e);
    }
  }

  /**
   * This parses different kinds of messages contained in the response,
   * and pass them to specific methods for further operations 
   */
  public void parseUpsMessage(UACommand message) throws IOException {
    List<Long> acksList = message.getAcksList();
    for (long ack : acksList) {
      handleAcks(ack);
    }

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
      AUCommand.Builder command = AUCommand.newBuilder();
      command.addAcks(error.getErrorSeqnum());
      sendAcksToUps(command);
    }
  }

  /**
   * This asks the UPS server for package pick-up
   */
  public void pickPackage(long packageId, APurchaseMore arrived) {
    Thread th = new Thread() {
      @Override()
      public void run() {
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
        System.out.println("Request for picking package " + packageId);
      }
    };
    th.start();
  }

  /**
   * This handles packages on an arrived truck
   */
  public void handleArrivedTruck(UAReadyForPickup ready) {
    if (!ackedSeqnum.contains(ready.getSeqnum())) {
      ackedSeqnum.add(ready.getSeqnum());
      int truckId = ready.getTruckid();
      System.out.println("Truck " + truckId + " is arrived");
      List<Long> packageIdList = ready.getPackageidList();
      for (Long packageId : packageIdList) {
        new DatabaseOperator().updateTruckId(packageId, truckId);
        String status = new DatabaseOperator().getPackageStatus(packageId);
        if (status.equals("packed")) {
          switcher.requestLoadPackage(packageId, truckId);
        }
      }
    }
    AUCommand.Builder command = AUCommand.newBuilder();
    command.addAcks(ready.getSeqnum());
    sendAcksToUps(command);
  }

  /**
   * This handles delivered packages
   */
  public void handleDeliveredPackage(UAPackageDelivered delivered) {
    if (!ackedSeqnum.contains(delivered.getSeqnum())) {
      ackedSeqnum.add(delivered.getSeqnum());
      long packageId = delivered.getPackageid();
      new DatabaseOperator().updatePackageStatus(packageId, "delivered");
      System.out.println("Package " + packageId + " is delivered");
    }
    AUCommand.Builder command = AUCommand.newBuilder();
    command.addAcks(delivered.getSeqnum());
    sendAcksToUps(command);
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
        new DatabaseOperator().updateUpsAccount(packageId, "Invalid Account");
      }
    }
    System.out.println("UPS account of package " + packageId + " is checked");
  }

  /**
   * This asks the UPS server to send the truck for delivery
   */
  public void deliverTruck(int truckId) {
    Thread th = new Thread() {
      @Override()
      public void run() {
        AUReadyForDelivery.Builder delivery = AUReadyForDelivery.newBuilder();
        delivery.setTruckid(truckId);
        long seqnum = seqnumFactory.createSeqnum();
        delivery.setSeqnum(seqnum);

        AUCommand.Builder command = AUCommand.newBuilder();
        command.addDeliveryReady(delivery);
        sendMessageToUps(seqnum, command);
      }
    };
    th.start();
    new DatabaseOperator().updateDeliveringStatus(truckId);
    System.out.println("Truck " + truckId + " start delivering");
  }

  /**
   * This stops a repetitive message sending thread with received ack number
   */
  public void handleAcks(long ack) {
    if (runningFuture.containsKey(ack)) {
      runningFuture.get(ack).cancel(true);
      runningFuture.remove(ack);
    }
    if (runningService.containsKey(ack)) {
      runningService.get(ack).shutdown();
      runningService.remove(ack);
    }
  }

  /**
   * This sends commands to the UPS server
   */
  public void sendMessageToUps(long seqnum, AUCommand.Builder message) {
    Runnable send = () -> {
      synchronized(out) {
        try {
          new MessageOperator().sendMessage(message.build(), out);
        }
        catch (IOException e) {
          System.out.println("Send message to UPS: " + e);
        }
      }
    };
    ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
    ScheduledFuture<?> future = service.scheduleAtFixedRate(send, 1, 20, TimeUnit.SECONDS);
    runningService.put(seqnum, service);
    runningFuture.put(seqnum, future);
  }

  /**
   * This sends acks to the UPS server
   */
  public void sendAcksToUps(AUCommand.Builder message) {
    synchronized(out) {
      try {
        new MessageOperator().sendMessage(message.build(), out);
      }
      catch (IOException e) {
        System.out.println("Send message to UPS: " + e);
      }
    }    
  }
}
