package it.unict.dmi.antipole;

public class Pole {

  private final ElementNode A;
  private final ElementNode B;

  public Pole(ElementNode a, ElementNode b) {
    A = a;
    B = b;
  }

  public ElementNode getA() {
    return A;
  }

  public ElementNode getB() {
    return B;
  }
}
