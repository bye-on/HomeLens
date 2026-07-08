import os
import requests

GEMINI_API_KEY = os.getenv("GMS_KEY")
GEMINI_ENDPOINT = (
    "https://gms.ssafy.io/gmsapi/generativelanguage.googleapis.com"
    "/v1beta/models/gemini-2.5-flash:generateContent"
)

def gemini_generate(text: str, system_prompt: str) -> str:
    payload = {
        "contents": [
            {
                "parts": [
                    {
                        "text": system_prompt + "\n\n" + text
                    }
                ]
            }
        ]
    }

    resp = requests.post(
        f"{GEMINI_ENDPOINT}?key={GEMINI_API_KEY}",
        json=payload,
        timeout=10,
    )
    resp.raise_for_status()

    data = resp.json()
    return data["candidates"][0]["content"]["parts"][0]["text"]



