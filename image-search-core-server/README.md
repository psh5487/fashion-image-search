# 유사 패션 이미지 검색 서비스 Spring Core 서버

## 동일 프로젝트의 Repository
회사 관련 정보들은 모두 제거했습니다. 

- Spring Core Server   
https://github.com/psh5487/fashion-image-search/tree/master/image-search-core-server

- Flask Tensorflow Imagenet Feature 처리 Server   
https://github.com/psh5487/fashion-image-search/tree/master/image-search-tensorflow-server

- Front UI   
https://github.com/psh5487/fashion-image-search/tree/master/image-search-front

## 목차

[1. Spring Boot 프로젝트 구조](#spring-boot-프로젝트-구조)

[2. API](#api)

[3. Domain & DTO](#domain--dto)

[4. 서비스](#서비스)

[5. 시작하기](#시작하기)


## 서비스 아키텍처

<img width="550" alt="구조" src="https://user-images.githubusercontent.com/26567880/93172333-a0d08880-f765-11ea-8091-3206a113893a.png">

## Spring Boot 프로젝트 구조

- api
  - ImageController
- config
  - CORSConfig
  - LibLoadingConfig
- domain
  - Image
- dto
  - ImageSearchReq
  - ImageSearchRes
- enums
  - SearchType
- service
  - serviceImpl
    - ImageServiceImpl
    - ColorProcessingServiceImpl
    - TextureProcessingServiceImpl
    - DescriptorProcessingServiceImpl
    - CommonProcessingServiceImpl
    - ElasticSearchRestClientServiceImpl
    - FlaskApiHandlingServiceImpl
  - ImageService
  - ColorProcessingService
  - TextureProcessingService
  - DescriptorProcessingService
  - CommonProcessingService
  - ElasticSearchRestClientService
  - FlaskApiHandlingService
- ProjectApplication

## API

### 이미지 저장

| Method | Path             | Explanation                                 |
| ------ | ---------------- | ------------------------------------------- |
| POST   | /images/register | 이미지 프로세싱 후 Elasticsearch에 Doucment 저장 |

* 요청 헤더

```
Content-Type : application/json
```

* 요청 바디 예시

```json
{
    "itemId": "4816029088",
    "itemName": "STS1619 10컬러 무지 남녀공용 오버핏 박시핏 라운드티 티셔츠 (S~3XL) 소녀나라",
    "categoryId": "00000030",
    "imgUrl": "https://shop.example.net/20200210_262/1581301917288vHeVa_JPEG/105713_1.jpg?type=w640"
}
```

### 더미 이미지 저장

| Method | Path              | Explanation                                 |
|--------|-------------------|---------------------------------------------|
| GET    | /images/saveDummy | 더미 이미지 프로세싱 후 Elasticsearch에 Doucment 저장 |

* 요청 예시 

```
http://localhost:8080/images/saveDummy?page=1&pageSize=100
```

### 이미지 전체 조회

| Method | Path               | Explanation                             |
| ------ | ------------------ | --------------------------------------- |
| GET    | /images/search/all | Elasticsearch에 저장된 모든 이미지 조회 |

* 요청 헤더

```
Content-Type : application/json
```

* 응답 바디 예시

```json
[
    {
        "itemId": "4825340078",
        "itemName": "[무배/오늘출발] 베리 투웨이 롱 드롭 귀걸이",
        "categoryId": "00000094",
        "imgUrl": "https://shop-phinf.pstatic.net/20170424_146/showindowCommon_1493023214316G8m2p_JPEG/400790.jpg?type=w640",
        "registerTime": "2020-02-21 14:07:16",
        "score": 0.007663548,
        "dominantColorHexCandidate": "#4f3d2f",
        "dominantColorHsvCandidate": "26.25,40.50633,30"
    },
    {
        "itemId": "4800697296",
        "itemName": "XA5120N 네오민트",
        "categoryId": "00000147",
        "imgUrl": "https://shop-phinf.pstatic.net/20200125_155/1579925579696cafW2_JPEG/17288968219551292_509712385.jpg?type=w640",
        "registerTime": "2020-02-21 14:07:18",
        "score": 0.16934681,
        "dominantColorHexCandidate": "#889d98",
        "dominantColorHsvCandidate": "165.71428,13.375796,61"
    }
]
```

### 유사 이미지 검색

| Method | Path           | Explanation                                   |
| ------ | -------------- | ----------------------------------------------|
| POST   | /images/search | 전체 이미지 리스트에서 특정 이미지 선택 시, 유사 이미지 조회 |

* 요청 헤더

```
Content-Type : application/json
```

* 요청 바디 예시

```
{
      "searchType":"COLOR",      // COLOR, PATTERN, DESCRIPTOR, TF_FEATURES, ALL 중 하나가 들어감 
      "scoreRatioArr":[0.4,0.6], // 각 요소 별 비율. 현재 COLOR : PATTERN = 0.4 : 0.6 
      "image":{
          "itemId":"4798173762",
          "itemName":"[4000장 돌파!] 그레이쉬 스키니 팬츠 (중청)",
          "categoryId":"00000036",
          "imgUrl":"https://shop-phinf.pstatic.net/20200121_204/157960838019627seQ_JPEG/f13495cb5fa776542239024427d494f9.jpg?type=w640"
      }
}
```

* 요청 바디 내 searchType에 따라 다르게 수행됨 

1) searchType 이 All 이 아닐 경우 (단일 검색 기준) 
```
return imageServiceImpl.sortAndNormalizeSearchTypeSingleImageSearchResult(
	imageServiceImpl.searchImage(imageReq));
``` 
	
2) searchType 이 All 일 경우 (복합 검색 기준) 
```
List<List<ImageSearchRes>> searchResultList = new ArrayList<>();

//검색 서비스 두 번 수행됨 (SearchType.COLOR, SearchType.PATTERN) 
Arrays.asList(SearchType.COLOR, SearchType.PATTERN).stream().forEach(s -> {
	imageReq.setSearchType(s);
        searchResultList.add(imageServiceImpl.searchImage(imageReq));

});

return imageServiceImpl.sortAndNormalizeSearchTypeAllImageSearchResult(
	searchResultList, imageReq.getScoreRatioArr());
```

* 응답 바디 예시

```json
[
  {
      "itemId":"4798173762",
      "itemName":"[4000장 돌파!] 그레이쉬 스키니 팬츠 (중청)",
      "categoryId":"00000036",
      "imgUrl":"https://shop-phinf.pstatic.net/20200121_204/157960838019627seQ_JPEG/f13495cb5fa776542239024427d494f9.jpg?type=w640",
      "registerTime":"2020-02-21 14:07:16",
      "score":0.0,
      "dominantColorHexCandidate":"#b0a494",
      "dominantColorHsvCandidate":"34.285713,15.909091,69"
  },
  {
      "itemId":"4791900099",
      "itemName":"[sundayup made] 데이 크림진 (cream)",
      "categoryId":"00000036",
      "imgUrl":"https://shop-phinf.pstatic.net/20200115_24/1579082389098syDzJ_GIF/079daa95a1f095d61a889eb986388ef1.gif?type=w640",
      "registerTime":"2020-02-21 14:07:17",
      "score":1.0,
      "dominantColorHexCandidate":"#442f1e",
      "dominantColorHsvCandidate":"26.842108,55.88235,26"
  }
]
```

## Domain & DTO

##### **Image.java**

- Image 도메인 클래스

```java
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
```

##### **ImageSearchReq.java**

- Image 검색 요청 DTO


```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageSearchReq {
    private SearchType searchType;  // 검색 타입 (DESCRIPTOR, COLOR, PATTERN, TF_FEATURES, ALL)
    private float[] scoreRatioArr;  // 전체(ALL) 검색시 (COLOR, PATTERN)의 score 반영 비율
    private Image image;            // Image 도메인 클래스
}
```

##### **ImageSearchRes.java**

- Image 검색 응답 DTO


```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
```

## 서비스

#### **ImageService.java / ImageServiceImpl.java**

메인 서비스 / 구현체

이미지 전체 조회, 이미지 저장, 유사 이미지 검색 등 메인 컨트롤러(ImageController)에서 호출하는 메소드 포함

1. ##### <u>이미지 전체 조회</u>

```java
List<ImageSearchRes> findAllImages();
```

* 설명 

	-ES에 Search 쿼리 요청에 담을 FunctionScoreQueryBuilder 생성 
	
	-ScoreFunctionBuilders.randomFunction( ) : 랜덤으로 스코어링이 이루어지게 해서 이미지 전체 조회시에 랜덤 노출이 되도록 구현

```java
final BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

final FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders
        .functionScoreQuery(boolQuery, ScoreFunctionBuilders.randomFunction())
        .boostMode(CombineFunction.REPLACE);
```

2. ##### <u>이미지 저장</u>

```java
void saveImage(final Image image);
```

- 이미지 데이터 저장 프로세스

  ​	1) Elasticsearch에 이미지 피쳐 값(색상/패턴/특징점)과 이미지 일반 정보(itemId, itemName, categoryId, imgUrl)가 담긴 Image 객체 저장 요청 

  ​	2) Flask 에 TF특징점을 추출하여 1)의 과정을 통해 저장된 ES 도큐먼트에 "imagenet_feature" 필드를 추가하도록 요청

