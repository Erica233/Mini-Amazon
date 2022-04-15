package edu.duke.ece568.amazon;

import edu.duke.ece568.amazon.protocol.AmazonUps.*;
import edu.duke.ece568.amazon.protocol.WorldAmazon.*;

public class WorldUpsSwitcher {
  
  private WorldOperator worldOperator;
  private UpsOperator upsOperator;

  /**
   * This constructs a world-UPS switcher with specified worldOperator and upsOperator
   */
  public WorldUpsSwitcher (WorldOperator worldOperator, UpsOperator upsOperator) {
    this.worldOperator = worldOperator;
    this.upsOperator = upsOperator;
  }

  /**
   * This receives a request for picking package from the worldOperator and 
   * asks the upsOperator for package pick up
   */
  public void requestPickPackage(long packageId, APurchaseMore arrived) {
    upsOperator.pickPackage(packageId, arrived);
  }

  /**
   * This receives a request for loading package from the upsOperator and 
   * asks the worldOperator for package loading
   */
  public void requestLoadPackage(long packageId, int truckId) {
    worldOperator.loadPackage(packageId, truckId);
  }

  public void requestDelivery(int truckId) {
    upsOperator.deliverTruck(truckId);
  }

}