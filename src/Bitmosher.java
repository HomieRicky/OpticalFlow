import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by RicardoFS on 14/08/2017.
 */
public class Bitmosher {
    static int FPS;
    static VideoCapture vc = null;
    static FileInputStream fr = null;
    static VideoWriter vw = null;
    static Mat m;
    static Mat memoryFrame;
    static int WINDOW_SIZE = 0;
    static int flowData[][][] = null;
    static int THRESHOLD = 200;
    static FrameHeaders header;
    static FrameHeaders lastHeader = FrameHeaders.SKIP;
    static boolean first = true;
    static int count = 0;


    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        m = new Mat();
        getInputs(args);
        while(vc.isOpened()) {
            vc.read(m);
            if(!m.empty()) {
                if(first) {
                    vw = new VideoWriter("moshed.mp4", VideoWriter.fourcc('X', '2', '6', '4'), FPS, m.size());
                    first = false;
                }
                try {
                    header = FrameHeaders.getHeader((byte) fr.read());
                    if (header == FrameHeaders.ONLY_APPLY_MOVEMENTS) {
                        byte[] intBytes = new byte[4];
                        fr.read(intBytes);
                        int frameHeight = ByteBuffer.wrap(intBytes).order(ByteOrder.BIG_ENDIAN).getInt();
                        fr.read(intBytes);
                        int frameWidth = ByteBuffer.wrap(intBytes).order(ByteOrder.BIG_ENDIAN).getInt();
                        WINDOW_SIZE = m.cols()/frameWidth;
                        flowData = new int[frameHeight][frameWidth][3];
                        System.out.println(frameWidth + " " + frameHeight + " " + WINDOW_SIZE);
                        for(int i = 0; i < frameHeight; i++) for(int j = 0; j < frameWidth; j++) {
                            fr.read();
                            fr.read();
                            fr.read(intBytes);
                            if(ByteBuffer.wrap(intBytes).order(ByteOrder.BIG_ENDIAN).getInt() < THRESHOLD) {
                                try {
                                    Mat copy = m.submat(new Rect(j*WINDOW_SIZE, i*WINDOW_SIZE, WINDOW_SIZE, WINDOW_SIZE));
                                    copy.copyTo(memoryFrame.submat(new Rect(j*WINDOW_SIZE, i*WINDOW_SIZE, WINDOW_SIZE, WINDOW_SIZE)));

                                } catch (CvException e) {
                                    e.printStackTrace();
                                    System.out.print(i + " " + j);
                                    System.exit(1);
                                }

                            }
                        }

                        vw.write(memoryFrame);

                    } else if (header == FrameHeaders.APPLY_MOVEMENT_TRANSLATE_MOVEMENT) {
                        //TEMPORARY
                        memoryFrame = m.clone();
                        vw.write(m);
                    } else {
                        memoryFrame = m.clone();
                        vw.write(m);
                    }
                    lastHeader = header;
                    System.out.println(count);
                    count++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                vc.release();
                vw.release();
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void getInputs(String[] args) {
        if(args.length != 3) {
            System.out.print("Proper argument useage: [input video path] [input data path] [fps]");
            System.exit(1);
        }
        FPS = Integer.parseInt(args[2]);
        vc = new VideoCapture(args[0]);
        try {
            fr = new FileInputStream(args[1]);
        } catch (FileNotFoundException e) {
            System.out.println("Data file not found");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