```java
// ES에 Save 요청
elasticSearchRestClientServiceImpl.saveRequestHandler(image, "img_list", "_doc");

// TF 특징점 - Flask 서버에 요청
flaskApiHandlingServiceImpl.saveImagenetFeature(image);
```

3. ##### <u>유사 이미지 검색 결과 조회</u>

```java
List<ImageSearchRes> searchImage(final ImageSearchReq imageSearchReq);	
```

- 요청 받는 SearchType - COLOR, PATTERN, DESCRIPTOR, TF_FEATURES, ALL 에 따라 ES에 요청할 score script를 각각 다르게

```java
if (searchType == SearchType.COLOR) {

    String dominantColorHSVCandidate =
            colorProcessingServiceImpl.getAllDominantColorCandidate(bufferedImage).get(1);

    params.put("dominant_color_hsv_candidate", dominantColorHSVCandidate);
    script = new Script(ScriptType.STORED, null, "calculate-score-by-color", params);

} else if (searchType == SearchType.PATTERN) {
    ...
    
    int[] lbpHistogram = textureProcessingServiceImpl.procImageToULBPHistogram(cannyImg);
    
    params.put("lbp_histogram", lbpHistogram);
    script = new Script(ScriptType.STORED, null, "calculate-score-by-pattern", params);
    
} else if (searchType == SearchType.DESCRIPTOR) {
   ...
   
   List<String> orbMatrix = descriptorProcessingServiceImpl.procImageUsingORB(img);

   params.put("orb_matrix", orbMatrix);
   script = new Script(ScriptType.STORED, null, "calculate-score", params);
   
} else { //SearchType.TF_FEATURES
   ...
   
   // ES에서 item_id 에 해당하는 imagenet_feature 읽어오기
   GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
   
   params.put("imagenet_feature", getResponse.getSourceAsMap().get("imagenet_feature"));
   script = new Script(ScriptType.STORED, null, "calculate-score-by-imagenetFeature", params);
}
```

