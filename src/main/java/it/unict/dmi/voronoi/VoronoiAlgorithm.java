package it.unict.dmi.voronoi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import javax.swing.JProgressBar;

public class VoronoiAlgorithm {

  private static int size;

  public static BufferedImage evaluate(int w, int h, int side, boolean transparent, JProgressBar bar) {
    if (bar != null) {
      bar.setString("Voronoi Diagram...");
    }
    int ww = w / side;
    int hh = h / side;
    size = ww * hh;

    ArrayList<Point> points = new ArrayList<>();
    while (points.size() < size) {
      Point p = new Point((int) (Math.random() * w), (int) (Math.random() * h));
      if (!points.contains(p)) {
        if (bar != null) {
          bar.setValue(50 * points.size() / size);
        }
        points.add(p);
      }
    }

    Point[] p = new Point[size];
    points.toArray(p);

    return evaluate(p, w, h, transparent, bar);
  }

  public static int getVoronoiRegionCount() {
    return size;
  }

  public static BufferedImage evaluate(Point[] p, int w, int h, boolean transparent, JProgressBar bar) {
    if (bar != null) {
      bar.setString("Voronoi Diagram...");
    }

    if (bar != null) {
      bar.setIndeterminate(true);
    }
    VoronoiArea[] va=VoronoiAlgorithm  .evaluate(p, w, h);
    if (bar != null) {
      bar.setIndeterminate(false);
    }

    BufferedImage buff = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = buff.createGraphics();
    if (!transparent) {
      g2.setPaint(Color.white);
      g2.fillRect(0, 0, w, h);
    }
    g2.setPaint(Color.black);
    g2.drawRect(0, 0, w, h);
    for (int i = 0; i < va.length; i++) {
      if (bar != null && i % 5 == 0) {
        bar.setValue(50 + 50 * i / va.length);
      }
      g2.draw(va[i]);
    }
    g2.dispose();
    System.gc();

    return buff;
  }

  public static VoronoiArea[] evaluate(Point[] p, int w, int h) {
    Rectangle bounds = new Rectangle(0, 0, w, h);
    VoronoiArea[] va=new   VoronoiArea[p.length];
    for (int i = 0; i < va.length; i++) {
      va[i] = new VoronoiArea(p[i], bounds);
    }
    Arrays.sort(va, (o1, o2) -> o1.getGenerator().x - o2.getGenerator().x);

    Point[] border = new Point[4];
    border[0] = new Point(bounds.x, bounds.y);
    border[1] = new Point(bounds.x + bounds.width, bounds.y);
    border[2] = new Point(bounds.x + bounds.width, bounds.y + bounds.height);
    border[3] = new Point(bounds.x, bounds.y + bounds.height);
    split(0, p.length - 1, va,border );
    return va;
  }

  private static void split(int start, int finish, VoronoiArea[] va, Point[] border) {
    int m = (start + finish) / 2;
    if (start < m) {
      split(start, m, va,border );
    }
    if (m + 1 < finish) {
      split(m + 1, finish, va,border );
    }
    for (int i = start; i <= m; i++) {
      for (int j = m + 1; j <= finish; j++) {
        if (va[i].intersects(va[j])) {
          Area[] area = getHalfPlane(va[i].getGenerator(), va[j].getGenerator(), border);
          va[i].intersect(area[0]);
          va[j].intersect(area[1]);
        }
      }
    }
  }

  private static Area[] getHalfPlane(Point p1, Point p2, Point[] border) {
    //punto medio p1-p2
    double xm = (p1.x + p2.x) / 2.0D;
    double ym = (p1.y + p2.y) / 2.0D;
    //retta p1-p2
    double a = p2.y - p1.y;
    double b = p1.x - p2.x;
    //bisettrice p1-p2
    double aBisettrice = -b;
    double bBisettrice = a;
    double cBisettrice = b * xm - a * ym;
    //punti di intersezione
    ArrayList<Point[]> points = new ArrayList<>();
    ArrayList<Point> prevPoints = new ArrayList<>();
    Point p = horizontalIntersection(border[0].y, aBisettrice, bBisettrice, cBisettrice, border[0].x, border[1].x);
    if (p != null) {
      points.add(new Point[]{border[0], p});
      prevPoints.add(p);
    }
    p = horizontalIntersection(border[3].y, aBisettrice, bBisettrice, cBisettrice, border[3].x, border[2].x);
    if (p != null && !prevPoints.contains(p)) {
      points.add(new Point[]{border[2], p});
      prevPoints.add(p);
    }
    p = verticalIntersection(border[0].x, aBisettrice, bBisettrice, cBisettrice, border[0].y, border[3].y);
    if (p != null && !prevPoints.contains(p)) {
      points.add(new Point[]{border[3], p});
      prevPoints.add(p);
    }
    p = verticalIntersection(border[1].x, aBisettrice, bBisettrice, cBisettrice, border[1].y, border[2].y);
    if (p != null && !prevPoints.contains(p)) {
      points.add(new Point[]{border[1], p});
    }
    //costruzione semipiano
    Polygon pol1 = new Polygon();
    Polygon pol2 = new Polygon();
    boolean insertIn1 = true;
    for (Point point : border) {
      if (insertIn1) {
        pol1.addPoint(point.x, point.y);
        insertIn1 = !changePolygon(points, point, pol1, pol2, insertIn1);
      } else {
        pol2.addPoint(point.x, point.y);
        insertIn1 = changePolygon(points, point, pol1, pol2, insertIn1);
      }
    }
    if (pol1.contains(p1)) {
      return new Area[]{new Area(pol1), new Area(pol2)};
    } else {
      return new Area[]{new Area(pol2), new Area(pol1)};
    }
  }

  private static boolean changePolygon(ArrayList<Point[]> points, Point p, Polygon pol1, Polygon pol2, boolean prevInsert) {
    Iterator<Point[]> iter = points.iterator();
    boolean found = false;
    while (iter.hasNext() && !found) {
      Point[] point = iter.next();
      found = point[0].equals(p);
      if (found) {
        points.remove(point);
        if (prevInsert) {
          if (!point[0].equals(point[1])) {
            pol1.addPoint(point[1].x, point[1].y);
          }
          pol2.addPoint(point[1].x, point[1].y);
        } else {
          pol1.addPoint(point[1].x, point[1].y);
          if (!point[0].equals(point[1])) {
            pol2.addPoint(point[1].x, point[1].y);
          }
        }
      }
    }
    return found;
  }

  private static Point horizontalIntersection(int y, double a, double b, double c, int x1, int x2) {
    double x;
    if (b == 0) {
      x = -c / a;
    } else {
      x = (-c - b * y) / a;
    }
    if (x1 <= x && x <= x2) {
      return new Point((int) Math.round(x), y);
    } else {
      return null;
    }
  }

  private static Point verticalIntersection(int x, double a, double b, double c, int y1, int y2) {
    double y;
    if (a == 0) {
      y = -c / b;
    } else {
      y = (-c - a * x) / b;
    }
    if (y1 <= y && y <= y2) {
      return new Point(x, (int) Math.round(y));
    } else {
      return null;
    }
  }

  private VoronoiAlgorithm() {
  }
}
