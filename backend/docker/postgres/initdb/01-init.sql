-- ==============
-- 0) pgvector 확장 활성화
-- ==============
CREATE EXTENSION IF NOT EXISTS vector;

-- ==============
-- 1) 테이블 생성 (소문자/따옴표 최소화)
-- ==============

-- 사용자
CREATE TABLE IF NOT EXISTS user_info (
  user_id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_email      VARCHAR(24)  NOT NULL,
  user_pw         VARCHAR(100)  NOT NULL,
  user_name       VARCHAR(24)  NOT NULL,
  user_tel        VARCHAR(24)  NOT NULL,
  user_role       VARCHAR(24)  NOT NULL DEFAULT 'user',
  user_created_at DATE         NOT NULL DEFAULT CURRENT_DATE,
  user_refresh_token VARCHAR(2000) NULL,
  user_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
  user_status     VARCHAR(20)  NOT NULL DEFAULT 'active'
);

-- 매물(속성)
CREATE TABLE "property" (
	item_id	BIGINT NOT NULL PRIMARY KEY,
	title	VARCHAR(100)		NOT NULL,
  sales_type        VARCHAR(10)  NOT NULL,
  service_type      VARCHAR(20)  NOT NULL,

	local1	VARCHAR(25)		NOT NULL,
	local2	VARCHAR(25)		NOT NULL,
	local3	VARCHAR(25)		NOT NULL,
	jibun_address	VARCHAR(50)		NOT NULL,

	deposit	INT		NOT NULL,
	rent	INT	DEFAULT 0	NOT NULL,
	manage_cost	FLOAT		NOT NULL,
	manage_not_includes	VARCHAR(100)		NULL,

	options	VARCHAR(100)		NULL,

	area_m2	INT	     NULL,
	area_contract_m2	INT		NULL,

	floor_level	INT		NULL,
	all_floors	INT		NULL,

  like_count INT NOT NULL DEFAULT 0,
  sold BOOLEAN NOT NULL DEFAULT FALSE,

	subway	JSON		NULL,
	lat	FLOAT		NOT NULL,
	lng	FLOAT		NOT NULL,
	nb_distribution	JSON		NULL,
	nb_amenity	JSON		NULL,
  nb_poi JSON NULL,

  item_image JSON NULL,

	embedding    vector(1536) NOT NULL
);

-- 게시판
CREATE TABLE IF NOT EXISTS board (
  board_id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id     BIGINT      NOT NULL,
  title       VARCHAR(50) NOT NULL,
  content     TEXT        NOT NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_board_user
    FOREIGN KEY (user_id)
    REFERENCES user_info (user_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
);

-- 좋아요(관심매물)
CREATE TABLE IF NOT EXISTS like_home (
  user_id BIGINT  NOT NULL,
  item_id BIGINT  NOT NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, item_id),
  CONSTRAINT fk_like_home_user
    FOREIGN KEY (user_id)
    REFERENCES user_info (user_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT fk_like_home_property
    FOREIGN KEY (item_id)
    REFERENCES property (item_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

-- 이메일 인증 토큰 테이블
CREATE TABLE IF NOT EXISTS email_verification_token (
  id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

  user_id     BIGINT NOT NULL
              REFERENCES user_info(user_id)
              ON UPDATE CASCADE
              ON DELETE CASCADE,

  -- 토큰 원문은 저장하지 말고, SHA-256 같은 해시를 저장하는 걸 추천합니다.
  -- SHA-256 hex라면 길이 64
  token_hash  CHAR(64) NOT NULL UNIQUE,

  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  expires_at  TIMESTAMPTZ NOT NULL DEFAULT (now() + interval '5 minutes'),

  used_at     TIMESTAMPTZ NULL,

  CONSTRAINT chk_evt_expires CHECK (expires_at > created_at)
);

-- 지역 코드, 시, 구, 동
CREATE TABLE region (
  dong_code   BIGINT PRIMARY KEY,
  local1   VARCHAR(20) NOT NULL,
  local2  VARCHAR(20),
  local3   VARCHAR(20)
);

-- 방문자 처리
CREATE TABLE IF NOT EXISTS visit_log (
  id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  visitor_key  VARCHAR(128) NOT NULL,  -- 방문자 식별키(아래 참고)
  user_id      BIGINT NULL,            -- 로그인 유저면 채움
  visited_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  day          DATE NOT NULL DEFAULT CURRENT_DATE
);


-- ==============
-- 2) 인덱스 (권장)
-- ==============

-- 이메일 중복 방지/로그인 조회 최적화
CREATE UNIQUE INDEX IF NOT EXISTS ux_user_info_email
  ON user_info (user_email);

-- 지역 필터 최적화(필요 시)
CREATE INDEX IF NOT EXISTS ix_property_local
  ON property (local1, local2, local3);

-- like_home 조회 최적화
CREATE INDEX IF NOT EXISTS ix_like_home_user
  ON like_home (user_id);

CREATE INDEX IF NOT EXISTS ix_like_home_item
  ON like_home (item_id);
  
CREATE INDEX IF NOT EXISTS ix_like_home_created_at
  ON like_home (created_at);

CREATE INDEX IF NOT EXISTS ix_like_home_created_item
  ON like_home (created_at, item_id);

-- 게시글 조회 최적화(유저별 최신순)
CREATE INDEX IF NOT EXISTS ix_board_user_created
  ON board (user_id, created_at DESC);
  
-- "하루 1번만 카운트" 용 유니크 제약(중복 방문 방지)
CREATE UNIQUE INDEX IF NOT EXISTS ux_visit_log_day_visitor
  ON visit_log(day, visitor_key);

-- 집계용
CREATE INDEX IF NOT EXISTS ix_visit_log_day
  ON visit_log(day);

-- ==============
-- 3) 벡터 검색용 인덱스 (선택)
--    데이터가 충분히 쌓이고 벡터 검색을 자주 하면 켜세요.
-- ==============

CREATE INDEX idx_property_location
    ON property (local1, local2, local3, sales_type);

CREATE INDEX idx_property_vector
    ON property
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);
