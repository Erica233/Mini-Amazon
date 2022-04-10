package edu.duke.ece568.amazon;

import com.google.protobuf.*;
import edu.duke.ece568.amazon.protocol.AmazonUps.*;
import edu.duke.ece568.amazon.protocol.WorldAmazon.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class DatabaseOperator {

  public DatabaseOperator() {}

  public List<AInitWarehouse> getWarehouse() {
    try{
      List<AInitWarehouse> warehouseList = new ArrayList<> ();

      Class.forName("org.postgresql.Driver");
      Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/amazon", "postgres", "passw0rd");
      String sql = "SELECT * FROM amazon_warehouse";
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
}