- 같은 카테고리 내에서만 조회가 되도록 TermQuery를 추가

```java
TermQueryBuilder matchAllQueryBuilder =
        QueryBuilders.termQuery("category_id", image.getCategoryId());

ScriptScoreQueryBuilder scriptScoreQueryBuilder = 
	new ScriptScoreQueryBuilder(matchAllQueryBuilder, script);
```

- ES에 Search 요청. elasticSearchRestClientServiceImpl 에서 처리 
```
return elasticSearchRestClientServiceImpl.searchRequestHandler(scriptScoreQueryBuilder, "_score", 0, 1000, "img_list");
```

4. ##### <u>단일 기준으로 검색시 Score 정규화</u>

```java
List<ImageSearchRes> sortAndNormalizeSearchTypeSingleImageSearchResult(final List<ImageSearchRes> result);	
```

- 설명 

	-정규화 식 : X new = ( X - X min ) / ( X max - X min ) 

```java
final float minScore = result.get(0).getScore();                 // 최소 스코어
final float maxScore = result.get(result.size() - 1).getScore(); // 최대 스코어

// 정규화 (Normalization)
result.forEach(r -> r.setScore((r.getScore() - minScore) / (maxScore - minScore)));
```

5. ##### <u>복합 기준 조회시 Score 정규화 및 정렬</u>

