# 유사 패션 이미지 검색 서비스 Front

### 동일 프로젝트의 Repository
회사 관련 정보들은 모두 제거했습니다.

- Spring Core Server   
https://github.com/psh5487/fashion-image-search/tree/master/image-search-core-server

- Flask Tensorflow Imagenet Feature 처리 Server   
https://github.com/psh5487/fashion-image-search/tree/master/image-search-tensorflow-server

- Front UI   
https://github.com/psh5487/fashion-image-search/tree/master/image-search-front

## 서비스 UI

### 1. 이미지 전체 조회 화면

- 페이지 접속시 최초로 렌더링 되는 화면으로, ES에 저장된 이미지 및 상품 정보를 랜덤으로 100개 호출 후 화면에 노출시킴

<img width="500" alt="첫화면" src="https://user-images.githubusercontent.com/26567880/93170544-644f5d80-f762-11ea-811c-bfccfb1e2989.png">

- 카드형 상품 정보에는 상품 이미지 / 상품명 / 상품 고유 번호 / 상품 주요 색상 정보가 있음

<img width="150" alt="상품" src="https://user-images.githubusercontent.com/26567880/93170556-69aca800-f762-11ea-8985-5ffa6310cfce.png">

### 2. 유사 이미지 검색 결과 조회 화면

- 이미지 전체 조회 화면에서 클릭한 상품에 대해서, 유사 이미지 검색 결과를 유사도가 높은 순으로 화면에 노출시킴

<img width="604" alt="전체검색결과" src="https://user-images.githubusercontent.com/26567880/93171305-cfe5fa80-f763-11ea-93ae-e005656a3964.png">

- 화면 상단의 색상 / 패턴 / 특징점 / TF 특징점 / 전체 탭은 각각 유사 이미지 검색에 사용되는 이미지 특징, 분석 엔진 등에 따라 대응하는 검색 결과를 노출함

<img width="398" alt="탭설명" src="https://user-images.githubusercontent.com/26567880/93170591-79c48780-f762-11ea-8f2e-49de1b9bd67a.png">

- 탭 검색 유형 설명 
  - 색상 : 주요 색상을 기준으로 유사 상품을 차례대로 노출
  - 패턴 : 주요 패턴을 기준으로 유사 상품을 차례대로 노출
  - 특징점 : Open CV 엔진이 추출한 ORB 특징점을 기준으로 유사 상품을 차례대로 노출
  - TF 특징점 : CNN 기반의 Tensorflow ImageNet Inception-v3이 추출한 이미지 특징점을 기준으로 유사 상품을 차례대로 노출
  - 전체 : 색상 점수 + 패턴 점수 기반으로 유사 상품을 차례대로 노출
  
- 검색 유형
  - <전체> 검색 유형을 통해 OpenCV 이미지 프로세싱 기반의 검색 양상을 확인 할 수 있음
  - <TF 특징점>검색 유형을 통해 CNN 모델 학습 기반의 검색 양상을 확인 할 수 있음

## 시작하기

### 준비

- node.js 설치

### 의존성

- package.json

```xml
  "dependencies": {
    "@egjs/infinitegrid": "^3.6.2",
    "@egjs/react-infinitegrid": "^3.0.2",
    "@testing-library/jest-dom": "^4.2.4",
    "@testing-library/react": "^9.3.2",
    "@testing-library/user-event": "^7.1.2",
    "@types/jest": "^24.0.0",
    "@types/node": "^12.0.0",
    "@types/react": "^16.9.0",
    "@types/react-dom": "^16.9.0",
    "@types/react-router-dom": "^5.1.3",
    "antd": "^3.26.7",
    "axios": "^0.19.1",
    "babel-plugin-import": "^1.13.0",
    "customize-cra": "^0.9.1",
    "less": "^3.10.3",
    "less-loader": "^5.0.0",
    "rc-slider": "^9.2.0",
    "react": "^16.12.0",
    "react-bootstrap-toggle": "^2.3.2",
    "react-dom": "^16.12.0",
    "react-hook-form": "^4.5.5",
    "react-radio-buttons": "^1.2.2",
    "react-router-dom": "^5.1.2",
    "react-scripts": "3.3.0",
    "react-spinners": "^0.8.0",
    "react-stack-grid": "^0.7.1",
    "typescript": "~3.7.2"
  }
```

### 실행하기

- npm install 또는 yarn install
- npm start 또는 yarn start
