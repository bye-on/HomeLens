# app/api/routes/semantic.py
from fastapi import APIRouter
import json
import logging

from models.schemas import SearchRequest, SearchResponse, Filters
from core.openai_client import client, EMBED_DIM, STRUCTURE_SYSTEM_PROMPT
from core.gemini_client import gemini_generate

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/semantic", tags=["semantic"])

@router.post("/prepare", response_model=SearchResponse)
def prepare_semantic_search(req: SearchRequest):
    # openAI 구조화
    chat_resp = client.chat.completions.create(
        model="gpt-4o-mini",
        messages=[
            {"role": "system", "content": STRUCTURE_SYSTEM_PROMPT},
            {"role": "user", "content": req.query},
        ],
        temperature=0.0,
    )
    content = chat_resp.choices[0].message.content
    
    # 1️⃣ Gemini로 구조화
    # content = gemini_generate(
    #     text=req.query,
    #     system_prompt=STRUCTURE_SYSTEM_PROMPT,
    # )

    try:
        parsed = json.loads(content)
    except json.JSONDecodeError as e:
        logger.error("JSON 파싱 실패: %s / content=%r", e, content)
        raise

    filters_dict = parsed.get("filters", {}) or {}
    embedding_text = parsed.get("embedding_text") or req.query

    emb_resp = client.embeddings.create(
        model="text-embedding-3-large",
        input=[embedding_text],
        dimensions=EMBED_DIM,
    )
    embedding_vector = emb_resp.data[0].embedding

    return SearchResponse(
        embedding_text=embedding_text,
        embedding_vector=embedding_vector,
        filters=Filters(**filters_dict),
    )