package it.unict.dmi.srm;

class Region {

  int sumR, sumG, sumB;
  int card;
  int pos;
  int color;
  Region parent;
  boolean done;

  Region(int c, int p) {
    color = c;
    pos = p;
    sumR = (color >> 16) & 0xFF;
    sumG = (color >> 8) & 0xFF;
    sumB = color & 0xFF;
    card = 1;
  }

  public void merge(Region region) {
    this.sumR += region.sumR;
    this.sumG += region.sumG;
    this.sumB += region.sumB;
    this.card += region.card;
    region.parent = this;
  }

  public boolean isTop() {
    return parent == null;
  }

  public static Region findCompress(Region region) {
    if (region.isTop()) {
      return region;
    } else {
      region.parent = findCompress(region.parent);
      return region.parent;
    }
  }
}
