package intern.shopadbox.project.dto;

import intern.shopadbox.project.domain.Image;
import intern.shopadbox.project.enums.SearchType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Image 검색 요청 DTO
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageSearchReq {
    private SearchType searchType;  // 검색 타입 (DESCRIPTOR, COLOR, PATTERN, TF_FEATURES, ALL)
    private float[] scoreRatioArr;  // 전체(ALL) 검색시 (COLOR, PATTERN)의 score 반영 비율
    private Image image;            // Image 도메인 클래스
}