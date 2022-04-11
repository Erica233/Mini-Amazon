package edu.duke.ece568.amazon;

import com.google.protobuf.*;
import edu.duke.ece568.amazon.protocol.AmazonUps.*;
import edu.duke.ece568.amazon.protocol.WorldAmazon.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class DatabaseOperator {

  public DatabaseOperator() {}

  /**
   * This extracts locations of all warehouses from database
   */
  public List<AInitWarehouse> getWarehouse() {
    try{
      List<AInitWarehouse> warehouseList = new ArrayList<> ();
      Class.forName("org.postgresql.Driver");
      Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/amazon", "postgres", "passw0rd");
      String sql = "SELECT * FROM amazon_warehouse;";
      Statement stmt = connection.createStatement();
      ResultSet res = stmt.executeQuery(sql);
      while (res.next()) {
          AInitWarehouse.Builder warehouse = AInitWarehouse.newBuilder();
          warehouse.setId(res.getInt("id"));
          warehouse.setX(res.getInt("location_x"));
          warehouse.setY(res.getInt("location_y"));
          warehouseList.add(warehouse.build());
      }
      stmt.close();
      connection.close();
      return warehouseList;
    }
    catch (Exception e) {
      System.out.println(e);
    }
    return null;
  }

  /**
   * This extracts products of the specific packageId from database
   */
  public APurchaseMore.Builder getPurchaseProduct(long packageId) {
    try{
      APurchaseMore.Builder purchase = APurchaseMore.newBuilder();
      List<AProduct> productList = new ArrayList<> ();
      Class.forName("org.postgresql.Driver");
      Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/amazon", "postgres", "passw0rd");
      String sql = "SELECT amazon_package.warehouse_id, amazon_product.id, amazon_product.description, amazon_order.product_num" +
                   "FROM amazon_order, amazon_package, amazon_product WHERE amazon_order.product_id=amazon_product.id AND" +
                   "amazon_order.package_id=amazon_package.id AND amazon_package.id=" + packageId + ";";
      Statement stmt = connection.createStatement();
      ResultSet res = stmt.executeQuery(sql);
      purchase.setWhnum(res.getInt("amazon_package.warehouse_id"));
      while (res.next()) {
          AProduct.Builder product = AProduct.newBuilder();
          product.setId(res.getInt("amazon_product.id"));
          product.setDescription(res.getString("amazon_product.description"));
          product.setCount(res.getInt("amazon_order.product_num"));
          productList.add(product.build());
      }
      stmt.close();
      connection.close();
      purchase.addAllThings(productList);
      return purchase;
    }
    catch (Exception e) {
      System.out.println(e);
    }
    return null;
  }

}
