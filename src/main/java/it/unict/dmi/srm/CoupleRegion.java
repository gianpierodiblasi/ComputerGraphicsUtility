package it.unict.dmi.srm;

class CoupleRegion implements Comparable<CoupleRegion> {

  private final int distance;
  Region region1, region2;

  CoupleRegion(Region reg1, Region reg2) {
    region1 = reg1;
    region2 = reg2;

    int r1 = (region1.color >> 16) & 0xFF;
    int g1 = (region1.color >> 8) & 0xFF;
    int b1 = region1.color & 0xFF;

    int r2 = (region2.color >> 16) & 0xFF;
    int g2 = (region2.color >> 8) & 0xFF;
    int b2 = region2.color & 0xFF;

    int rDiff = Math.abs(r1 - r2);
    int gDiff = Math.abs(g1 - g2);
    int bDiff = Math.abs(b1 - b2);

    distance = Math.max(Math.max(rDiff, gDiff), bDiff);
  }

  @Override
  public int compareTo(CoupleRegion obj) {
    return this.distance - obj.distance;
  }
}
