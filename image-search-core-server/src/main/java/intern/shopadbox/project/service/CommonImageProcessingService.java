package intern.shopadbox.project.service;

import intern.shopadbox.project.domain.Image;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/*
 * 이미지 프로세싱 - 공통 작업 Service 인터페이스
 */

public interface CommonImageProcessingService {

    BufferedImage urlToBufferedImage(final String ImgUrl) throws IOException;

    Mat bufferedImageToMat(final BufferedImage bufferedImage) throws IOException;

    File getLocalFileFromImageUrl(final Image image, final String filePath);

    String[][] MatDescriptorsTo2DArray(Mat descriptors, int rows, int cols);

    List<String> ArrayDescriptorsTo1DList(final String[][] feature_matrix);

}
