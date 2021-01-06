# 인생샷을 위한 블루투스 삼각대와 앱

## 목차

1. 주제 선정 배경 및 작동 방법
2. 사용 기술
   - 딥러닝
   - App-아두이노 통신
   - 사진 저장 Web 서비스
3. 작품 사진 및 동작 결과
   <br><br>

# 1. 주제 선정 배경 및 작동 방법

## 주제 선정 배경

- ### 삼각대를 사용하는 목적
  장거리의 경우
  | 단체 사진(2명 이상)인 경우
  | 구도를 고정하여 찍는 경우
  | 혼자서 촬영하는 경우<br>
  ## 🤦‍♀️
  - 구도를 변경할 때마다 직접 삼각대를 움직여야 한다.
  - 블루투스 리모컨을 누를 때마다 한장 씩만 촬영된다.
  - 불필요한 시간 소요
    <br><br>
- ### 사진을 찍는 목적
  여행에서 추억을 남기고 싶을 때
  | SNS에 잘 나온 사진을 올리고 싶어서
  | 친구들과의 추억을 남기기 위해<br>
  ## 🤷‍♀️
  - 잘 나온 사진을 구분하는 것이 어렵고
  - 분류하는 데에 상당 시간이 소요된다.
<br>

## 작동 방법
<img src = "https://user-images.githubusercontent.com/41332126/103402722-f011bc00-4b90-11eb-83c6-33989e6d5c0f.png" width="200px" align="right">

1. 사진 촬영 시작(블루투스 리모컨)
2. 모터가 여러 각도로 회전한다.
3. 모터의 각도 변경에 맞추어 앱과 블루투스 통신하여 카메라가 연속 촬영한다.
4. 촬영된 사진들을 내림차순으로 정렬하여 추천해준다.
5. 사용자는 전체 저장하거나 혹은 원하는 사진들을 선택하여 부분 저장할 수 있다.

<br><br>

# 2. 사용 기술

- ### Android Studio
  - 사진 분류 및 저장 어플 제작
  - 아두이노 블루투스 통신
- ### Arduino
  - Servo motor: 좌우 움직임 조절
  - Stepping motor: 상하 움직임 조절
- ### Deep Learning
  - 딥러닝 모델 선정 및 데이터셋 학습 진행
  - 촬영된 사진들 중 가장 잘 나온 사진 순위 선정
- ### Android-Web 통신 (DB)
  - 촬영된 모든 사진을 서버에 전송하여 업로드
  - 저장하지 않은 사진까지 웹에서도 확인 가능

<br><br>

# 2.1. 딥러닝

## Inception-v3

- 촬영된 사진들 중 최적의 구도를 추천하기 위해, **이미지 분류** 에 적합한 학습 모델
- 특정 Task에 이미 잘 작동하는 것이 검증된 모델

이미지 인식(추론)에 대해 구글이 만든 Inception 모델의 구조와 파라미터들을 가져와<br>
이를 기반으로 새로운 데이터셋에 **_retraining_** 을 하는 **_Transfer learning_** 과정을 사용하였다.

`Inception v3 Neural Networks의 Architecture`

<img src = "https://user-images.githubusercontent.com/41332126/103403125-7d094500-4b92-11eb-96e6-2dadd70cbc97.png" height="200px">
<br>

## 데이터셋 클래스 분류

**_bad_** 와 **_good_** label을 가진 폴더를 만들고<br>
각 폴더 안에 구도가 좋은 사진과 그렇지 않은 사진을<br>
약 4000개씩 jpg(jpeg)로 저장하여 원본 dataset을 만들었다.

<img src = "https://user-images.githubusercontent.com/41332126/103395748-1a548100-4b73-11eb-85ca-96d06144d8ce.png" width="350px">
<br>

## 데이터셋 직선 성분 추출

구도를 식별하기 위해 OpenCV를 통해 원본 데이터셋에서<br>
직선성분만을 추출하여 good/bad로 구분하여 데이터셋 구축.

`👇 추출한 직선 성분 중 good 데이터셋 일부`

<img src = "https://user-images.githubusercontent.com/41332126/103396072-6d7b0380-4b74-11eb-8b3e-bfa7e98a9a16.png" width="200px" align="left">
<img src = "https://user-images.githubusercontent.com/41332126/103396116-93a0a380-4b74-11eb-8401-b50a7803f058.png" width="200px" align="left">
<img src = "https://user-images.githubusercontent.com/41332126/103396120-9b604800-4b74-11eb-8cff-35ae4ff55167.png" width="200px">
<br>

