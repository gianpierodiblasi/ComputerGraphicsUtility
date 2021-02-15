package it.unict.dmi.antipole;

import java.io.Serializable;

public interface Element extends Serializable {

  double distance(Element e);
}
