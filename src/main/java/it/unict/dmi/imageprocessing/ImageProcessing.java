package it.unict.dmi.imageprocessing;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import javax.vecmath.GMatrix;
import javax.vecmath.GVector;

public class ImageProcessing {

  public static void equalize(int[] data, int w, int h) {
    int dim = w * h;
    double[] cumHysto = new double[256];
    for (int i = 0; i < data.length; i++) {
      data[i] &= 0xFF;
      cumHysto[data[i]]++;
    }

    cumHysto[0] *= 255.0 / dim;
    for (int i = 1; i < cumHysto.length; i++) {
      cumHysto[i] *= 255.0 / dim;
      cumHysto[i] += cumHysto[i - 1];
    }
    for (int i = 0; i < data.length; i++) {
      data[i] = (int) (cumHysto[data[i]]);
    }
  }

  public static void convolve(double[] convolvedData, int[] data, int w, int h, double[] kernel, int n) {
    int n2 = n / 2;

    for (int y = n2; y < h - n2; y++) {
      for (int x = n2; x < w - n2; x++) {
        double value = 0;
        for (int yy = -n2; yy <= n2; yy++) {
          for (int xx = -n2; xx <= n2; xx++) {
            value += (data[(y + yy) * w + (x + xx)] & 0xFF) * kernel[(yy + n2) * n + xx + n2];
          }
        }
        convolvedData[y * w + x] = value;
      }
    }
  }

  @SuppressWarnings("UseOfSystemOutOrSystemErr")
  public static void mahalanobisDistance(double[] distance, int[] data, int w, int h, double[] sigma, double[][][] kernel, int features, int n) {
    int n2 = n / 2;

    GVector[] xVect = new GVector[w * h];
    GVector xMeanVect = new GVector(features * sigma.length);

    for (int index = 0; index < sigma.length; index++) {
      int c = features * index;

      double[] convolvedData = new double[w * h];
      for (int i = 0; i < features; i++) {
        ImageProcessing.convolve(convolvedData, data, w, h, kernel[index][i], n);
        evaluateMean(xVect, xMeanVect, w, h, n2, features, sigma, convolvedData, c + i);
      }
    }

    xMeanVect.scale(1.0 / ((w - n + 1) * (h - n + 1)));

    System.out.println("Covariance");
    GMatrix gmatrix = new GMatrix(features * sigma.length, features * sigma.length);
    for (int i = 0; i < features * sigma.length; i++) {
      for (int j = 0; j <= i; j++) {
        double value = 0;
        for (int y = n2; y < h - n2; y++) {
          for (int x = n2; x < w - n2; x++) {
            int c = y * w + x;
            value += (xVect[c].getElement(i) - xMeanVect.getElement(i)) * (xVect[c].getElement(j) - xMeanVect.getElement(j));
          }
        }
        value /= (w - n + 1) * (h - n + 1) - 1;
        gmatrix.setElement(i, j, value);
        gmatrix.setElement(j, i, value);
      }
    }
    gmatrix.invert();

    System.out.println("Mahalanobis");
    GVector diff = new GVector(features * sigma.length);
    GVector prod = new GVector(features * sigma.length);

    for (int y = n2; y < h - n2; y++) {
      for (int x = n2; x < w - n2; x++) {
        int c = y * w + x;
        diff.sub(xVect[c], xMeanVect);
        prod.mul(gmatrix, diff);
        distance[c] = (float) diff.dot(prod);
      }
    }
  }

  private static void evaluateMean(GVector[] xVect, GVector xMeanVect, int w, int h, int n2, int features, double[] sigma, double[] convolvedData, int c) {
    double value = 0;
    for (int y = n2; y < h - n2; y++) {
      for (int x = n2; x < w - n2; x++) {
        int cc = y * w + x;
        if (c == 0) {
          xVect[cc] = new GVector(features * sigma.length);
        }
        xVect[cc].setElement(c, convolvedData[cc]);
        value += convolvedData[cc];
      }
    }
    xMeanVect.setElement(c, value);
  }

  public static void putIntoImage(int[] dataInt, double[] dataDouble, int w, int h) {
    double min = Double.MAX_VALUE;
    double max = Double.MIN_VALUE;

    for (int x = 0; x < w; x++) {
      for (int y = 0; y < h; y++) {
        int c = y * w + x;
        if (dataDouble[c] < min) {
          min = dataDouble[c];
        } else if (dataDouble[c] > max) {
          max = dataDouble[c];
        }
      }
    }

    double diff = max - min;

    for (int x = 0; x < w; x++) {
      for (int y = 0; y < h; y++) {
        int c = y * w + x;
        dataInt[c] = (int) (255 * (dataDouble[c] - min) / diff);
        dataInt[c] = 0xFF000000 | dataInt[c] << 16 | dataInt[c] << 8 | dataInt[c];
      }
    }
  }

  public static BufferedImage evaluateEdgesFromSegmentation(int[] buffer, int w, int h) {
    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    image.setRGB(0, 0, w, h, buffer, 0, w);

    float[] k = new float[]{
      0, -1, 0,
      -1, 4, -1,
      0, -1, 0
    };
    Kernel convolveKernel = new Kernel(3, 3, k);
    ConvolveOp convolveOp = new ConvolveOp(convolveKernel);
    image = convolveOp.filter(image, null);

    image.getRGB(0, 0, w, h, buffer, 0, w);
    for (int c = 0; c < buffer.length; c++) {
      if (buffer[c] == 0xFF000000) {
        buffer[c] = 0xFFFFFFFF;
      } else {
        buffer[c] = 0xFF000000;
      }
    }
    image.setRGB(0, 0, w, h, buffer, 0, w);
    return image;
  }

  private ImageProcessing() {
  }

}
