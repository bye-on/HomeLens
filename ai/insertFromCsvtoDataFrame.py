from pathlib import Path
import json
import pandas as pd
import numpy as np

poi_types  = ["지하철역", "세탁소", "카페", "약국", "대형마트", "편의점", "버스정류장"]
poi_fields = ["exists", "distance", "transport", "timeTaken"]

def main() -> pd.DataFrame:
    objs = read_json_files()                 # ✅ 호출
    # print(objs)
    df = makeDf(objs)
    df = makeEssentialDfCol(df)              # ✅ 리턴 받기
    # print(df)
    # print(df.columns)
    # print(df.head(1)["subways"])
    return df;

def read_json_files():
    JSON_DIR = Path("json")   # ← 본인 폴더로 수정
    files = sorted(JSON_DIR.glob("*.json"))

    objs = []
    bad = []
    kinds = {"dict": 0, "list": 0, "other": 0}

    for fp in files:
        try:
            with open(fp, "r", encoding="utf-8") as f:
                data = json.load(f)

            if isinstance(data, dict):
                objs.append(data)
                kinds["dict"] += 1
            elif isinstance(data, list):
                # 리스트 안에 dict들이 들어있다고 가정하고 풀어서 넣기
                objs.extend([x for x in data if isinstance(x, dict)])
                kinds["list"] += 1
            else:
                kinds["other"] += 1

        except Exception as e:
            bad.append((fp.name, str(e)))

    return objs;

def parse_neighborhoods(item: dict) -> dict:
    nb = item.get("neighborhoods", {}) or {}
    out = {}

    # 1) distributions / amenities는 JSON 문자열로 저장 (필요하면 나중에 파싱)
    out["nb_distributions"] = json.dumps(nb.get("distributions", []), ensure_ascii=False)
    out["nb_amenities"] = json.dumps(nb.get("amenities", []), ensure_ascii=False)

    # 2) nearbyPois는 poiType별로 컬럼 펼치기
    pois = nb.get("nearbyPois", []) or []
    by_type = {}
    for p in pois:
        if isinstance(p, dict) and "poiType" in p:
            by_type[p["poiType"]] = p

    for t in poi_types:
        p = by_type.get(t, {})
        exists = p.get("exists", None)

        if exists is True:
            out[f"poi_{t}_exists"] = True
            out[f"poi_{t}_distance"] = p.get("distance")
            out[f"poi_{t}_transport"] = p.get("transport")
            out[f"poi_{t}_timeTaken"] = p.get("timeTaken")
        else:
            # exists가 False이거나 아예 없으면 → 전부 None
            out[f"poi_{t}_exists"] = None
            out[f"poi_{t}_distance"] = None
            out[f"poi_{t}_transport"] = None
            out[f"poi_{t}_timeTaken"] = None

    return out

def makeDf(objs):
    rows = []
    for o in objs:
        item = o.get("item", {}) or {}
        price = item.get("price", {}) or {}
        area = item.get("area", {}) or {}
        location = item.get("location", {}) or {}
        addr = item.get("addressOrigin", {}) or {}
        floor = item.get("floor", {}) or {}
        manage = item.get("manageCost", {}) or {}
        manage_detail = item.get("manageCostDetail", {}) or {}

        subways = o.get("subways", []) or []   # ✅ 최상위 subways

        row = {
            "item_id": item.get("itemId"),
            "sales_type": item.get("salesType"),
            "service_type": item.get("serviceType"),
            "status": item.get("status"),
            "updated_at": item.get("updatedAt"),

            "title": item.get("title"),
            "description": item.get("description"),

            "deposit": price.get("deposit"),
            "rent": price.get("rent"),
            
            "lat" : location.get("lat"),
            "lng" : location.get("lng"),

            "area_m2": area.get("전용면적M2"),
            "area_contract_m2": area.get("계약면적M2"),

            "local1": addr.get("local1"),
            "local2": addr.get("local2"),
            "local3": addr.get("local3"),
            "address_text": addr.get("fullText"),
            "local_text": addr.get("localText"),
            "jibun_address": item.get("jibunAddress"),

            "floor_level": floor.get("floor"),
            "all_floors": floor.get("allFloors"),

            "manage_cost_amount": manage.get("amount"),
            "manage_not_includes": json.dumps(manage.get("notIncludes", []), ensure_ascii=False),
            # "avg_manage_cost": manage_detail.get("avgManageCost"),

            "options": json.dumps(item.get("options", []), ensure_ascii=False),
            "images" : json.dumps(item.get("images", []), ensure_ascii=False),

            # ✅ 지하철 추가
            "subways": json.dumps(subways, ensure_ascii=False),
            "subway_names": ", ".join([s.get("name","") for s in subways if isinstance(s, dict)]),
        }

        row.update(parse_neighborhoods(item))
        
        rows.append(row)
    
    return pd.DataFrame(rows);

