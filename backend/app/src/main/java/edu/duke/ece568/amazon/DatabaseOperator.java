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
   * This cleans up remaining items and packages in the database created by the last docker start
   */
  public void cleanUpDatabase() {
    Connection connection = null;
    Statement stmt = null;
    try{
      Class.forName("org.postgresql.Driver");
      connection = DriverManager.getConnection("jdbc:postgresql://db:5432/amazon", "postgres", "postgres");
      connection.setAutoCommit(false);
      stmt = connection.createStatement();
      String sql = "DELETE FROM amazon_item;";
      sql += "DELETE FROM amazon_package;";
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
   * This set up the data of warehouse locations, product categories and product information into database
   */
  public void setUpDatabase() {
    Connection connection = null;
    Statement stmt = null;
    try{
      Class.forName("org.postgresql.Driver");
      connection = DriverManager.getConnection("jdbc:postgresql://db:5432/amazon", "postgres", "postgres");
      connection.setAutoCommit(false);
      stmt = connection.createStatement();
      String sql = "INSERT INTO amazon_warehouse (location_x, location_y) SELECT 10, 10 " + 
                   "WHERE NOT EXISTS (SELECT * FROM amazon_warehouse WHERE location_x=10);";
      sql += "INSERT INTO amazon_warehouse (location_x, location_y) SELECT 20, 20 " + 
             "WHERE NOT EXISTS (SELECT * FROM amazon_warehouse WHERE location_x=20);";
      sql += "INSERT INTO amazon_warehouse (location_x, location_y) SELECT 30, 30 " + 
             "WHERE NOT EXISTS (SELECT * FROM amazon_warehouse WHERE location_x=30);";
      sql += "INSERT INTO amazon_warehouse (location_x, location_y) SELECT 40, 40 " + 
             "WHERE NOT EXISTS (SELECT * FROM amazon_warehouse WHERE location_x=40);";
      sql += "INSERT INTO amazon_warehouse (location_x, location_y) SELECT 50, 50 " + 
             "WHERE NOT EXISTS (SELECT * FROM amazon_warehouse WHERE location_x=50);";

      sql += "INSERT INTO amazon_category (category) SELECT 'meat' " +
             "WHERE NOT EXISTS (SELECT * FROM amazon_category WHERE category='meat');";
      sql += "INSERT INTO amazon_category (category) SELECT 'vegetable' " +
             "WHERE NOT EXISTS (SELECT * FROM amazon_category WHERE category='vegetable');";
      sql += "INSERT INTO amazon_category (category) SELECT 'fruit' " +
             "WHERE NOT EXISTS (SELECT * FROM amazon_category WHERE category='fruit');";
      sql += "INSERT INTO amazon_category (category) SELECT 'drink' " +
             "WHERE NOT EXISTS (SELECT * FROM amazon_category WHERE category='drink');";

      sql += "INSERT INTO amazon_product (name, price, description, pic, category_id) SELECT " +
             "'beef', 10, 'Perfect Portion Strip Steak', '/static/pics/beef.jpg', " + 
             "(SELECT id FROM amazon_category WHERE category='meat') WHERE NOT EXISTS " + 
             "(SELECT * FROM amazon_product WHERE name='beef');";
      sql += "INSERT INTO amazon_product (name, price, description, pic, category_id) SELECT " +
             "'pork', 6, 'Boneless Pork Chop', '/static/pics/pork.jpg', " + 
             "(SELECT id FROM amazon_category WHERE category='meat') WHERE NOT EXISTS " + 
             "(SELECT * FROM amazon_product WHERE name='pork');";
      sql += "INSERT INTO amazon_product (name, price, description, pic, category_id) SELECT " +
             "'chicken', 8, 'Breaded Chicken Cutlets', '/static/pics/chicken.jpg', " + 
             "(SELECT id FROM amazon_category WHERE category='meat') WHERE NOT EXISTS " + 
             "(SELECT * FROM amazon_product WHERE name='chicken');";
      sql += "INSERT INTO amazon_product (name, price, description, pic, category_id) SELECT " +
             "'tomato', 2, 'Beefsteak Tomatoes', '/static/pics/tomato.jpg', " + 
             "(SELECT id FROM amazon_category WHERE category='vegetable') WHERE NOT EXISTS " + 
             "(SELECT * FROM amazon_product WHERE name='tomato');";
      sql += "INSERT INTO amazon_product (name, price, description, pic, category_id) SELECT " +
             "'cucumber', 3, 'Seeded Cucumbers', '/static/pics/cucumber.jpg', " + 
             "(SELECT id FROM amazon_category WHERE category='vegetable') WHERE NOT EXISTS " + 
             "(SELECT * FROM amazon_product WHERE name='cucumber');";
      sql += "INSERT INTO amazon_product (name, price, description, pic, category_id) SELECT " +
             "'cabbage', 4, 'Organic Green Cabbage', '/static/pics/cabbage.jpg', " + 
             "(SELECT id FROM amazon_category WHERE category='vegetable') WHERE NOT EXISTS " + 
             "(SELECT * FROM amazon_product WHERE name='cabbage');";
      sql += "INSERT INTO amazon_product (name, price, description, pic, category_id) SELECT " +
             "'apple', 3, 'Honeycrisp Apples', '/static/pics/apple.jpg', " + 
             "(SELECT id FROM amazon_category WHERE category='fruit') WHERE NOT EXISTS " + 
             "(SELECT * FROM amazon_product WHERE name='apple');";
      sql += "INSERT INTO amazon_product (name, price, description, pic, category_id) SELECT " +
             "'banana', 2, 'Organic Bananas', '/static/pics/banana.jpg', " + 
             "(SELECT id FROM amazon_category WHERE category='fruit') WHERE NOT EXISTS " + 
             "(SELECT * FROM amazon_product WHERE name='banana');";
      sql += "INSERT INTO amazon_product (name, price, description, pic, category_id) SELECT " +
             "'kiwi', 5, 'Tropical Kiwi', '/static/pics/kiwi.jpg', " + 
             "(SELECT id FROM amazon_category WHERE category='fruit') WHERE NOT EXISTS " + 
             "(SELECT * FROM amazon_product WHERE name='kiwi');";
      sql += "INSERT INTO amazon_product (name, price, description, pic, category_id) SELECT " +
             "'cola', 4, 'Coca-Cola Cola', '/static/pics/cola.jpg', " + 
             "(SELECT id FROM amazon_category WHERE category='drink') WHERE NOT EXISTS " + 
             "(SELECT * FROM amazon_product WHERE name='cola');";
      sql += "INSERT INTO amazon_product (name, price, description, pic, category_id) SELECT " +
             "'juice', 5, 'Tropicana Pure Premium 100% Juice', '/static/pics/juice.jpg', " + 
             "(SELECT id FROM amazon_category WHERE category='drink') WHERE NOT EXISTS " + 
             "(SELECT * FROM amazon_product WHERE name='juice');";
      sql += "INSERT INTO amazon_product (name, price, description, pic, category_id) SELECT " +
             "'beer', 18, 'Corona Extra Beer', '/static/pics/beer.jpg', " + 
             "(SELECT id FROM amazon_category WHERE category='drink') WHERE NOT EXISTS " + 
             "(SELECT * FROM amazon_product WHERE name='beer');";                                

      stmt.executeUpdate(sql);
      stmt.close();
      connection.commit();
      connection.close();
    }
    catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
  }

  /**
   * This extracts the associated warehouse ID of the specific packageId from database
   */
  public int getWhnum(long packageId) {
    Connection connection = null;
    Statement stmt = null;
    try{
      Class.forName("org.postgresql.Driver");
      connection = DriverManager.getConnection("jdbc:postgresql://db:5432/amazon", "postgres", "postgres");
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
      connection = DriverManager.getConnection("jdbc:postgresql://db:5432/amazon", "postgres", "postgres");
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
      connection = DriverManager.getConnection("jdbc:postgresql://db:5432/amazon", "postgres", "postgres");
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
      connection = DriverManager.getConnection("jdbc:postgresql://db:5432/amazon", "postgres", "postgres");
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
      connection = DriverManager.getConnection("jdbc:postgresql://db:5432/amazon", "postgres", "postgres");
      connection.setAutoCommit(false);
      stmt = connection.createStatement();
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
      connection = DriverManager.getConnection("jdbc:postgresql://db:5432/amazon", "postgres", "postgres");
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
      connection = DriverManager.getConnection("jdbc:postgresql://db:5432/amazon", "postgres", "postgres");
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
      connection = DriverManager.getConnection("jdbc:postgresql://db:5432/amazon", "postgres", "postgres");
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
      connection = DriverManager.getConnection("jdbc:postgresql://db:5432/amazon", "postgres", "postgres");
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
      connection = DriverManager.getConnection("jdbc:postgresql://db:5432/amazon", "postgres", "postgres");
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
      connection = DriverManager.getConnection("jdbc:postgresql://db:5432/amazon", "postgres", "postgres");
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
      connection = DriverManager.getConnection("jdbc:postgresql://db:5432/amazon", "postgres", "postgres");
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
      connection = DriverManager.getConnection("jdbc:postgresql://db:5432/amazon", "postgres", "postgres");
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
