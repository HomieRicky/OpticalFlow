import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


public class OpticalFlowDemo extends JFrame {
    static int FPS;
    static boolean isVideoPlaying = true;
    static volatile Mat frameToUse;
    static volatile Mat editedFrameToUse;
    static boolean first = true;
    static VideoCapture vc;
    static final int WINDOW_SIZE = 10;
    static final int PARALELLISM = 50;
    static long time = System.currentTimeMillis();


    public static void main(String args[]) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        vc = new VideoCapture();
        getInputs(args);
        Mat frame = new Mat();
        Mat m;
        Mat oldMat = new Mat();
        Mat flowDisplay;
        VideoWriter vw = new VideoWriter();
        int count = 1;

        while(vc.isOpened()) {
            vc.read(frame);
            if(!frame.empty()) {
                if(first) {
                    frameToUse = frame;
                    editedFrameToUse = frame;
                    OpticalFlowDemo window = new OpticalFlowDemo(frame.size(), FPS);
                    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                    frame.convertTo(oldMat, CvType.CV_8UC1);
                    vw = new VideoWriter("outvid.mp4", VideoWriter.fourcc('X','2','6','4'), FPS, frame.size());
                    first = false;
                } else {
                    m = frame.clone();
                    frameToUse = frame;
                    Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2GRAY);
                    m.convertTo(m, CvType.CV_8UC1);

                    //get conversions
                    int flowBytes[][][] = new int[m.cols()][m.rows()][3];
                    calcOpticalFlow(m, oldMat, m.cols()/WINDOW_SIZE, m.rows()/WINDOW_SIZE, flowBytes);

                    oldMat = m.clone();

                    flowDisplay = new Mat(frameToUse.size(), frameToUse.type(), Scalar.all(255));
                    int i = 0;
                    for(int j = 0; j < flowBytes.length; j++) {
                        for(int k = 0; k < flowBytes[j].length; k++) {
                            //Imgproc.circle(flowDisplay, new Point(k*WINDOW_SIZE + (WINDOW_SIZE/2), j*WINDOW_SIZE + (WINDOW_SIZE/2)), 4, new Scalar(flowBytes[j][k][0], flowBytes[j][k][1], flowBytes[j][k][2]), 2);
                            Imgproc.arrowedLine(flowDisplay, new Point(WINDOW_SIZE*k, WINDOW_SIZE*j), new Point((WINDOW_SIZE*k)-flowBytes[j][k][0], (WINDOW_SIZE*j)-flowBytes[j][k][1]), Scalar.all(flowBytes[j][k][2]));
                            i+=2;
                        }
                    }
                    editedFrameToUse = flowDisplay;
                    vw.write(editedFrameToUse);
                    System.out.println("frame " + count + " | " + (System.currentTimeMillis()-time) + "ms");
                    time = System.currentTimeMillis();
                    count++;
                }
            } else {
                vw.release();
                break;
            }
        }
        vc.release();
    }

    public OpticalFlowDemo(Size videoDimensions, final int fps) {
        JLabel vidLabel = new JLabel(new ImageIcon(matToBufferedImage(new Mat((int) videoDimensions.height, (int) videoDimensions.width, CvType.CV_8UC3, Scalar.all(0)))));
        JLabel editLabel = new JLabel(new ImageIcon(matToBufferedImage(new Mat((int) videoDimensions.height, (int) videoDimensions.width, CvType.CV_8UC3, Scalar.all(0)))));
        JButton toggle = new JButton("Toggle video");
        toggle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                isVideoPlaying = !isVideoPlaying;
            }
        });
        JPanel p = new JPanel();
        p.add(vidLabel);
        p.add(editLabel);
        p.add(toggle);
        JScrollPane scroll = new JScrollPane(p);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        add(scroll);
        //pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        new Thread() {
            public void run() {
                int i = 0;
                long time = System.currentTimeMillis();
                //System.out.println(1000/fps);
                while (true) {
                    if (isVideoPlaying && System.currentTimeMillis() - time > (1000 / fps)) {
                        time = System.currentTimeMillis();
                        vidLabel.setIcon(new ImageIcon(matToBufferedImage(frameToUse)));
                        editLabel.setIcon(new ImageIcon(matToBufferedImage(editedFrameToUse)));
                    }
                }
            }
        }.start();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    public static BufferedImage matToBufferedImage(Mat frame) {
        //Mat() to BufferedImage
        int type = 0;
        if (frame.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (frame.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();
        frame.get(0, 0, data);
        return image;
    }

    public static void getInputs(String[] args) {
        if(args.length != 3) {
            System.out.print("Proper argument useage: [cam|vid] [camera ID | video path] [fps]");
            System.exit(1);
        }
        if(args[0].equals("cam")) vc = new VideoCapture(Integer.parseInt(args[1]));
        else if(args[0].equals("vid")) vc = new VideoCapture(args[1]);
        else {
            System.out.print("Proper argument useage: [cam|vid] [camera ID | video path] [fps]");
            System.exit(1);
        }
        FPS = Integer.parseInt(args[2]);
    }

    public static void calcOpticalFlow(Mat current, Mat old, int numBlocksX, int numBlocksY, int returnData[][][]) {
        ExecutorService executor = Executors.newWorkStealingPool(PARALELLISM);
        ArrayList<Callable<Integer[]>> callables = new ArrayList<>();
        for(int i = 0; i < numBlocksY; i++) {
            for(int j = 0; j < numBlocksX; j++) {
                callables.add(new GetSADDifference(current, old, j, i, numBlocksX-1, numBlocksY-1, WINDOW_SIZE));
            }
        }
        try {
            List<Future<Integer[]>> futures = executor.invokeAll(callables);
            for(Future<Integer[]> future : futures) {
                Integer[] result = future.get();
                returnData[result[4]][result[3]][0] = result[0];
                returnData[result[4]][result[3]][1] = result[1];
                returnData[result[4]][result[3]][2] = result[2];
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }
}
