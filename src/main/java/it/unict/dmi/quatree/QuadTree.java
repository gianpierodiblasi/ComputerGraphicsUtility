package it.unict.dmi.quatree;

import java.awt.Rectangle;

public class QuadTree extends Rectangle {

  private static final long serialVersionUID = 1L;

  private final QuadTree[] child = new QuadTree[4];

  public QuadTree(int x, int y, int w, int h) {
    super(x, y, w, h);
  }

  public QuadTree[] getChild() {
    return child;
  }

  public boolean isLeaf() {
    return child[0] == null && child[1] == null && child[2] == null && child[3] == null;
  }
}
