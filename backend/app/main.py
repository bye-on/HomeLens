# app/main.py
from fastapi import FastAPI
from api.routes.semantic import router as semantic_router

app = FastAPI()

app.include_router(semantic_router)