# HomeLens

## docker 

1. wsl2 설치
2. windows docker desktop 설치(wsl2사용한다는 옵션 체크)
3. powershell 실행
4. 프로젝트 루트 디렉토리로 이동
5. docker compose up -d

### docker dump 파일

- 로컬 환경의 docker에서 각자 sql서버를 실행하고 로컬 도커 환경에 데이터를 저장하고 있으므로 필요에 따라 DB데이터를 공유해야 하는 상황이 생김

#### docker dump 파일 저장

```bash
docker exec homelens-postgres pg_dump -U homelens -d homelens -Fc -f /dump/homelens.dump
```


#### docker dump 파일 복원

```bash
docker compose up -d --build
docker compose --profile restore run --rm -T db-restore
```

## FastAPI

### 실행하기

프로젝트 루트 디렉토리에서 

```bash
docker compose up -d -build
```

실행하면 됩니다.

* docker ps 혹은 docker application에서 sementic api 컨테이너 돌아가고 있는 거 확인하시면 잘 된겁니다.
* http://localhost:8000/docs
