package it.unict.dmi.antipole;

import java.io.Serializable;

public class ElementNode implements Serializable {

  private static final long serialVersionUID = 1L;

  private Element key;
  private double radius;

  public ElementNode() {
    key = null;
  }

  public ElementNode(Element val, double r) {
    key = val;
    radius = r;
  }

  public Element getKey() {
    return key;
  }

  public double getRadius() {
    return radius;
  }

  public void setRadius(double r) {
    radius = r;
  }

  public void setKey(Element k) {
    key = k;
  }
}