def build_poi_json(row):
    poi_dict = {}
    for t in poi_types:
        sub = {}
        for f in poi_fields:
            col = f"poi_{t}_{f}"
            if col not in row.index:
                continue
            v = row[col]
            # NA는 그냥 무시
            if isinstance(v, float) and np.isnan(v):
                continue
            if v is None:
                continue
            sub[f] = v
        if sub:  # 해당 타입에 유효한 값이 하나라도 있으면만 포함
            poi_dict[t] = sub

    # 없으면 None, 있으면 JSON 문자열
    return None if not poi_dict else json.dumps(poi_dict, ensure_ascii=False)

def m2_to_py(m2):
    """m² -> 평 변환, NaN 안전 처리."""
    try:
        if pd.isna(m2):
            return None
        return round(float(m2) / 3.3, 1)
    except Exception:
        return None

def _parse_json_like(val):
    """nb_distributions / nb_amenities 같이 JSON 또는 리스트로 들어오는 컬럼 파싱."""
    if isinstance(val, list):
        return val
    if isinstance(val, str):
        s = val.strip()
        if not s:
            return []
        try:
            return json.loads(s)
        except Exception:
            return []
    return []

def classify_floor_text(floor_level, all_floors):
    """
    floor_level: 현재 층 (정수 또는 문자열 '중' 등)
    all_floors : 전체 층수
    규칙:
      - all_floors > 5 이면: 저/중/고
      - all_floors <= 5 이면: '3층'처럼 정수 표기 유지
    """
    # 먼저 숫자로 파싱 가능한지 시도
    floor_num = None
    if isinstance(floor_level, (int, float)):
        if not math.isnan(floor_level):
            floor_num = int(floor_level)
    elif isinstance(floor_level, str):
        s = floor_level.strip()
        if s.isdigit():
            floor_num = int(s)

    # 전체 층수 파싱
    total_num = None
    if isinstance(all_floors, (int, float)):
        if not math.isnan(all_floors):
            total_num = int(all_floors)
    elif isinstance(all_floors, str):
        s = all_floors.strip()
        if s.isdigit():
            total_num = int(s)

    # 둘 다 숫자로 파싱 안 되면, 그냥 원문 반환
    if floor_num is None or total_num is None:
        if isinstance(floor_level, str) and floor_level.strip():
            return floor_level.strip()
        return None

    # 5층 이하 → 정수층 그대로 사용
    if total_num <= 5:
        return f"{floor_num}층"

    # 6층 이상 → 비율로 저/중/고
    ratio = floor_num / total_num
    if ratio <= 1/3:
        return "저층"
    elif ratio <= 2/3:
        return "중층"
    else:
        return "고층"

def format_price_krw_from_manwon(deposit_m, rent_m, manage_cost_w):
    """
    deposit, rent는 '만원' 단위라고 가정.
    manage_cost는 '원' 단위라고 가정.
    임베딩용 간단한 한국어 표현으로 바꿔서 문자열 반환.
    """
    phrases = []

    # 보증금
    if pd.notna(deposit_m):
        deposit_m = int(deposit_m)
        # 억 단위로 표현
        eok = deposit_m // 10000       # 10000만원 = 1억
        man = deposit_m % 10000
        if eok > 0:
            if man > 0:
                phrases.append(f"보증금 {eok}억 {man}만원")
            else:
                phrases.append(f"보증금 {eok}억")
        else:
            phrases.append(f"보증금 {deposit_m}만원")

    # 월세
    if pd.notna(rent_m) and int(rent_m) > 0:
        rent_m = int(rent_m)
        phrases.append(f"월세 {rent_m}만원")

    # 관리비
    if pd.notna(manage_cost_w) and manage_cost_w > 0:
        # 원 → 만원
        mc_m = int(round(manage_cost_w / 10000))
        if mc_m > 0:
            phrases.append(f"관리비 약 {mc_m}만원")

    return ", ".join(phrases) if phrases else None


