package it.unict.dmi.antipole;

import java.io.Serializable;
import java.util.ArrayList;

public class ElementList extends ArrayList<ElementNode> implements Serializable {

  private static final long serialVersionUID = 1L;

  private ElementList left, right;
  private boolean leaf;

  public ElementList() {
    left = right = null;
    leaf = true;
  }

  public ElementList getLeft() {
    return left;
  }

  public void setLeft(ElementList l) {
    left = l;
  }

  public boolean isLeaf() {
    return leaf;
  }

  public void setLeaf(boolean l) {
    leaf = l;
  }

  public ElementList getRight() {
    return right;
  }

  public void setRight(ElementList r) {
    right = r;
  }
}
