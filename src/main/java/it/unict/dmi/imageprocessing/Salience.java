package it.unict.dmi.imageprocessing;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class Salience {

  private final static int DG_DX = 0;
  private final static int DG_DY = 1;
  private final static int D2G_DX2 = 2;
  private final static int D2G_DY2 = 3;
  private final static int D2G_DXDY = 4;

  public static double[] getMahalanobisDistance(BufferedImage buffer) {
    int w = buffer.getWidth();
    int h = buffer.getHeight();

    int[] data = new int[w * h];
    buffer.getRGB(0, 0, w, h, data, 0, w);

    double[] sigma = new double[]{1, 2, 4, 8};
    int n = 5;
    int features = 5;

    double[][][] kernel = new double[sigma.length][features][];
    for (int index = 0; index < sigma.length; index++) {
      kernel[index][0] = getKernel(sigma[index], n, Salience.DG_DX);
      kernel[index][1] = getKernel(sigma[index], n, Salience.DG_DY);
      kernel[index][2] = getKernel(sigma[index], n, Salience.D2G_DX2);
      kernel[index][3] = getKernel(sigma[index], n, Salience.D2G_DY2);
      kernel[index][4] = getKernel(sigma[index], n, Salience.D2G_DXDY);
    }

    double[] mahalanobis = new double[w * h];
    ImageProcessing.mahalanobisDistance(mahalanobis, data, w, h, sigma, kernel, features, n);

    return mahalanobis;
  }

  public static BufferedImage evaluate(BufferedImage buffer) {
    int w = buffer.getWidth();
    int h = buffer.getHeight();
    int[] data = new int[w * h];
    int n = 5;

    double[] mahalanobis = getMahalanobisDistance(buffer);

    Arrays.fill(data, 0xFFFFFFFF);
    for (int y = n / 2; y < h - n / 2; y++) {
      for (int x = n / 2; x < w - n / 2; x++) {
        int c = y * w + x;
        if (mahalanobis[c] > 9) {
          data[c] = 0xFF000000;
        }
      }
    }

    BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    output.setRGB(0, 0, w, h, data, 0, w);
    return output;
  }

  private static double[] getKernel(double sigma, int n, int kernelType) {
    double[] kernel = new double[n * n];
    double sigmaSquare = sigma * sigma;
    double twoSigmaSquare = 2 * sigmaSquare;
    double twoPIsigmaSquare = (float) (twoSigmaSquare * Math.PI);
    double sigmaPower4 = sigmaSquare * sigmaSquare;
    int n2 = n / 2;

    for (int y = -n2; y <= n2; y++) {
      for (int x = -n2; x <= n2; x++) {
        int c = (y + n2) * n + x + n2;
        double xSquare = x * x;
        double ySquare = y * y;
        double G = Math.exp(-(xSquare + ySquare) / twoSigmaSquare) / twoPIsigmaSquare;
        switch (kernelType) {
          case Salience.DG_DX:
            kernel[c] = -x * G / sigmaSquare;
            break;
          case Salience.DG_DY:
            kernel[c] = -y * G / sigmaSquare;
            break;
          case Salience.D2G_DX2:
            kernel[c] = (xSquare - sigmaSquare) * G / sigmaPower4;
            break;
          case Salience.D2G_DY2:
            kernel[c] = (ySquare - sigmaSquare) * G / sigmaPower4;
            break;
          case Salience.D2G_DXDY:
            kernel[c] = x * y * G / sigmaPower4;
            break;
        }
      }
    }

    return kernel;
  }

  private Salience() {
  }
}
