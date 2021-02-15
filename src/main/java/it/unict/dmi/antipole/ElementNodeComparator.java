package it.unict.dmi.antipole;

import java.util.Comparator;

public class ElementNodeComparator implements Comparator<ElementNode> {

  private final Element source;

  public ElementNodeComparator(Element s) {
    source = s;
  }

  @Override
  public int compare(ElementNode obj1, ElementNode obj2) {
    double d1 = source.distance(obj1.getKey());
    double d2 = source.distance(obj2.getKey());
    if (d1 < d2) {
      return -1;
    } else if (d1 > d2) {
      return 1;
    } else {
      return 0;
    }
  }
}
