from typing import List, Optional
from pydantic import BaseModel

class SearchRequest(BaseModel):
    query: str
    top_k: int = 20
    
class EmbedRequest(BaseModel):
    embedding_text: str

class Filters(BaseModel):
    sales_type: Optional[str] = None
    service_type: Optional[str] = None
    local1: Optional[str] = None
    local2: Optional[str] = None
    local3: Optional[str] = None
    deposit_min: Optional[int] = None
    deposit_max: Optional[int] = None
    rent_max: Optional[int] = None
    area_m2_min: Optional[int] = None
    area_m2_max: Optional[int] = None
    manage_cost_max: Optional[int] = None

class SearchResponse(BaseModel):
    embedding_text: str
    embedding_vector: List[float]
    filters: Filters

class PrepareResponse(BaseModel):
    embedding_text: str
    filters: Filters

class EmbedResponse(BaseModel):
    embedding_vector: List[float]