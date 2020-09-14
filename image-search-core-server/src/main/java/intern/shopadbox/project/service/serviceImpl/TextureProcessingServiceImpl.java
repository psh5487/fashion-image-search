package intern.shopadbox.project.service.serviceImpl;

import intern.shopadbox.project.service.TextureProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/*
 * 이미지 프로세싱 - 질감 처리 작업 ServiceImpl
 */

@Slf4j
@Service
public class TextureProcessingServiceImpl implements TextureProcessingService {

    // Non-Uniform Binary Pattern 횟수를 저장하는 히스토그램 배열 Index
    private static final int NON_UNIFORM_BIN = 256;

    /*
     * 가운데 영역 이미지 자르기(OpenCV)
     *
     * @param File imgFile
     * @return Mat imgCropped
     */
    @Override
    public Mat cropImage(Mat img) {

        // 관심 영역 설정 - x, y, width, height
        int x = (img.width() / 5) * 2;
        int y = (img.height() / 5) * 2;
        int w = x + 120;
        int h = y + 120;

        // 두 개의 좌표로 Rectangle 만듦
        Rect area = new Rect(new Point(x, y), new Point(w, h));

        Mat imgCropped = img.submat(area);

        return imgCropped;
    }

    /*
     * CannyEdge 처리된 이미지 구하기(OpenCV) - Noise 제거
     *
     * @param  Mat img
     * @return Mat cannyImg
     */

    @Override
    public Mat cannyEdgeImage(Mat img) {

        // Settings
        Size BLUR_SIZE = new Size(3, 3);
        int lowThresh = 40;
        int RATIO = 3;
        int KERNEL_SIZE = 3;

        // Canny Detection
        Mat imgBlur = new Mat();
        Mat detectedEdges = new Mat();

        Imgproc.blur(img, imgBlur, BLUR_SIZE);
        Imgproc.Canny(imgBlur, detectedEdges, lowThresh, lowThresh * RATIO, KERNEL_SIZE, false);

        Mat cannyImg = new Mat(img.size(), CvType.CV_8UC3, Scalar.all(0));
        img.copyTo(cannyImg, detectedEdges);

        return cannyImg;
    }

    /*
     * ULBP Histogram 구하기
     *
     * (1) image 의 모든 픽셀을 탐색하며, 각 픽셀마다 주변 8개 픽셀과 밝기 비교
     * (2) Uniform Local Binary Patterns(ULBP) 를 만듦
     *     - Uniform LBP 는 Binary Pattern 이 Uniform(0->1 또는 1->0 변화 횟수가 2회 이하)일 때 각각 카운트
     *     -               "                 Non-Uniform 인 경우는 히스토그램 Bin 한 개에 몰아서 카운트
     *     - ULBP 종류는 총 58 가지 ex) 00110000
     * (3) ULBP 종류 별로 등장 횟수를 세어, LBP Histogram 을 만듦
     *     ex) 00110000 의 등장 횟수 10번이라면, histogram[48] = 10
     *
     * @param Mat image
     * @return Mat cannyImg
     */
    @Override
    public int[] procImageToULBPHistogram(Mat img) {

//        System.out.println("image Mat channel: "+ image.channels());
//        System.out.println("image Mat type: "+ image.type());

        // LBP 라면, 256 개의 히스토그램 Bin(0~255)을 사용
        // ULBP 라면, 58 개의 Uniform Pattern 히스토그램 Bin + 1개의 Non-Uniform Pattern 히스토그램 Bin 을 사용
        int[] histogram = new int[257];

        for (int x = 1; x < img.rows() - 2; x++) {
            for (int y = 1; y < img.cols() - 2; y++) {
                int[] diffs = new int[8];

                // Find the differences between the current center pixel and its neighbours
                double center = img.get(x, y)[0];

                diffs[0] = (Double.compare(img.get(x, y + 1)[0], center) >= 0) ? 1 : 0;
                diffs[1] = (Double.compare(img.get(x + 1, y + 1)[0], center) >= 0) ? 1 : 0;
                diffs[2] = (Double.compare(img.get(x + 1, y)[0], center) >= 0) ? 1 : 0;
                diffs[3] = (Double.compare(img.get(x + 1, y - 1)[0], center) >= 0) ? 1 : 0;
                diffs[4] = (Double.compare(img.get(x, y - 1)[0], center) >= 0) ? 1 : 0;
                diffs[5] = (Double.compare(img.get(x - 1, y - 1)[0], center) >= 0) ? 1 : 0;
                diffs[6] = (Double.compare(img.get(x - 1, y)[0], center) >= 0) ? 1 : 0;
                diffs[7] = (Double.compare(img.get(x - 1, y + 1)[0], center) >= 0) ? 1 : 0;

                // Binary pattern 이 Uniform 인지 Check
                // Binary pattern 이 Uniform 이라면, Binary Pattern 에 해당하는 히스토그램 값 증가
                // Non-Uniform 이라면, NON_UNIFORM_BIN 히스토그램 값 증가
                if (isBinaryUniform(diffs)) {
                    int histIndex = 0;

                    for (int i = 0; i < diffs.length; i++) {
                        histIndex += diffs[i] * Math.pow(2, i);
                    }

                    histogram[histIndex] += 1;
                } else {
                    histogram[NON_UNIFORM_BIN] += 1;
                }
            }
        }

        return histogram;
    }

