package edu.duke.ece568.amazon;

import com.google.protobuf.*;
import edu.duke.ece568.amazon.protocol.AmazonUps.*;
import edu.duke.ece568.amazon.protocol.WorldAmazon.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class AmazonServer {
  private final String worldHost = "vcm-24690.vm.duke.edu";
  private final int worldPort = 23456;
  private Socket worldSocket;
  private InputStream in;
  private OutputStream out;

  private Socket upsSocket;
  private final String upsHost = "vcm-24306.vm.duke.edu";
  private final int upsPort = 8888;

  private final int frontendPort = 6666;

  private List<AInitWarehouse> warehouseList;
  private long seqnum;

  private ThreadPoolExecutor myThreadPool;

  public AmazonServer() {
    seqnum = 1;
    myThreadPool = new ThreadPoolExecutor(50, 100, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    setupWarehouse();
  }

  public void setupWarehouse () {
    this.warehouseList = null;
  }

  public void getWorldConnection() throws IOException {
    this.worldSocket = new Socket(worldHost, worldPort);
    in = worldSocket.getInputStream();
    out = worldSocket.getOutputStream();
    AConnect.Builder connectRequest = AConnect.newBuilder();

  }

  public void runServer() {}
}
