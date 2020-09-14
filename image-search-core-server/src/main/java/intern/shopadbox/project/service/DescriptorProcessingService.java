package intern.shopadbox.project.service;

import org.opencv.core.Mat;

import java.util.List;

/*
 * 이미지 프로세싱 - 특징점 이용 작업 Service 인터페이스
 */

public interface DescriptorProcessingService {

    List<String> procImageUsingORB(Mat img);

    List<String> procImageUsingBrisk(Mat img);

}