## App과 OpenCV 연동

두 개의 학습 그래프를 모바일 최적화하는 과정을 거친다.<br>
| <center>모바일 최적화 전</center> | <center>모바일 최적화 후</center> |
| -------- | -------------- |
| retained_graph.pb | optimized_graph.pb |
| retained_line_graph.pb | optimized_line_graph.pb |
<br>

👇 직선 성분 학습 그래프를 이용한 분류 결과를 도출 시 사용

<img src = "https://user-images.githubusercontent.com/41332126/103397717-debeb480-4b7c-11eb-9ab8-8081752c3ffd.png" width="700px">

## App과 학습 데이터 접목

총 2종류의 Dataset을 tensorflow로 각각 학습 시킨 후, 모바일에 최적화 시킨<br>
원본 이미지 학습 그래프와 직선 성분 학습 그래프를 안드로이드 앱에 적용한 결과.

<img src = "https://user-images.githubusercontent.com/41332126/103396325-e62e8f80-4b75-11eb-863a-71024129b8ef.png" width="500px"><br>

## 결과 컨볼루션

최종 이미지 분류 결과는 안드로이드에서<br>
구도 판별 이미지 분류와 원본 이미지 분류 결과를 조합하여 도출하였다.

<img src = "https://user-images.githubusercontent.com/41332126/103396605-905ae700-4b77-11eb-9271-293260f4e9cf.png" width="400px"><br>

```
정렬된 사진은 사진 속 인물의 위치가 적절한가, 구도적으로 사진 속 수직/수평 성분이 많은 가에 의해 결정된다.
```

<br><br>

# 2.2. App-삼각대 통신

## CameraX란?

카메라 앱 개발을 더욱 쉽게 할 수 있도록 만들어진 Jetpack 지원 라이브러리

- 대부분의 Android 기기에서 작동하는 일솬성 있고 사용하기 쉬운 API 노출 영역을 제공하며, Android 5.0(API Level21)까지 호환
- camera2의 기능을 활용하면서 수명 주기를 인식하는 더 단순한 사용 사례 기반의 방식을 사용
  <br><br>

## 아두이노와 블루투스 통신
<img src = "https://user-images.githubusercontent.com/41332126/103398610-cfda0100-4b80-11eb-9b30-3897ef2eea78.png" width="220px" align="right">

Bluetooth SPP 라이브러리를 사용해 삼각대에 달린 아두이노와 블루투스 연결을 하여 통신한다.
- 안드로이드와 아두이노 통신으로 데이터를 안드로이드에서 아두이노로 보낸 후 모터를 작동시킨다.
- 모터 작동이 끝난 후 아두이노에서 다시 안드로이드로 데이터를 보내고<br>
  안드로이드에서 자동으로 사진 촬영이 이루어져 연속 촬영 및 모터 구동을 구현하였다.
  <br><br>

## 사용 부품과 연결도

삼각대에 달린 아두이노와 블루투스 통신을 사용해<br>
모터의 각도 변경과 촬영 타이밍을 결정하여 연속 촬영한다.
| 부품 | 역할 |
| -------- | -------------- |
| Servo motor | 카메라 좌우 회전 |
| Stepping motor | 카메라 상하 이동 |
| 블루투스 모듈 | 안드로이드와 아두이노 블루투스 통신 |
| 아두이노 우노보드 | 브룰투스 연결 및 모터 제어 |
| 스텝모터 드라이버 | 스텝모터와 아두이노 연결 |
| 9V 건전지 | 모터 회전을 위한 전력 공급 |
<br>
<img src = "https://user-images.githubusercontent.com/41332126/103398748-5bec2880-4b81-11eb-8af7-3e81c2345515.png" width="500px" align="left">
<img src = "https://user-images.githubusercontent.com/41332126/103398757-64446380-4b81-11eb-8b98-e13976176566.png" width="300px">
<br><br>

## 블루투스&모터 제어

안드로이드에서 촬영후 블루투스 연결을 통해 신호를 수신할 때 마다 서보모터가 회전한다.

70도, 80도, 90도 3번 촬영 후, 스텝모터가 작동하여 높이 조절한 뒤에 다시 70도, 80도, 90도 차례로 회전하며 촬영된다.

<img src = "https://user-images.githubusercontent.com/41332126/103398914-006e6a80-4b82-11eb-9967-d4fdfdfc5103.png" width="230px" align="left">
<img src = "https://user-images.githubusercontent.com/41332126/103398925-095f3c00-4b82-11eb-93a3-58856e66a9cf.png" width="290px">
<br><br>