# ---------------------------
# 최종 embedded_text 생성 함수
# ---------------------------

def build_embedded_text(row: pd.Series) -> str:
    parts = []

    # 1) 제목
    title = row.get("title")
    if isinstance(title, str) and title.strip():
        parts.append(f"제목: {title.strip()}")

    # 2) 설명 (길이 제한)
    desc = row.get("description")
    if isinstance(desc, str):
        desc = desc.strip()
        if desc:
            if len(desc) > 400:
                desc = desc[:400]
            parts.append(f"설명: {desc}")

    # 3) 면적 (평)
    area_m2 = row.get("area_m2")
    py = m2_to_py(area_m2)
    if py is not None:
        parts.append(f"전용면적: {py}평")

    area_contract_m2 = row.get("area_contract_m2")
    py_c = m2_to_py(area_contract_m2)
    if py_c is not None:
        parts.append(f"계약면적: {py_c}평")

    # 4) 층수 (저/중/고 or '3층')
    floor_level = row.get("floor_level")
    all_floors = row.get("all_floors")
    floor_text = classify_floor_text(floor_level, all_floors)
    if floor_text:
        parts.append(f"층수: {floor_text}")

    # 5) 금액 (보증금/월세/관리비) – 단위 정규화 후 자연어 표현
    deposit = row.get("deposit")          # 만원 단위 가정
    rent = row.get("rent")                # 만원 단위 가정
    manage_cost = row.get("manage_cost")  # 원 단위 가정

    price_text = format_price_krw_from_manwon(deposit, rent, manage_cost)
    if price_text:
        parts.append(price_text)

    # 6) 옵션
    opts = row.get("options")
    if isinstance(opts, str):
        opts = opts.strip()
        if opts and opts != "[]":
            parts.append(f"옵션: {opts}")

    # 7) 인근역 (subway_names)
    subway_names = row.get("subway_names")
    if isinstance(subway_names, str):
        sn = subway_names.strip()
        if sn:
            parts.append(f"인근역: {sn}")

    # 8) 배송(distributions) – 쿠팡, SSG, 컬리, 배민, 요기요 등
    dists = _parse_json_like(row.get("nb_distributions"))
    shipping_phrases = []
    for d in dists:
        if not isinstance(d, dict):
            continue
        company = d.get("companyName")
        kinds = d.get("kinds") or []
        if not company or not kinds:
            continue
        kinds_txt = ", ".join(kinds)
        shipping_phrases.append(f"{company} {kinds_txt} 가능")
    if shipping_phrases:
        parts.append("배송: " + ", ".join(shipping_phrases))

    # 9) 슬세권/숲세권/학세권 등 amenities
    amens = _parse_json_like(row.get("nb_amenities"))
    amen_titles = []
    for a in amens:
        if not isinstance(a, dict):
            continue
        t = a.get("title")
        if isinstance(t, str) and t.strip():
            amen_titles.append(t.strip())
    if amen_titles:
        parts.append("주변환경: " + ", ".join(amen_titles))

    # (중요) 위치 정보 local1/local2/local3/jibun_address는
    #       여기에는 넣지 않고, SQL WHERE로만 하드 필터링해 사용.

    return " | ".join(parts)

def makeEssentialDfCol(df_items: pd.DataFrame) -> pd.DataFrame:
    df_items["deposit"] = df_items["deposit"].astype(float)
    df_items["rent"] = df_items["rent"].astype(float)
    df_items["poi_json"] = df_items.apply(build_poi_json, axis=1)
    df_items["embedded_text"] = df_items.apply(build_embedded_text, axis=1)
    poi_cols = [c for c in df_items.columns if c.startswith("poi_") and c != "poi_json"]
    df_items = df_items.drop(columns=poi_cols)
    df_items = df_items.drop(columns="subway_names")
    df_items = df_items[df_items["item_id"].notna()]      # NaN 제거
    df_items["item_id"] = df_items["item_id"].astype("int64")
    
    return df_items;
    

if __name__ == "__main__":
    main()
    

