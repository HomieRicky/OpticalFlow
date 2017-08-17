import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by RicardoFS on 14/08/2017.
 */
public class Bitmosher {
    static int FPS;
    static VideoCapture vc = null;
    static FileInputStream fr = null;
    static Mat m;
    static Mat memoryFrame;
    static int flowData[][][];

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        getInputs(args);
        while(vc.isOpened()) {
            vc.read(m);
            if(!m.empty()) {
                //todo: add stuff

            } else {
                vc.release();
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
            System.exit(1);
        }
    }
}
