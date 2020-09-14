package intern.shopadbox.project.api;

import intern.shopadbox.project.domain.Image;
import intern.shopadbox.project.dto.ImageSearchRes;
import intern.shopadbox.project.dto.ImageSearchReq;
import intern.shopadbox.project.enums.SearchType;
import intern.shopadbox.project.service.serviceImpl.ImageServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 유사 이미지 검색 서비스 Controller
 */

@Slf4j
@RestController
@RequestMapping("/images")
public class ImageController {


    private final ImageServiceImpl imageServiceImpl;


    public ImageController(final ImageServiceImpl imageServiceImpl) {

        this.imageServiceImpl = imageServiceImpl;
    }


    /**
     * 유사 이미지 검색
     *
     * @param imageReq
     * @return List<ImageSearchRes>
     */
    @PostMapping("/search")
    public List<ImageSearchRes> searchImage(@RequestBody ImageSearchReq imageReq) {
        try {
            if (imageReq.getSearchType() != SearchType.ALL) {

                return imageServiceImpl.sortAndNormalizeSearchTypeSingleImageSearchResult(imageServiceImpl.searchImage(imageReq));

            } else {

                List<List<ImageSearchRes>> searchResultList = new ArrayList<>();

                Arrays.asList(SearchType.COLOR, SearchType.PATTERN).stream().forEach(s -> {

                    imageReq.setSearchType(s);
                    searchResultList.add(imageServiceImpl.searchImage(imageReq));

                });

                return imageServiceImpl.sortAndNormalizeSearchTypeAllImageSearchResult(searchResultList, imageReq.getScoreRatioArr());

            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }


    /**
     * 이미지 저장
     *
     * @param image
     */
    @PostMapping("/register")
    public void registerImage(@RequestBody Image image) {
        imageServiceImpl.saveImage(image);
    }


    /**
     * 이미지 전체 조회
     *
     * @return List<ImageSearchRes>
     */
    @GetMapping("/search/all")
    public List<ImageSearchRes> getAllImages() {
        return imageServiceImpl.findAllImages();
    }


    /**
     * ES에 탑탑 이미지 정보 저장
     */
    @GetMapping("/saveDummy")
    public void saveDummyImages(@RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "1000") int pageSize) {
        imageServiceImpl.saveDummyData(page, pageSize);
    }

}