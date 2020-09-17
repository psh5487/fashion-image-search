# fashion-image-search
Naver 인턴 - 유사 패션 이미지 검색 서비스 개발

부서 : 쇼핑박스개발   
팀원 : 박소현, 박영우   

## Stack
### Server
- Spring Boot
- Flask
- Elasticsearch
- OpenCV

### Front
- React
- Typescript

## 동일 프로젝트의 Repository
회사 관련 정보들은 모두 제거했습니다. 

- Spring Core Server   
https://github.com/psh5487/fashion-image-search/tree/master/image-search-core-server

- Flask Tensorflow Imagenet Feature 처리 Server   
https://github.com/psh5487/fashion-image-search/tree/master/image-search-tensorflow-server

- Front UI   
https://github.com/psh5487/fashion-image-search/tree/master/image-search-front


## 프로젝트 아키텍쳐
<img width="550" alt="아키텍쳐" src="https://user-images.githubusercontent.com/26567880/93092670-b2b91980-f6da-11ea-846a-93ee9bcddd11.png">

## 이미지 Feature 처리
### OpenCV에서 제공하는 Feature 관련 기능 
1. Feature Detection and Descriptions
2. Descriptor Matchers
3. Drawing functions
4. Object Categorization

### Feature Detection 알고리즘
1. SIFT (Scale invariant Feature Transform)
2. SURF (Speeded-Up Robust Features)
3. FAST (Features from Accelerated Segment Test)
4. BRIEF (Binary Robust Independent Elementary Features)
5. ORB (Oriented Fast and Rotated BRIEF)

- 구체적 내용 정리 : https://blog.naver.com/sgs03091/222073134257
- ORB 알고리즘 선택 : 정확성은 조금 떨어지더라도 다른 알고리즘들의 장점을 대부분 포함

### 유사도 측정
1. Manhatten Distance
2. Euclidean Distance
3. Minkowski Distance
4. Cosine Similarity
5. Hamming Distance

- 구체적 내용 정리 : https://blog.naver.com/sgs03091/222073353848
- Hamming Distance 선택 : ORB 특징점 계산과 함께 쓰임 

## ORB + Hamming Distance 시도
### Feature Detection - ORB
- org.opencv.features2d.ORB 클래스 사용
- 10진수 int를 원소로 갖는 2차원 배열을 return
- 해당 2차원 배열의 원소를 Binary String 으로 변환
- [(이미지 크기에 따라 가변) X 32열] 크기의 Matrix 형태
- ex) [['10010' , '1110', '11110' ... '101'], ... ['10', '110', '11111' ... '1101']]

### ES 연산량 제한에 걸림 
- 2D String Array Descriptor 를 1D String List 로 표현
- ex) ['10010, 1110, 11110, ..., 101', ... '10, 110, 11111, ..., 1101']

### Feature Matching - Hamming Distance
- ES에서 계산을 해야하므로, 2차원 배열에서의 OpenCV Hamming Distance 계산 방법을 구체적으로 파악해야 했음
<img width="350" alt="hamming" src="https://user-images.githubusercontent.com/26567880/93119048-1bfd5480-f6fc-11ea-94ac-68268da6a3f2.png">

- 2차원 배열 간의 Hamming Distance 구하기

1. req_arr 의 모든 행에 대해 다음 과정 실행
2. req_arr 의 한 행에 대해, src_arr의 모든 행을 돌면서,   
2-1. req_arr의 행과 src_arr의 행 간의 모든 요소에 대해서 해밍거리 구하고 더하기   
2-2. 값이 최솟값이라면, 최솟값 업데이트   
3. req_arr의 각 행에서 2번 과정을 통해 구한 최솟값들 다 더하기

## Elasticsearch에 값 저장하기 + Score 계산하는 Query 문 만들기
### ES에 저장하는 쿼리
```
POST img_list/_doc/4800745731
{
  "item_id" : "4800745731",
  "item_name" : "블랙 털옷",
  "img_url" : "https://shop.example.net/20191203_226/1575333435862o7h8A_JPEG/01e55cb….jpg?type=o640",
  "register_time" : "2020-01-08T14:40:43",
  "orb_matrix" : ["10,1101,...,100", "101,1101,...,101", ..., "101,1101,...,101"]
}
```
### Painless Script 저장하는 쿼리
- ORB 특징점 간의 Hamming 거리 계산
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

### 스코어 계산 쿼리
```
GET img_list/_search
{
  "sort": [
    {
      "_score": {
        "order": "asc"
      }
    }
  ],
  "query": {
    "script_score": {
      "query": {
        "match_all": {}
      },
      "script": {
        "id": "calculate-score",
        "params": {
          "req_orb_matrix": ["10,1101,...,100", "1,101,...,111", "101,1101,...,101", "111,10,...,10001"]
        }
      }
    }
  }
}
```
### Java High Level Rest Client API로 ES에 요청 보내기
- Save Request   

