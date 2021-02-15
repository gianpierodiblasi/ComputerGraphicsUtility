package it.unict.dmi.srm;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import javax.swing.JProgressBar;

public class SRM {

  private static int regionCount;

  /**
   * Statistical Region Merging
   *
   * @param image L'immagine da segmentare
   * @param q scala di segmentazione [0..255]
   * @param bar L'eventuale barra di avanzamento
   * @return l'array di interi dell'immagine segmentata con il valore medio
   */
  public static int[] evaluate(BufferedImage image, int q, JProgressBar bar) {
    if (bar != null) {
      bar.setString("Statistical Region Merging...");
    }
    int w = image.getWidth();
    int h = image.getHeight();
    int[] data = new int[w * h];
    image.getRGB(0, 0, w, h, data, 0, w);

    //Costruzione Tabella 4-Connessa
    Region[] region = new Region[w * h];
    CoupleRegion[] table = new CoupleRegion[(w - 1) * h + w * (h - 1)];
    createCouplePixelTable(data, w, h, region, table);
    if (bar != null) {
      bar.setValue(20);
    }

    //Statistical Region Merging
    int[] numRegionWithKPixel = new int[w * h - 1]; //numRegionWithKPixel[i] numero delle regioni con i pixel
    numRegionWithKPixel[0] = w * h; //Numero regioni con esattamente 1 pixel (tutte all'inizio)

    double b = (Math.log(1) - (Math.log(6) + 2 * Math.log(w * h)));
    int twoQ = 2 * q;

    regionCount = w * h;

    for (int i = 0; i < table.length; i++) {
      if (bar != null && i % 100 == 0) {
        bar.setValue(20 + i * 60 / table.length);
      }
      Region region1 = Region.findCompress(table[i].region1);
      Region region2 = Region.findCompress(table[i].region2);

      if (region1 != region2) {
        int num1 = numRegionWithKPixel[region1.card - 1];
        int num2 = numRegionWithKPixel[region2.card - 1];

        if (mergingPredicate(region1, region2, num1, num2, b, twoQ)) {
          numRegionWithKPixel[region1.card - 1]--;
          numRegionWithKPixel[region2.card - 1]--;
          numRegionWithKPixel[region1.card + region2.card - 1]++;

          region2.merge(region1);
          regionCount--;
        }
      }
    }

    int res[] = new int[w * h];

    for (int i = 0; i < region.length; i++) {
      if (bar != null && i % 100 == 0) {
        bar.setValue(80 + i * 20 / table.length);
      }
      if (!region[i].done) {
        region[i].done = true;
        Region r = region[i];
        r = Region.findCompress(r);
        int color
                = 0xFF000000
                | (r.sumR / r.card) << 16
                | (r.sumG / r.card) << 8
                | (r.sumB / r.card);

        r = region[i];
        while (r != null) {
          res[r.pos] = color;
          r.done = true;
          r = r.parent;
        }
      }
    }

    return res;
  }

  public static int getRegionCount() {
    return regionCount;
  }

  private static boolean mergingPredicate(Region region1, Region region2, int num1, int num2, double b, int twoQ) {
    double a1 = 256 / Math.sqrt(twoQ * region1.card);
    double a2 = 256 / Math.sqrt(twoQ * region2.card);

    double bR1 = a1 * Math.sqrt(Math.log(num1) - b);
    double bR2 = a2 * Math.sqrt(Math.log(num2) - b);

    int R1med = region1.sumR / region1.card;
    int G1med = region1.sumG / region1.card;
    int B1med = region1.sumB / region1.card;

    int R2med = region2.sumR / region2.card;
    int G2med = region2.sumG / region2.card;
    int B2med = region2.sumB / region2.card;

    double th = Math.sqrt(bR1 * bR1 + bR2 * bR2);

    return Math.abs(R2med - R1med) <= th
            && Math.abs(G2med - G1med) <= th
            && Math.abs(B2med - B1med) <= th;
  }

  private static void createCouplePixelTable(int[] data, int w, int h, Region[] region, CoupleRegion[] table) {
    for (int c = 0; c < region.length; c++) {
      region[c] = new Region(data[c], c);
    }

    int counter = 0;

    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w - 1; x++) {
        int c = y * w + x;
        int cc = y * w + x + 1;
        table[counter] = new CoupleRegion(region[c], region[cc]);
        counter++;
      }
    }

    for (int x = 0; x < w; x++) {
      for (int y = 0; y < h - 1; y++) {
        int c = y * w + x;
        int cc = (y + 1) * w + x;
        table[counter] = new CoupleRegion(region[c], region[cc]);
        counter++;
      }
    }

    Arrays.sort(table);
  }

  public static void main(String a[]) throws Exception {
    BufferedImage buffer = javax.imageio.ImageIO.read(new java.io.File("/Users/giampo76/Images/Lena.png"));
    int w = buffer.getWidth();
    int h = buffer.getHeight();
    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    image.createGraphics().drawImage(buffer, 0, 0, null);
    //long start=System.currentTimeMillis();
    int[] res = SRM.evaluate(image, 200, null);
    //System.out.println(System.currentTimeMillis()-start);
    image.setRGB(0, 0, w, h, res, 0, w);
    javax.imageio.ImageIO.write(image, "png", new java.io.File("/Users/giampo76/Desktop/seg200.png"));
//    it.unict.dmi.imageprocessing.ImageProcessing.evaluateEdgesFromSegmentation(res,w,h);
//    image.setRGB(0,0,w,h,res,0,w);
//    javax.imageio.ImageIO.write(image,"png",new java.io.File("/Users/giampo76/Desktop/edge1.png"));
    //System.out.println(SRM.getRegionCount());
//    javax.swing.JOptionPane.showMessageDialog(null,new javax.swing.JLabel(new javax.swing.ImageIcon(image)));
  }
}
