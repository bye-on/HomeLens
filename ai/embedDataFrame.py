import insertFromCsvtoDataFrame;
from openai import OpenAI
import os
import pandas as pd
import numpy as np
from pathlib import Path

NOT_NULL_COLS = [
    "item_id",
    "title",
    "sales_type",
    "service_type",
    "local1",
    "local2",
    "local3",
    "jibun_address",
    "deposit",
    "rent",
    "manage_cost",
    "lat",
    "lng",
]

TEXT_COLS = ["title", "sales_type", "service_type", "local1", "local2", "local3", "jibun_address"]

NUM_COLS = ["item_id", "deposit", "rent", "manage_cost", "lat", "lng"]

def build_property_df(df_items: pd.DataFrame) -> pd.DataFrame:
    df = df_items.copy()

    # 타입 정리
    df["item_id"] = df["item_id"].astype("int64")
    df["title"] = df["title"].astype(str).str.slice(0, 150)

    # ✅ sales_type / service_type 포함
    df["sales_type"] = df["sales_type"].astype(str)       # "전세", "월세" 등
    df["service_type"] = df["service_type"].astype(str)   # "오피스텔", "원룸" 등

    df["local1"] = df["local1"].astype(str)
    df["local2"] = df["local2"].astype(str)
    df["local3"] = df["local3"].astype(str)
    df["jibun_address"] = df["jibun_address"].astype(str).str.slice(0, 100)

    df["deposit"] = df["deposit"].fillna(0).astype(int)
    df["rent"] = df["rent"].fillna(0).astype(int)

    # 관리비
    df["manage_cost"] = df["manage_cost_amount"].fillna(0).astype(float)
    df["manage_not_includes"] = df["manage_not_includes"].astype(str)

    # 옵션 문자열(이미 "에어컨, 냉장고..." 식으로 들어있다면 그대로 사용)
    df["options"] = df["options"].astype(str)
    
    df["images"] = df["images"].astype(str)

    # 면적
    df["area_m2"] = df["area_m2"].astype(float)
    df["area_contract_m2"] = df["area_contract_m2"].astype(float)

    # 층 정보: 텍스트 그대로 저장
    df["floor_level_text"] = df["floor_level"].astype(str)
    df["all_floors"] = pd.to_numeric(df["all_floors"], errors="coerce").astype("Int64")

    # JSON 컬럼들: 파이썬 dict/list 상태로 유지 → psycopg2가 JSONB로 넣어줌
    def ensure_json(x):
        if isinstance(x, str):
            try:
                return json.loads(x)
            except Exception:
                return None
        return x

    df["subway"] = df["subways"].apply(ensure_json)
    df["nb_distribution"] = df["nb_distributions"].apply(ensure_json)
    df["nb_amenity"] = df["nb_amenities"].apply(ensure_json)
    df["poi_json"] = df["poi_json"].apply(ensure_json)

    # 위치
    df["lat"] = df["lat"].astype(float)
    df["lng"] = df["lng"].astype(float)

    # 이미 만들어둔 텍스트
    df["embedded_text"] = df["embedded_text"].astype(str)

    # 최종 DB에 넣을 컬럼만 선택
    cols = [
        "item_id",
        "title",
        "sales_type",
        "service_type",
        "local1",
        "local2",
        "local3",
        "jibun_address",
        "deposit",
        "rent",
        "manage_cost",
        "manage_not_includes",
        "options",
        "area_m2",
        "area_contract_m2",
        "floor_level_text",
        "all_floors",
        "subway",
        "lat",
        "lng",
        "nb_distribution",
        "nb_amenity",
        "poi_json",
        "images",
        "embedded_text",
    ]
    return df[cols]

def embedding(df_items: pd.DataFrame) -> pd.DataFrame:
    # GMS 환경 설정 (이미 쓰시던 값 사용)
    GMS_BASE_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1"  # 필요시 기존 값 유지

    client = OpenAI(
        api_key=os.environ["GMS_KEY"],
        base_url=GMS_BASE_URL,
    )

    OUT = Path("vector_cache")
    OUT.mkdir(exist_ok=True)

    meta_path = OUT / "meta.parquet"
    emb_path  = OUT / "embeddings.npy"

    # 메타 저장 (디버깅/재현용)
    df_items.to_parquet(meta_path, index=False)
    print("saved meta:", meta_path)

    N = len(df_items)
    D = 1536
    batch_size = 8   # 🔹텍스트가 길어서 안전하게 작은 배치 권장

    emb = np.memmap(emb_path, dtype=np.float32, mode="w+", shape=(N, D))

    row_texts = df_items["embedded_text"].tolist()

    start = 0
    while start < N:
        end = min(start + batch_size, N)
        chunk = row_texts[start:end]

        resp = client.embeddings.create(
            model="text-embedding-3-large",
            input=chunk,
            dimensions=D,
        )

        vectors = np.array([d.embedding for d in resp.data], dtype=np.float32)
        emb[start:end] = vectors

        print(f"embedded: {start} ~ {end-1}")
        start = end

    emb.flush()
    print("saved embeddings:", emb_path)

    # DataFrame에도 embedding 컬럼 추가 (각 행에 numpy 배열 넣기)
    # embedding_matrix = np.array(emb)
    # df_items["embedding"] = [embedding_matrix[i] for i in range(N)]
    
    return df_items;

def drop_rows_with_null_required(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()

    # 1) 문자열 컬럼: 앞뒤 공백 제거 + 빈문자열을 NA로 처리
    for c in TEXT_COLS:
        if c in df.columns:
            df[c] = df[c].astype("string").str.strip()
            df.loc[df[c] == "", c] = pd.NA

    # 2) 숫자 컬럼: 숫자로 변환 불가하면 NaN 처리
    for c in NUM_COLS:
        if c in df.columns:
            df[c] = pd.to_numeric(df[c], errors="coerce")

    # 3) rent는 결측이면 0으로 채우기(테이블 DEFAULT 0과 맞춤)
    if "rent" in df.columns:
        df["rent"] = df["rent"].fillna(0)

    # 4) 필수 컬럼 중 하나라도 NA면 drop
    missing_cols = [c for c in NOT_NULL_COLS if c not in df.columns]
    if missing_cols:
        raise KeyError(f"df에 필수 컬럼이 없습니다: {missing_cols}")

    before = len(df)
    df = df.dropna(subset=NOT_NULL_COLS)
    after = len(df)

    print(f"[필수값 결측 drop] {before - after} rows removed / remaining {after}")

    # 5) (선택) 타입 정리: DB insert 전에 안전하게
    df["item_id"] = df["item_id"].astype("int64")
    df["deposit"] = df["deposit"].astype("int64")
    df["rent"] = df["rent"].astype("int64")
    df["manage_cost"] = df["manage_cost"].astype("float64")
    df["lat"] = df["lat"].astype("float64")
    df["lng"] = df["lng"].astype("float64")

    return df

def main() -> pd.DataFrame:
    df = insertFromCsvtoDataFrame.main();
    df_db = build_property_df(df);
    # 임베딩 전에 null이면 안되는 데 null인 애들 drop
    df_db = drop_rows_with_null_required(df_db);
    ans = input("임베딩을 실행할까요? (y/N): ").strip().lower()
    if ans == "y":
        df_with_embedding = embedding(df_db)
        df_with_embedding.to_csv("df_db_with_embedding.csv", index=False, encoding="utf-8-sig")
        print("저장 완료: df_db_with_embedding.csv")
        return df_with_embedding;
    else:
        print("임베딩을 건너뜁니다.")
        # print(df_db.head(-10));
        return df_db;

if __name__ == "__main__":
    main()