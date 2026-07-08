import psycopg2
from psycopg2.extras import execute_batch
from pgvector.psycopg2 import register_vector
from psycopg2.extras import Json
import embedDataFrame;
import json;
import os

import pandas as pd
import numpy as np


def safe_int_or_none(x):
    """NaN 이면 None, 아니면 int 로 캐스트"""
    if x is None:
        return None
    # pandas NaN 처리
    try:
        if pd.isna(x):
            return None
    except TypeError:
        # 숫자형이 아닐 수도 있음 -> int로 시도
        pass
    return int(x)

def safe_none(x):
    if x is None:
        return None
    elif pd.isna(x):
        return None
    else:
        return x;

def normalize_json_field(val):
    """
    - None / NaN -> None
    - dict / list -> Json(val)
    - str        -> JSON 문자열이면 json.loads 후 Json(), 아니면 그냥 Json(val)
    """
    if val is None:
        return None

    # NaN 방지
    try:
        if pd.isna(val):
            return None
    except TypeError:
        pass

    # 이미 dict/list면 그대로 Json
    if isinstance(val, (dict, list)):
        return Json(val)

    # 문자열인 경우: JSON일 수도, Python repr일 수도
    if isinstance(val, str):
        s = val.strip()
        # 일단 JSON이라고 가정하고 파싱 시도
        try:
            parsed = json.loads(s)
            if isinstance(parsed, (dict, list)):
                return Json(parsed)
        except Exception:
            # json.loads 실패하면 그냥 문자열 자체를 Json로 감싸서 저장
            # (이 경우는 진짜 JSON 컬럼에 "그냥 문자열"로 저장됨)
            return Json(s)

    # 그 외 타입은 거의 없겠지만, 일단 문자열로 캐스팅해서 Json
    return Json(str(val))

def insertInSql(df: pd.DataFrame):
    # 1) PostgreSQL 연결
    conn = psycopg2.connect(
        dbname=os.getenv("POSTGRES_DB", "homelens"),`r`n        user=os.getenv("POSTGRES_USER", "homelens"),`r`n        password=os.getenv("POSTGRES_PASSWORD", "homelens"),`r`n        host=os.getenv("POSTGRES_HOST", "localhost"),`r`n        port=int(os.getenv("POSTGRES_PORT", "5432")),
    )
    cur = conn.cursor()

    # 2) pgvector 확장 및 어댑터 등록
    cur.execute("CREATE EXTENSION IF NOT EXISTS vector;")
    conn.commit()

    register_vector(conn)  # ✅ 이거 한 번만 해주면 파이썬 list/ndarray → vector 로 자동 변환
    N = len(df)
    D = 1536

    emb_matrix = np.memmap(
        "./vector_cache/embeddings.npy",
        dtype=np.float32,
        mode="r",   # 읽기 전용
        shape=(N, D)
    )
    
    # df = df.reset_index(drop=True)

    # 3) DataFrame → 파라미터 리스트 변환
    rows = []
    for i, row in df.iterrows():
        emb_vec = emb_matrix[i].tolist()
        
        rows.append((
            int(row["item_id"]),
            row["sales_type"],
            row["service_type"],
            row["title"],
            row["local1"],
            row["local2"],
            row["local3"],
            row["jibun_address"],
            int(row["deposit"]),
            int(row["rent"]),
            float(row["manage_cost"]),
            row.get("manage_not_includes"),
            row.get("options"),
            safe_int_or_none(row["area_m2"]),         # 혹시 모를 NaN 방지
            safe_int_or_none(row["area_contract_m2"]),# ★ 여기서 NaN → None
            safe_int_or_none(row.get("floor_level")),
            safe_int_or_none(row.get("all_floors")),
            Json(row.get("subway")),     # JSON 문자열이라고 가정
            float(row["lat"]),
            float(row["lng"]),
            Json(row.get("nb_distribution_obj")),  # ← 여기!
            Json(row.get("nb_amenity_obj")),
            Json(safe_none(row.get("poi_json"))),# ← 여기!
            Json(row.get("images")),
            emb_vec                                # vector
        ))

    # 4) execute_batch 로 한 번에 INSERT
    sql = """
    INSERT INTO property (
        item_id,
        sales_type,
        service_type,
        title,
        local1,
        local2,
        local3,
        jibun_address,
        deposit,
        rent,
        manage_cost,
        manage_not_includes,
        options,
        area_m2,
        area_contract_m2,
        floor_level,
        all_floors,
        subway,
        lat,
        lng,
        nb_distribution,
        nb_amenity,
        nb_poi,
        item_image,
        embedding
    ) VALUES (
        %s,  -- item_id
        %s,  -- sales_type
        %s,  -- service_type
        %s,  -- title
        %s,  -- local1
        %s,  -- local2
        %s,  -- local3
        %s,  -- jibun_address
        %s,  -- deposit
        %s,  -- rent
        %s,  -- manage_cost
        %s,  -- manage_not_includes
        %s,  -- options
        %s,  -- area_m2
        %s,  -- area_contract_m2
        %s,  -- floor_level
        %s,  -- all_floors
        %s,  -- subway (json/jsonb)
        %s,  -- lat
        %s,  -- lng
        %s,  -- nb_distribution (json/jsonb)
        %s,  -- nb_amenity (json/jsonb)
        %s,  -- nb_poi
        %s,  -- item_images
        %s   -- embedding (vector)
    )
    """
    # 확인용
    # print(sql.count("%s"))  # 23 나와야 정상


    execute_batch(cur, sql, rows, page_size=100)
    conn.commit()

    cur.close()
    conn.close()
    
    return "done";
    

def main():
    print(insertInSql(embedDataFrame.main()))

if __name__ == "__main__":
    main()