```java
List<ImageSearchRes> sortAndNormalizeSearchTypeAllImageSearchResult(
            final List<List<ImageSearchRes>> prevResultList,
            final float[] scoreRatioArr);
```

- 복합적인 기준으로 검색 시(All 검색), score 기준 정렬 및 정규화 순서는 다음과 같음

  ​	1) 각 피쳐(현재 COLOR, PATTERN)별 검색 리스트에서 최소 score, 최대 score를 별도의 ArrayList에 저장

  ​	2) 각 피쳐별 검색 결과 리스트를 동일하게 itemId 필드를 기준으로 오름차순 정렬
  
  ​	3) item 필드를 기준으로 오름차순 정렬된 각 피쳐별 검색 리스트를 각각 정규화
  
  ​	4) 각 피쳐별 검색리스트의 score를 합한 값을 최종 결과 리스트에 저장
  
  ​	5) 최종 결과 리스트를 score를 기준으로 오름차순 정렬

```java
prevResultList.forEach(pr -> {

    // 최소 score, 최대 score 저장을 위한 ArrayList
    List<ImageSearchRes> resultOrderedByItemId = new ArrayList<>(pr);

    // itemId 필드를 기준으로 오름차순 정렬
    Collections.sort(resultOrderedByItemId);

    resultListOrderedByItemId.add(resultOrderedByItemId);

    minScoreList.add(pr.get(0).getScore());
    maxScoreList.add(pr.get(pr.size() - 1).getScore());

});
```

```java
// 각 기준별 정규화 된 score 통합
for (int j = 0; j < sortedAndNormalizedResult.size(); j++) {

    float scoreColor = resultListOrderedByItemId.get(0).get(j).getScore();
    float scorePattern = resultListOrderedByItemId.get(1).get(j).getScore();

    float finalScore =
            (scoreRatioArr[0] * scoreColor) +
            (scoreRatioArr[1] * scorePattern);

    sortedAndNormalizedResult.get(j).setScore(finalScore);

}
```

#### ElasticSearchRestClientService.java / ElasticSearchRestClientServiceImpl.java

ElasticSearch 검색 및 저장 요청 서비스 / 구현체

1. ES에 Search 요청 
```
List<ImageSearchRes> searchRequestHandler(final QueryBuilder queryBuilder,
                                                 final String fieldForSorting,
                                                 final int searchStart,
                                                 final int searchSize,
                                                 final String index)

```

* 설명 

```
// SearchSourceBuilder 만들기 
SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
searchSourceBuilder.query(queryBuilder);

// 스코어 기준 오름차순 정렬   
searchSourceBuilder.sort(fieldForSorting, SortOrder.ASC); 

// 받아올 데이터 개수 지정 
searchSourceBuilder.from(searchStart);
searchSourceBuilder.size(searchSize);

// ES에 요청 보내고, 스코어 먹여진 데이터 받기 
searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
SearchHits searchHits = searchResponse.getHits();

...

// 각 데이터에 대해 ImageSearchRes 객체로 변환 (SNAKE_CASE 필드 -> CAMEL_CASE 필드로 바뀜)
final ObjectMapper mapper = new ObjectMapper();
mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

final ImageSearchRes imageSearchRes = mapper.convertValue(hitAsMap, ImageSearchRes.class);

// Set Score from ES
imageSearchRes.setScore(hit.getScore());

// List<ImageSearchRes> resultMap 에 담기 
resultMap.add(imageSearchRes);

```

2. ES에 저장 요청 
```
void saveRequestHandler(final Image image, final String index, final String type)
```

* 설명 

