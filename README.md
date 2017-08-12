# OpticalFlow
Trying to make visual effects with optical flow. Uses OpenCV

Dependencies:
-OpenCV 3.2
-FFMpeg DLL for OpenCV in your project folder (you can find the DLL in [opencv_folder]\build\bin to read mp4 files
-OpenH264 to output/encode mp4 files with h.264 encoding

Program takes the following 3 arguments:
[cam|vid] [camera ID | video path] [output video fps]

Next milestone is to export the flow data and simulate datamoshing effects
