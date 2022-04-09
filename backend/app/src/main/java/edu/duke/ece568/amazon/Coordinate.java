package edu.duke.ece568.amazon;

public class Coordinate {
  private final int x;
  private final int y;
  
  /**
   * This constructs a Coordinate with the specified x and y coordinate
   */
  public Coordinate(int _x, int _y) {
    x = _x;
    y = _y;
  }

  /**
   * This get the x coordinate of Coordinate
   */
  public int getX() {
    return x;
  }

  /**
   * This get the y coordinate of Coordinate
   */
  public int getY() {
    return y;
  }

  /**
   * This transforms the Coordinate string to hashcode
   */
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

 /**
   * This transforms the Coordinate to string
   */
  @Override
  public String toString() {
    return "("+ x +", " + y + ")";
  }

  /**
   * This compares whether two Coordinates are the same
   */
  @Override
  public boolean equals(Object o) {
    if (o.getClass().equals(getClass())) {
     Coordinate c =  (Coordinate) o;
      return x == c.x && y == c.y;
    }
    return false;
  }
}