```
//문서 색인 - 요청 보내자마자 refresh 되도록 설정 
final IndexRequest request = new IndexRequest(index, type, docId);
request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

//ES에 저장 시, 필드가 SNAKE_CASE로 저장되도록 설정 
final ObjectMapper mapper = new ObjectMapper();
mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

//JSON 으로 Image 객체 저장 요청 보내기
request.source(
        mapper.writeValueAsString(image), XContentType.JSON
);

IndexResponse indexResponse = restHighLevelClient.index(request, RequestOptions.DEFAULT);
```


#### CommonImageProcessingService.java / CommonImageProcessingServiceImpl.java

이미지 전처리 작업 서비스 / 구현체

1. #### 이미지 URL 주소로부터 BufferedImage 객체로 변환
```
BufferedImage urlToBufferedImage(final String ImgUrl) throws IOException;
```

2. #### BufferedImage 로부터 OpenCV에서 제공하는 Mat 객체로 변환
```
Mat bufferedImageToMat(final BufferedImage bufferedImage) throws IOException;
```

3. #### Mat 객체를 2차원 배열로 변환

- ex) {{"1", "2", "3"}, {"4", "5", "6"}}

```
String[][] MatDescriptorsTo2DArray(Mat descriptors, int rows, int cols);
```

4. #### 2차원 특징점 배열을 1차원 List<String>으로 변환

- ex) {{"1", "2", "3"}, {"4", "5", "6"}} -> {"1,2,3", "4,5,6"}

```
List<String> ArrayDescriptorsTo1DList(final String[][] feature_matrix);
```


#### ColorProcessingService.java / ColorProcessingServiceImpl.java

이미지 프로세싱 - 색상 이용 작업 관련 서비스 / 구현체

이미지의 가운데를 자른 새로운 이미지에 대해서 각 픽셀 별로 가장 빈도수가 높은 색상값(RGB/HSV)를 계산하는 서비스 

해당 서비스에서 사용하는 이미지 크기 재조정 비율과 관련된 설정

```yml
color:
  resize:
    ratio: 0.7
```

```java
@Value("${color.resize.ratio}")
private float resizeRatio;
```

1. ##### <u>이미지 중앙 기준으로 주요 RGB 색상 추출하기</u>

```java
List<String> getColorFromImageCenter(BufferedImage bufferedImage, boolean isHexColor);
```

* 설명 

	-픽셀별로 Brute-Force하게 대응되는 RGB값을 반환하고, 해당 RGB값과 빈도수를 저장하는 colorMap을 업데이트 
	
	-최종적으로 반환되는 colorMap을 통해 주요 RGB 색상을 추출할 수 있음

```java
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
```

2. ##### <u>RGB ColorMap을 RGB Hex or HSV List로 변환하기</u>

```java
private static List<String> translateRGBColorMapToRGBHexOrHSV
  (Map<Integer, Integer> colorMap, boolean isRGBHex);
```
* 설명 

- RGB 색상 값 및 빈도수가 포함된 colorMap을 counter(빈도수)를 기준으로 내림차순 정렬

```java
List<Map.Entry<Integer, Integer>> colorMapAsList = new LinkedList<>(colorMap.entrySet());

// counter 기준으로 내림차순으로 정렬
Collections.sort(colorMapAsList, (Map.Entry<Integer, Integer> obj1, Map.Entry<Integer, Integer> obj2)
     -> ((Comparable) obj1.getValue()).compareTo(obj2.getValue()));

List<String> colorValueStrList = new ArrayList<>();
```

- 파라미터로 받는 isRGBHex 값에 따라 해당 메소드의 return 값을 RGB Hex String List 또는 HSV Sting List로 분기해서 처리


#### TextureProcessingService.java / TextureProcessingServiceImpl.java

1. #### Mat 이미지를 중앙 위주로 자르기 
```
Mat cropImage(Mat img);
```

* 설명 

```
// 관심 영역 설정 - x, y, width, height
int x = (img.width() / 5) * 2;
int y = (img.height() / 5) * 2;
int w = x + 120;
int h = y + 120;

// 두 개의 좌표로 Rectangle 만듦
Rect area = new Rect(new Point(x, y), new Point(w, h));
Mat imgCropped = img.submat(area);
```

