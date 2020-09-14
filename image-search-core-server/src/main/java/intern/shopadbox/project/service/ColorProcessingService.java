package intern.shopadbox.project.service;

import java.awt.image.BufferedImage;
import java.util.List;

/*
 * 이미지 프로세싱 - 색상 이용 작업 Service 인터페이스
 */

public interface ColorProcessingService {

    List<String> getAllDominantColorCandidate(BufferedImage bufferedImage);

    List<String> getColorFromImageCenter(BufferedImage bufferedImage, boolean isHexColor);

}