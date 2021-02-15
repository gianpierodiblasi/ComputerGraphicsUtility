package it.unict.dmi.voronoi;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Point2D;

public class VoronoiArea extends Area {

  private final Point generator;
  private Rectangle bounds;

  public VoronoiArea(Point p, Shape shape) {
    super(shape);
    generator = p;
    bounds = shape.getBounds();
  }

  public Point getGenerator() {
    return generator;
  }

  public boolean intersects(VoronoiArea va) {
    return this.bounds.intersects(va.bounds);
  }

  @Override
  public Rectangle getBounds() {
    return bounds;
  }

  @Override
  public void intersect(Area a) {
    super.intersect(a);
    bounds = super.getBounds();
  }

  public Point2D.Double getCentroid(double[][] densityFunction) {
    double cx = 0;
    double cy = 0;
    double density = 0;
    for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
      for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
        if (this.contains(x, y)) {
          cx += x * densityFunction[x][y];
          cy += y * densityFunction[x][y];
          density += densityFunction[x][y];
        }
      }
    }

    return new Point2D.Double(cx / density, cy / density);
  }
}