2. #### Mat 이미지 CannyEdge 처리하여 노이즈 제거하기
```
Mat cannyEdgeImage(Mat img);
```

* 설명 

	-org.opencv.imgproc.Imgproc 클래스 사용 
	
	-lowThreshold 값이 커질 수록 노이즈가 많이 제거됨 

```
// Settings
Size BLUR_SIZE = new Size(3, 3);
int lowThresh = 40;
int RATIO = 3;
int KERNEL_SIZE = 3;

// Canny Detection
Mat imgBlur = new Mat();
Mat detectedEdges = new Mat();

Imgproc.blur(img, imgBlur, BLUR_SIZE);
Imgproc.Canny(imgBlur, detectedEdges, lowThresh, lowThresh * RATIO, KERNEL_SIZE, false);

Mat cannyImg = new Mat(img.size(), CvType.CV_8UC3, Scalar.all(0));
img.copyTo(cannyImg, detectedEdges);
```

3. #### Mat 이미지의 Uniform LBP 히스토그램 구하기
```
int[] procImageToULBPHistogram(Mat img);
```

- 알고리즘 설명 
  
  ​	1) 이미지의 모든 픽셀을 탐색하며, 각 픽셀마다 주변 8개 픽셀과 밝기 비교

  ​	2) 주변 밝기 비교 할 때마다, LBP(Local Binary Patterns)을 만듦 ex) 00110000 

	-만들어질 수 있는 LBP 종류는 256가지, Uniform LBP 종류는 58가지 

  ​	3-1) LBP가 Uniform Pattern일 경우(0->1 또는 1->0 변화 횟수가 2회 이하), LBP 에 해당하는 히스토그램의 값 증가 

	ex. 00110000(48) 이면, histogram[48] += 1

  ​	3-2) LBP가 Non-Uniform 일 경우(0->1 또는 1->0 변화 횟수가 3회 이상), 히스토그램 Bin 한 개에 몰아서 카운트

	ex. 01011000 이면, histogram[NON_UNIFORM_BIN] += 1 
	
  ​	4) ULBP 히스토그램 반환 

- 참고 설명: https://bskyvision.com/280

- 패턴 히스토그램 특징 

  ​	1) 이미지의 패턴을 대표함 

  ​	2) 복잡한 패턴일수록, histogram[NON_UNIFORM_BIN]의 값이 증가 


#### DescriptorProcessingService.java / DescriptorProcessingServiceImpl.java

이미지 프로세싱 - 특징점 이용 작업 서비스 / 구현체

Open CV Image Feature Detection & Description을 활용한 이미지 프로세싱 엔진 기반 이미지 특징점 분석 서비스

두 가지 이미지 특징점 검출/분석 엔진(ORB / BRISK)에 대한 구현 사항

1. ##### <u>ORB 엔진을 이용한 이미지 특징점 검출</u>

```java
List<String> procImageUsingORB(Mat img);
```
* 설명 

	-org.opencv.features2d.ORB 클래스 이용
	
	-ORB.detectAndCompute(Mat image, Mat mask, MatOfKeyPoint keypoints, Mat descriptors) : 이미지 특징점/특징점 분석값 검출
	
	-코너 위주로 특징점 잡음. 이미지 자체의 유사성 판별 시 사용 

```java
// ORB Detector 생성
ORB orbDetector = ORB.create(75, 1.2f, 8, 31, 0, 2, ORB.HARRIS_SCORE, 31, 20);

// ORB Feature 개수 제한
orbDetector.setMaxFeatures(MAX_FEATURES_ORB);

// Detect & Compute ORB Descriptor - Feature 정보를 담음
MatOfKeyPoint keyPoints = new MatOfKeyPoint();
Mat descriptors = new Mat();

orbDetector.detectAndCompute(img, new Mat(), keyPoints, descriptors);
```

2. ##### <u>BRISK 엔진을 이용한 이미지 특징점 검출</u>

```java
List<String> procImageUsingBrisk(Mat img);
```

* 설명 

	-ORB엔진이 배경 위주로 피처를 잡는다는 문제점으로 인해 시도한 엔진 
	
	-threshold 값과 patternScale 값 조정으로 옷에 피처가 찍히도록 함 
	
	-하지만 검색 품질을 크게 높이지 않아 현재 사용하지 않음 


