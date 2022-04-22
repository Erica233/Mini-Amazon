package edu.duke.ece568.amazon;

import com.google.protobuf.*;
import edu.duke.ece568.amazon.protocol.AmazonUps.*;
import edu.duke.ece568.amazon.protocol.WorldAmazon.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class DatabaseOperator {

  /**
   * This constructs a database operator
   */
  public DatabaseOperator() {}

  /**
   * This extracts the associated warehouse ID of the specific packageId from database
   */
  public int getWhnum(long packageId) {
    Connection connection = null;
    Statement stmt = null;
    try{
      Class.forName("org.postgresql.Driver");
      connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/amazon", "postgres", "postgres");
      connection.setAutoCommit(false);
      stmt = connection.createStatement();
      String sql = "SELECT warehouse_id FROM amazon_package WHERE id=" + packageId + ";";
      ResultSet res = stmt.executeQuery(sql);
      int whnum = -1;
      while (res.next()) {
        whnum = res.getInt("warehouse_id");
      }
      res.close();
      stmt.close();
      connection.close();
      return whnum;
    }
    catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
    return -1;
  }

  /**
   * This extracts locations of all warehouses from database
   */
  public List<AInitWarehouse> getWarehouseList() {
    Connection connection = null;
    Statement stmt = null;
    try{
      List<AInitWarehouse> warehouseList = new ArrayList<> ();
      Class.forName("org.postgresql.Driver");
      connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/amazon", "postgres", "postgres");
      connection.setAutoCommit(false);
      
      stmt = connection.createStatement();
      String sql = "SELECT * FROM amazon_warehouse;";
      ResultSet res = stmt.executeQuery(sql);
      while (res.next()) {
          AInitWarehouse.Builder warehouse = AInitWarehouse.newBuilder();
          warehouse.setId(res.getInt("id"));
          warehouse.setX(res.getInt("location_x"));
          warehouse.setY(res.getInt("location_y"));
          warehouseList.add(warehouse.build());
      }
      res.close();
      stmt.close();
      connection.close();
      return warehouseList;
    }
    catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
    return null;
  }

  /**
   * This extracts products of the specific packageId from database
   */
  public APurchaseMore.Builder getPurchaseProduct(long packageId) {
    Connection connection = null;
    Statement stmt = null;
    try{
      APurchaseMore.Builder purchase = APurchaseMore.newBuilder();
      List<AProduct> productList = new ArrayList<> ();
      Class.forName("org.postgresql.Driver");
      connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/amazon", "postgres", "postgres");
      connection.setAutoCommit(false);
      stmt = connection.createStatement();
      String sql = "SELECT amazon_package.warehouse_id, amazon_product.id, amazon_product.name, amazon_item.product_num " +
                   "FROM amazon_item, amazon_package, amazon_product WHERE amazon_item.product_id=amazon_product.id AND " +
                   "amazon_item.package_id=amazon_package.id AND amazon_package.id=" + packageId + ";";
      ResultSet res = stmt.executeQuery(sql);
      while (res.next()) {
          purchase.setWhnum(res.getInt("warehouse_id"));
          AProduct.Builder product = AProduct.newBuilder();
          product.setId(res.getInt("id"));
          product.setDescription(res.getString("name"));
          product.setCount(res.getInt("product_num"));
          productList.add(product.build());
      }
      res.close();
      stmt.close();
      connection.close();
      purchase.addAllThings(productList);
      return purchase;
    }
    catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
    return null;
  }

  /**
   * This extracts the package status of the specific packageId from database
   */
  public String getPackageStatus(long packageId) {
    Connection connection = null;
    Statement stmt = null;
    try{
      Class.forName("org.postgresql.Driver");
      connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/amazon", "postgres", "postgres");
      connection.setAutoCommit(false);
      stmt = connection.createStatement();
      String sql = "SELECT status FROM amazon_package WHERE id=" + packageId + ";";
      ResultSet res = stmt.executeQuery(sql);
      String status = null;
      while (res.next()) {
        status = res.getString("status");
      }
      res.close();
      stmt.close();
      connection.close();
      return status;
    }
    catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
    return null;
  }

  /**
   * This updates package status of the specific packageId in database
   */
  public void updatePackageStatus(long packageId, String status) {
    Connection connection = null;
    Statement stmt = null;
    try{
      Class.forName("org.postgresql.Driver");
      connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/amazon", "postgres", "postgres");
      connection.setAutoCommit(false);
      stmt = connection.createStatement();
      System.out.println("The status is: " + status);
      String sql = "UPDATE amazon_package SET status=\'" + status + "\' WHERE id=" + packageId + ";";
      stmt.executeUpdate(sql); 
      connection.commit();
      stmt.close();
      connection.close();
    }
    catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
  }

  /**
   * This extracts the related UPS account of the specific packageId from database if exists
   */
  public String getUpsAccount(long packageId) {
    Connection connection = null;
    Statement stmt = null;
    try{
      Class.forName("org.postgresql.Driver");
      connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/amazon", "postgres", "postgres");
      connection.setAutoCommit(false);
      stmt = connection.createStatement();
      String sql = "SELECT ups_account FROM amazon_package WHERE id=" + packageId + ";";
      ResultSet res = stmt.executeQuery(sql);
      String upsAccount = null;
      while (res.next()) {
        upsAccount = res.getString("ups_account");
      }
      res.close();
      stmt.close();
      connection.close();
      return upsAccount;
    }
    catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
    return null;
  }

  /**
   * This update verified UPS account of the specific packageId in database
   */
  public void updateUpsAccount(long packageId, String account) {
    Connection connection = null;
    Statement stmt = null;
    try{
      Class.forName("org.postgresql.Driver");
      connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/amazon", "postgres", "postgres");
      connection.setAutoCommit(false);
      stmt = connection.createStatement();
      String sql = "UPDATE amazon_package SET ups_account=\'" + account + "\', ups_verified=true WHERE id=" + packageId + ";";
      stmt.executeUpdate(sql);
      connection.commit();
      stmt.close();
      connection.close();
    }
    catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
  }

  /**
   * This extracts the x location of destination of the specific packageId from database
   */
  public int getDestx(long packageId) {
    Connection connection = null;
    Statement stmt = null;
    try{
      Class.forName("org.postgresql.Driver");
      connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/amazon", "postgres", "postgres");
      connection.setAutoCommit(false);
      stmt = connection.createStatement();
      String sql = "SELECT destination_x FROM amazon_package WHERE id=" + packageId + ";";
      ResultSet res = stmt.executeQuery(sql);
      int destx = -1;
      while (res.next()) {
        destx = res.getInt("destination_x");
      }
      res.close();
      stmt.close();
      connection.close();
      return destx;
    }
    catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
    return -1;
  }

  /**
   * This extracts the y location of destination of the specific packageId from database
   */
  public int getDesty(long packageId) {
    Connection connection = null;
    Statement stmt = null;
    try{
      Class.forName("org.postgresql.Driver");
      connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/amazon", "postgres", "postgres");
      connection.setAutoCommit(false);
      stmt = connection.createStatement();
      String sql = "SELECT destination_y FROM amazon_package WHERE id=" + packageId + ";";
      ResultSet res = stmt.executeQuery(sql);
      int desty = -1;
      while (res.next()) {
        desty = res.getInt("destination_y");
      }
      res.close();
      stmt.close();
      connection.close();
      return desty;
    }
    catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
    return -1;
  }

  /**
   * This extracts the associated truck ID of the specific packageId from database
   */
  public int getTruckId(long packageId) {
    Connection connection = null;
    Statement stmt = null;
    try{
      Class.forName("org.postgresql.Driver");
      connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/amazon", "postgres", "postgres");
      connection.setAutoCommit(false);
      stmt = connection.createStatement();
      String sql = "SELECT truck_id FROM amazon_package WHERE id=" + packageId + ";";
      ResultSet res = stmt.executeQuery(sql);
      int truckId = -1;
      while (res.next()) {
        truckId = res.getInt("truck_id");
      }
      res.close();
      stmt.close();
      connection.close();
      return truckId;
    }
    catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
    return -1;
  }

  /**
   * This update truck id of the specific packageId in database
   */
  public void updateTruckId(long packageId, int truckId) {
    Connection connection = null;
    Statement stmt = null;
    try{
      Class.forName("org.postgresql.Driver");
      connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/amazon", "postgres", "postgres");
      connection.setAutoCommit(false);
      stmt = connection.createStatement();
      String sql = "UPDATE amazon_package SET truck_id=" + truckId + "WHERE id=" + packageId + ";";
      stmt.executeUpdate(sql);
      connection.commit();
      stmt.close();
      connection.close();
    }
    catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
  }

  /**
   * This checks whether packages that assigned to the truck are all loaded
   */
  public boolean checkAllPackagesLoaded(int truckId) {
    Connection connection = null;
    Statement stmt = null;
    try{
      boolean result = true;
      Class.forName("org.postgresql.Driver");
      connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/amazon", "postgres", "postgres");
      connection.setAutoCommit(false);
      stmt = connection.createStatement();
      String sql = "SELECT status FROM amazon_package WHERE truck_id=" + truckId + " AND status!='delivering' AND status!='delivered';";
      ResultSet res = stmt.executeQuery(sql);
      while (res.next()) {
        String status = res.getString("status");
        if (!status.equals("loaded")) {
          result = false;
          break;
        }
        else {
          continue;
        }    
      }
      res.close();
      stmt.close();
      connection.close();
      return result;
    }
    catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
    return false;
  }

/**
   * This updates package status to 'delivering' of the specific truckId in database
   */
  public void updateDeliveringStatus(int truckId) {
    Connection connection = null;
    Statement stmt = null;
    try{
      Class.forName("org.postgresql.Driver");
      connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/amazon", "postgres", "postgres");
      connection.setAutoCommit(false);
      stmt = connection.createStatement();
      String sql = "UPDATE amazon_package SET status='delivering' WHERE truck_id=" + truckId + " AND status='loaded';";
      stmt.executeUpdate(sql);
      connection.commit();
      stmt.close();
      connection.close();
    }
    catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
  }

}