## 스텝모터 제어

스텝모터를 일정횟수 회전시켜 카메라 지지대의 높이를 조절한다.

촬영 후 원위치로 돌아오기 위해서 펄스값을 조절하여 회전방향을 제어한다.

- turn_up 👉 반시계방향 회전
- turn_down 👉 시계방향 회전

<img src = "https://user-images.githubusercontent.com/41332126/103399028-85598400-4b82-11eb-9423-f70b740bffc4.png" width="201px" align="left">
<img src = "https://user-images.githubusercontent.com/41332126/103399037-8e4a5580-4b82-11eb-9b64-d6158d2f5d15.png" width="210px">

<br><br>

# 2.3. 사진 저장 Web 서비스

**1.**

<img src = "https://user-images.githubusercontent.com/41332126/103399281-a9699500-4b83-11eb-942a-b096573e4938.png" width="850px">
<br><br>

## CameraActivity.kt

**2.** 6장 연속 촬영이 끝나면 uploadFile() 함수를 6번 반복 실행한다.

<img src = "https://user-images.githubusercontent.com/41332126/103399419-6cea6900-4b84-11eb-9722-ac5faafb3c6a.png" width="750px">
<br><br>

**3.** 지정한 서버 주소로 파일을 전송한다.

<img src = "https://user-images.githubusercontent.com/41332126/103399712-b4bdc000-4b85-11eb-9ad9-7bd626bf3d36.png" width="420px"><br>

<img src = "https://user-images.githubusercontent.com/41332126/103399779-f6e70180-4b85-11eb-9615-824c168d5c0b.png" width="550px"><br>

<img src = "https://user-images.githubusercontent.com/41332126/103399763-e767b880-4b85-11eb-9af9-24bc55ea7dff.png" width="480px">
<br><br>

**4.** upload.php

<img src = "https://user-images.githubusercontent.com/41332126/103399878-6eb52c00-4b86-11eb-8a4b-72b43fdeb392.png" width="550px"><br>
<img src = "https://user-images.githubusercontent.com/41332126/103399956-bc319900-4b86-11eb-9d85-9bc69bd82fd6.png" width="680px">
<br><br>

## Server

**5.** Database 관리<br>

- server : MariaDB
- database : project
- table : gallery<br>

&emsp;👉no, uid, filename, imgurl, size

<img src = "https://user-images.githubusercontent.com/41332126/103400595-06b41500-4b89-11eb-8219-7f1e0e4f1b5b.png" width="700px"><br>
<img src = "https://user-images.githubusercontent.com/41332126/103400625-1e8b9900-4b89-11eb-8eae-33b57d8305c5.png" width="700px">
<br><br>

**6.** index.html

<img src = "https://user-images.githubusercontent.com/41332126/103400726-8a6e0180-4b89-11eb-80ac-a7729c5cb83a.png" width="550px"><br>
<img src = "https://user-images.githubusercontent.com/41332126/103400747-9e196800-4b89-11eb-8280-4190c5b85af8.png" width="800px">
<br><br>

**7.** login.php

<img src = "https://user-images.githubusercontent.com/41332126/103400857-f81a2d80-4b89-11eb-96e4-d9e9883e5090.png" width="480px"><br>
<img src = "https://user-images.githubusercontent.com/41332126/103402098-f9018e00-4b8e-11eb-99e5-f7c5fd1c6674.png" width="480px">
<br><br>

**8.** album.php

<img src = "https://user-images.githubusercontent.com/41332126/103402144-2817ff80-4b8f-11eb-928f-a70b39079576.png" width="800px">

<br><br>

# 3. 작품 사진 및 동작 결과

<img src = "https://user-images.githubusercontent.com/41332126/103403421-5a2b6080-4b93-11eb-846e-ee23b9836cfc.png" width="200px" align="left">
<img src = "https://user-images.githubusercontent.com/41332126/103403344-19cbe280-4b93-11eb-9934-e9fbecf655b6.png" width="200px" align="left">
<img src = "https://user-images.githubusercontent.com/41332126/103403450-79c28900-4b93-11eb-8f9a-61b9b9f2163a.png" width="200px" align="left">
<img src = "https://user-images.githubusercontent.com/41332126/103403462-8810a500-4b93-11eb-9cdf-1b3a81941000.png" width="200px">
<br><br>

[![클릭하면 유튜브로 이동](https://img.youtube.com/vi/UlWEPGqljLQ/0.jpg)](https://youtu.be/UlWEPGqljLQ)