#### FlaskApiHandlingService.java / FlaskApiHandlingServiceImpl.java

이미지 프로세싱 - TF 특징점 작업 관련 서비스 / 구현체

Flask 서버에 이미지 TF 특징점 처리 요청 서비스

- 해당 서비스에서 사용하는 Flask 서버 주소 관련 설정

```
python:
  flask:
    address: "http://0.0.0.0:5000/"
```

```
@Value("${python.flask.address}")
private String flask_address;
```

1. #### 서비스에서 요청을 보내면, Flask에서 TF 특징점(imagenet_feature) 추출하고, ES 의 'img_list' 저장

```
void saveImagenetFeature(Image image) throws IOException
```

* 설명 

```
HttpPost httpPost = new HttpPost(flask_address + "saveImagenetFeature");

// 요청 헤더
httpPost.setHeader("Accept", "application/json");
httpPost.setHeader("Content-type", "application/json");

// 요청 바디
ObjectMapper mapper = new ObjectMapper();

Map<String, Object> map = new HashMap<String, Object>();
map.put("item_id", image.getItemId());
map.put("img_url", image.getImgUrl());

String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
StringEntity entity = new StringEntity(json);
httpPost.setEntity(entity);

// 요청 보내기 
HttpClient client = HttpClientBuilder.create().build();
client.execute(httpPost);
```

- Flask 서버 : 주소 


## 시작하기

### 실행하기 전

- Java 8 이상 개발 환경에 설치

- Open CV 3.2.0 버전 로컬 개발 환경에 설치

- Python Flask 서버 설치 : 주소

### Elasticsearch 설정 

- elasticsearch.yml 파일에 코드 추가

```
script.painless.regex.enabled: true 
```

- Painless Script 저장 

1. 색상 거리 계산
```
POST _scripts/calculate-score-by-color
{
  "script": {
    "lang": "painless",
    "source": """
            
            double score = 0.0;
            
            String req_hsv_candidate = params.dominant_color_hsv_candidate;
            String source_hsv_candidate = params._source.dominant_color_hsv_candidate;
            
            String[] req_hsv_candidate_str_arr = /,/.split(req_hsv_candidate);
            String[] source_hsv_candidate_str_arr = /,/.split(source_hsv_candidate);
            
            double [] req_hsv_candidate_list = Arrays.asList(req_hsv_candidate_str_arr).stream().mapToDouble(Double::parseDouble).toArray();
            
            double [] source_hsv_candidate_list = Arrays.asList(source_hsv_candidate_str_arr).stream().mapToDouble(Double::parseDouble).toArray();
            
            // HSV의 Saturation(채도), Value(명도)가 모두 50 미만일 경우, Hue(색상)으로만 거리 계산을 한다.
            
            if(req_hsv_candidate_list[2] < 25.0){
              
              score = Math.pow(
                    (Math.abs(source_hsv_candidate_list[2] - req_hsv_candidate_list[2])), 2);
                    
              score *= 4;
              
            }
            
            // 이외의 경우에는 Hue요소를 사용해서 거리 계산을 한다.
            
            else {
              
                double score_1 = Math.pow(
                    (Math.abs(source_hsv_candidate_list[0] - req_hsv_candidate_list[0])), 2);
                    
                double score_2 = Math.pow(
                    (Math.abs(source_hsv_candidate_list[1] - req_hsv_candidate_list[1])), 2);
                double score_3 = Math.pow(
                    (Math.abs(source_hsv_candidate_list[2] - req_hsv_candidate_list[2])), 2);
    
                score_1 *= 2;        
                score_2 *= 1;
                score_3 *= 1;
                score = score_1 + score_2 + score_3;
            }
            
                score = Math.sqrt(score);
                
            return score;
            
            """
  }
}
```

