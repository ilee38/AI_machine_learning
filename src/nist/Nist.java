package nist;

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.*;

public class Nist implements Runnable {
    Scanner in; // for reading from the agent
    PrintStream out; // for printing info to the agent
    boolean askuser;

    public Nist(Scanner in, PrintStream out) {
        this.in = in;
        this.out = out;
        this.askuser = false;
    }

    public void run() {
        String filename = null;
        // Each value contains 8 bits, one row of a 8x8 image
        int[][] images = new int[60000][8];
        // Each value is the digit in the corresponding image
        int[] labels = new int[60000];
        try {
            // read in images
            filename = "train-images-idx3-8x8bitmap";
            FileInputStream data = new FileInputStream(new File(filename));
            for (int i = 0; i < 60000; i++) {
                for (int j = 0; j < 8; j++) {
                    int c = data.read();
                    if (c == -1) {
                        data.close();
                        throw new RuntimeException("Expecting more than " + i
                                + " images");
                    }
                    images[i][j] = c;
                }
            }
            data.close();

            // read in labels
            filename = "train-labels-idx1-char";
            data = new FileInputStream(new File(filename));
            for (int i = 0; i < 60000; i++) {
                int c = data.read();
                if (c == -1) {
                    data.close();
                    throw new RuntimeException("Expecting more than " + i
                            + " labels");
                }
                labels[i] = c - 48;
            }
            data.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("unable to open " + filename);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception reading "
                    + filename);
        }

        int correct = 0, incorrect = 0;
        // a different seed will be used for testing
        Random random = new Random(13);

        for (int i = 0; i < 60000; i++) {
            /* select next image randomly */
            int r = i + random.nextInt(60000 - i);
            int temp = labels[i];
            labels[i] = labels[r];
            labels[r] = temp;
            for (int j = 0; j < 8; j++) {
                temp = images[i][j];
                images[i][j] = images[r][j];
                images[r][j] = temp;
            }

            /* Output an image */
            String imageline = "";
            for (int j = 0; j < 8; j++) {
                temp = images[i][j];
                for (int k = 0; k < 8; k++) {
                    imageline += temp % 2;
                    temp /= 2;
                }
            }
            out.print(imageline);
            out.print('\n');

            /* read in prediction */
            int prediction = -1;
            if (askuser) {
                BufferedImage image = new BufferedImage(100, 100,
                        BufferedImage.TYPE_INT_ARGB);
                Graphics g = image.getGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, 100, 100);
                for (int j = 0; j < 8; j++) {
                    temp = images[i][j];
                    for (int k = 0; k < 8; k++) {
                        if (temp % 2 == 1) {
                            g.setColor(Color.BLACK);
                        } else {
                            g.setColor(Color.WHITE);
                        }
                        g.fillRect((k + 1) * 10, (j + 1) * 10, 10, 10);
                        temp /= 2;
                    }
                }
                Icon icon = new ImageIcon(image);

                Integer[] digits = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
                prediction = (Integer) JOptionPane.showInputDialog(null,
                        "What digit is this image?", "NIST Digit Recognition",
                        JOptionPane.QUESTION_MESSAGE, icon, digits, digits[0]);
            } else if (in.hasNextInt()) {
                prediction = in.nextInt();
            } else {
                String trash = in.next();
                throw new RuntimeException(trash + " is not a digit");
            }
            if (prediction == labels[i]) {
                out.printf("correct ");
                correct++;
            } else {
                out.printf("incorrect ");
                incorrect++;
            }
            out.printf("(label is %d, error rate = %d/%d = %.2f)\n", labels[i],
                    incorrect, correct + incorrect, 100 * (0.0 + incorrect)
                            / (0.0 + correct + incorrect));
            int linetest = 10;
            while (linetest < i + 1)
                linetest *= 10;
            linetest /= 10;
            if ((i + 1) % linetest == 0) {
                System.out.println(imageline);
                System.out.println(prediction);
                System.out.printf("(label is %d, error rate = %d/%d = %.2f)\n",
                        labels[i], incorrect, correct + incorrect, 100
                                * (0.0 + incorrect)
                                / (0.0 + correct + incorrect));
            }
        }
        out.print("quit");
        out.print('\n');
        in.close();
        out.close();
    }

    public static void main(String[] args) {
        Nist nist = new Nist(new Scanner(System.in), System.out);
        nist.askuser = true;
        nist.run();
    }
}
