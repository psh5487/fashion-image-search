package intern.shopadbox.project.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Image {

    private String itemId;          // 상품 고유 번호
    private String itemName;        // 상품 이름
    private String categoryId;      // 상품 카테고리 고유 번호
    private String imgUrl;          // 상품 이미지 url

    private String registerTime;    // 상품 등록 시간

    private String dominantColorHexCandidate;   // 주요 색상 후보(RGB Hex)
    private String dominantColorHsvCandidate;   // 주요 색상 후보(HSV)
    private List<String> orbMatrix;             // ORB 특징점
    private int[] lbpHistogram;                 // LBP 히스토그램


    // 이미지 프로세싱 후 사용되는 setter
    public void setFeatureValues(final String dominantColorHexCandidate,
                                 final String dominantColorHsvCandidate,
                                 final List<String> orbMatrix,
                                 final int[] lbpHistogram){

        this.registerTime =
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.dominantColorHexCandidate = dominantColorHexCandidate;
        this.dominantColorHsvCandidate = dominantColorHsvCandidate;
        this.orbMatrix = orbMatrix;
        this.lbpHistogram = lbpHistogram;

    }

}