```
// 문서명
final String docId = image.getItemId();

// 문서 저장
final IndexRequest request = new IndexRequest("img_list", "_doc", docId);
request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

final ObjectMapper mapper = new ObjectMapper();
mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

request.source(
  mapper.writeValueAsString(image), XContentType.JSON
);

IndexResponse indexResponse = restHighLevelClient.index(request, RequestOptions.DEFAULT);
```

- Search Request   
SpringBoot 서버에서 Query문을 빌딩하여, Search 요청을 보내고, Score가 작은 순서(거리가 가까운 순서)대로 결과를 받아 오도록 함

```
// 검색할 이미지의 ORB 특징점 추출
List<String> orbMatrix = descriptorProcessingServiceImpl.procImageUsingORB(img);

// 저장된 Script 사용
Map<String, Object> params = new HashMap<>();
params.put("orb_matrix", orbMatrix);

Script script = new Script(ScriptType.STORED, null, "calculate-score", params);
                
// 같은 Lv2 카테고리 내에서 Search 하도록
TermQueryBuilder matchAllQueryBuilder =
  QueryBuilders.termQuery("category_id", image.getCategoryId());

// Score 계산 쿼리 빌딩
ScriptScoreQueryBuilder scriptScoreQueryBuilder = new ScriptScoreQueryBuilder(matchAllQueryBuilder, script);

// ES에 Search 요청 보내기
SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
searchSourceBuilder.query(scriptScoreQueryBuilder);
searchSourceBuilder.sort("_score", SortOrder.ASC);
searchSourceBuilder.from(0);
searchSourceBuilder.size(1000);

SearchRequest searchRequest = new SearchRequest("img_list");
searchRequest.source(searchSourceBuilder);

// ES로 부터 데이터 받기
SearchResponse searchResponse = null;

try {
  searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
} catch (IOException e) {
  e.printStackTrace();
}

SearchHits searchHits = searchResponse.getHits();

List<ImageSearchRes> resultMap = new ArrayList<>();

searchHits.forEach(hit->{
  Map<String, Object> hitAsMap = hit.getSourceAsMap();
  
  // JSON 역직렬화
  final ObjectMapper mapper = new ObjectMapper();
  mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
  
  final ImageSearchRes imageSearchRes = mapper.convertValue(hitAsMap, ImageSearchRes.class);
  imageSearchRes.setScore(hit.getScore());

  resultMap.add(imageSearchRes);
});
```

## 첫 시도 ORB 특징점 기반 유사도 측정 결과 및 시사점
- React와 Typescript로 프론트 UI를 만들어 결과 확인     
- 똑같은 이미지 구분에 있어서는 좋은 성능을 보였음
- 하지만, 전반적으로 검색 결과가 **유사한 이미지 순으로 전혀 보이지 않았음**

