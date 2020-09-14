package intern.shopadbox.project.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;

/**
 * Image 검색 응답 DTO
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageSearchRes implements Comparable<ImageSearchRes> {

    private String itemId;          // 상품 고유 번호
    private String itemName;        // 상품 이름
    private String categoryId;      // 상품 카테고리 고유 번호
    private String imgUrl;          // 상품 이미지 url

    private String registerTime;    // 등록 일자

    private float score;                        // 유사도 점수
    private String dominantColorHexCandidate;   // 주요 색상 후보(RGB Hex)
    private String dominantColorHsvCandidate;   // 주요 색상 후보(HSV)


    // itemId 기준 오름차 순 정렬
    @Override
    public int compareTo(ImageSearchRes o) {
        if (Long.parseLong(this.getItemId()) > Long.parseLong(o.getItemId())) {
            return 1;
        }
        return -1;
    }

    // score 기준 오름차 순 정렬
    @Data
    public static class ImageRespScoreComparator implements Comparator<ImageSearchRes> {
        @Override
        public int compare(ImageSearchRes o1, ImageSearchRes o2) {
            if (o1.getScore() > o2.getScore()) return 1;
            return -1;
        }
    }

}