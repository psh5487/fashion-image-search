package intern.shopadbox.project.service.serviceImpl;

import intern.shopadbox.project.domain.Image;
import intern.shopadbox.project.dto.ImageSearchReq;
import intern.shopadbox.project.enums.SearchType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ImageServiceImplTest {

    @Autowired
    private ImageServiceImpl imageServiceImpl;

    @Test
    public void testSearchImage() throws Exception {

        String item_id = "4789280331";
        String item_name = "오픈 브이넥 카라 쉬폰 블라우스 blouse";
        String img_url = "https://shop-phinf.pstatic.net/20200113_190/1578900635615FbMKM_JPEG/129433_1.jpg?type=w640";
        String category_id = "00000028";

        Image mockImg = Image.builder()
                .itemId(item_id)
                .itemName(item_name)
                .imgUrl(img_url)
                .categoryId(category_id)
                .build();

        ImageSearchReq imageSearchReq = ImageSearchReq.builder()
                .searchType(SearchType.PATTERN)
                .image(mockImg)
                .build();

        imageServiceImpl.searchImage(imageSearchReq);
    }

    @Test
    public void testSaveImageFromUrl() {

            String item_id = "4789280331";
            String item_name = "오픈 브이넥 카라 쉬폰 블라우스 blouse";
            String img_url = "https://shop-phinf.pstatic.net/20200113_190/1578900635615FbMKM_JPEG/129433_1.jpg?type=w640";
            String category_id = "00000028";

            Image mockImg = Image.builder()
                    .itemId(item_id)
                    .itemName(item_name)
                    .imgUrl(img_url)
                    .categoryId(category_id)
                    .build();

            imageServiceImpl.saveImage(mockImg);

    }

    @Test
    public void testSaveTextureFromUrl() {

        for(int i=1; i<=24; i++){
            String item_id = Integer.toString(i);
            String item_name = item_id;
            String img_url = "";
            String category_id = "";

            Image mockImg = Image.builder()
                    .itemId(item_id)
                    .itemName(item_name)
                    .imgUrl(img_url)
                    .categoryId(category_id)
                    .build();

            imageServiceImpl.saveImage(mockImg);
        }
    }

    @Test
    public void testSaveDummyData() {

        imageServiceImpl.saveDummyData(1, 2000);
    }

}