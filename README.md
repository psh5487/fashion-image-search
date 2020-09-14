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
### Painless Script 저장
- ORB 특징점 Hamming 거리 계산
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
### Java High Level Rest Client API
- SpringBoot 서버에서 위 Query문 빌딩하여, Java High Level Rest Client Api 로 Search 요청을 보내고, Score가 작은 순서(거리가 가까운 순서)대로 결과를 받아 오도록 함



## 첫 시도 결과와 시사점
- React와 Typescript로 프론트 UI를 만들어 결과 확인   
- ** 검색 결과가 전혀 유사한 이미지 순으로 보이지 않았음ㅠㅠ**   
- 옷에 특화된 이미지 검색 방법 필요성 인지   
- 특징점을 한 번에 잡는 것이 아닌, 옷의 색상, 패턴, 모양의 특징을 나누어 잡아야 함을 깨달음   
- 참고 논문 : Clothing Color and Pattern Recognition for Impaired people, Anuradha.S.G, 2016 (https://www.ijecs.in/index.php/ijecs/article/download/969/871/)
