- 옷에 특화된 이미지 검색 방법 필요성 인지   
- 특징점을 한 번에 잡는 것이 아닌, **옷의 색상, 패턴, 모양**의 특징을 나누어 잡아야 함을 깨달음   
- 참고 논문 : Clothing Color and Pattern Recognition for Impaired people, Anuradha.S.G, 2016 (https://www.ijecs.in/index.php/ijecs/article/download/969/871/)

## 옷의 색상 잡기
### HSV 추출
- 주요 RGB Hex 색상, HSV 색상(색깔 Hue, 채도 Saturation, 명도 Value) 추출
<img width="128" alt="색상" src="https://user-images.githubusercontent.com/26567880/93159629-943e3700-f749-11ea-853d-69356aa74bd3.png">

### HSV 대표 값 비교
- Euclidian 거리 계산 

<img width="400" alt="유클리드" src="https://user-images.githubusercontent.com/26567880/93158531-3dcff900-f747-11ea-9090-a9061a018292.png">

## 옷의 패턴 잡기
### 질감 인식 알고리즘, LBP

Local Binary Patterns(LBP)는 이미지 질감(texture) 및 얼굴 인식 등에 활용되는 대표적인 알고리즘

### LBP 알고리즘 원리

1. 현재 픽셀을 gc라 하고, 이웃 픽셀을 g1, g2, ... , g8 이라고 하자.
2. 이웃 픽셀이 센터 픽셀 보다 크거나 같으면 1, 작으면 0
3. 이웃 픽셀 8개를 모두 비교한 후, 1 또는 0으로 구성된 8개의 숫자 리스트가 만들어짐 
<img width="400" alt="엘비피" src="https://user-images.githubusercontent.com/26567880/93158503-327ccd80-f747-11ea-8886-e7bbe74fe6dc.png">

4. 이미지의 모든 픽셀에 대해 위의 방식 계산을 진행 후, 등장 횟수를 세어 히스토그램을 만듦 
<img width="188" alt="히스토그램" src="https://user-images.githubusercontent.com/26567880/93158521-390b4500-f747-11ea-9dae-562e5f8124e7.png">
 
- 총 2^8 = 256 개의 히스토그램 bin 이 채워짐 
5. 패션 이미지의 패턴을 상징하는 히스토그램이 int 배열로 리턴됨

### 두 LBP 히스토그램의 비교
- Euclidian 거리 계산 
<img width="400" alt="유클리드" src="https://user-images.githubusercontent.com/26567880/93158531-3dcff900-f747-11ea-9090-a9061a018292.png">

### LBP 알고리즘 Test
- LBP 알고리즘은 직접 구현
- 일반 사물에 있어서는 완벽히 구분
<img width="500" alt="textureTest" src="https://user-images.githubusercontent.com/26567880/93158939-28a79a00-f748-11ea-8903-d9f62193af07.png">

- 옷의 패턴에 있어서는 복잡한 패턴은 꽤 구분하였으나, 민무늬 옷을 전혀 잡지 못함
<img width="500" alt="textureImage" src="https://user-images.githubusercontent.com/26567880/93158951-2e04e480-f748-11ea-897d-c75db0447f0e.png">

### Canny Edge로 이미지 노이즈 제거
- LBP 히스토그램 구하기 전, 이미지 노이즈 제거 전처리 작업 추가
- **민무늬 옷 구분 가능해짐**
<img width="500" alt="cannyImage" src="https://user-images.githubusercontent.com/26567880/93159429-1b3edf80-f749-11ea-829c-272f705b9693.png">

## 옷의 색상, 패턴 기준 유사 이미지 검색 Test
### 색상
![스크린샷 2020-09-15 오후 12 41 16](https://user-images.githubusercontent.com/26567880/93162974-eb93d580-f750-11ea-9682-93f87fa2d8ae.png)

### 패턴
![패턴 test](https://user-images.githubusercontent.com/26567880/93163166-62c96980-f751-11ea-9ae0-609df99a6824.png)
![민무늬test](https://user-images.githubusercontent.com/26567880/93163199-77a5fd00-f751-11ea-8e7a-287b4a4f53b5.png)

### 한계점
- 모델들의 포즈가 다양하거나 이미지의 특징점이 배경에서 잡히는 경우, 결과 방해
- **중앙 위주로 자르는 방식 시도** → 좀 더 나은 결과를 보임
- 하지만 중앙 위주로 자른 이미지에도 옷이 아닌 것들(손, 핸드폰, 다리 사이 배경 등)이 잡힐 경우, 결과 방해

## 기계 학습 모델을 사용하여 TF(Tensor Flow)특징점 뽑기 시도
### TF 특징점
- Tensorflow Imagenet Inception_v3 모델을 기반으로 추출한 imagenet_feature

### Flask Server
- Python Flask Server 사용
- TF 특징점을 뽑은 후, ES에 저장

### Inception_v3
Inception-v3은 ImageNet 데이터베이스의 1백만 개가 넘는 이미지에 대해 훈련된 컨벌루션 신경망이다. 이 네트워크는 48개의 계층으로 구성돼 있으며, 이미지 입력 크기는 299x299 이다. Inception-v3는 이미지를 대표하는 다양한 특징들을 학습하였고, 그 결과 이미지를 1,000가지 사물 범주로 분류할 수 있는 기능을 갖추고 있다. 

참고 : https://datascienceschool.net/view-notebook/8d34d65bcced42ef84996b5d56321ba9/

### TF 특징점 Test
- 유사 패션 이미지 검색에 있어서 좋은 성능을 보이지 못함
- 결과적으로, **색상과 패턴의 점수를 합쳐 계산하는 방식이 가장 좋은 결과를 보임**

## 개선할 점
- 유사한 이미지 검색 결과를 보이는데 약 7초의 시간이 걸림 -> 최소 3초 이내로 줄일 방법 찾아야함
- 배경이 복잡한 사진에서 옷 부분을 정확히 캐치하는 방법 찾아야함

## 쇼핑팀 인턴 소감

- 커머스 개발에 있어서 다양한 시도들을 해볼 수 있고, 실제로 여러 도전들이 이루어지고 있다는 점이 인상 깊었다.
- 프로덕션 레벨에 이르기까지는 또 다른 차원의 깊이가 필요하다.
- 실제 이론을 실무에서 활용해보는 과정, 정답이 정해져 있지 않은 상황에서 가능성을 찾아가는 과정이 쉽지는 않았지만 즐거웠다.  
- 매주 개발 회의에 참가하여 네이버의 코드 리뷰 문화와 쇼핑 팀에서 사용하는 기술 스택에 대해 알아볼 수 있었다.
- 프로 개발자로 성장하기 위해서는 JAVA, Spring, 설계에 대한 꼼꼼한 이해가 뒷받침되어야 한다! -> 더 공부해야함...




















