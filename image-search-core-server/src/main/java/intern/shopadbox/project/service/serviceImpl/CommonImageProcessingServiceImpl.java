package intern.shopadbox.project.service.serviceImpl;

import intern.shopadbox.project.domain.Image;
import intern.shopadbox.project.service.CommonImageProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/*
 * 이미지 프로세싱 - 공통 작업 ServiceImpl
 */

@Slf4j
@Service
public class CommonImageProcessingServiceImpl implements CommonImageProcessingService {

    @Override
    public BufferedImage urlToBufferedImage(final String ImgUrl) throws IOException {

        URL url = new URL(ImgUrl);
        BufferedImage bufferedImage = ImageIO.read(url);

        return bufferedImage;
    }

    @Override
    public Mat bufferedImageToMat(final BufferedImage bufferedImage) throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
        byteArrayOutputStream.flush();

        return new MatOfByte(byteArrayOutputStream.toByteArray());
    }

    /*
     * 요청 이미지 객체 내 URL -> Local 에 itemId.jpg 로 File 저장 -> File 읽어오기
     */
    @Override
    public File getLocalFileFromImageUrl(final Image image, final String filePath) {

        try {

            // 이미지의 url 로부터 이미지 파일을 Local 에 저장
            String fileName = image.getItemId() + ".jpg";
            saveImageFromUrl(image.getImgUrl(), filePath, fileName);

            // Local에 저장된 파일 읽기
            File imgFile = new File(filePath + fileName);

            return imgFile;

        } catch (Exception e) {
            return null;
        }
    }

    private static void saveImageFromUrl(String imgUrl, String path, String fileName) {

        try {

            URL url = new URL(imgUrl);
            BufferedImage img = ImageIO.read(url);
            File file = new File(path + fileName);
            ImageIO.write(img, "jpg", file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String[][] MatDescriptorsTo2DArray(Mat descriptors, int rows, int cols){

        String[][] desArray = new String[rows][cols];

        for (int i = 0; i < rows; i++) {

            String temp = "";

            for (int j = 0; j < cols; j++) {

                double buff = descriptors.get(i, j)[0];
                desArray[i][j] = Integer.toString((int) buff);
                temp += ((int) buff);
            }
        }

        return desArray;
    }

    @Override
    public List<String> ArrayDescriptorsTo1DList(final String[][] feature_matrix) {

        // matrix size
        int row_size = feature_matrix.length;
        int col_size = feature_matrix[0].length;

        // 2차원 Descriptor Array 를 1차원 List 에 담기
        // ex) {{"001", "011", "111"}, {"001", "011", "111"}} -> {"001, 011, 111", "001, 011, 111"}

        List<String> orbMatrix = new ArrayList<>();

        for (int i = 0; i < row_size; i++) {

            String orbMatrixToString = "";

            for (int j = 0; j < col_size; j++) {

                orbMatrixToString += feature_matrix[i][j];

                if (j != col_size - 1) {
                    orbMatrixToString += ",";
                }

            }
            orbMatrix.add(orbMatrixToString);
        }

        return orbMatrix;
    }
}