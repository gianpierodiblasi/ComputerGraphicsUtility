package it.unict.dmi.morphology;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class MorphologicalOperation {

  public static BufferedImage dilate(BufferedImage image, boolean transparent, int size) {
    int w = image.getWidth();
    int h = image.getHeight();
    BufferedImage buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = buffer.createGraphics();

    if (!transparent) {
      g2.setPaint(Color.white);
      g2.fillRect(0, 0, w, h);
    }

    g2.setPaint(Color.black);
    for (int x = 0; x < w; x++) {
      for (int y = 0; y < h; y++) {
        if (image.getRGB(x, y) == 0xFF000000) {
          g2.fillRect(x - size / 2, y - size / 2, size, size);
        }
      }
    }
    g2.dispose();

    return buffer;
  }

  public static BufferedImage erode(BufferedImage image, boolean transparent, int size) {
    int w = image.getWidth();
    int h = image.getHeight();
    BufferedImage buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = buffer.createGraphics();

    if (!transparent) {
      g2.setPaint(Color.black);
      g2.fillRect(0, 0, w, h);
    }

    g2.setPaint(Color.white);
    for (int x = 0; x < w; x++) {
      for (int y = 0; y < h; y++) {
        if (image.getRGB(x, y) == 0xFFFFFFFF) {
          g2.fillRect(x - size / 2, y - size / 2, size, size);
        }
      }
    }
    g2.dispose();

    return buffer;
  }

  private MorphologicalOperation() {
  }
}