2. 패턴 거리 계산 (Euclidean Distance)
```
POST _scripts/calculate-score-by-pattern
{
  "script": {
    "lang": "painless",
    "source": """
    
            int req_len = params.lbp_histogram.length;
            double distance = 0;
            
            for(int i = 0; i < req_len-1; i++){
              int a = params.lbp_histogram[i];
              int b = params._source.lbp_histogram[i];
              
              distance += Math.pow(a-b, 2);
            }
          
            double score =  Math.sqrt(distance);
            
            return score;
            
            """
  }
}
```

3. ORB 특징점 거리 계산 (Hamming Distance)
```
POST _scripts/calculate-score
{
  
  "script": {
    "lang": "painless",
    "source": """
    
            int req_row = params.orb_matrix.length;
            int source_row = params._source.orb_matrix.length;
            int total_dist = 0;

            for(int i = 0; i < req_row; i++){
              
                String req_col_str = params.orb_matrix[i];
              
                String[] req_col_str_list = /,/.split(req_col_str);
              
                int min_dist = Integer.MAX_VALUE;
                
                
                for(int j = 0; j < source_row; j++){
                  
                  String source_col_str = params._source.orb_matrix[j];
                  
                  String[] source_col_str_list = /,/.split(source_col_str);
                  
                  int temp_dist = 0;
                  
                  // Hamming Distance 계산
                  for(int k = 0; k < req_col_str_list.length; k++){
                    
                      int req_element = Integer.parseInt(req_col_str_list[k]);
                      int source_element = Integer.parseInt(source_col_str_list[k]);
                      
                      int xor =  req_element^source_element;
                      
                      
                      temp_dist = temp_dist + Integer.bitCount(xor);
                      if(temp_dist > min_dist) break;
                  }

                  // 최소 거리 업데이트
                  if(temp_dist < min_dist) min_dist = temp_dist;  
                  
                }
                
                // 거리합 업데이트
                total_dist += min_dist;
                
            }
            
            double score =  (double)total_dist / req_row;
            
            return score;
            
            """
  }
}
```

4. TF 특징점 거리 계산 (Euclidean Distance)

```
POST _scripts/calculate-score-by-imagenetFeature
{
  "script": {
    "lang": "painless",
    "source": """
    
            int req_len = params.imagenet_feature.length;
            double distance = 0;
            
            for(int i = 0; i < req_len; i++){
              double a = params.imagenet_feature[i];
              double b = params._source.imagenet_feature[i];
              
              distance += Math.pow(a-b, 2);
            }
          
            double score =  Math.sqrt(distance);
            
            return score;
            
            """
  }
}
```

### Maven 의존성 추가

- pom.xml

  ```xml
  <dependencies>
     <!-- Spring Boot Starter Web -->
     <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
     </dependency>
     <!-- Spring Boot Starter Test -->
     <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
     </dependency>
     <!-- Elasticsearch -->
     <dependency>
        <groupId>org.elasticsearch</groupId>
        <artifactId>elasticsearch</artifactId>
        <version>7.5.1</version>
     </dependency>
     <!-- Elasticsearch High Level REST Client -->
     <dependency>
        <groupId>org.elasticsearch.client</groupId>
        <artifactId>elasticsearch-rest-high-level-client</artifactId>
        <version>7.5.1</version>
     </dependency>
     <!-- Elasticsearch Low Level REST Client -->
     <dependency>
        <groupId>org.elasticsearch.client</groupId>
        <artifactId>elasticsearch-rest-client</artifactId>
        <version>7.5.1</version>
     </dependency>
     <!-- OpenCV -->
     <dependency>
        <groupId>org.openpnp</groupId>
        <artifactId>opencv</artifactId>
        <version>3.2.0-1</version>
     </dependency>
     <!-- Lombok -->
     <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
     </dependency>
     <!-- Common IO -->
     <dependency>
        <groupId>commons-io</groupId>
        <artifactId>commons-io</artifactId>
        <version>2.6</version>
     </dependency>
     <!-- JSON Simple  -->
     <dependency>
        <groupId>com.googlecode.json-simple</groupId>
        <artifactId>json-simple</artifactId>
        <version>1.1</version>
     </dependency>
  </dependencies>
  ```

### 실행하기

- mvn spring-boot:run 또는 IDE 환경에서 Run 'ProjectApplication' 
