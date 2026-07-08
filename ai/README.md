# ssafyhome AI

## 소개

- python을 이용해 크롤링한 json데이터를 gms api로 벡터화하고 pandas dataframe에 병합해서 db에 insert하는 프로그램

## 사용방법

### 환경 설정

- Conda 가상환경 설정(기존 가상환경 있다면 사용 가능)

```bash
conda create -n fastapi-env python=3.12
```

- 가상환경 활성화

```bash
conda activate fastapi-env
```

- 필수 패키지 설치(사실 필수 아님)

```bash
pip install -r requirements.txt
```

### 실행하기 json파일에서 필요한 부분만 파싱 -> 임베딩 수행해서 데이터 병합 -> DB에 삽입

<h3>실행하고 y 입력하면 토큰 사용하니 **반드시** 주의해서 사용</h3>

```bash
python vectorToDb.py
```