package intern.shopadbox.project.service.serviceImpl;

import intern.shopadbox.project.domain.Image;
import intern.shopadbox.project.dto.ImageSearchRes;
import intern.shopadbox.project.dto.ImageSearchReq;
import intern.shopadbox.project.enums.SearchType;
import intern.shopadbox.project.service.ImageService;

import lombok.extern.slf4j.Slf4j;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.index.query.functionscore.ScriptScoreQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.opencv.core.*;

import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.List;

/**
 * 유사 이미지 검색 서비스 Main ServiceImpl
 */

@Slf4j
@Service
public class ImageServiceImpl implements ImageService {

    @Value("${fashionImage.toptop}")
    private String imageResource;

    private final RestHighLevelClient restHighLevelClient;
    private final DescriptorProcessingServiceImpl descriptorProcessingServiceImpl;
    private final TextureProcessingServiceImpl textureProcessingServiceImpl;
    private final ColorProcessingServiceImpl colorProcessingServiceImpl;
    private final CommonImageProcessingServiceImpl commonImageProcessingServiceImpl;
    private final ElasticSearchRestClientServiceImpl elasticSearchRestClientServiceImpl;
    private final FlaskApiHandlingServiceImpl flaskApiHandlingServiceImpl;

    public ImageServiceImpl(final RestHighLevelClient restHighLevelClient,
                            final DescriptorProcessingServiceImpl descriptorProcessingService,
                            final TextureProcessingServiceImpl textureProcessingServiceImpl,
                            final ColorProcessingServiceImpl colorProcessingServiceImpl,
                            final CommonImageProcessingServiceImpl commonImageProcessingServiceImpl,
                            final ElasticSearchRestClientServiceImpl elasticSearchRestClientServiceImpl,
                            final FlaskApiHandlingServiceImpl flaskApiHandlingServiceImpl) {
        this.restHighLevelClient = restHighLevelClient;
        this.descriptorProcessingServiceImpl = descriptorProcessingService;
        this.textureProcessingServiceImpl = textureProcessingServiceImpl;
        this.colorProcessingServiceImpl = colorProcessingServiceImpl;
        this.commonImageProcessingServiceImpl = commonImageProcessingServiceImpl;
        this.elasticSearchRestClientServiceImpl = elasticSearchRestClientServiceImpl;
        this.flaskApiHandlingServiceImpl = flaskApiHandlingServiceImpl;
    }


    /**
     * 이미지 데이터 전체 조회
     *
     * @return response Elasticsearch Response Data
     */
    @Override
    public List<ImageSearchRes> findAllImages() {

        final BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        final FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders
                .functionScoreQuery(boolQuery, ScoreFunctionBuilders.randomFunction())
                .boostMode(CombineFunction.REPLACE);

        return elasticSearchRestClientServiceImpl.searchRequestHandler(functionScoreQueryBuilder, "_score",
                0, 100, "img_list");

    }

