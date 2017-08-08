import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.concurrent.Callable;

/**
 * Created by RicardoFS on 07/08/2017.
 */
public class GetSADDifference implements Callable<Integer[]> {
    int WINDOW_SIZE;
    int lowestDifference;
    int lowX;
    int lowY;
    int SAD;
    int extSAD;
    Mat current;
    Mat old;
    int x;
    int y;
    int maxX;
    int maxY;

    public GetSADDifference(Mat current, Mat old, int x, int y, int maxX, int maxY, int windowSize) {
        WINDOW_SIZE = windowSize;
        lowestDifference = Integer.MAX_VALUE;
        lowX = 0;
        lowY = 0;
        SAD = (int) Core.sumElems(old.submat(new Rect(x * WINDOW_SIZE, y * WINDOW_SIZE, WINDOW_SIZE, WINDOW_SIZE))).val[0];
        this.x = x;
        this.y = y;
        this.current = current;
        this.old = old;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public Integer[] call() {
            try {
                //Top left
                if (x > 0 && y > 0) {
                    for (int i = -WINDOW_SIZE / 2; i > 0; i--) {
                        extSAD = (int) Core.sumElems(current.submat(new Rect(x * WINDOW_SIZE + i, y * WINDOW_SIZE - (WINDOW_SIZE / 2), WINDOW_SIZE, WINDOW_SIZE))).val[0];
                        if (extSAD < lowestDifference) {
                            lowestDifference = extSAD;
                            lowX = i;
                            lowY = -WINDOW_SIZE / 2;
                        }
                    }
                }
                //Top
                if (y > 0) {
                    extSAD = (int) Core.sumElems(current.submat(new Rect(x * WINDOW_SIZE, y * WINDOW_SIZE - (WINDOW_SIZE / 2), WINDOW_SIZE, WINDOW_SIZE))).val[0];
                    if (extSAD < lowestDifference) {
                        lowestDifference = extSAD;
                        lowX = 0;
                        lowY = -WINDOW_SIZE / 2;
                    }
                }
                //Top right
                if (x < maxX && y > 0) {
                    for (int i = 1; i <= WINDOW_SIZE / 2; i++) {
                        extSAD = (int) Core.sumElems(current.submat(new Rect(x * WINDOW_SIZE + i, y * WINDOW_SIZE - (WINDOW_SIZE / 2), WINDOW_SIZE, WINDOW_SIZE))).val[0];
                        if (extSAD < lowestDifference) {
                            lowestDifference = extSAD;
                            lowX = i;
                            lowY = -WINDOW_SIZE / 2;
                        }
                    }
                }
                //Bottom left
                if (x > 0 && y < maxY) {
                    for (int i = -WINDOW_SIZE / 2; i > 0; i--) {
                        extSAD = (int) Core.sumElems(current.submat(new Rect(x * WINDOW_SIZE + i, y * WINDOW_SIZE + (WINDOW_SIZE / 2), WINDOW_SIZE, WINDOW_SIZE))).val[0];
                        if (extSAD < lowestDifference) {
                            lowestDifference = extSAD;
                            lowX = i;
                            lowY = -WINDOW_SIZE / 2;
                        }
                    }
                }
                //Bottom
                if (y < maxY) {
                    extSAD = (int) Core.sumElems(current.submat(new Rect(x * WINDOW_SIZE, y * WINDOW_SIZE + (WINDOW_SIZE / 2), WINDOW_SIZE, WINDOW_SIZE))).val[0];
                    if (extSAD < lowestDifference) {
                        lowestDifference = extSAD;
                        lowX = 0;
                        lowY = -WINDOW_SIZE / 2;
                    }
                }
                //Bottom right
                if (x < maxX && y < maxY) {
                    for (int i = 1; i <= WINDOW_SIZE / 2; i++) {
                        extSAD = (int) Core.sumElems(current.submat(new Rect(x * WINDOW_SIZE + i, y * WINDOW_SIZE + (WINDOW_SIZE / 2), WINDOW_SIZE, WINDOW_SIZE))).val[0];
                        if (extSAD < lowestDifference) {
                            lowestDifference = extSAD;
                            lowX = i;
                            lowY = -WINDOW_SIZE / 2;
                        }
                    }
                }
                //Upper left
                if (x > 0 && y > 0) {
                    for (int i = (-WINDOW_SIZE / 2) + 1; i < 0; i++) {
                        extSAD = (int) Core.sumElems(current.submat(new Rect(x * WINDOW_SIZE - (WINDOW_SIZE / 2), y * WINDOW_SIZE + (i), WINDOW_SIZE, WINDOW_SIZE))).val[0];
                        if (extSAD < lowestDifference) {
                            lowestDifference = extSAD;
                            lowX = -WINDOW_SIZE / 2;
                            lowY = i;
                        }
                    }
                }
                //Left
                if (x > 0) {
                    extSAD = (int) Core.sumElems(current.submat(new Rect(x * WINDOW_SIZE - (WINDOW_SIZE / 2), y * WINDOW_SIZE, WINDOW_SIZE, WINDOW_SIZE))).val[0];
                    if (extSAD < lowestDifference) {
                        lowestDifference = extSAD;
                        lowX = -WINDOW_SIZE / 2;
                        lowY = 0;
                    }

                }
                //Lower left
                if (x > 0 && y < maxY) {
                    for (int i = 1; i <= (WINDOW_SIZE / 2) - 1; i++) {
                        extSAD = (int) Core.sumElems(current.submat(new Rect(x * WINDOW_SIZE - (WINDOW_SIZE / 2), y * WINDOW_SIZE + (i), WINDOW_SIZE, WINDOW_SIZE))).val[0];
                        if (extSAD < lowestDifference) {
                            lowestDifference = extSAD;
                            lowX = -WINDOW_SIZE / 2;
                            lowY = i;
                        }
                    }
                }
                //Upper right
                if (x < maxX && y > 0) {
                    for (int i = (-WINDOW_SIZE / 2) + 1; i < 0; i++) {
                        extSAD = (int) Core.sumElems(current.submat(new Rect(x * WINDOW_SIZE + (WINDOW_SIZE / 2), y * WINDOW_SIZE + (i), WINDOW_SIZE, WINDOW_SIZE))).val[0];
                        if (extSAD < lowestDifference) {
                            lowestDifference = extSAD;
                            lowX = WINDOW_SIZE / 2;
                            lowY = i;
                        }
                    }
                }
                //Right
                if (x < maxX) {
                    extSAD = (int) Core.sumElems(current.submat(new Rect(x * WINDOW_SIZE + (WINDOW_SIZE / 2), y * WINDOW_SIZE, WINDOW_SIZE, WINDOW_SIZE))).val[0];
                    if (extSAD < lowestDifference) {
                        lowestDifference = extSAD;
                        lowX = WINDOW_SIZE / 2;
                        lowY = 0;
                    }

                }
                //Lower right
                if (x < maxX && y < maxY) {
                    for (int i = 1; i <= (WINDOW_SIZE / 2) - 1; i++) {
                        extSAD = (int) Core.sumElems(current.submat(new Rect(x * WINDOW_SIZE + (WINDOW_SIZE / 2), y * WINDOW_SIZE + (i), WINDOW_SIZE, WINDOW_SIZE))).val[0];
                        if (extSAD < lowestDifference) {
                            lowestDifference = extSAD;
                            lowX = WINDOW_SIZE / 2;
                            lowY = i;
                        }
                    }
                }
            } catch (CvException e) {
                e.printStackTrace();
                System.out.print(x + "|" + y + "|" + maxX + "|" + maxY + "|" +x*WINDOW_SIZE + "|" +y*WINDOW_SIZE);
                //System.exit(1);
            }

            return new Integer[]{lowX, lowY, lowestDifference, x, y};
    }
}
