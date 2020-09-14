package intern.shopadbox.project.service.serviceImpl;

import intern.shopadbox.project.service.DescriptorProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.features2d.BRISK;
import org.opencv.features2d.ORB;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * 이미지 프로세싱 - 특징점 이용 작업 ServiceImpl
 */

@Slf4j
@Service
public class DescriptorProcessingServiceImpl implements DescriptorProcessingService {

    @Value("${orb.feature.max}")
    private int MAX_FEATURES_ORB;

    @Value("${brisk.feature.max}")
    private int MAX_FEATURES_BRISK;

    private final CommonImageProcessingServiceImpl commonImageProcessingServiceImpl;

    public DescriptorProcessingServiceImpl(final CommonImageProcessingServiceImpl commonImageProcessingServiceImpl) {
        this.commonImageProcessingServiceImpl = commonImageProcessingServiceImpl;
    }

    /*
     * ORB(OpenCV)로 이미지 Feature(특징점) Descriptor 구하기
     *
     * @param File imageFile
     * @return List<String> desString
     */
    @Override
    public List<String> procImageUsingORB(Mat img) {

        try {

            // ORB Detector 생성
            ORB orbDetector = ORB.create(75, 1.2f, 8, 31, 0, 2, ORB.HARRIS_SCORE, 31, 20);

            // ORB Feature 개수 제한
            orbDetector.setMaxFeatures(MAX_FEATURES_ORB);

            // Detect & Compute ORB Descriptor - Feature 정보를 담음
            MatOfKeyPoint keyPoints = new MatOfKeyPoint();
            Mat descriptors = new Mat();

            orbDetector.detectAndCompute(img, new Mat(), keyPoints, descriptors);

            // ORB Mat Descriptor 를 2D String Array 로 표현
            String[][] desArray = commonImageProcessingServiceImpl.MatDescriptorsTo2DArray(descriptors, descriptors.rows(), descriptors.cols());

            // 2D String Array Descriptor 를 1D String List 로 표현 - ES 에서 계산량 줄이기 위함
            List<String> desString = commonImageProcessingServiceImpl.ArrayDescriptorsTo1DList(desArray);

            return desString;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * BRISK(OpenCV)로 이미지 Feature(특징점) Descriptor 구하기
     *
     * @param  File imageFile
     * @return List<String> desString
     */
    @Override
    public List<String> procImageUsingBrisk(Mat img) {

        try {

            // BRISK Detector 생성 - "thresh" : 민감도, 30 아래여야. "octaves" : Layer 수, 여러 개 필요X. "pattenScale" : 배경 빼는 역할
            BRISK brisk = BRISK.create(20, 0, 10.0f);

            // Detect & Compute BRISK Descriptor - Feature 정보를 담음
            Mat descriptors = new Mat();
            MatOfKeyPoint keyPoints = new MatOfKeyPoint();

            brisk.detectAndCompute(img, new Mat(), keyPoints, descriptors);

            // BRISK 점이 하나도 안 찍힐 경우, 초기 옵션으로 재진행
            if (descriptors.rows() == 0) {
                brisk = BRISK.create();
                brisk.detectAndCompute(img, new Mat(), keyPoints, descriptors);
            }

            // BRISK Feature 수 제한
            int rows = 0;
            int maxRows = MAX_FEATURES_BRISK;

            if (descriptors.rows() > maxRows) {
                rows = maxRows;
            } else {
                rows = descriptors.rows();
            }

            // BRISK Mat Descriptor 를 2D String Array 로 표현
            String[][] desArray = commonImageProcessingServiceImpl.MatDescriptorsTo2DArray(descriptors, rows, descriptors.cols());

            // 2D String Array Descriptor 를 1D String List 로 표현 - ES 에서 계산량 줄이기 위함
            List<String> desString = commonImageProcessingServiceImpl.ArrayDescriptorsTo1DList(desArray);

            return desString;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
