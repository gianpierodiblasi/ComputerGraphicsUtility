package it.unict.dmi.quatree;

import java.awt.image.BufferedImage;

public class QuadTreeCreator {

  public static QuadTree evaluate(BufferedImage buffer, int variance, int wTileMin, int hTileMin, int wTileMax, int hTileMax) {
    return evaluate(buffer, 0, 0, variance, wTileMin, hTileMin, wTileMax, hTileMax);
  }

  private static QuadTree evaluate(BufferedImage square, int x, int y, int variance, int wTileMin, int hTileMin, int wTileMax, int hTileMax) {
    int widthsq = square.getWidth();
    int heightsq = square.getHeight();

    QuadTree quadTree = new QuadTree(x, y, widthsq, heightsq);

    //Immagine troppo piccola per poterla suddividere
    if (widthsq < wTileMin * 2 || heightsq < hTileMin * 2) {
      return quadTree;
    }

    //Immagine troppo grande per non essere suddivisa
    if (widthsq > wTileMax * 2 || heightsq > hTileMax * 2) {
      subdivision(quadTree, square, x, y, variance, wTileMin, hTileMin, wTileMax, hTileMax);
      return quadTree;
    }

    int wh = widthsq * heightsq;

    double medianR = 0;
    double medianG = 0;
    double medianB = 0;
    for (int xx = 0; xx < widthsq; xx++) {
      for (int yy = 0; yy < heightsq; yy++) {
        medianR += (square.getRGB(xx, yy) >> 16) & 0xFF;
        medianG += (square.getRGB(xx, yy) >> 8) & 0xFF;
        medianB += square.getRGB(xx, yy) & 0xFF;
      }
    }
    medianR /= wh;
    medianG /= wh;
    medianB /= wh;

    double varR = 0;
    double varG = 0;
    double varB = 0;
    for (int xx = 0; xx < widthsq; xx++) {
      for (int yy = 0; yy < heightsq; yy++) {
        int r = (square.getRGB(xx, yy) >> 16) & 0xFF;
        varR += (medianR - r) * (medianR - r);
        int g = (square.getRGB(xx, yy) >> 8) & 0xFF;
        varG += (medianG - g) * (medianG - g);
        int b = square.getRGB(xx, yy) & 0xFF;
        varB += (medianB - b) * (medianB - b);
      }
    }
    varR = Math.sqrt(varR / (wh - 1));
    varG = Math.sqrt(varG / (wh - 1));
    varB = Math.sqrt(varB / (wh - 1));

    //Varianza sotto il limite massimo
    if (varR <= variance && varG <= variance && varB <= variance) {
      return quadTree;
    }

    subdivision(quadTree, square, x, y, variance, wTileMin, hTileMin, wTileMax, hTileMax);
    return quadTree;
  }

  private static void subdivision(QuadTree quadTree, BufferedImage square, int x, int y, int variance, int wTileMin, int hTileMin, int wTileMax, int hTileMax) {
    int widthsq = square.getWidth();
    int heightsq = square.getHeight();

    //controllo la divisibilita' dell'immagine per 2
    boolean bw = widthsq % 2 == 0;
    boolean bh = heightsq % 2 == 0;

    BufferedImage imm1 = square.getSubimage(0, 0, widthsq / 2, heightsq / 2);
    BufferedImage imm2 = square.getSubimage(widthsq / 2, 0, widthsq / 2 + (bw ? 0 : 1), heightsq / 2);
    BufferedImage imm3 = square.getSubimage(0, heightsq / 2, widthsq / 2, heightsq / 2 + (bh ? 0 : 1));
    BufferedImage imm4 = square.getSubimage(widthsq / 2, heightsq / 2, widthsq / 2 + (bw ? 0 : 1), heightsq / 2 + (bh ? 0 : 1));

    QuadTree[] child = quadTree.getChild();
    child[0] = evaluate(imm1, x, y, variance, wTileMin, hTileMin, wTileMax, hTileMax);
    child[1] = evaluate(imm2, x + widthsq / 2, y, variance, wTileMin, hTileMin, wTileMax, hTileMax);
    child[2] = evaluate(imm3, x, y + heightsq / 2, variance, wTileMin, hTileMin, wTileMax, hTileMax);
    child[3] = evaluate(imm4, x + widthsq / 2, y + heightsq / 2, variance, wTileMin, hTileMin, wTileMax, hTileMax);
  }

  private QuadTreeCreator() {
  }
}
