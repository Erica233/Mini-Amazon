package edu.duke.ece568.amazon;

import com.google.protobuf.*;
import edu.duke.ece568.amazon.protocol.AmazonUps.*;
import edu.duke.ece568.amazon.protocol.WorldAmazon.*;
import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class WorldOperator {

  private final String worldHost = "vcm-24690.vm.duke.edu";
  private final int worldPort = 23456;
  private Socket worldSocket;
  private InputStream in;
  private OutputStream out;
  private WorldUpsSwitcher switcher;
  private SeqnumFactory seqnumFactory;
  private ConcurrentHashMap<Long, APurchaseMore> purchasingProduct;
  private ConcurrentHashMap<Long, ScheduledExecutorService> runningService;
  private ConcurrentHashMap<Long, ScheduledFuture> runningFuture;
  private Set<Long> ackedSeqnum;
  
  /**
   * This constructs a world operator
   */
  public WorldOperator(SeqnumFactory seqnumFactory) {
    this.seqnumFactory = seqnumFactory;
    purchasingProduct = purchasingProduct = new ConcurrentHashMap<> ();
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
   * This tries to make a socket connection betweeen amazon server and the world simulator 
   */
  public void getWorldConnection(long worldid) throws IOException {
    while(true) {
      worldSocket = new Socket(worldHost, worldPort);
      in = worldSocket.getInputStream();
      out = worldSocket.getOutputStream();
      AConnect.Builder connectRequest = AConnect.newBuilder();
      connectRequest.setWorldid(worldid);
      List<AInitWarehouse> warehouseList = new DatabaseOperator().getWarehouseList();
      connectRequest.addAllInitwh(warehouseList);
      connectRequest.setIsAmazon(true);
      new MessageOperator().sendMessage(connectRequest.build(), out);
      AConnected.Builder connectResponse = AConnected.newBuilder();
      new MessageOperator().receiveMessage(connectResponse, in);
      System.out.println("World connection: worldid is " + connectResponse.getWorldid());
      System.out.println("World connection: " + connectResponse.getResult());
      String result = connectResponse.getResult();
      if (result.equals("connected!")) {
        return;
      }
      else {
        continue;
      }
    }
  }

  /**
   * This handles messages sent from the world simulator
   */
  public void handleWorldMessage() {
    try {
      AResponses.Builder response = AResponses.newBuilder();
      new MessageOperator().receiveMessage(response, in);
      Thread th = new Thread() {
       @Override()
        public void run() {
          try {
            parseWorldMessage(response.build());
          }
          catch (IOException e) {
            System.out.println("Message from world: " + e);
          }
        }
      };
      th.start();
    }
    catch (IOException e) {
      //System.out.println("Message from world: " + e);
    }
  }

  /**
   * This parses different kinds of messages contained in the response,
   * and pass them to specific methods for further operations 
   */
  public void parseWorldMessage(AResponses message) throws IOException {
    List<Long> acksList = message.getAcksList();
    for (long ack : acksList) {
      handleAcks(ack);
    }

    List<APurchaseMore> arrivedList = message.getArrivedList();
    for (APurchaseMore arrived : arrivedList) {
      handleArrivedPackage(arrived);
    }

    List<APacked> readyList = message.getReadyList();
    for (APacked ready : readyList) {
      handleReadyPackage(ready);
    }
    
    List<ALoaded> loadedList = message.getLoadedList();
    for (ALoaded loaded : loadedList) {
      handleLoadedPackage(loaded);
    }

    List<AErr> errorList = message.getErrorList();
    for (AErr error : errorList) {
      System.out.println("Message from world: " + error.getErr());
      ACommands.Builder command = ACommands.newBuilder();
      command.addAcks(error.getSeqnum());
      sendAcksToWorld(command);
    }
    
    if (message.hasFinished()) {
      System.out.println("Disconnect to world!");
    }

    List<APackage> packageStatusList = message.getPackagestatusList();
    for (APackage packageStatus :packageStatusList) {
      if (!ackedSeqnum.contains(packageStatus.getSeqnum())) {
        ackedSeqnum.add(packageStatus.getSeqnum());
        long packageId = packageStatus.getPackageid();
        String status = packageStatus.getStatus();
        new DatabaseOperator().updatePackageStatus(packageId, status);
      }
      ACommands.Builder command = ACommands.newBuilder();
      command.addAcks(packageStatus.getSeqnum());
      sendAcksToWorld(command);
    }
  }

  /**
   * This purchases required products from the world simulator
   */
  public void purchaseProduct(long packageId) throws IOException {
    Thread th = new Thread() {
      @Override()
      public void run() {
        APurchaseMore.Builder purchase = new DatabaseOperator().getPurchaseProduct(packageId);
        long seqnum = seqnumFactory.createSeqnum();
        purchase.setSeqnum(seqnum);
        purchasingProduct.put(packageId, purchase.build());
        ACommands.Builder command = ACommands.newBuilder();
        command.addBuy(purchase.build());
        sendMessageToWorld(seqnum, command);
        System.out.println("Purchasing package " + packageId);
      }
    };
    th.start();
  }

  /**
   * This handles arrived packages
   */
  public void handleArrivedPackage(APurchaseMore arrived) {
    if (!ackedSeqnum.contains(arrived.getSeqnum())) {
      ackedSeqnum.add(arrived.getSeqnum());
      synchronized (purchasingProduct) {
        long packageId = -1;
        ConcurrentHashMap.KeySetView<Long, APurchaseMore> keySet = purchasingProduct.keySet();
        Iterator<Long> it = keySet.iterator();
        while(it.hasNext()) {
          long id = it.next();
          APurchaseMore purchase = purchasingProduct.get(id);
          if (purchase.getWhnum() == arrived.getWhnum() && purchase.getThingsList().equals(arrived.getThingsList())) {
            packageId = id;
            purchasingProduct.remove(id);
            break;
          }
          else {
            continue;
          }
        }
        if (packageId == -1) {
          System.out.println("Purchased package: package not Found!");
        }
        else {
          new DatabaseOperator().updatePackageStatus(packageId, "purchased");
          System.out.println("Package " + packageId + " is purchased");
          switcher.requestPickPackage(packageId, arrived);
          packPackage(packageId, arrived);
        }
      }
    }
    ACommands.Builder command = ACommands.newBuilder();
    command.addAcks(arrived.getSeqnum());
    sendAcksToWorld(command);
  }

  /**
   * This asks the world simulator for package packing
   */
  public void packPackage(long packageId, APurchaseMore arrived) {
    Thread th = new Thread() {
      @Override()
      public void run() {
        int whnum = arrived.getWhnum();
        List<AProduct> things = arrived.getThingsList();
        long seqnum = seqnumFactory.createSeqnum();
        APack.Builder topack = APack.newBuilder();
        topack.setWhnum(whnum);
        topack.addAllThings(things);
        topack.setShipid(packageId);
        topack.setSeqnum(seqnum);
        ACommands.Builder command = ACommands.newBuilder();
        command.addTopack(topack.build());
        sendMessageToWorld(seqnum, command);
        new DatabaseOperator().updatePackageStatus(packageId, "packing");
        System.out.println("Packing package " + packageId);
      }
    };
    th.start();
  }

  /**
   * This handles packed packages
   */
  public void handleReadyPackage(APacked ready) {
    if (!ackedSeqnum.contains(ready.getSeqnum())) {
      ackedSeqnum.add(ready.getSeqnum());
      long packageId = ready.getShipid();
      new DatabaseOperator().updatePackageStatus(packageId, "packed");
      System.out.println("Package " + packageId + " is packed");
      int truckId = new DatabaseOperator().getTruckId(packageId);
      if (truckId != -1) {
        loadPackage(packageId, truckId);
      }
    }
    ACommands.Builder command = ACommands.newBuilder();
    command.addAcks(ready.getSeqnum());
    sendAcksToWorld(command);
  }

  /**
   * This asks the world simulator for package loading
   */
  public void loadPackage(long packageId, int truckId) {
    Thread th = new Thread() {
      @Override()
      public void run() {
        int whnum = new DatabaseOperator().getWhnum(packageId);
        long seqnum = seqnumFactory.createSeqnum();
        APutOnTruck.Builder toload = APutOnTruck.newBuilder();
        toload.setWhnum(whnum);
        toload.setTruckid(truckId);
        toload.setShipid(packageId);
        toload.setSeqnum(seqnum);
        ACommands.Builder command = ACommands.newBuilder();
        command.addLoad(toload.build());
        sendMessageToWorld(seqnum, command);
        new DatabaseOperator().updatePackageStatus(packageId, "loading");
        System.out.println("Loading package " + packageId);
      }
    };
    th.start();
  }

  /**
   * This handles loaded packages
   */
  public void handleLoadedPackage(ALoaded loaded) {
    if (!ackedSeqnum.contains(loaded.getSeqnum())) {
      ackedSeqnum.add(loaded.getSeqnum());
      long packageId = loaded.getShipid();
      new DatabaseOperator().updatePackageStatus(packageId, "loaded");
      System.out.println("Package " + packageId + " is loaded");
      int truckId = new DatabaseOperator().getTruckId(packageId);
      if (new DatabaseOperator().checkAllPackagesLoaded(truckId)) {
        switcher.requestDelivery(truckId);
      }
    }
    ACommands.Builder command = ACommands.newBuilder();
    command.addAcks(loaded.getSeqnum());
    sendAcksToWorld(command);
  }

  /**
   * This queries the status of a package
   */
  public void queryPackage(int packageId) {
    Thread th = new Thread() {
      @Override()
      public void run() {
        AQuery.Builder query = AQuery.newBuilder();
        query.setPackageid(packageId);
        long seqnum = seqnumFactory.createSeqnum();
        query.setSeqnum(seqnum);
        ACommands.Builder command = ACommands.newBuilder();
        command.addQueries(query.build());
        sendMessageToWorld(seqnum, command);
      }
    };
    th.start();
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
   * This sends commands to the world simulator
   */
  public void sendMessageToWorld(long seqnum, ACommands.Builder message) {
    Runnable send = () -> {
      synchronized(out) {
        try {
          new MessageOperator().sendMessage(message.build(), out);
        }
        catch (IOException e) {
          System.out.println("Send message to world: " + e);
        }
      }
    };
    ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
    ScheduledFuture<?> future = service.scheduleAtFixedRate(send, 1, 20, TimeUnit.SECONDS);
    runningService.put(seqnum, service);
    runningFuture.put(seqnum, future);
  }

  /**
   * This sends acks to the world simulator
   */
  public void sendAcksToWorld(ACommands.Builder message) {
    synchronized(out) {
      try {
        new MessageOperator().sendMessage(message.build(), out);
      }
      catch (IOException e) {
        System.out.println("Send message to world: " + e);
      }
    }    
  }
}
