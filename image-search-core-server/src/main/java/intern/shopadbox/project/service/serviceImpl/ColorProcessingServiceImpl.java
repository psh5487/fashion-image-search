package intern.shopadbox.project.service.serviceImpl;

import intern.shopadbox.project.service.ColorProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/*
 * 이미지 프로세싱 - 색상 이용 작업 ServiceImpl
 */


@Slf4j
@Service
public class ColorProcessingServiceImpl implements ColorProcessingService {

    final float resizeRatio = 0.9f;

    /**
     * 모든 주요 색상 후보 추출
     */
    @Override
    public List<String> getAllDominantColorCandidate(BufferedImage bufferedImage) {

        // 이미지 중앙 기준 주요 RGB Hex 색상 추출
        List<String> dominantHexColorListFromImageCenter =
                getColorFromImageCenter(bufferedImage, true);

        // 이미지 중앙 기준 주요 HSV 색상 추출
        List<String> dominantHSVColorListFromImageCenter =
                getColorFromImageCenter(bufferedImage, false);

        List<String> dominantColorList = new ArrayList<>();

        dominantColorList.add(dominantHexColorListFromImageCenter.get(0));
        dominantColorList.add(dominantHSVColorListFromImageCenter.get(0));

        return dominantColorList;
    }


    /**
     * 이미지 중앙 기준 주요 RGB Hex 색상 추출
     */
    @Override
    public List<String> getColorFromImageCenter(BufferedImage bufferedImage, boolean isHexColor) {

        // 중앙 기준으로 resize한 image 좌표 위치, 길이 반환
        final int[] resizedValues = calculateResizedImagePosAndSizeValues(bufferedImage, resizeRatio);

        // 중앙 기준으로 자르기
        final BufferedImage croppedImage =
                bufferedImage.getSubimage(resizedValues[0], resizedValues[1], resizedValues[2], resizedValues[3]);

        Map<Integer, Integer> colorMap = new HashMap<>();

        for (int x = 0; x < croppedImage.getWidth(); x++) {

            for (int y = 0; y < croppedImage.getHeight(); y++) {

                // (x,y) 좌표 위치에 대응하는 RGB 값 반환
                int rgb = croppedImage.getRGB(x, y);

                // 해당 rgb값의 현재 counter 값 반환
                Integer counter = colorMap.get(rgb);

                // counter가 null일 경우 counter를 0으로 바꿔준다.
                if (counter == null) {
                    counter = 0;
                }

                // 해당 rgb를 Key로 하는 colorMap의 Value(counter)를 1 증가시킨다.
                colorMap.put(rgb, ++counter);

            }
        }
        if(isHexColor) { return getDominantCommonColorListAsHex(colorMap); }
        else { return getDominantCommonColorListAsHSV(colorMap); }
    }


    /**
     * RGB ColorMap을 RGB Hex String List로 변환
     */
    private static List<String> getDominantCommonColorListAsHex(Map<Integer, Integer> map) {

        List<Map.Entry<Integer, Integer>> list = new LinkedList<>(map.entrySet());

        // counter 기준으로 내림차순으로 정렬
        Collections.sort(list, (Map.Entry<Integer, Integer> obj1, Map.Entry<Integer, Integer> obj2)
                -> ((Comparable) obj1.getValue()).compareTo(obj2.getValue()));

        List<String> hexList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {

            Map.Entry<Integer, Integer> entry = list.get(list.size() - (i + 1));

            int[] rgb = getRGBArr(entry.getKey());

            String hex =
                    "#" + String.format("%2s", Integer.toHexString(rgb[0])).replace(' ', '0')
                            + String.format("%2s", Integer.toHexString(rgb[1])).replace(' ', '0')
                            + String.format("%2s", Integer.toHexString(rgb[2])).replace(' ', '0');

            hexList.add(hex);

        }

        return hexList;

    }

    /**
     * RGB ColorMap을 HSV String List로 변환
     */
    private static List<String> getDominantCommonColorListAsHSV(Map<Integer, Integer> map) {

        List<Map.Entry<Integer, Integer>> list = new LinkedList<>(map.entrySet());

        // counter 기준으로 내림차순으로 정렬
        Collections.sort(list, (Map.Entry<Integer, Integer> obj1, Map.Entry<Integer, Integer> obj2)
                -> ((Comparable) obj1.getValue()).compareTo(obj2.getValue()));

        List<String> hsvList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {

            Map.Entry<Integer, Integer> entry = list.get(list.size() - (i + 1));

            int[] rgb = getRGBArr(entry.getKey());

            float[] hsvAsFloat = new float[3];

            Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], hsvAsFloat);
            String hsv = (hsvAsFloat[0] * 360) + "," + (hsvAsFloat[1] * 100) + "," + (int) (hsvAsFloat[2] * 100);

            hsvList.add(hsv);

        }

        return hsvList;
    }

    /**
     * 중앙 기준으로 resize한 image 좌표 위치, 길이 반환
     */
    private static int[] calculateResizedImagePosAndSizeValues(BufferedImage bufferedImage, float resizeRatio) {

        final int newWidth = (int) (bufferedImage.getWidth() * (1.0 - resizeRatio));
        final int newHeight = (int) (bufferedImage.getHeight() * (1.0 - resizeRatio));

        final int startX = ((bufferedImage.getWidth()) - newWidth) / 2;
        final int startY = ((bufferedImage.getHeight()) - newHeight) / 2;

        return new int[]{ startX, startY, newWidth, newHeight};

    }

    /**
     * int pixel 값 -> int[] rgb 배열 변환
     */
    private static int[] getRGBArr(int pixel) {

        int alpha = (pixel >> 24) & 0xff;
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;

        return new int[]{red, green, blue};

    }
}