    /**
     * 이미지 데이터 저장
     *
     * @param image
     * @return response Elasticsearch Response Data
     */
    @Override
    public void saveImage(Image image) {

        try {

            // 요청 이미지 URL -> BufferedImage
            BufferedImage bufferedImage = commonImageProcessingServiceImpl.urlToBufferedImage(image.getImgUrl());

            // BufferedImage -> Mat
            Mat mat = commonImageProcessingServiceImpl.bufferedImageToMat(bufferedImage);

            // Mat 변환 with Color
            Mat img = Imgcodecs.imdecode(mat, Imgcodecs.CV_LOAD_IMAGE_COLOR);

            // Mat 변환 with GrayScale
            Mat img_gray = Imgcodecs.imdecode(mat, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

            // (1) Color - 대표 HSV
            List<String> allDominantColorList = colorProcessingServiceImpl.getAllDominantColorCandidate(bufferedImage);
            String dominantColorHexCandidate = allDominantColorList.get(0);
            String dominantColorHSVCandidate = allDominantColorList.get(1);

            // (2) Texture - ULBP Histogram
            Mat imgCropped = textureProcessingServiceImpl.cropImage(img_gray);
            Mat cannyImg = textureProcessingServiceImpl.cannyEdgeImage(imgCropped);
            int[] lbpHistogram = textureProcessingServiceImpl.procImageToULBPHistogram(cannyImg);

            // (3) 특징점 - ORB Feature Descriptor
            List<String> orbMatrix = descriptorProcessingServiceImpl.procImageUsingORB(img);

            image.setFeatureValues(dominantColorHexCandidate,
                    dominantColorHSVCandidate, orbMatrix, lbpHistogram);

            // ES에 Save 요청
            elasticSearchRestClientServiceImpl.saveRequestHandler(image, "img_list", "_doc");

            // (4) TF 특징점 - Flask 서버에 요청
            flaskApiHandlingServiceImpl.saveImagenetFeature(image);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 이미지 검색 데이터 전체 조회
     *
     * @param imageSearchReq
     * @return List<ImageSearchRes> ElasticSearch Response Data
     */
    @Override
    public List<ImageSearchRes> searchImage(final ImageSearchReq imageSearchReq) {

        try {

            // 요청 받은 검색 타입(DESCRIPTOR, COLOR, PATTERN, TF_FEATURES, ALL)
            SearchType searchType = imageSearchReq.getSearchType();

            Image image = imageSearchReq.getImage();

            // 요청 이미지 URL -> BufferedImage
            BufferedImage bufferedImage = commonImageProcessingServiceImpl.urlToBufferedImage(image.getImgUrl());

            Script script;

            Map<String, Object> params = new HashMap<>();

            if (searchType == SearchType.COLOR) {

                /*
                 * (1) Color - 대표 HSV
                 */

                String dominantColorHSVCandidate =
                        colorProcessingServiceImpl.getAllDominantColorCandidate(bufferedImage).get(1);

                params.put("dominant_color_hsv_candidate", dominantColorHSVCandidate);
                script = new Script(ScriptType.STORED, null, "calculate-score-by-color", params);

            } else if (searchType == SearchType.PATTERN) {

                /*
                 * (2) Texture - ULBP Histogram
                 */
                // BufferedImage -> Mat
                Mat mat = commonImageProcessingServiceImpl.bufferedImageToMat(bufferedImage);

                // Mat 변환 with GrayScale
                Mat img_gray = Imgcodecs.imdecode(mat, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

                Mat imgCropped = textureProcessingServiceImpl.cropImage(img_gray);
                Mat cannyImg = textureProcessingServiceImpl.cannyEdgeImage(imgCropped);
                int[] lbpHistogram = textureProcessingServiceImpl.procImageToULBPHistogram(cannyImg);

                params.put("lbp_histogram", lbpHistogram);
                script = new Script(ScriptType.STORED, null, "calculate-score-by-pattern", params);

            } else if (searchType == SearchType.DESCRIPTOR) {

                /*
                 * (3) 특징점 - ORB Feature Descriptor
                 *
                 */

                // BufferedImage -> Mat
                Mat mat = commonImageProcessingServiceImpl.bufferedImageToMat(bufferedImage);

                // Mat 변환 with Color
                Mat img = Imgcodecs.imdecode(mat, Imgcodecs.CV_LOAD_IMAGE_COLOR);

                List<String> orbMatrix = descriptorProcessingServiceImpl.procImageUsingORB(img);

                params.put("orb_matrix", orbMatrix);
                script = new Script(ScriptType.STORED, null, "calculate-score", params);

            } else {

                /*
                 * (4) TF 특징점 - Features from Tensorflow Imagenet Inception_v3 Model
                 *             - Using Python Server
                 */

                // ES에서 item_id 에 해당하는 imagenet_feature 읽어오기
                String indexName = "img_list";
                String typeName = "_doc";
                String docId = imageSearchReq.getImage().getItemId();

                GetRequest getRequest = new GetRequest(indexName, typeName, docId);
                GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);

                params.put("imagenet_feature", getResponse.getSourceAsMap().get("imagenet_feature"));

                script = new Script(ScriptType.STORED, null, "calculate-score-by-imagenetFeature", params);

            }

            // 같은 Lv2 카테고리 내에서 Search
            TermQueryBuilder matchAllQueryBuilder =
                    QueryBuilders.termQuery("category_id", image.getCategoryId());

            ScriptScoreQueryBuilder scriptScoreQueryBuilder = new ScriptScoreQueryBuilder(matchAllQueryBuilder, script);

            // ES에 Search 요청
            return elasticSearchRestClientServiceImpl.searchRequestHandler(scriptScoreQueryBuilder, "_score",
                    0, 1000, "img_list");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<ImageSearchRes> sortAndNormalizeSearchTypeSingleImageSearchResult(final List<ImageSearchRes> result) {

        final float minScore = result.get(0).getScore();  // 최소 스코어
        final float maxScore = result.get(result.size() - 1).getScore();   // 최대 스코어

        // 정규화 (Normalization)
        result.forEach(r -> r.setScore((r.getScore() - minScore) / (maxScore - minScore)));

        return result;

    }

    @Override
    public List<ImageSearchRes> sortAndNormalizeSearchTypeAllImageSearchResult(
            final List<List<ImageSearchRes>> prevResultList,
            final float[] scoreRatioArr) {

        List<List<ImageSearchRes>> resultListOrderedByItemId = new ArrayList<>();

        // 각 피쳐 기준별 최소/최대 점수 리스트
        List<Float> minScoreList = new ArrayList<>();
        List<Float> maxScoreList = new ArrayList<>();

        prevResultList.forEach(pr -> {

            List<ImageSearchRes> resultOrderedByItemId = new ArrayList<>(pr);

            // itemId 기준 정렬
            Collections.sort(resultOrderedByItemId);

            resultListOrderedByItemId.add(resultOrderedByItemId);

            minScoreList.add(pr.get(0).getScore());
            maxScoreList.add(pr.get(pr.size() - 1).getScore());

        });

        // 정규화 (Normalization)
        for (int i = 0; i < resultListOrderedByItemId.size(); i++) {

            for (int j = 0; j < resultListOrderedByItemId.get(i).size(); j++) {

                float currentScore =
                        resultListOrderedByItemId.get(i).get(j).getScore();

                float normalizedScore =
                        (currentScore - minScoreList.get(i)) / (maxScoreList.get(i) - minScoreList.get(i));

                resultListOrderedByItemId.get(i).get(j).setScore(normalizedScore);

            }
        }

        // resultList 들이 모두 itemId를 기준으로 오름차순 정렬되어 있기 때문에, 임의로 첫번째 resultList 의 리스트를 가져옴
        List<ImageSearchRes> finalSearchResult = resultListOrderedByItemId.get(0);

        // 각 기준별 정규화 된 score 통합
        for (int j = 0; j < finalSearchResult.size(); j++) {

            float scoreColor = resultListOrderedByItemId.get(0).get(j).getScore();
            float scorePattern = resultListOrderedByItemId.get(1).get(j).getScore();

            float finalScore =
                    (scoreRatioArr[0] * scoreColor) +
                            (scoreRatioArr[1] * scorePattern);

            finalSearchResult.get(j).setScore(finalScore);

        }

        // score 기준으로 최종 정렬(오름차순)
        Collections.sort(finalSearchResult, new ImageSearchRes.ImageRespScoreComparator());
        return finalSearchResult;

    }

    @Override
    public void saveDummyData(int page, int pageSize) {

        curlNaverTopTopApi(page, pageSize).forEach(i -> saveImage(i));

    }

    private List<Image> curlNaverTopTopApi(int page, int pageSize) {

        try {

            String JSONInput = ("{\n" +
                    "    \"page\": " + page + ",\n" +
                    "    \"pageSize\": " + pageSize + ",\n" +
                    "    \"sortField\": \"allScore.exposeIndex\",\n" +
                    "    \"sortType\": \"ASC\",\n" +
                    "    \"filter\": {}\n" +
                    "}");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity param = new HttpEntity(JSONInput, headers);
            RestTemplate restTemplate = new RestTemplate();
            String url = imageResource;
            String result = restTemplate.postForObject(url, param, String.class);

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObj = (JSONObject) jsonParser.parse(result);
            JSONArray itemArray = (JSONArray) jsonObj.get("content");

            List<Image> imageList = new ArrayList<>();

            for (int i = 0; i < itemArray.size(); i++) {

                JSONObject itemObject = (JSONObject) itemArray.get(i);

                String itemId = (String) itemObject.get("productId");
                String itemName = (String) itemObject.get("productName");
                String categoryId = (String) itemObject.get("catIdLv2");
                String imageUrl = (String) ((JSONObject) itemObject.get("productImage")).get("imageUrl");

                Image image = Image.builder()
                        .itemId(itemId)
                        .itemName(itemName)
                        .categoryId(categoryId)
                        .imgUrl(imageUrl)
                        .build();

                imageList.add(image);
            }

            return imageList;
        } catch (Exception e) {
            return null;
        }
    }
}