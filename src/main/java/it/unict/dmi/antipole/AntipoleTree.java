package it.unict.dmi.antipole;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class AntipoleTree implements Serializable {

  private static final long serialVersionUID = 1L;
  private final ElementList root;
  private final double sigma;
  private static final int MIN = 10;

  public AntipoleTree(Element[] input, double s) {
    root = new ElementList();

    sigma = s;
    for (Element el : input) {
      ElementNode elem = new ElementNode(el, 0.0);
      root.add(elem);
    }
    this.antipoleClustering(root);
  }

  private void antipoleClustering(ElementList node) {
    Pole p = null;
    if (node.size() > MIN) {
      p = this.antipole(node);
    }
    if (p == null) {
      return;
    }

    ElementList l = new ElementList();
    ElementList r = new ElementList();
    double maxr = 0;
    double maxl = 0;

    ElementList oldNode = new ElementList();
    oldNode.setLeaf(node.isLeaf());
    oldNode.setRight(null);
    oldNode.setLeft(null);
    oldNode.addAll(node);

    for (int i = 0; i < node.size(); i++) {
      ElementNode item = node.get(i);

      double dA = p.getA().getKey().distance(item.getKey());
      double dB = p.getB().getKey().distance(item.getKey());

      if (dA < dB) {
        item.setRadius(dA);
        l.add(item);
        if (maxl < dA) {
          maxl = dA;
        }
      } else {
        item.setRadius(dB);
        r.add(item);
        if (maxr < dB) {
          maxr = dB;
        }
      }
      node.remove(i);
      i--;
    }

    if ((l.size() < MIN) || (r.size() < MIN)) {
      node.setLeaf(oldNode.isLeaf());
      node.setRight(null);
      node.setLeft(null);
      node.addAll(oldNode);
    } else {
      node.setLeaf(false);
      node.add(new ElementNode(p.getA().getKey(), maxl));
      node.add(new ElementNode(p.getB().getKey(), maxr));
      node.setLeft(l);
      node.setRight(r);

      this.antipoleClustering(l);
      this.antipoleClustering(r);
    }
  }

  private Pole antipole(ElementList node) {
    int length = node.size();
    ElementList aux = new ElementList();

    if (length > 2) {
      int i = 0;
      for (; i < length - 2; i += 3) {
        ElementNode en0 = node.get(i);
        ElementNode en1 = node.get(i + 1);
        ElementNode en2 = node.get(i + 2);
        double d0 = en0.getKey().distance(en1.getKey());
        double d1 = en1.getKey().distance(en2.getKey());
        double d2 = en2.getKey().distance(en0.getKey());
        if (d0 > d1 && d0 > d2 && d0 > sigma) {
          return new Pole(en0, en1);
        }
        if (d1 > d2 && d1 > d0 && d1 > sigma) {
          return new Pole(en1, en2);
        }
        if (d2 > d0 && d2 > d1 && d2 > sigma) {
          return new Pole(en2, en0);
        }

        if (d0 > d1 && d0 > d2) {
          aux.add(en0);
          aux.add(en1);
        } else if (d1 > d2 && d1 > d0) {
          aux.add(en1);
          aux.add(en2);
        } else if (d2 > d0 && d2 > d1) {
          aux.add(en2);
          aux.add(en0);
        }
      }

      if (length - i == 1) {
        aux.add(node.get(i));
      } else if (length - i == 2) {
        ElementNode en0 = node.get(i);
        ElementNode en1 = node.get(i + 1);
        if (en0.getKey().distance(en1.getKey()) > sigma) {
          return new Pole(en0, en1);
        } else {
          aux.add(en0);
          aux.add(en1);
        }
      }
      return this.antipole(aux);
    } else if (length == 2) {
      ElementNode en0 = node.get(0);
      ElementNode en1 = node.get(1);
      double d0 = en0.getKey().distance(en1.getKey());
      if (d0 > sigma) {
        return new Pole(en0, en1);
      }
    }
    return null;
  }

  public ElementList nearestElementSearch(Element item, boolean sort) {
    ElementList node = root;
    while (!node.isLeaf()) {
      if (item.distance(node.get(0).getKey()) < item.distance(node.get(1).getKey())) {
        node = node.getLeft();
      } else {
        node = node.getRight();
      }
    }

    ElementList list = new ElementList();
    list.addAll(node);
    if (sort) {
      Collections.sort(list, new ElementNodeComparator(item));
    }
    return list;
  }

  public ElementList getRoot() {
    return root;
  }

  public ElementList[] getLeaves() {
    if (root.isLeaf()) {
      return new ElementList[]{root};
    } else {
      ArrayList<ElementList> arrayList = new ArrayList<>();
      ElementList left = root.getLeft();
      ElementList right = root.getRight();
      this.internalGetLeaves(arrayList, left);
      this.internalGetLeaves(arrayList, right);

      ElementList[] elementList = new ElementList[arrayList.size()];
      arrayList.toArray(elementList);

      return elementList;
    }
  }

  private void internalGetLeaves(ArrayList<ElementList> arrayList, ElementList elementList) {
    if (elementList.isLeaf()) {
      arrayList.add(elementList);
    } else {
      ElementList left = elementList.getLeft();
      ElementList right = elementList.getRight();
      this.internalGetLeaves(arrayList, left);
      this.internalGetLeaves(arrayList, right);
    }
  }
}
