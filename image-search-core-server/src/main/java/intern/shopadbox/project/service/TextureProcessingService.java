package intern.shopadbox.project.service;

import org.opencv.core.Mat;
import java.io.IOException;

/*
 * 이미지 프로세싱 - 질감 이용 작업 Service 인터페이스
 */

public interface TextureProcessingService {

    Mat cropImage(Mat img);

    Mat cannyEdgeImage(Mat img);

    int[] procImageToULBPHistogram(Mat img);

    int[] ULBPtoURLBPHistogram(int[] ULBPHistogram);

    void histogramToCSV(int[] histogram, String filePath) throws IOException;

}