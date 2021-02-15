package it.unict.dmi.guideline;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import javax.swing.JProgressBar;

public class GuidelineDetector {

  private static final int N = 5;
  private static final int N2 = N / 2;
  private static final double SIGMA = 16;
  private static final double TWO_SIGMA_SQUARE = 2 * SIGMA * SIGMA;
  private static final double TWO_PI_SIGMA_SQUARE = TWO_SIGMA_SQUARE * Math.PI;

  private static int w;
  private static int h;
  private static BufferedImage image;
  private static int[] data;
  private static double[] kernel;
  private static double mean;
  private static double variance;
  private static double[] vect;

  private static void init(BufferedImage buffer) {
    w = buffer.getWidth();
    h = buffer.getHeight();
    image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    ColorConvertOp colorOp = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
    colorOp.filter(buffer, image);
    data = new int[w * h];
    image.getRGB(0, 0, w, h, data, 0, w);
  }

  private static void equalize() {
    int dim = w * h;
    double[] cumHistogram = new double[256];
    for (int i = 0; i < data.length; i++) {
      data[i] &= 0xFF;
      cumHistogram[data[i]]++;
    }
    cumHistogram[0] *= 255.0 / dim;
    for (int i = 1; i < cumHistogram.length; i++) {
      cumHistogram[i] *= 255.0 / dim;
      cumHistogram[i] += cumHistogram[i - 1];
    }
    for (int i = 0; i < data.length; i++) {
      data[i] = (int) (cumHistogram[data[i]]);
    }
  }

  private static void setKernel() {
    kernel = new double[N * N];

    for (int y = -N2; y <= N2; y++) {
      for (int x = -N2; x <= N2; x++) {
        kernel[(y + N2) * N + x + N2] = Math.exp(-(x * x + y * y) / TWO_SIGMA_SQUARE) / TWO_PI_SIGMA_SQUARE;
      }
    }
  }

  private static void convolveAndEvaluateMean() {
    vect = new double[w * h];
    mean = 0;
    int tot = (w - N + 1) * (h - N + 1);
    for (int y = N2; y < h - N2; y++) {
      for (int x = N2; x < w - N2; x++) {
        int c = y * w + x;
        for (int yy = -N2; yy <= N2; yy++) {
          for (int xx = -N2; xx <= N2; xx++) {
            vect[c] += data[(y + yy) * w + (x + xx)] * kernel[(yy + N2) * N + xx + N2];
          }
        }
        mean += vect[c];
      }
    }
    mean /= tot;
  }

  private static void evaluateVariance() {
    variance = 0;
    for (int y = N2; y < h - N2; y++) {
      for (int x = N2; x < w - N2; x++) {
        double diff = vect[y * w + x] - mean;
        variance += diff * diff;
      }
    }
    variance /= (w - N + 1) * (h - N + 1) - 1;
  }

  private static void segment() {
    Graphics g2 = image.createGraphics();
    g2.setColor(Color.white);
    g2.fillRect(0, 0, w, h);
    g2.dispose();

    double threshold = .25 * variance;
    for (int y = N2; y < h - N2; y++) {
      for (int x = N2; x < w - N2; x++) {
        if (Math.abs(vect[y * w + x] - mean) > threshold) {
          image.setRGB(x, y, 0xFF000000);
        }
      }
    }

    for (int x = 0; x < w; x++) {
      int val = image.getRGB(x, N2);
      for (int y = 0; y < N2; y++) {
        image.setRGB(x, y, val);
      }
      val = image.getRGB(x, h - N2 - 1);
      for (int y = h - N2; y < h; y++) {
        image.setRGB(x, y, val);
      }
    }
    for (int y = N2; y < h - N2; y++) {
      int val = image.getRGB(N2, y);
      for (int x = 0; x < N2; x++) {
        image.setRGB(x, y, val);
      }
      val = image.getRGB(w - N2 - 1, y);
      for (int x = w - N2; x < w; x++) {
        image.setRGB(x, y, val);
      }
    }
  }

  private static void detectGuidelines() {
    float[] k = new float[]{
      0, -1, 0,
      -1, 4, -1,
      0, -1, 0
    };
    Kernel convolveKernel = new Kernel(3, 3, k);
    ConvolveOp convolveOp = new ConvolveOp(convolveKernel);
    image = convolveOp.filter(image, null);
    Graphics g2 = image.createGraphics();
    g2.setColor(Color.white);
    g2.setXORMode(Color.black);
    g2.fillRect(0, 0, w, h);
    g2.dispose();

    image.setRGB(1, 1, 0xFFFFFFFF);
    image.setRGB(w - 2, h - 2, 0xFFFFFFFF);
    image.setRGB(1, h - 2, 0xFFFFFFFF);
    image.setRGB(w - 2, 1, 0xFFFFFFFF);
  }

  private static void disposeAll() {
    image = null;
    data = null;
    vect = null;
    kernel = null;
  }

  public static BufferedImage evaluate(BufferedImage buffer, JProgressBar bar) {
    if (bar != null) {
      bar.setString("Guideline Detection...");
    }
    init(buffer);
    if (bar != null) {
      bar.setValue(14);
    }
    equalize();
    if (bar != null) {
      bar.setValue(28);
    }
    setKernel();
    if (bar != null) {
      bar.setValue(42);
    }
    convolveAndEvaluateMean();
    if (bar != null) {
      bar.setValue(56);
    }
    evaluateVariance();
    if (bar != null) {
      bar.setValue(70);
    }
    segment();
    if (bar != null) {
      bar.setValue(84);
    }
    detectGuidelines();
    if (bar != null) {
      bar.setValue(100);
    }
    BufferedImage finish = image;
    disposeAll();

    return finish;
  }

  private GuidelineDetector() {
  }
}