    public boolean isBinaryUniform(int[] binary) {
        int transitions = 0;

        for (int i = 0; i < binary.length - 1; i++) {
            if (binary[i] != binary[i + 1]) {
                transitions++;
            }
        }
        return transitions <= 2;
    }

    /*
     * ULBP 히스토그램으로부터 URLBP 히스토그램 구하기
     *
     * URBLP 는 ULBP의 회전의 취약함을 보완. 회전시키면 같은 패턴인 것을 하나로 여김
     * ex) 00001111(15), 00011110(30), 00111100(60), 01111000(120),
     *     10000111(135), 11000011(195), 11100001(225), 11110000(240) 은 모두 같은 패턴
     *
     * @param int[] ULBPHistogram
     * @return int[10] URLBPHistogram
     */
    @Override
    public int[] ULBPtoURLBPHistogram(int[] ULBPHistogram) {

        // Uniform Bin 9개 + Non-Uniform Bin 1개 사용
        int[] URLBPHistogram = new int[10];

        // index가 0(00000000) 일 경우
        URLBPHistogram[0] = ULBPHistogram[0];

        for (int i = 1; i < 8; i++) {
            byte index = (byte) (Math.pow(2, i) - 1);

            //한 칸씩 미루더라도, 자리를 넘어가지 않을 경우 ex) 00000011(3), 00001100(6), ..., 11000000(192)
            for (int j = 0; j <= 8 - i; j++) {
                URLBPHistogram[i] += ULBPHistogram[index & 0xFF];
                index <<= 1;
            }

            //한 칸씩 미루다가, 자리를 넘어갈 경우 ex) 10000001(129)
            for (int j = 0; j < i - 1; j++) {
                index += 1;
                URLBPHistogram[i] += ULBPHistogram[index & 0xFF];
                index <<= 1;
            }
        }

        // index가 255(11111111) 일 경우
        URLBPHistogram[8] = ULBPHistogram[255];

        URLBPHistogram[9] = ULBPHistogram[NON_UNIFORM_BIN];

        return URLBPHistogram;
    }

    /*
     * 히스토그램을 csv 파일로 저장하기
     *
     * @param int[] histogram, String filePath
     */
    @Override
    public void histogramToCSV(int[] histogram, String filePath) throws IOException {

        BufferedWriter bf = new BufferedWriter(new FileWriter(filePath + ".csv", false));

        bf.write("binaryString" + "," + "count");
        bf.newLine();

        for (int i = 0; i < histogram.length; i++) {
            bf.write(i + "," + histogram[i]);
            bf.newLine();
        }

        bf.flush();
        bf.close();
    }

}