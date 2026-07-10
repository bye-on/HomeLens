# HomeLens Artifact Docker Deploy

This deployment uses already-built artifacts:

- Spring Boot: `backend/target/*.jar`
- Vue frontend: `frontend/dist`

Docker on EC2 only packages and runs those artifacts. It does not run Maven or npm builds.

## Structure

```text
home-lens/
├─ backend/
│  ├─ Dockerfile
│  └─ target/*.jar
├─ frontend/
│  ├─ Dockerfile
│  ├─ dist/
│  └─ nginx/default.conf
└─ docker-compose.yml
```

## 1. Install Docker on EC2

Ubuntu:

```bash
sudo apt update
sudo apt install -y docker.io docker-compose-plugin
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ubuntu
```

Reconnect SSH, then check:

```bash
docker version
docker compose version
```

## 2. Build locally

### Backend

Build a jar locally.

```bash
cd backend
mvn clean package -DskipTests
```

Expected output:

```text
backend/target/*.jar
```

The backend Dockerfile only copies the jar:

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 80
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Frontend

Make sure `frontend/.env` has the Kakao keys, then build.

```bash
cd frontend
npm ci
npm run build
```

Expected output:

```text
frontend/dist
```

The frontend Dockerfile only copies the already-built `dist` directory into Nginx:

```dockerfile
FROM nginx:1.27-alpine
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## 3. Nginx routing

`frontend/nginx/default.conf` is the entrypoint for browser traffic.

Request flow:

```text
Browser
  -> EC2 port 80
  -> frontend container Nginx
  -> /api/v1/* proxies to backend container
  -> backend Spring Boot
```

Important project-specific proxy:

```nginx
location /api/v1/ {
    proxy_pass http://backend:80/api/v1/;
}
```

Here `backend` is the Docker Compose service name. It is equivalent to the example's `api`.

This project also proxies Kakao Mobility route API:

```nginx
location /v1/ {
    proxy_pass https://apis-navi.kakaomobility.com/v1/;
}
```

## 4. Upload project/artifacts to EC2

Example:

```bash
scp -i HomeLens.pem -r ./home-lens ubuntu@EC2_PUBLIC_IP:/home/ubuntu/home-lens
```

Or clone the repo on EC2, then upload only artifacts:

```bash
scp -i HomeLens.pem backend/target/*.jar ubuntu@EC2_PUBLIC_IP:/home/ubuntu/home-lens/backend/target/
scp -i HomeLens.pem -r frontend/dist ubuntu@EC2_PUBLIC_IP:/home/ubuntu/home-lens/frontend/
```

## 5. Configure environment on EC2

```bash
cd ~/home-lens
cp .env.example .env
nano .env
```

Required runtime values:

```env
POSTGRES_USER=homelens
POSTGRES_PASSWORD=your_password
POSTGRES_DB=homelens

HTTP_PORT=80
APP_VERIFY_BASE_URL=http://EC2_PUBLIC_IP
```

The Kakao frontend keys are baked into `frontend/dist` at local build time.

## 6. Start containers

```bash
docker compose up -d --build
```

Open:

```text
http://EC2_PUBLIC_IP
```

Check:

```bash
docker compose ps
docker compose logs -f
```

## 7. Restore DB dump

Put the dump here:

```text
backend/docker/postgres/dumps/homelens.dump
```

Then run:

```bash
docker compose --profile restore up db-restore
docker compose restart backend semantic-api
```

## 8. Check

```bash
docker compose ps
docker compose logs -f backend
curl http://localhost/api/v1/properties/count/all
curl "http://localhost/api/v1/properties/map?size=5"
```

## 9. Operations

```bash
docker compose restart frontend backend
docker compose logs -f frontend
docker compose logs -f semantic-api
docker compose down
```

To reset DB volume and restore from dump again:

```bash
docker compose down -v
docker compose up -d db
docker compose --profile restore up db-restore
docker compose up -d
```

Use `down -v` carefully because it deletes the PostgreSQL volume.
