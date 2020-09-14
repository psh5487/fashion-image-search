package intern.shopadbox.project.service;

import intern.shopadbox.project.domain.Image;
import intern.shopadbox.project.dto.ImageSearchRes;
import intern.shopadbox.project.dto.ImageSearchReq;

import java.util.List;

/*
 * 유사 이미지 검색 서비스 메인 서비스
 */

public interface ImageService {

    List<ImageSearchRes> findAllImages();

    void saveImage(final Image image);

    List<ImageSearchRes> searchImage(final ImageSearchReq imageSearchReq);

    List<ImageSearchRes> sortAndNormalizeSearchTypeSingleImageSearchResult(final List<ImageSearchRes> result);

    List<ImageSearchRes> sortAndNormalizeSearchTypeAllImageSearchResult(
            final List<List<ImageSearchRes>> prevResultList,
            final float[] scoreRatioArr);

    void saveDummyData(int page, int pageSize);

}