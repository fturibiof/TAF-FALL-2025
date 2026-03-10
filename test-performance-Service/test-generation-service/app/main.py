from __future__ import annotations

import re
from typing import Optional

from fastapi import FastAPI
from pydantic import BaseModel, Field

JMX_TEST_GENERATION_ENDPOINT = "/api/performance/test-generation/jmeter"


class LLMGenerateRequest(BaseModel):
    prompt: str = Field(min_length=5, max_length=4000)


class JMeterHttpPlan(BaseModel):
    protocol: str = "HTTP"
    method: str = "GET"
    domain: str = "api.example.com"
    path: str = "/"
    port: str = ""
    nbThreads: str = "10"
    rampTime: str = "5"
    duration: str = "60"
    loop: str = "1"
    data: str = ""


class LLMGenerateResponse(BaseModel):
    status: str
    test_plan: JMeterHttpPlan
    explanation: str


app = FastAPI(title="Test Generation Service", version="0.1.0")


def _baml_generate(prompt: str) -> Optional[JMeterHttpPlan]:
    """
    Uses a BAML generated client.

    BAML source files are maintained in:
    - baml_src/clients.baml (LLM provider connection)
    - baml_src/jmeter_generation.baml (prompt + contract)
    """
    try:
        from baml_client import b  

        result = b.GenerateJMeterPlan(prompt=prompt)
        return JMeterHttpPlan(**result)
    except Exception:
        return None


def _extract_number(prompt: str, patterns: list[str], default: str) -> str:
    for pattern in patterns:
        m = re.search(pattern, prompt, flags=re.IGNORECASE)
        if m:
            return m.group(1)
    return default


def _fallback_generate(prompt: str) -> JMeterHttpPlan:
    lower = prompt.lower()

    method = "GET"
    for candidate in ["POST", "PUT", "DELETE", "PATCH", "GET"]:
        if candidate.lower() in lower:
            method = candidate
            break

    protocol = "HTTPS" if "https" in lower else "HTTP"

    path_match = re.search(r"(/[-a-zA-Z0-9_./{}]*)", prompt)
    path = path_match.group(1) if path_match else "/"

    domain_match = re.search(r"([a-zA-Z0-9.-]+\.[a-zA-Z]{2,})", prompt)
    domain = domain_match.group(1) if domain_match else "api.example.com"

    plan = JMeterHttpPlan(
        protocol=protocol,
        method=method,
        domain=domain,
        path=path,
        nbThreads=_extract_number(prompt, [r"(\d+)\s*(users|utilisateurs|threads?)"], "10"),
        rampTime=_extract_number(prompt, [r"ramp(?:-|\s)?up\s*(\d+)", r"mont[ée]e\s*(\d+)"], "5"),
        duration=_extract_number(prompt, [r"(\d+)\s*(seconds|secondes|sec|s)"], "60"),
        loop=_extract_number(prompt, [r"(\d+)\s*(loops|boucles?)"], "1"),
    )

    if method in {"POST", "PUT", "PATCH"} and "{" in prompt and "}" in prompt:
        json_candidate = prompt[prompt.find("{") : prompt.rfind("}") + 1]
        plan.data = json_candidate

    return plan


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post(JMX_TEST_GENERATION_ENDPOINT, response_model=LLMGenerateResponse)
def generate_jmeter_test_plan(request: LLMGenerateRequest) -> LLMGenerateResponse:
    generated = _baml_generate(request.prompt) or _fallback_generate(request.prompt)

    return LLMGenerateResponse(
        status="success",
        test_plan=generated,
        explanation="Generated from BAML prompt contract (with deterministic fallback).",
    )